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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizRewardServiceTest {

    @InjectMocks
    private QuizRewardServiceImpl quizRewardService;

    @Mock
    private QuizActivityMapper quizActivityMapper;
    @Mock
    private QuizForumActivityMapper quizForumActivityMapper;
    @Mock
    private QuizRewardRuleMapper quizRewardRuleMapper;
    @Mock
    private QuizRewardRecordMapper quizRewardRecordMapper;
    @Mock
    private QuizLeaderboardService quizLeaderboardService;
    @Mock
    private GamificationPointsService gamificationPointsService;
    @Mock
    private GamificationNotificationService gamificationNotificationService;

    @Test
    void distributeQuizRewards_grantsPointsAndIsIdempotent() {
        Long quizActivityId = 1L;
        AtomicReference<QuizRewardRecordDO> storedRecord = new AtomicReference<>();
        when(quizActivityMapper.selectById(quizActivityId)).thenReturn(QuizActivityDO.builder()
                .id(quizActivityId)
                .activityId(100L)
                .build());
        when(quizForumActivityMapper.selectById(100L))
                .thenReturn(buildForumActivity(100L, LocalDateTime.now().minusHours(1)));
        when(quizRewardRuleMapper.selectByQuizActivityId(quizActivityId)).thenReturn(Collections.singletonList(
                QuizRewardRuleDO.builder()
                        .quizActivityId(quizActivityId)
                        .rankStart(1)
                        .rankEnd(1)
                        .rewardType(QuizRewardRuleDO.REWARD_TYPE_POINTS)
                        .pointAmount(30)
                        .rewardName("Top 1")
                        .build()));
        when(quizLeaderboardService.getRankedDetails(quizActivityId)).thenReturn(Collections.singletonList(
                buildDetail(501L, 2001L)));
        when(quizRewardRecordMapper.selectByQuizActivityIdAndUserIdAndRewardName(quizActivityId, 2001L, "Top 1"))
                .thenAnswer(invocation -> storedRecord.get());
        doAnswer(invocation -> {
            QuizRewardRecordDO record = invocation.getArgument(0);
            record.setId(9001L);
            storedRecord.set(record);
            return 1;
        }).when(quizRewardRecordMapper).insert(any(QuizRewardRecordDO.class));
        when(gamificationPointsService.grantPoints(eq(2001L), eq(30), any(), eq(9001L), eq("quiz rank reward")))
                .thenReturn(true);
        when(gamificationNotificationService.sendNotification(anyLong(), any(), any(), any(), any())).thenReturn(true);

        int firstRun = quizRewardService.distributeQuizRewards(quizActivityId);
        int secondRun = quizRewardService.distributeQuizRewards(quizActivityId);

        assertEquals(1, firstRun);
        assertEquals(0, secondRun);
        assertEquals(QuizRewardRecordDO.STATUS_SUCCESS, storedRecord.get().getStatus());
        verify(gamificationPointsService).grantPoints(eq(2001L), eq(30), any(), eq(9001L), eq("quiz rank reward"));
    }

    @Test
    void distributeQuizRewards_keepsPhysicalRewardsPendingAndRespectsWindow() {
        Long quizActivityId = 2L;
        AtomicReference<QuizRewardRecordDO> storedRecord = new AtomicReference<>();
        when(quizActivityMapper.selectById(quizActivityId)).thenReturn(QuizActivityDO.builder()
                .id(quizActivityId)
                .activityId(200L)
                .build());
        when(quizForumActivityMapper.selectById(200L))
                .thenReturn(buildForumActivity(200L, LocalDateTime.now().minusHours(2)));
        when(quizRewardRuleMapper.selectByQuizActivityId(quizActivityId)).thenReturn(Collections.singletonList(
                QuizRewardRuleDO.builder()
                        .quizActivityId(quizActivityId)
                        .rankStart(1)
                        .rankEnd(1)
                        .rewardType(QuizRewardRuleDO.REWARD_TYPE_PHYSICAL)
                        .pointAmount(0)
                        .rewardName("Gift Box")
                        .build()));
        when(quizLeaderboardService.getRankedDetails(quizActivityId)).thenReturn(Collections.singletonList(
                buildDetail(601L, 3001L)));
        doAnswer(invocation -> {
            QuizRewardRecordDO record = invocation.getArgument(0);
            record.setId(9002L);
            storedRecord.set(record);
            return 1;
        }).when(quizRewardRecordMapper).insert(any(QuizRewardRecordDO.class));

        int distributed = quizRewardService.distributeQuizRewards(quizActivityId);

        assertEquals(0, distributed);
        assertEquals(QuizRewardRecordDO.STATUS_PENDING_FULFILLMENT, storedRecord.get().getStatus());
        verify(gamificationPointsService, never()).grantPoints(anyLong(), any(), any(), any(), any());

        when(quizForumActivityMapper.selectById(200L))
                .thenReturn(buildForumActivity(200L, LocalDateTime.now().minusHours(30)));
        assertEquals(0, quizRewardService.distributeQuizRewards(quizActivityId));
    }

    private QuizForumActivityDO buildForumActivity(Long activityId, LocalDateTime endTime) {
        QuizForumActivityDO activity = new QuizForumActivityDO();
        activity.setId(activityId);
        activity.setEndTime(endTime);
        return activity;
    }

    private QuizLeaderboardService.LeaderboardDetail buildDetail(Long attemptId, Long userId) {
        QuizLeaderboardService.LeaderboardDetail detail = new QuizLeaderboardService.LeaderboardDetail();
        detail.setAttemptId(attemptId);
        detail.setUserId(userId);
        detail.setScore(99);
        detail.setElapsedMillis(5000L);
        detail.setSubmitSequence(1L);
        detail.setNickname("Champion");
        detail.setSubmittedAt(LocalDateTime.now().minusMinutes(5));
        return detail;
    }
}
