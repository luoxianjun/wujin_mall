package cn.iocoder.yudao.module.forum.service.sign;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRecordPageReqVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRespVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignRuleRespVO;
import cn.iocoder.yudao.module.forum.controller.app.sign.vo.AppSignStatusRespVO;
import cn.iocoder.yudao.module.forum.convert.sign.ForumSignConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignPeriodStatDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRecordDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.sign.ForumSignRuleDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.sign.ForumSignPeriodStatMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.sign.ForumSignRecordMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.sign.ForumSignRuleMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.framework.sign.PeriodKeyUtils;
import cn.iocoder.yudao.module.forum.framework.sign.PeriodKeyUtils.PeriodType;
import cn.iocoder.yudao.module.forum.service.point.ForumPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛签到 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumSignServiceImpl implements ForumSignService {

    private static final int[] DEFAULT_WEEKLY_POINTS = new int[] { 1, 1, 3, 5, 7, 9, 10 };

    @Resource
    private ForumSignRecordMapper signRecordMapper;

    @Resource
    private ForumSignPeriodStatMapper signPeriodStatMapper;

    @Resource
    private ForumSignRuleMapper signRuleMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private ForumPointService pointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppSignRespVO sign(Long userId, boolean isTest, LocalDate date) {
        LocalDate today = LocalDate.now();
        if (isTest) {
            today = date;
        }

        // 1. 检查今天是否已签到
        ForumSignRecordDO todayRecord = signRecordMapper.selectByUserIdAndDate(userId, today);
        if (todayRecord != null) {
            throw ServiceExceptionUtil.exception(SIGN_ALREADY_TODAY);
        }

        // 2. 获取用户资料
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 3. 按周处理签到积分（每周重置）
        List<ForumSignRuleDO> configuredRules = loadConfiguredRules();
        Map<Integer, List<ForumSignRuleDO>> rulesByPeriod = groupRulesByPeriod(configuredRules);
        int totalPoints = 0;
        StringBuilder remarkBuilder = new StringBuilder();
        for (PeriodType periodType : resolvePeriodTypes(rulesByPeriod)) {
            int points = handleCheckInForPeriod(userId, today, periodType, rulesByPeriod.get(periodType.getCode()));
            totalPoints += points;
            remarkBuilder.append(periodType.name().toLowerCase()).append(":").append(points).append(";");
        }

        // 4. 计算总连续天数（按自然日）
        int continuousDays = calculateContinuousDays(userId, today);

        // 5. 创建签到记录
        ForumSignRecordDO signRecord = ForumSignRecordDO.builder()
                .userId(userId)
                .signDate(today)
                .continuousDays(continuousDays)
                .point(totalPoints)
                .remark(remarkBuilder.toString())
                .build();
        signRecordMapper.insert(signRecord);

        // 6. 更新用户资料的签到信息
        updateUserSignInfo(profile, continuousDays, today);

        // 7. 增加积分
        if (totalPoints > 0) {
            pointService.addSignPoint(userId, totalPoints, signRecord.getId().toString());
        }

        log.info("[sign][用户签到成功，userId={}, continuousDays={}, totalPoints={}]", userId, continuousDays, totalPoints);

        return ForumSignConvert.INSTANCE.convert(signRecord);
    }

    @Override
    public AppSignStatusRespVO getSignStatus(Long userId) {
        ForumUserProfileDO profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 检查今天是否已签到
        LocalDate today = LocalDate.now();
        ForumSignRecordDO todayRecord = signRecordMapper.selectByUserIdAndDate(userId, today);

        AppSignStatusRespVO respVO = new AppSignStatusRespVO();
        respVO.setTodaySigned(todayRecord != null);
        respVO.setContinuousDays(profile.getContinuousSignDays());
        respVO.setTotalDays(profile.getTotalSignDays());
        respVO.setLastSignDate(profile.getLastSignDate());
        // 当前周统计
        String weekKey = PeriodKeyUtils.buildKey(today, PeriodType.WEEK);
        ForumSignPeriodStatDO weekStat = signPeriodStatMapper.selectByUserAndPeriod(userId, PeriodType.WEEK.getCode(),
                weekKey);
        respVO.setWeekContinuousDays(weekStat != null ? weekStat.getCurrentStreak() : 0);
        respVO.setWeekMaxContinuousDays(weekStat != null ? weekStat.getMaxStreak() : 0);
        respVO.setWeekTotalPoints(weekStat != null ? weekStat.getTotalPoints() : 0);
        respVO.setWeekLastSignDate(weekStat != null ? weekStat.getLastSignDate() : null);
        // 本周已签到的日期列表（按周一为一周的第一天）
        WeekFields wf = WeekFields.ISO;
        LocalDate weekStart = today.with(wf.dayOfWeek(), 1);
        LocalDate weekEnd = today.with(wf.dayOfWeek(), 7);
        List<LocalDate> weekSignedDates = signRecordMapper
                .selectListByUserIdBetweenDates(userId, weekStart, weekEnd)
                .stream()
                .map(ForumSignRecordDO::getSignDate)
                .collect(Collectors.toList());
        respVO.setWeekSignedDates(weekSignedDates);

        List<ForumSignRuleDO> configuredRules = loadConfiguredRules();
        List<ForumSignRuleDO> displayRules = configuredRules.isEmpty() ? buildDefaultWeeklyRules() : configuredRules;
        respVO.setSignRules(buildAppRuleList(displayRules));

        List<ForumSignRuleDO> weekRules = configuredRules.isEmpty()
                ? displayRules
                : groupRulesByPeriod(configuredRules).get(PeriodType.WEEK.getCode());
        respVO.setWeekRewardPoints(buildWeekRewardPoints(weekRules));

        return respVO;
    }

    @Override
    public PageResult<ForumSignRecordDO> getSignRecordPage(AppSignRecordPageReqVO reqVO) {
        return signRecordMapper.selectPage(reqVO);
    }

    /**
     * 计算连续签到天数
     */
    private int calculateContinuousDays(Long userId, LocalDate today) {
        ForumSignRecordDO latestRecord = signRecordMapper.selectLatestByUserId(userId);

        if (latestRecord == null) {
            // 首次签到
            return 1;
        }

        LocalDate lastSignDate = latestRecord.getSignDate();
        long daysBetween = ChronoUnit.DAYS.between(lastSignDate, today);

        if (daysBetween == 1) {
            // 连续签到
            return latestRecord.getContinuousDays() + 1;
        } else {
            // 中断了，重新开始
            return 1;
        }
    }

    /**
     * 更新用户签到信息
     */
    private void updateUserSignInfo(ForumUserProfileDO profile, int continuousDays, LocalDate today) {
        ForumUserProfileDO updateObj = ForumUserProfileDO.builder()
                .id(profile.getId())
                .continuousSignDays(continuousDays)
                .totalSignDays(profile.getTotalSignDays() + 1)
                .lastSignDate(today)
                .build();

        userProfileMapper.updateById(updateObj);
    }

    private int handleCheckInForPeriod(Long userId, LocalDate today, PeriodType periodType,
                                       List<ForumSignRuleDO> periodRules) {
        String periodKey = PeriodKeyUtils.buildKey(today, periodType);
        ForumSignPeriodStatDO stat = signPeriodStatMapper.selectByUserAndPeriod(userId, periodType.getCode(),
                periodKey);
        if (stat == null) {
            stat = createNewPeriodStat(userId, periodType, periodKey, today);
        } else {
            updateStreak(stat, today);
        }

        int currentVersion = stat.getVersion() == null ? 0 : stat.getVersion();
        int points = calcPointsByRule(periodType, stat.getCurrentStreak(), periodRules);
        ForumSignPeriodStatDO updateObj = ForumSignPeriodStatDO.builder()
                .id(stat.getId())
                .userId(stat.getUserId())
                .periodType(stat.getPeriodType())
                .periodKey(stat.getPeriodKey())
                .currentStreak(stat.getCurrentStreak())
                .maxStreak(Math.max(stat.getMaxStreak(), stat.getCurrentStreak()))
                .totalPoints(stat.getTotalPoints() + points)
                .lastSignDate(today)
                .version(currentVersion)
                .build();
        int rows = signPeriodStatMapper.updateById(updateObj);
        if (rows == 0) {
            throw ServiceExceptionUtil.exception(SIGN_ALREADY_TODAY); // 乐观锁失败兜底
        }
        return points;
    }

    private ForumSignPeriodStatDO createNewPeriodStat(Long userId, PeriodType type, String periodKey, LocalDate today) {
        ForumSignPeriodStatDO stat = ForumSignPeriodStatDO.builder()
                .userId(userId)
                .periodType(type.getCode())
                .periodKey(periodKey)
                .currentStreak(1)
                .maxStreak(1)
                .totalPoints(0)
                .lastSignDate(today)
                .version(0)
                .build();
        signPeriodStatMapper.insert(stat);
        return stat;
    }

    private void updateStreak(ForumSignPeriodStatDO stat, LocalDate today) {
        LocalDate last = stat.getLastSignDate();
        if (last == null) {
            stat.setCurrentStreak(1);
            return;
        }
        if (last.isEqual(today)) {
            return;
        }
        if (last.plusDays(1).isEqual(today)) {
            stat.setCurrentStreak(stat.getCurrentStreak() + 1);
        } else {
            stat.setCurrentStreak(1);
        }
    }

    private int calcPointsByRule(PeriodType periodType, int streak, List<ForumSignRuleDO> periodRules) {
        ForumSignRuleDO matchedRule = matchRule(periodRules, streak);
        if (matchedRule != null) {
            return matchedRule.getPoints();
        }
        if (periodType == PeriodType.WEEK && (periodRules == null || periodRules.isEmpty())) {
            return calcDefaultWeeklyPoints(streak);
        }
        return 0;
    }

    private int calcDefaultWeeklyPoints(int streak) {
        // 每周连续签到对应积分：1, 1, 3, 5, 7, 9, 10
        int idx = Math.min(Math.max(streak, 1), DEFAULT_WEEKLY_POINTS.length) - 1;
        return DEFAULT_WEEKLY_POINTS[idx];
    }

    private ForumSignRuleDO matchRule(List<ForumSignRuleDO> periodRules, int streak) {
        if (periodRules == null || periodRules.isEmpty()) {
            return null;
        }
        for (ForumSignRuleDO rule : periodRules) {
            if (rule.getMinDays() != null && rule.getMaxDays() != null
                    && streak >= rule.getMinDays() && streak <= rule.getMaxDays()) {
                return rule;
            }
        }
        return null;
    }

    private List<ForumSignRuleDO> loadConfiguredRules() {
        return signRuleMapper.selectListByPeriodType(null).stream()
                .sorted(Comparator.comparing(ForumSignRuleDO::getPeriodType)
                        .thenComparing(ForumSignRuleDO::getMinDays))
                .collect(Collectors.toList());
    }

    private Map<Integer, List<ForumSignRuleDO>> groupRulesByPeriod(List<ForumSignRuleDO> rules) {
        Map<Integer, List<ForumSignRuleDO>> result = new HashMap<>();
        for (ForumSignRuleDO rule : rules) {
            result.computeIfAbsent(rule.getPeriodType(), key -> new ArrayList<>()).add(rule);
        }
        result.values().forEach(list -> list.sort(Comparator.comparing(ForumSignRuleDO::getMinDays)));
        return result;
    }

    private List<PeriodType> resolvePeriodTypes(Map<Integer, List<ForumSignRuleDO>> rulesByPeriod) {
        if (rulesByPeriod.isEmpty()) {
            return Collections.singletonList(PeriodType.WEEK);
        }

        List<PeriodType> result = new ArrayList<>();
        for (PeriodType periodType : PeriodType.values()) {
            if (rulesByPeriod.containsKey(periodType.getCode())) {
                result.add(periodType);
            }
        }
        return result;
    }

    private List<ForumSignRuleDO> buildDefaultWeeklyRules() {
        List<ForumSignRuleDO> rules = new ArrayList<>(DEFAULT_WEEKLY_POINTS.length);
        for (int index = 0; index < DEFAULT_WEEKLY_POINTS.length; index++) {
            int days = index + 1;
            rules.add(ForumSignRuleDO.builder()
                    .periodType(PeriodType.WEEK.getCode())
                    .minDays(days)
                    .maxDays(days)
                    .points(DEFAULT_WEEKLY_POINTS[index])
                    .build());
        }
        return rules;
    }

    private List<AppSignRuleRespVO> buildAppRuleList(List<ForumSignRuleDO> rules) {
        return rules.stream().map(rule -> {
            AppSignRuleRespVO respVO = new AppSignRuleRespVO();
            respVO.setPeriodType(rule.getPeriodType());
            respVO.setMinDays(rule.getMinDays());
            respVO.setMaxDays(rule.getMaxDays());
            respVO.setPoints(rule.getPoints());
            return respVO;
        }).collect(Collectors.toList());
    }

    private List<Integer> buildWeekRewardPoints(List<ForumSignRuleDO> weekRules) {
        if (weekRules == null || weekRules.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> rewardPoints = new ArrayList<>(DEFAULT_WEEKLY_POINTS.length);
        for (int day = 1; day <= DEFAULT_WEEKLY_POINTS.length; day++) {
            ForumSignRuleDO matchedRule = matchRule(weekRules, day);
            rewardPoints.add(matchedRule != null ? matchedRule.getPoints() : 0);
        }
        return rewardPoints;
    }

}
