package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizHeartbeatReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizResultRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizSubmitReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAnswerDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionOptionDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAnswerMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizForumActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizQuestionOptionMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Import(QuizSessionServiceImpl.class)
class QuizSessionServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private QuizSessionService quizSessionService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private QuizActivityMapper quizActivityMapper;
    @MockBean
    private QuizQuestionMapper quizQuestionMapper;
    @MockBean
    private QuizQuestionOptionMapper quizQuestionOptionMapper;
    @MockBean
    private QuizAttemptMapper quizAttemptMapper;
    @MockBean
    private QuizAnswerMapper quizAnswerMapper;
    @MockBean
    private QuizForumActivityMapper quizForumActivityMapper;
    @MockBean
    private QuizLeaderboardService quizLeaderboardService;

    @Test
    void startQuiz_createsRedisSession() {
        QuizAttemptDO insertedAttempt = QuizAttemptDO.builder().id(101L).build();
        mockCommonQuizConfig(1L, 101L);
        when(quizAttemptMapper.selectMaxAttemptNo(1L, 101L)).thenReturn(0);
        doAnswer(invocation -> {
            QuizAttemptDO attempt = invocation.getArgument(0);
            attempt.setId(insertedAttempt.getId());
            return 1;
        }).when(quizAttemptMapper).insert(any(QuizAttemptDO.class));

        AppQuizStartReqVO reqVO = new AppQuizStartReqVO();
        reqVO.setQuizActivityId(1L);

        quizSessionService.startQuiz(101L, reqVO);

        String cacheValue = stringRedisTemplate.opsForValue().get("quiz:session:1:101:1");
        assertNotNull(cacheValue);
        assertTrue(cacheValue.contains("\"questionIds\""));
    }

    @Test
    void submitQuiz_calculatesElapsedTimeServerSide() {
        mockCommonQuizConfig(2L, 202L);
        QuizAttemptDO attempt = QuizAttemptDO.builder()
                .id(2020L)
                .quizActivityId(2L)
                .activityId(200L)
                .userId(202L)
                .attemptNo(1)
                .status(QuizAttemptDO.STATUS_IN_PROGRESS)
                .score(0)
                .elapsedMillis(0L)
                .startedAt(LocalDateTime.now().minusSeconds(5))
                .build();
        when(quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(2L, 202L, 1)).thenReturn(attempt);
        doReturn(1).when(quizAnswerMapper).delete(any(SFunction.class), any());
        List<QuizAnswerDO> storedAnswers = new ArrayList<>();
        doAnswer(invocation -> {
            QuizAnswerDO answer = invocation.getArgument(0);
            answer.setId((long) storedAnswers.size() + 1);
            storedAnswers.add(answer);
            return 1;
        }).when(quizAnswerMapper).insert(any(QuizAnswerDO.class));
        when(quizAnswerMapper.selectByAttemptId(2020L)).thenAnswer(invocation -> new ArrayList<>(storedAnswers));
        when(quizLeaderboardService.getLeaderboard(2L, 202L)).thenReturn(myRank(1));
        stringRedisTemplate.opsForValue().set(
                "quiz:session:2:202:1",
                JSONUtil.toJsonStr(JSONUtil.createObj()
                        .set("serverStartAt", System.currentTimeMillis() - 3500)
                        .set("lastHeartbeatAt", System.currentTimeMillis())
                        .set("questionIds", Collections.singletonList(11L))
                        .set("optionOrderMap", JSONUtil.createObj().set("11", Collections.singletonList("A")))
                        .set("markedQuestionIds", Collections.emptyList())),
                300,
                TimeUnit.SECONDS);

        AppQuizSubmitReqVO reqVO = new AppQuizSubmitReqVO();
        reqVO.setQuizActivityId(2L);
        reqVO.setAttemptNo(1);
        AppQuizSubmitReqVO.AnswerItem answerItem = new AppQuizSubmitReqVO.AnswerItem();
        answerItem.setQuestionId(11L);
        answerItem.setSelectedOptionKeys(Collections.singletonList("A"));
        answerItem.setMarked(false);
        reqVO.setAnswers(Collections.singletonList(answerItem));

        AppQuizResultRespVO result = quizSessionService.submitQuiz(202L, reqVO);

        assertEquals(5, result.getScore());
        assertTrue(result.getElapsedMillis() >= 3000L);
        assertEquals(QuizAttemptDO.STATUS_SUBMITTED, attempt.getStatus());
    }

    @Test
    void submitQuiz_returnsLeaderboardSizeForResultPageVisibility() {
        mockCommonQuizConfig(5L, 505L);
        QuizAttemptDO attempt = QuizAttemptDO.builder()
                .id(5050L)
                .quizActivityId(5L)
                .activityId(500L)
                .userId(505L)
                .attemptNo(1)
                .status(QuizAttemptDO.STATUS_IN_PROGRESS)
                .score(0)
                .elapsedMillis(0L)
                .startedAt(LocalDateTime.now().minusSeconds(5))
                .build();
        when(quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(5L, 505L, 1)).thenReturn(attempt);
        doReturn(1).when(quizAnswerMapper).delete(any(SFunction.class), any());
        when(quizAnswerMapper.selectByAttemptId(5050L)).thenReturn(Collections.emptyList());
        AppQuizLeaderboardRespVO hiddenLeaderboard = new AppQuizLeaderboardRespVO();
        hiddenLeaderboard.setLeaderboardSize(0);
        hiddenLeaderboard.setRankings(Collections.emptyList());
        when(quizLeaderboardService.getLeaderboard(5L, 505L)).thenReturn(hiddenLeaderboard);
        stringRedisTemplate.opsForValue().set(
                "quiz:session:5:505:1",
                JSONUtil.toJsonStr(JSONUtil.createObj()
                        .set("serverStartAt", System.currentTimeMillis() - 3500)
                        .set("lastHeartbeatAt", System.currentTimeMillis())
                        .set("questionIds", Collections.singletonList(11L))
                        .set("optionOrderMap", JSONUtil.createObj().set("11", Collections.singletonList("A")))
                        .set("markedQuestionIds", Collections.emptyList())),
                300,
                TimeUnit.SECONDS);

        AppQuizSubmitReqVO reqVO = new AppQuizSubmitReqVO();
        reqVO.setQuizActivityId(5L);
        reqVO.setAttemptNo(1);
        AppQuizSubmitReqVO.AnswerItem answerItem = new AppQuizSubmitReqVO.AnswerItem();
        answerItem.setQuestionId(11L);
        answerItem.setSelectedOptionKeys(Collections.singletonList("A"));
        answerItem.setMarked(false);
        reqVO.setAnswers(Collections.singletonList(answerItem));

        AppQuizResultRespVO result = quizSessionService.submitQuiz(505L, reqVO);

        assertEquals(0, result.getLeaderboardSize());
    }

    @Test
    void heartbeatQuiz_marksAttemptINVALIDATED_whenTimedOut() {
        QuizAttemptDO attempt = QuizAttemptDO.builder()
                .id(3030L)
                .quizActivityId(3L)
                .activityId(300L)
                .userId(303L)
                .attemptNo(1)
                .status(QuizAttemptDO.STATUS_IN_PROGRESS)
                .build();
        when(quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(3L, 303L, 1)).thenReturn(attempt);
        stringRedisTemplate.opsForValue().set(
                "quiz:session:3:303:1",
                JSONUtil.toJsonStr(JSONUtil.createObj()
                        .set("serverStartAt", System.currentTimeMillis() - 60_000)
                        .set("lastHeartbeatAt", System.currentTimeMillis() - 35_000)
                        .set("questionIds", Collections.emptyList())
                        .set("optionOrderMap", JSONUtil.createObj())
                        .set("markedQuestionIds", Collections.emptyList())),
                300,
                TimeUnit.SECONDS);

        AppQuizHeartbeatReqVO reqVO = new AppQuizHeartbeatReqVO();
        reqVO.setQuizActivityId(3L);
        reqVO.setAttemptNo(1);

        Boolean result = quizSessionService.heartbeatQuiz(303L, reqVO);

        assertFalse(result);
        assertEquals(QuizAttemptDO.STATUS_INVALIDATED, attempt.getStatus());
        assertEquals("HEARTBEAT_TIMEOUT", attempt.getInvalidatedReason());
        assertEquals(null, stringRedisTemplate.opsForValue().get("quiz:session:3:303:1"));
    }

    @Test
    void heartbeatQuiz_marksAttemptINVALIDATED_whenExitQuiz() {
        QuizAttemptDO attempt = QuizAttemptDO.builder()
                .id(4040L)
                .quizActivityId(4L)
                .activityId(400L)
                .userId(404L)
                .attemptNo(1)
                .status(QuizAttemptDO.STATUS_IN_PROGRESS)
                .build();
        when(quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(4L, 404L, 1)).thenReturn(attempt);
        stringRedisTemplate.opsForValue().set(
                "quiz:session:4:404:1",
                JSONUtil.toJsonStr(JSONUtil.createObj()
                        .set("serverStartAt", System.currentTimeMillis() - 5000)
                        .set("lastHeartbeatAt", System.currentTimeMillis())
                        .set("questionIds", Collections.emptyList())
                        .set("optionOrderMap", JSONUtil.createObj())
                        .set("markedQuestionIds", Collections.emptyList())),
                300,
                TimeUnit.SECONDS);

        AppQuizHeartbeatReqVO reqVO = new AppQuizHeartbeatReqVO();
        reqVO.setQuizActivityId(4L);
        reqVO.setAttemptNo(1);
        reqVO.setExitQuiz(true);

        Boolean result = quizSessionService.heartbeatQuiz(404L, reqVO);

        assertFalse(result);
        assertEquals(QuizAttemptDO.STATUS_INVALIDATED, attempt.getStatus());
        assertEquals("USER_EXIT", attempt.getInvalidatedReason());
        assertEquals(null, stringRedisTemplate.opsForValue().get("quiz:session:4:404:1"));
    }

    private void mockCommonQuizConfig(Long quizActivityId, Long userId) {
        when(quizActivityMapper.selectById(quizActivityId)).thenReturn(QuizActivityDO.builder()
                .id(quizActivityId)
                .activityId(quizActivityId * 100)
                .questionBankId(500L)
                .questionCount(1)
                .maxAttempts(3)
                .durationSeconds(600)
                .randomOptionOrder(false)
                .randomQuestionOrder(false)
                .answerRevealMode(QuizActivityDO.ANSWER_REVEAL_MODE_AFTER_SUBMIT)
                .status(QuizActivityDO.STATUS_ENABLED)
                .build());
        when(quizForumActivityMapper.selectById(quizActivityId * 100))
                .thenReturn(buildForumActivity(quizActivityId * 100));
        when(quizQuestionMapper.selectByBankId(500L)).thenReturn(Collections.singletonList(QuizQuestionDO.builder()
                .id(11L)
                .questionType(QuizQuestionDO.QUESTION_TYPE_SINGLE_CHOICE)
                .content("2 + 3 = ?")
                .score(5)
                .explanation("5")
                .build()));
        when(quizQuestionOptionMapper.selectByQuestionIds(Collections.singletonList(11L))).thenReturn(java.util.Arrays.asList(
                QuizQuestionOptionDO.builder().questionId(11L).optionKey("A").content("5").isCorrect(true).build(),
                QuizQuestionOptionDO.builder().questionId(11L).optionKey("B").content("6").isCorrect(false).build()));
    }

    private AppQuizLeaderboardRespVO myRank(Integer rank) {
        AppQuizLeaderboardRespVO leaderboard = new AppQuizLeaderboardRespVO();
        AppQuizLeaderboardRespVO.RankItem rankItem = new AppQuizLeaderboardRespVO.RankItem();
        rankItem.setRank(rank);
        leaderboard.setMyRank(rankItem);
        leaderboard.setRankings(Collections.singletonList(rankItem));
        return leaderboard;
    }

    private QuizForumActivityDO buildForumActivity(Long activityId) {
        QuizForumActivityDO activity = new QuizForumActivityDO();
        activity.setId(activityId);
        activity.setStartTime(LocalDateTime.now().minusHours(1));
        activity.setEndTime(LocalDateTime.now().plusHours(1));
        return activity;
    }
}
