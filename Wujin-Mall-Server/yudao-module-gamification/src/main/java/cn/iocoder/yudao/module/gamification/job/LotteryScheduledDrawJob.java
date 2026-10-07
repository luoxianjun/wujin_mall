package cn.iocoder.yudao.module.gamification.job;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.service.lottery.LotteryDrawService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时开奖任务
 *
 * 每分钟扫描一次，对达到开奖时间的定时抽奖活动执行开奖。
 * 使用 Redis 键确保每个活动仅开奖一次。
 */
@Component
public class LotteryScheduledDrawJob {

    private static final Logger log = LoggerFactory.getLogger(LotteryScheduledDrawJob.class);

    private static final String DRAW_EXECUTED_KEY_PREFIX = "lottery:draw:executed:";

    @Resource
    private LotteryActivityMapper lotteryActivityMapper;

    @Resource
    private LotteryDrawService lotteryDrawService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每分钟执行一次，查找需要开奖的定时抽奖活动
     */
    @Scheduled(fixedDelay = 60 * 1000)
    public void execute() {
        List<LotteryActivityDO> pendingActivities = TenantUtils.executeIgnore(() -> {
            LocalDateTime now = LocalDateTime.now();

            // 查询需要开奖的定时抽奖活动：type=SCHEDULED, drawTime <= now, status=ENABLED
            return lotteryActivityMapper.selectList(
                    new LambdaQueryWrapper<LotteryActivityDO>()
                            .eq(LotteryActivityDO::getType, LotteryActivityDO.TYPE_SCHEDULED)
                            .le(LotteryActivityDO::getDrawTime, now)
                            .eq(LotteryActivityDO::getStatus, 0) // ENABLED
            );
        });

        if (pendingActivities.isEmpty()) {
            return;
        }

        for (LotteryActivityDO activity : pendingActivities) {
            Long tenantId = activity.getTenantId();
            if (tenantId == null) {
                log.warn("[execute] Lottery {} has no tenant id, skip", activity.getId());
                continue;
            }
            TenantUtils.execute(tenantId, () -> executeActivity(activity));
        }
    }

    private void executeActivity(LotteryActivityDO activity) {
        String redisKey = DRAW_EXECUTED_KEY_PREFIX + activity.getId();

        // 幂等检查：是否已经开过奖
        Boolean alreadyExecuted = stringRedisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(alreadyExecuted)) {
            log.debug("[execute] Lottery {} already drawn, skip", activity.getId());
            return;
        }

        // 标记为已开奖（过期时间24小时）
        stringRedisTemplate.opsForValue().set(redisKey, "1", 24, TimeUnit.HOURS);

        try {
            log.info("[execute] Executing scheduled draw for lottery activity: id={}, activityId={}",
                    activity.getId(), activity.getActivityId());
            lotteryDrawService.executeScheduledDraw(activity.getId());

            // 标记活动为已禁用（已开奖）
            activity.setStatus(1); // DISABLED
            lotteryActivityMapper.updateById(activity);

            log.info("[execute] Scheduled draw completed for lottery activity: id={}", activity.getId());
        } catch (Exception e) {
            // 开奖失败，移除Redis标记以允许重试
            stringRedisTemplate.delete(redisKey);
            log.error("[execute] Scheduled draw failed for lottery activity: id={}", activity.getId(), e);
        }
    }
}
