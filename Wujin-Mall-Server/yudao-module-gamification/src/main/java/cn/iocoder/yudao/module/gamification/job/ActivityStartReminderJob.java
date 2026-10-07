package cn.iocoder.yudao.module.gamification.job;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivityMapper;
import cn.iocoder.yudao.module.gamification.service.notification.GamificationNotificationService;
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
 * 活动开始提醒定时任务 (CROSS-02)
 * 
 * 每5分钟扫描一次，对即将在30分钟内开始的活动发送参与提醒。
 * 使用 Redis 键确保每个活动仅发送一次提醒。
 */
@Component
public class ActivityStartReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ActivityStartReminderJob.class);

    private static final String REMINDED_KEY_PREFIX = "activity:start:reminded:";

    @Resource
    private QuizForumActivityMapper quizForumActivityMapper;

    @Resource
    private GamificationNotificationService notificationService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每5分钟执行一次，查找即将开始的活动并发送提醒
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void execute() {
        // 注意：忽略自动多租户，因为定时任务没有租户上下文
        TenantUtils.executeIgnore(() -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime thirtyMinutesLater = now.plusMinutes(30);

            // 查询即将在30分钟内开始的活动
            List<QuizForumActivityDO> upcomingActivities =
                    quizForumActivityMapper.selectStartingBetween(now, thirtyMinutesLater);

            if (upcomingActivities.isEmpty()) {
                return;
            }

            for (QuizForumActivityDO activity : upcomingActivities) {
                String redisKey = REMINDED_KEY_PREFIX + activity.getId();

                // 检查是否已经发送过提醒（幂等保护）
                Boolean alreadyReminded = stringRedisTemplate.hasKey(redisKey);
                if (Boolean.TRUE.equals(alreadyReminded)) {
                    log.debug("[execute] Activity {} already reminded, skip", activity.getId());
                    continue;
                }

                // 标记为已提醒（过期时间2小时，足够覆盖活动开始后的窗口）
                stringRedisTemplate.opsForValue().set(redisKey, "1", 2, TimeUnit.HOURS);

                log.info("[execute] Sending start reminder for activity: id={}, title={}",
                        activity.getId(), activity.getTitle());

                // TODO: 此处需要查询活动参与者列表
                // 当前实现：仅记录日志，等待参与者查询接口可用后补充
                // 未来可通过 ForumActivityApi 或直接查询 forum_activity_join 表获取参与者
                // notificationService.batchSendActivityStartReminder(participantUserIds, activity.getId(), activity.getTitle());
            }
        });
    }
}
