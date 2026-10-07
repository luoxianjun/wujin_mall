package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.forum.api.activity.ForumActivityApi;
import cn.iocoder.yudao.module.forum.api.activity.dto.ForumActivityParticipationConfigDTO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryRecordMapper;
import cn.iocoder.yudao.module.gamification.service.notification.GamificationNotificationService;
import cn.iocoder.yudao.module.gamification.service.points.GamificationPointsService;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 抽奖核心服务实现
 */
@Service
public class LotteryDrawServiceImpl implements LotteryDrawService {

    private static final Logger log = LoggerFactory.getLogger(LotteryDrawServiceImpl.class);

    /** 抽奖积分业务类型 */
    private static final Integer BIZ_TYPE_LOTTERY_COST = MemberPointBizTypeEnum.LOTTERY_COST.getType();
    /** 抽奖中奖积分业务类型 */
    private static final Integer BIZ_TYPE_LOTTERY_PRIZE = MemberPointBizTypeEnum.LOTTERY_WIN.getType();

    @Resource
    private LotteryActivityMapper lotteryActivityMapper;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Resource
    private LotteryActivityPrizeMapper lotteryActivityPrizeMapper;
    @Resource
    private LotteryRecordMapper lotteryRecordMapper;
    @Resource
    private GamificationPointsService pointsService;
    @Resource
    private GamificationNotificationService notificationService;
    @Resource
    private ForumActivityApi forumActivityApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LotteryRecordDO draw(Long userId, Long lotteryActivityId) {
        // 1. 验证可以抽奖
        LotteryActivityDO activity = lotteryActivityMapper.selectById(lotteryActivityId);
        if (activity == null) {
            throw new RuntimeException("抽奖活动不存在");
        }
        if (!canDrawInternal(userId, activity)) {
            throw new RuntimeException("不满足抽奖条件");
        }

        // 2. 扣除积分（如果需要）
        if (LotteryActivityDO.COST_POINTS.equals(activity.getCostType()) && activity.getCostAmount() > 0) {
            boolean deducted = pointsService.deductPoints(userId, activity.getCostAmount(),
                    BIZ_TYPE_LOTTERY_COST, lotteryActivityId, "抽奖消耗积分");
            if (!deducted) {
                throw new RuntimeException("积分不足");
            }
        }

        // 3. 执行抽奖逻辑（通过关联表获取奖品和概率）
        PrizeResult wonResult = selectPrize(userId, activity);

        // 4. 创建抽奖记录
        LotteryRecordDO record = LotteryRecordDO.builder()
                .lotteryActivityId(lotteryActivityId)
                .userId(userId)
                .drawTime(LocalDateTime.now())
                .build();

        if (wonResult != null && wonResult.prize != null
                && !LotteryPrizeDO.TYPE_THANK_YOU.equals(wonResult.prize.getType())) {
            // 中奖
            LotteryPrizeDO wonPrize = wonResult.prize;
            record.setPrizeId(wonPrize.getId());
            record.setPrizeName(wonPrize.getName());
            record.setPrizeType(wonPrize.getType());
            record.setWon(true);
            record.setDelivered(false);

            // 扣减库存
            int affected = lotteryPrizeMapper.decrementStock(wonPrize.getId());
            if (affected == 0) {
                // 库存不足，回退为未中奖
                record.setWon(false);
                record.setPrizeId(null);
                record.setPrizeName("谢谢参与");
                record.setPrizeType(LotteryPrizeDO.TYPE_THANK_YOU);
            } else {
                // 积分奖品自动发放
                if (LotteryPrizeDO.TYPE_POINTS.equals(wonPrize.getType()) && wonPrize.getValue() != null) {
                    pointsService.grantPoints(userId, wonPrize.getValue(),
                            BIZ_TYPE_LOTTERY_PRIZE, lotteryActivityId, "抽奖中奖获得积分");
                    record.setDelivered(true);
                }
            }
        } else {
            // 未中奖 / 谢谢参与
            record.setWon(false);
            record.setPrizeName("谢谢参与");
            record.setPrizeType(LotteryPrizeDO.TYPE_THANK_YOU);
            record.setDelivered(true);
        }

        lotteryRecordMapper.insert(record);

        // 5. 发送通知
        sendDrawNotification(userId, record);

        log.info("[draw] userId={}, activityId={}, won={}, prize={}",
                userId, lotteryActivityId, record.getWon(), record.getPrizeName());

        return record;
    }

    @Override
    public boolean canDraw(Long userId, Long lotteryActivityId) {
        LotteryActivityDO activity = lotteryActivityMapper.selectById(lotteryActivityId);
        if (activity == null) {
            return false;
        }
        return canDrawInternal(userId, activity);
    }

    @Override
    public List<LotteryRecordDO> getUserRecords(Long userId, Long lotteryActivityId) {
        return lotteryRecordMapper.selectByUserAndActivity(userId, lotteryActivityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeScheduledDraw(Long lotteryActivityId) {
        LotteryActivityDO activity = lotteryActivityMapper.selectById(lotteryActivityId);
        if (activity == null || !LotteryActivityDO.TYPE_SCHEDULED.equals(activity.getType())) {
            log.warn("[executeScheduledDraw] Activity not found or not scheduled type: {}", lotteryActivityId);
            return;
        }

        // 获取活动关联的论坛活动ID
        Long forumActivityId = activity.getActivityId();
        if (forumActivityId == null) {
            log.warn("[executeScheduledDraw] No forum activity linked, activityId={}", lotteryActivityId);
            return;
        }

        // 通过 ForumActivityApi 获取已通过审核的报名用户列表
        List<Long> participantUserIds = forumActivityApi.getApprovedUserIds(forumActivityId);
        if (participantUserIds == null || participantUserIds.isEmpty()) {
            log.info("[executeScheduledDraw] No participants for activity: {}", lotteryActivityId);
            return;
        }

        log.info("[executeScheduledDraw] Drawing for {} participants, activityId={}",
                participantUserIds.size(), lotteryActivityId);

        int successCount = 0;
        int failCount = 0;
        for (Long userId : participantUserIds) {
            try {
                // 为每个参与者执行抽奖（定时开奖跳过 canDraw 次数检查，由系统强制执行）
                PrizeResult wonResult = selectPrize(userId, activity);

                LotteryRecordDO record = LotteryRecordDO.builder()
                        .lotteryActivityId(lotteryActivityId)
                        .userId(userId)
                        .drawTime(LocalDateTime.now())
                        .build();

                if (wonResult != null && wonResult.prize != null
                        && !LotteryPrizeDO.TYPE_THANK_YOU.equals(wonResult.prize.getType())) {
                    LotteryPrizeDO wonPrize = wonResult.prize;
                    record.setPrizeId(wonPrize.getId());
                    record.setPrizeName(wonPrize.getName());
                    record.setPrizeType(wonPrize.getType());
                    record.setWon(true);
                    record.setDelivered(false);

                    int affected = lotteryPrizeMapper.decrementStock(wonPrize.getId());
                    if (affected == 0) {
                        record.setWon(false);
                        record.setPrizeId(null);
                        record.setPrizeName("谢谢参与");
                        record.setPrizeType(LotteryPrizeDO.TYPE_THANK_YOU);
                    } else if (LotteryPrizeDO.TYPE_POINTS.equals(wonPrize.getType()) && wonPrize.getValue() != null) {
                        pointsService.grantPoints(userId, wonPrize.getValue(),
                                BIZ_TYPE_LOTTERY_PRIZE, lotteryActivityId, "定时开奖中奖获得积分");
                        record.setDelivered(true);
                    }
                } else {
                    record.setWon(false);
                    record.setPrizeName("谢谢参与");
                    record.setPrizeType(LotteryPrizeDO.TYPE_THANK_YOU);
                    record.setDelivered(true);
                }

                lotteryRecordMapper.insert(record);
                sendDrawNotification(userId, record);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("[executeScheduledDraw] Draw failed for userId={}, activityId={}",
                        userId, lotteryActivityId, e);
            }
        }

        log.info("[executeScheduledDraw] Completed for activity: {}, success={}, fail={}",
                lotteryActivityId, successCount, failCount);
    }

    // ========== 内部方法 ==========

    private boolean canDrawInternal(Long userId, LotteryActivityDO activity) {
        // 1. 检查活动状态
        if (activity.getStatus() != 0) { // 非启用状态
            return false;
        }

        // 2. 参与条件：抽奖显式要求已报名时，或关联活动开启了报名流程时，都需要满足论坛活动报名规则
        if (!canParticipateByForumActivity(userId, activity)) {
            return false;
        }

        // 3. 即时抽奖才检查次数限制（定时开奖由系统自动执行）
        if (LotteryActivityDO.TYPE_INSTANT.equals(activity.getType())) {
            // 检查每日次数
            if (activity.getMaxDrawsPerDay() != null && activity.getMaxDrawsPerDay() > 0) {
                long todayCount = lotteryRecordMapper.countByUserAndActivityToday(userId, activity.getId());
                if (todayCount >= activity.getMaxDrawsPerDay()) {
                    return false;
                }
            }

            // 检查总次数
            if (activity.getMaxDrawsTotal() != null && activity.getMaxDrawsTotal() > 0) {
                long totalCount = lotteryRecordMapper.countByUserAndActivity(userId, activity.getId());
                if (totalCount >= activity.getMaxDrawsTotal()) {
                    return false;
                }
            }
        }

        // 4. 检查积分是否足够
        if (LotteryActivityDO.COST_POINTS.equals(activity.getCostType()) && activity.getCostAmount() > 0) {
            Integer userPoints = pointsService.getUserPoints(userId);
            if (userPoints == null || userPoints < activity.getCostAmount()) {
                return false;
            }
        }

        return true;
    }

    private boolean canParticipateByForumActivity(Long userId, LotteryActivityDO activity) {
        Long forumActivityId = activity.getActivityId();
        if (LotteryActivityDO.CONDITION_ENROLLED.equals(activity.getParticipationCondition())) {
            return forumActivityId != null
                    && forumActivityApi.isUserApprovedParticipated(forumActivityId, userId);
        }
        if (forumActivityId == null) {
            return true;
        }
        ForumActivityParticipationConfigDTO participationConfig = forumActivityApi.getParticipationConfig(forumActivityId);
        if (participationConfig == null || !Boolean.TRUE.equals(participationConfig.getSignUpRequired())) {
            return true;
        }
        if (Boolean.TRUE.equals(participationConfig.getApprovalRequired())) {
            return forumActivityApi.isUserApprovedParticipated(forumActivityId, userId);
        }
        return forumActivityApi.isUserParticipated(forumActivityId, userId);
    }

    /**
     * 内部结构：包含奖品和对应的活动级概率
     */
    private static class PrizeResult {
        final LotteryPrizeDO prize;
        final BigDecimal probability;

        PrizeResult(LotteryPrizeDO prize, BigDecimal probability) {
            this.prize = prize;
            this.probability = probability;
        }
    }

    /**
     * 通过关联表获取活动配置的奖品和概率，然后按概率选择
     * 1. 从关联表获取 activityId 配置的所有奖品 + 概率
     * 2. 批量加载对应的奖品实体
     * 3. 过滤有库存的奖品
     * 4. 检查保底机制
     * 5. 按概率权重随机选择
     */
    private PrizeResult selectPrize(Long userId, LotteryActivityDO activity) {
        // 1. 从关联表获取奖品配置
        List<LotteryActivityPrizeDO> activityPrizes = lotteryActivityPrizeMapper
                .selectByLotteryActivityId(activity.getId());

        if (activityPrizes.isEmpty()) {
            return null; // 没有配置奖品
        }

        // 2. 批量加载奖品实体
        List<Long> prizeIds = activityPrizes.stream()
                .map(LotteryActivityPrizeDO::getPrizeId)
                .collect(Collectors.toList());
        List<LotteryPrizeDO> prizes = lotteryPrizeMapper.selectBatchIds(prizeIds);

        // 构建 prizeId -> 奖品实体 的Map
        Map<Long, LotteryPrizeDO> prizeMap = prizes.stream()
                .collect(Collectors.toMap(LotteryPrizeDO::getId, p -> p));

        // 3. 组合：关联表概率 + 奖品实体，过滤有库存的奖品
        List<PrizeResult> availableResults = activityPrizes.stream()
                .filter(ap -> prizeMap.containsKey(ap.getPrizeId()))
                .map(ap -> new PrizeResult(prizeMap.get(ap.getPrizeId()), ap.getProbability()))
                .filter(pr -> LotteryPrizeDO.TYPE_THANK_YOU.equals(pr.prize.getType())
                        || (pr.prize.getRemainingStock() != null && pr.prize.getRemainingStock() > 0))
                .collect(Collectors.toList());

        if (availableResults.isEmpty()) {
            return null;
        }

        // 4. 检查保底机制
        if (activity.getGuaranteeDraws() != null && activity.getGuaranteeDraws() > 0) {
            long consecutiveLosses = lotteryRecordMapper.countConsecutiveLosses(userId, activity.getId());
            if (consecutiveLosses >= activity.getGuaranteeDraws()) {
                // 保底触发：选一个非"谢谢参与"的有库存奖品
                PrizeResult guaranteeResult = availableResults.stream()
                        .filter(pr -> !LotteryPrizeDO.TYPE_THANK_YOU.equals(pr.prize.getType())
                                && pr.prize.getRemainingStock() != null && pr.prize.getRemainingStock() > 0)
                        .findFirst()
                        .orElse(null);
                if (guaranteeResult != null) {
                    log.info("[selectPrize] Guarantee triggered: userId={}, consecutiveLosses={}",
                            userId, consecutiveLosses);
                    return guaranteeResult;
                }
            }
        }

        // 5. 按概率权重随机选择
        return weightedRandomSelect(availableResults);
    }

    /**
     * 加权随机选择（使用关联表中的概率）
     */
    private PrizeResult weightedRandomSelect(List<PrizeResult> results) {
        BigDecimal totalProbability = results.stream()
                .map(pr -> pr.probability != null ? pr.probability : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalProbability.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        double random = ThreadLocalRandom.current().nextDouble() * totalProbability.doubleValue();
        double cumulative = 0.0;

        for (PrizeResult pr : results) {
            double prob = pr.probability != null ? pr.probability.doubleValue() : 0.0;
            cumulative += prob;
            if (random <= cumulative) {
                return pr;
            }
        }

        // 兜底返回最后一个
        return results.get(results.size() - 1);
    }

    private void sendDrawNotification(Long userId, LotteryRecordDO record) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("prizeName", record.getPrizeName());
            params.put("won", record.getWon());

            String content = Boolean.TRUE.equals(record.getWon())
                    ? String.format("恭喜你在抽奖中获得了 %s！", record.getPrizeName())
                    : "很遗憾，本次未中奖，下次再来试试吧！";

            notificationService.sendNotification(userId, "LOTTERY_DRAW",
                    "抽奖结果通知", content, params);
        } catch (Exception e) {
            log.warn("[sendDrawNotification] Failed to send notification: userId={}", userId, e);
        }
    }
}
