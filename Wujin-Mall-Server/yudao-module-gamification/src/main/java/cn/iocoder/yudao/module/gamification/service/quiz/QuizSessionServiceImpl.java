package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizHeartbeatReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizResultRespVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizStartRespVO;
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
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

@Service
public class QuizSessionServiceImpl implements QuizSessionService {

    private static final String SESSION_KEY_PREFIX = "quiz:session:";
    private static final long HEARTBEAT_TIMEOUT_MILLIS = 30_000L;

    @Resource
    private QuizActivityMapper quizActivityMapper;
    @Resource
    private QuizQuestionMapper quizQuestionMapper;
    @Resource
    private QuizQuestionOptionMapper quizQuestionOptionMapper;
    @Resource
    private QuizAttemptMapper quizAttemptMapper;
    @Resource
    private QuizAnswerMapper quizAnswerMapper;
    @Resource
    private QuizForumActivityMapper quizForumActivityMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private QuizLeaderboardService quizLeaderboardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppQuizStartRespVO startQuiz(Long userId, AppQuizStartReqVO reqVO) {
        QuizActivityDO quizActivity = requireQuizActivity(reqVO.getQuizActivityId());
        QuizForumActivityDO activity = requireForumActivity(quizActivity.getActivityId());
        validateCanStart(quizActivity, activity);

        int nextAttemptNo = quizAttemptMapper.selectMaxAttemptNo(quizActivity.getId(), userId) + 1;
        if (quizActivity.getMaxAttempts() != null && nextAttemptNo > quizActivity.getMaxAttempts()) {
            throw new ServiceException(BAD_REQUEST.getCode(), "答题次数已用完");
        }

        List<QuizQuestionDO> selectedQuestions = selectQuestions(
                quizQuestionMapper.selectByBankId(quizActivity.getQuestionBankId()),
                quizActivity.getQuestionCount(),
                Boolean.TRUE.equals(quizActivity.getRandomQuestionOrder()));
        if (selectedQuestions.isEmpty()) {
            throw new ServiceException(BAD_REQUEST.getCode(), "题库暂无可用题目");
        }

        Map<Long, List<QuizQuestionOptionDO>> optionMap = quizQuestionOptionMapper
                .selectByQuestionIds(selectedQuestions.stream().map(QuizQuestionDO::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestionOptionDO::getQuestionId));

        QuizAttemptDO attempt = QuizAttemptDO.builder()
                .quizActivityId(quizActivity.getId())
                .activityId(quizActivity.getActivityId())
                .userId(userId)
                .attemptNo(nextAttemptNo)
                .status(QuizAttemptDO.STATUS_IN_PROGRESS)
                .score(0)
                .elapsedMillis(0L)
                .startedAt(LocalDateTime.now())
                .build();
        quizAttemptMapper.insert(attempt);

        SessionCache sessionCache = new SessionCache();
        sessionCache.setServerStartAt(System.currentTimeMillis());
        sessionCache.setLastHeartbeatAt(System.currentTimeMillis());
        sessionCache.setQuestionIds(selectedQuestions.stream().map(QuizQuestionDO::getId).collect(Collectors.toList()));
        Map<String, List<String>> optionOrderMap = new LinkedHashMap<>();
        for (QuizQuestionDO question : selectedQuestions) {
            List<QuizQuestionOptionDO> options = new ArrayList<>(optionMap.getOrDefault(question.getId(), Collections.emptyList()));
            if (Boolean.TRUE.equals(quizActivity.getRandomOptionOrder())) {
                Collections.shuffle(options);
            }
            optionOrderMap.put(String.valueOf(question.getId()),
                    options.stream().map(QuizQuestionOptionDO::getOptionKey).collect(Collectors.toList()));
        }
        sessionCache.setOptionOrderMap(optionOrderMap);
        sessionCache.setMarkedQuestionIds(new ArrayList<>());
        saveSession(quizActivity.getId(), userId, nextAttemptNo, sessionCache, quizActivity.getDurationSeconds());

        AppQuizStartRespVO respVO = new AppQuizStartRespVO();
        respVO.setQuizActivityId(quizActivity.getId());
        respVO.setAttemptNo(nextAttemptNo);
        respVO.setDurationSeconds(quizActivity.getDurationSeconds());
        respVO.setMaxAttempts(quizActivity.getMaxAttempts());
        respVO.setAnswerRevealMode(quizActivity.getAnswerRevealMode());
        respVO.setQuestions(selectedQuestions.stream().map(question -> {
            AppQuizStartRespVO.Question item = new AppQuizStartRespVO.Question();
            item.setId(question.getId());
            item.setQuestionType(question.getQuestionType());
            item.setContent(question.getContent());
            item.setImageUrl(question.getImageUrl());
            item.setScore(question.getScore());
            List<String> order = optionOrderMap.getOrDefault(String.valueOf(question.getId()), Collections.emptyList());
            Map<String, QuizQuestionOptionDO> rawOptions = optionMap.getOrDefault(question.getId(), Collections.emptyList())
                    .stream()
                    .collect(Collectors.toMap(QuizQuestionOptionDO::getOptionKey, option -> option));
            item.setOptions(order.stream().map(optionKey -> {
                QuizQuestionOptionDO option = rawOptions.get(optionKey);
                AppQuizStartRespVO.Option optionVO = new AppQuizStartRespVO.Option();
                optionVO.setOptionKey(option.getOptionKey());
                optionVO.setContent(option.getContent());
                return optionVO;
            }).collect(Collectors.toList()));
            return item;
        }).collect(Collectors.toList()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean heartbeatQuiz(Long userId, AppQuizHeartbeatReqVO reqVO) {
        QuizAttemptDO attempt = requireAttempt(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
        SessionCache sessionCache = loadSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
        if (sessionCache == null) {
            invalidateAttempt(attempt, "SESSION_MISSING");
            return false;
        }
        long now = System.currentTimeMillis();
        if (Boolean.TRUE.equals(reqVO.getExitQuiz())) {
            invalidateAttempt(attempt, "CLIENT_EXIT");
            deleteSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
            return false;
        }
        if (now - sessionCache.getLastHeartbeatAt() > HEARTBEAT_TIMEOUT_MILLIS) {
            invalidateAttempt(attempt, "HEARTBEAT_TIMEOUT");
            deleteSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
            return false;
        }
        sessionCache.setLastHeartbeatAt(now);
        saveSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo(), sessionCache, null);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppQuizResultRespVO submitQuiz(Long userId, AppQuizSubmitReqVO reqVO) {
        QuizAttemptDO attempt = requireAttempt(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
        QuizActivityDO quizActivity = requireQuizActivity(reqVO.getQuizActivityId());
        SessionCache sessionCache = loadSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
        if (sessionCache == null) {
            invalidateAttempt(attempt, "SESSION_MISSING");
            return buildResult(attempt, quizActivity);
        }
        long now = System.currentTimeMillis();
        if (now - sessionCache.getLastHeartbeatAt() > HEARTBEAT_TIMEOUT_MILLIS) {
            invalidateAttempt(attempt, "HEARTBEAT_TIMEOUT");
            deleteSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
            return buildResult(attempt, quizActivity);
        }

        List<QuizQuestionDO> questions = quizQuestionMapper.selectByBankId(quizActivity.getQuestionBankId()).stream()
                .filter(question -> sessionCache.getQuestionIds().contains(question.getId()))
                .sorted(Comparator.comparingInt(question -> sessionCache.getQuestionIds().indexOf(question.getId())))
                .collect(Collectors.toList());
        Map<Long, List<QuizQuestionOptionDO>> optionMap = quizQuestionOptionMapper.selectByQuestionIds(
                        questions.stream().map(QuizQuestionDO::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestionOptionDO::getQuestionId));
        Map<Long, AppQuizSubmitReqVO.AnswerItem> answerMap = reqVO.getAnswers().stream()
                .collect(Collectors.toMap(AppQuizSubmitReqVO.AnswerItem::getQuestionId, item -> item, (left, right) -> right));

        quizAnswerMapper.delete(QuizAnswerDO::getAttemptId, attempt.getId());
        int totalScore = 0;
        for (QuizQuestionDO question : questions) {
            AppQuizSubmitReqVO.AnswerItem answerItem = answerMap.get(question.getId());
            List<String> selectedKeys = answerItem == null || CollectionUtils.isEmpty(answerItem.getSelectedOptionKeys())
                    ? Collections.emptyList()
                    : answerItem.getSelectedOptionKeys().stream()
                    .filter(Objects::nonNull)
                    .map(key -> key.trim().toUpperCase(Locale.ROOT))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            Set<String> correctKeys = optionMap.getOrDefault(question.getId(), Collections.emptyList()).stream()
                    .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                    .map(option -> option.getOptionKey().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            boolean correct = new LinkedHashSet<>(selectedKeys).equals(correctKeys);
            int earnedScore = correct ? (question.getScore() == null ? 0 : question.getScore()) : 0;
            totalScore += earnedScore;
            quizAnswerMapper.insert(QuizAnswerDO.builder()
                    .attemptId(attempt.getId())
                    .questionId(question.getId())
                    .selectedOptionKeys(String.join(",", selectedKeys))
                    .isCorrect(correct)
                    .earnedScore(earnedScore)
                    .marked(answerItem != null && Boolean.TRUE.equals(answerItem.getMarked()))
                    .answeredAt(LocalDateTime.now())
                    .build());
        }

        long elapsedMillis = now - sessionCache.getServerStartAt();
        attempt.setScore(totalScore);
        attempt.setElapsedMillis(elapsedMillis);
        attempt.setStatus(quizActivity.getDurationSeconds() != null && elapsedMillis >= quizActivity.getDurationSeconds() * 1000L
                ? QuizAttemptDO.STATUS_TIMEOUT_SUBMITTED
                : QuizAttemptDO.STATUS_SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setInvalidatedReason(null);
        quizAttemptMapper.updateById(attempt);
        deleteSession(reqVO.getQuizActivityId(), userId, reqVO.getAttemptNo());
        quizLeaderboardService.recordSubmission(attempt);
        return buildResult(attempt, quizActivity);
    }

    @Override
    public AppQuizResultRespVO getQuizResult(Long userId, Long quizActivityId, Integer attemptNo) {
        QuizAttemptDO attempt = attemptNo == null
                ? quizAttemptMapper.selectLatestByQuizActivityIdAndUserId(quizActivityId, userId)
                : quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(quizActivityId, userId, attemptNo);
        if (attempt == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "答题记录不存在");
        }
        return buildResult(attempt, requireQuizActivity(quizActivityId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoSubmitExpiredAttempts() {
        int count = 0;
        for (QuizAttemptDO attempt : quizAttemptMapper.selectInProgressList()) {
            QuizActivityDO quizActivity = quizActivityMapper.selectById(attempt.getQuizActivityId());
            if (quizActivity == null || quizActivity.getDurationSeconds() == null || attempt.getStartedAt() == null) {
                continue;
            }
            if (LocalDateTime.now().isBefore(attempt.getStartedAt().plusSeconds(quizActivity.getDurationSeconds()))) {
                continue;
            }
            invalidateAttempt(attempt, "AUTO_TIMEOUT");
            deleteSession(attempt.getQuizActivityId(), attempt.getUserId(), attempt.getAttemptNo());
            count++;
        }
        return count;
    }

    private AppQuizResultRespVO buildResult(QuizAttemptDO attempt, QuizActivityDO quizActivity) {
        AppQuizResultRespVO respVO = new AppQuizResultRespVO();
        respVO.setStatus(attempt.getStatus());
        respVO.setScore(attempt.getScore());
        respVO.setElapsedMillis(attempt.getElapsedMillis());
        respVO.setSubmittedAt(attempt.getSubmittedAt());
        cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO leaderboard =
                quizLeaderboardService.getLeaderboard(attempt.getQuizActivityId(), attempt.getUserId());
        respVO.setLeaderboardSize(leaderboard.getLeaderboardSize());
        if (leaderboard.getMyRank() != null) {
            respVO.setRank(leaderboard.getMyRank().getRank());
        }
        if (QuizAttemptDO.STATUS_INVALIDATED.equals(attempt.getStatus())) {
            respVO.setQuestionResults(Collections.emptyList());
            return respVO;
        }
        List<QuizAnswerDO> answers = quizAnswerMapper.selectByAttemptId(attempt.getId());
        Map<Long, QuizAnswerDO> answerMap = answers.stream()
                .collect(Collectors.toMap(QuizAnswerDO::getQuestionId, answer -> answer));
        List<QuizQuestionDO> questions = quizQuestionMapper.selectByBankId(quizActivity.getQuestionBankId());
        Map<Long, List<QuizQuestionOptionDO>> optionMap = quizQuestionOptionMapper.selectByQuestionIds(
                        questions.stream().map(QuizQuestionDO::getId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.groupingBy(QuizQuestionOptionDO::getQuestionId));
        boolean reveal = shouldRevealQuestionResults(quizActivity, quizForumActivityMapper.selectById(quizActivity.getActivityId()));
        respVO.setQuestionResults(questions.stream()
                .filter(question -> answerMap.containsKey(question.getId()))
                .map(question -> {
                    QuizAnswerDO answer = answerMap.get(question.getId());
                    AppQuizResultRespVO.QuestionResult item = new AppQuizResultRespVO.QuestionResult();
                    item.setQuestionId(question.getId());
                    item.setContent(question.getContent());
                    item.setSelectedOptionKeys(splitOptionKeys(answer.getSelectedOptionKeys()));
                    item.setCorrect(answer.getIsCorrect());
                    item.setEarnedScore(answer.getEarnedScore());
                    if (reveal) {
                        item.setCorrectAnswer(optionMap.getOrDefault(question.getId(), Collections.emptyList()).stream()
                                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                                .map(QuizQuestionOptionDO::getOptionKey)
                                .collect(Collectors.toList()));
                        item.setExplanation(question.getExplanation());
                    }
                    return item;
                }).collect(Collectors.toList()));
        return respVO;
    }

    private boolean shouldRevealQuestionResults(QuizActivityDO quizActivity, QuizForumActivityDO activity) {
        if (QuizActivityDO.ANSWER_REVEAL_MODE_HIDDEN.equals(quizActivity.getAnswerRevealMode())) {
            return false;
        }
        if (QuizActivityDO.ANSWER_REVEAL_MODE_AFTER_ACTIVITY_END.equals(quizActivity.getAnswerRevealMode())) {
            return activity != null && activity.getEndTime() != null && !LocalDateTime.now().isBefore(activity.getEndTime());
        }
        return true;
    }

    private List<String> splitOptionKeys(String optionKeys) {
        if (optionKeys == null || optionKeys.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String optionKey : optionKeys.split(",")) {
            if (optionKey != null && !optionKey.trim().isEmpty()) {
                result.add(optionKey.trim());
            }
        }
        return result;
    }

    private List<QuizQuestionDO> selectQuestions(List<QuizQuestionDO> questions, Integer questionCount, boolean randomQuestionOrder) {
        List<QuizQuestionDO> selected = new ArrayList<>(questions);
        if (randomQuestionOrder) {
            Collections.shuffle(selected);
        }
        int max = questionCount == null || questionCount <= 0 ? selected.size() : Math.min(questionCount, selected.size());
        return new ArrayList<>(selected.subList(0, max));
    }

    private void validateCanStart(QuizActivityDO quizActivity, QuizForumActivityDO activity) {
        if (!QuizActivityDO.STATUS_ENABLED.equals(quizActivity.getStatus())) {
            throw new ServiceException(BAD_REQUEST.getCode(), "答题活动暂未启用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new ServiceException(BAD_REQUEST.getCode(), "活动未开始，暂时不能答题");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new ServiceException(BAD_REQUEST.getCode(), "活动已结束，不能继续答题");
        }
    }

    private QuizActivityDO requireQuizActivity(Long quizActivityId) {
        QuizActivityDO quizActivity = quizActivityMapper.selectById(quizActivityId);
        if (quizActivity == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "答题活动不存在");
        }
        return quizActivity;
    }

    private QuizForumActivityDO requireForumActivity(Long activityId) {
        QuizForumActivityDO activity = quizForumActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "活动不存在");
        }
        return activity;
    }

    private QuizAttemptDO requireAttempt(Long quizActivityId, Long userId, Integer attemptNo) {
        QuizAttemptDO attempt = quizAttemptMapper.selectByQuizActivityIdAndUserIdAndAttemptNo(quizActivityId, userId, attemptNo);
        if (attempt == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "答题记录不存在");
        }
        return attempt;
    }

    private void invalidateAttempt(QuizAttemptDO attempt, String reason) {
        attempt.setStatus(QuizAttemptDO.STATUS_INVALIDATED);
        attempt.setInvalidatedReason(reason);
        attempt.setSubmittedAt(LocalDateTime.now());
        quizAttemptMapper.updateById(attempt);
    }

    private void saveSession(Long quizActivityId, Long userId, Integer attemptNo, SessionCache sessionCache, Integer durationSeconds) {
        int ttl = durationSeconds == null || durationSeconds <= 0 ? 1800 : durationSeconds + 60;
        stringRedisTemplate.opsForValue().set(buildSessionKey(quizActivityId, userId, attemptNo), JSONUtil.toJsonStr(sessionCache), ttl, TimeUnit.SECONDS);
    }

    private SessionCache loadSession(Long quizActivityId, Long userId, Integer attemptNo) {
        String cacheValue = stringRedisTemplate.opsForValue().get(buildSessionKey(quizActivityId, userId, attemptNo));
        return cacheValue == null ? null : JSONUtil.toBean(cacheValue, SessionCache.class);
    }

    private void deleteSession(Long quizActivityId, Long userId, Integer attemptNo) {
        stringRedisTemplate.delete(buildSessionKey(quizActivityId, userId, attemptNo));
    }

    private String buildSessionKey(Long quizActivityId, Long userId, Integer attemptNo) {
        return SESSION_KEY_PREFIX + quizActivityId + ":" + userId + ":" + attemptNo;
    }

    @Data
    private static class SessionCache {
        private Long serverStartAt;
        private Long lastHeartbeatAt;
        private List<Long> questionIds;
        private Map<String, List<String>> optionOrderMap;
        private List<Long> markedQuestionIds;
    }
}
