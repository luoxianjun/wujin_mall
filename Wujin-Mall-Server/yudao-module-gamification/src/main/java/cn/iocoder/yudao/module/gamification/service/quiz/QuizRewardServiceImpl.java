package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRecordDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRuleDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizRewardRecordMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizRewardRuleMapper;
import cn.iocoder.yudao.module.gamification.service.notification.GamificationNotificationService;
import cn.iocoder.yudao.module.gamification.service.points.GamificationPointsService;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuizRewardServiceImpl implements QuizRewardService {

    @Resource
    private QuizActivityMapper quizActivityMapper;

    @Resource
    private QuizForumActivityMapper quizForumActivityMapper;

    @Resource
    private QuizRewardRuleMapper quizRewardRuleMapper;

    @Resource
    private QuizRewardRecordMapper quizRewardRecordMapper;

    @Resource
    private QuizLeaderboardService quizLeaderboardService;

    @Resource
    private GamificationPointsService gamificationPointsService;

    @Resource
    private GamificationNotificationService gamificationNotificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int distributePendingRewards() {
        int count = 0;
        for (QuizActivityDO quizActivity : quizActivityMapper.selectList()) {
            count += distributeQuizRewards(quizActivity.getId());
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int distributeQuizRewards(Long quizActivityId) {
        QuizActivityDO quizActivity = quizActivityMapper.selectById(quizActivityId);
        if (quizActivity == null) {
            return 0;
        }
        QuizForumActivityDO activity = quizForumActivityMapper.selectById(quizActivity.getActivityId());
        if (activity == null || activity.getEndTime() == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getEndTime()) || now.isAfter(activity.getEndTime().plusHours(24))) {
            return 0;
        }
        List<QuizRewardRuleDO> rules = quizRewardRuleMapper.selectByQuizActivityId(quizActivityId);
        if (rules.isEmpty()) {
            return 0;
        }
        List<QuizLeaderboardService.LeaderboardDetail> rankings = quizLeaderboardService.getRankedDetails(quizActivityId);
        int successCount = 0;
        for (QuizRewardRuleDO rule : rules) {
            int rankStart = rule.getRankStart() == null ? 1 : Math.max(rule.getRankStart(), 1);
            int rankEnd = rule.getRankEnd() == null ? rankStart : rule.getRankEnd();
            for (int rank = rankStart; rank <= rankEnd && rank <= rankings.size(); rank++) {
                QuizLeaderboardService.LeaderboardDetail detail = rankings.get(rank - 1);
                String rewardName = rule.getRewardName() == null || rule.getRewardName().trim().isEmpty()
                        ? "Rank " + rankStart + "-" + rankEnd
                        : rule.getRewardName();
                QuizRewardRecordDO existing = quizRewardRecordMapper
                        .selectByQuizActivityIdAndUserIdAndRewardName(quizActivityId, detail.getUserId(), rewardName);
                if (existing != null && !QuizRewardRecordDO.STATUS_FAILED.equals(existing.getStatus())) {
                    continue;
                }

                QuizRewardRecordDO rewardRecord = existing == null
                        ? QuizRewardRecordDO.builder()
                        .quizActivityId(quizActivityId)
                        .attemptId(detail.getAttemptId())
                        .userId(detail.getUserId())
                        .rewardType(rule.getRewardType())
                        .pointAmount(rule.getPointAmount())
                        .rewardName(rewardName)
                        .status(QuizRewardRecordDO.STATUS_PENDING)
                        .retryCount(0)
                        .build()
                        : existing;
                rewardRecord.setAttemptId(detail.getAttemptId());
                rewardRecord.setRewardType(rule.getRewardType());
                rewardRecord.setPointAmount(rule.getPointAmount());
                rewardRecord.setRewardName(rewardName);
                rewardRecord.setDistributedAt(null);

                if (existing == null) {
                    quizRewardRecordMapper.insert(rewardRecord);
                } else {
                    rewardRecord.setRetryCount(existing.getRetryCount() == null ? 0 : existing.getRetryCount());
                    quizRewardRecordMapper.updateById(rewardRecord);
                }

                if (QuizRewardRuleDO.REWARD_TYPE_POINTS.equals(rule.getRewardType())) {
                    boolean granted = gamificationPointsService.grantPoints(
                            detail.getUserId(),
                            rule.getPointAmount(),
                            MemberPointBizTypeEnum.QUIZ_RANK_REWARD.getType(),
                            rewardRecord.getId(),
                            "quiz rank reward");
                    rewardRecord.setStatus(granted ? QuizRewardRecordDO.STATUS_SUCCESS : QuizRewardRecordDO.STATUS_FAILED);
                    rewardRecord.setDistributedAt(granted ? now : null);
                    rewardRecord.setRetryCount(granted
                            ? rewardRecord.getRetryCount()
                            : (rewardRecord.getRetryCount() == null ? 0 : rewardRecord.getRetryCount()) + 1);
                    if (granted) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("rewardName", rewardName);
                        params.put("pointAmount", rule.getPointAmount());
                        gamificationNotificationService.sendNotification(
                                detail.getUserId(),
                                "QUIZ_RANK_REWARD",
                                "Quiz Reward",
                                "You received a quiz ranking reward.",
                                params);
                        successCount++;
                    }
                } else {
                    rewardRecord.setStatus(QuizRewardRecordDO.STATUS_PENDING_FULFILLMENT);
                    rewardRecord.setDistributedAt(now);
                }

                quizRewardRecordMapper.updateById(rewardRecord);
            }
        }
        return successCount;
    }
}
