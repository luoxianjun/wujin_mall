package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Import(QuizLeaderboardServiceImpl.class)
class QuizLeaderboardServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private QuizLeaderboardService quizLeaderboardService;

    @MockBean
    private QuizActivityMapper quizActivityMapper;
    @MockBean
    private QuizAttemptMapper quizAttemptMapper;
    @MockBean
    private MemberUserApi memberUserApi;

    @Test
    void leaderboard_ranksByScoreThenElapsedTime() {
        when(quizActivityMapper.selectById(1L)).thenReturn(QuizActivityDO.builder()
                .id(1L)
                .leaderboardSize(10)
                .build());
        when(memberUserApi.getUser(anyLong())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            MemberUserRespDTO user = new MemberUserRespDTO();
            user.setId(userId);
            user.setNickname("User" + userId);
            return user;
        });

        quizLeaderboardService.recordSubmission(buildAttempt(101L, 1L, 70, 6000L, LocalDateTime.now().minusSeconds(20)));
        quizLeaderboardService.recordSubmission(buildAttempt(102L, 2L, 95, 9000L, LocalDateTime.now().minusSeconds(10)));
        quizLeaderboardService.recordSubmission(buildAttempt(103L, 3L, 95, 5000L, LocalDateTime.now().minusSeconds(5)));

        AppQuizLeaderboardRespVO leaderboard = quizLeaderboardService.getLeaderboard(1L, 3L);

        assertEquals(3, leaderboard.getRankings().size());
        assertEquals(3L, leaderboard.getRankings().get(0).getUserId());
        assertEquals(2L, leaderboard.getRankings().get(1).getUserId());
        assertEquals(3L, leaderboard.getMyRank().getUserId());
    }

    @Test
    void leaderboard_usesSubmitOrderTieBreakerAndMasksNickname() {
        when(quizActivityMapper.selectById(2L)).thenReturn(QuizActivityDO.builder()
                .id(2L)
                .leaderboardSize(10)
                .build());
        when(memberUserApi.getUser(11L)).thenReturn(user(11L, "Alice"));
        when(memberUserApi.getUser(12L)).thenReturn(user(12L, "Bobby"));

        quizLeaderboardService.recordSubmission(buildAttempt(201L, 11L, 88, 7000L, LocalDateTime.now().minusSeconds(20)));
        quizLeaderboardService.recordSubmission(buildAttempt(202L, 12L, 88, 7000L, LocalDateTime.now().minusSeconds(10)));

        AppQuizLeaderboardRespVO leaderboard = quizLeaderboardService.getLeaderboard(2L, 12L);

        assertEquals(11L, leaderboard.getRankings().get(0).getUserId());
        assertEquals(12L, leaderboard.getRankings().get(1).getUserId());
        assertTrue(leaderboard.getRankings().get(0).getNickname().contains("*"));
    }

    @Test
    void leaderboard_hidesRankingsWhenLeaderboardSizeIsZero() {
        when(quizActivityMapper.selectById(3L)).thenReturn(QuizActivityDO.builder()
                .id(3L)
                .leaderboardSize(0)
                .build());
        when(memberUserApi.getUser(31L)).thenReturn(user(31L, "Carol"));

        quizLeaderboardService.recordSubmission(buildAttempt(301L, 3L, 31L, 90, 5000L, LocalDateTime.now()));

        AppQuizLeaderboardRespVO leaderboard = quizLeaderboardService.getLeaderboard(3L, 31L);

        assertEquals(0, leaderboard.getLeaderboardSize());
        assertEquals(0, leaderboard.getRankings().size());
        assertNull(leaderboard.getMyRank());
    }

    private QuizAttemptDO buildAttempt(
            Long attemptId,
            Long userId,
            Integer score,
            Long elapsedMillis,
            LocalDateTime submittedAt) {
        return buildAttempt(attemptId, userId <= 10 ? 1L : 2L, userId, score, elapsedMillis, submittedAt);
    }

    private QuizAttemptDO buildAttempt(
            Long attemptId,
            Long quizActivityId,
            Long userId,
            Integer score,
            Long elapsedMillis,
            LocalDateTime submittedAt) {
        return QuizAttemptDO.builder()
                .id(attemptId)
                .quizActivityId(quizActivityId)
                .userId(userId)
                .score(score)
                .elapsedMillis(elapsedMillis)
                .submittedAt(submittedAt)
                .status(QuizAttemptDO.STATUS_SUBMITTED)
                .build();
    }

    private MemberUserRespDTO user(Long userId, String nickname) {
        MemberUserRespDTO user = new MemberUserRespDTO();
        user.setId(userId);
        user.setNickname(nickname);
        return user;
    }
}
