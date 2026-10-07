package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.quiz.QuizAttemptMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class QuizLeaderboardServiceImpl implements QuizLeaderboardService {

    private static final String LEADERBOARD_KEY_PREFIX = "quiz:leaderboard:";
    private static final String LEADERBOARD_DETAIL_KEY_PREFIX = "quiz:leaderboard:detail:";
    private static final String SUBMIT_SEQ_KEY_PREFIX = "quiz:submit-seq:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private QuizActivityMapper quizActivityMapper;

    @Resource
    private QuizAttemptMapper quizAttemptMapper;

    @Override
    public void recordSubmission(QuizAttemptDO attempt) {
        LeaderboardDetail candidate = buildDetail(attempt, nextSubmitSequence(attempt.getQuizActivityId()));
        String detailKey = buildLeaderboardDetailKey(attempt.getQuizActivityId());
        Object existingJson = stringRedisTemplate.opsForHash().get(detailKey, String.valueOf(attempt.getUserId()));
        if (existingJson != null) {
            LeaderboardDetail existing = JSONUtil.toBean(existingJson.toString(), LeaderboardDetail.class);
            if (!isBetter(candidate, existing)) {
                return;
            }
        }
        saveLeaderboardEntry(attempt.getQuizActivityId(), candidate);
    }

    @Override
    public AppQuizLeaderboardRespVO getLeaderboard(Long quizActivityId, Long userId) {
        refreshIfNeeded(quizActivityId);
        QuizActivityDO quizActivity = quizActivityMapper.selectById(quizActivityId);
        int size = quizActivity == null || quizActivity.getLeaderboardSize() == null ? 10 : quizActivity.getLeaderboardSize();

        AppQuizLeaderboardRespVO respVO = new AppQuizLeaderboardRespVO();
        respVO.setLeaderboardSize(size);
        if (size <= 0) {
            respVO.setRankings(Collections.emptyList());
            respVO.setMyRank(null);
            return respVO;
        }

        String leaderboardKey = buildLeaderboardKey(quizActivityId);
        Set<String> topMembers = stringRedisTemplate.opsForZSet().reverseRange(leaderboardKey, 0, size - 1);
        respVO.setRankings(buildRankItems(quizActivityId, topMembers));
        respVO.setMyRank(buildMyRank(quizActivityId, leaderboardKey, userId, respVO.getRankings()));
        return respVO;
    }

    @Override
    public Long nextSubmitSequence(Long quizActivityId) {
        Long next = stringRedisTemplate.opsForValue().increment(SUBMIT_SEQ_KEY_PREFIX + quizActivityId);
        return next == null ? 0L : next;
    }

    @Override
    public long calculateCompositeScore(Integer score, Long elapsedMillis, Long submitSequence) {
        long safeScore = score == null ? 0 : score;
        long safeElapsed = elapsedMillis == null ? 0 : elapsedMillis;
        long safeSubmitSequence = submitSequence == null ? 0 : submitSequence;
        return safeScore * 1_000_000_000_000L - safeElapsed * 1000L - safeSubmitSequence;
    }

    @Override
    public void refreshFromAttempts(Long quizActivityId) {
        String leaderboardKey = buildLeaderboardKey(quizActivityId);
        String detailKey = buildLeaderboardDetailKey(quizActivityId);
        stringRedisTemplate.delete(leaderboardKey);
        stringRedisTemplate.delete(detailKey);
        List<QuizAttemptDO> bestAttempts = quizAttemptMapper.selectSubmittedByQuizActivityId(quizActivityId).stream()
                .collect(Collectors.toMap(
                        QuizAttemptDO::getUserId,
                        attempt -> attempt,
                        (left, right) -> isBetter(buildDetail(right, 0L), buildDetail(left, 0L)) ? right : left))
                .values()
                .stream()
                .sorted(Comparator
                        .comparing((QuizAttemptDO attempt) -> attempt.getScore() == null ? 0 : attempt.getScore(), Comparator.reverseOrder())
                        .thenComparing(attempt -> attempt.getElapsedMillis() == null ? Long.MAX_VALUE : attempt.getElapsedMillis())
                        .thenComparing(attempt -> attempt.getSubmittedAt() == null ? java.time.LocalDateTime.MAX : attempt.getSubmittedAt())
                        .thenComparing(attempt -> attempt.getId() == null ? Long.MAX_VALUE : attempt.getId()))
                .collect(Collectors.toList());
        long submitSequence = 1L;
        for (QuizAttemptDO attempt : bestAttempts) {
            saveLeaderboardEntry(quizActivityId, buildDetail(attempt, submitSequence++));
        }
    }

    @Override
    public List<LeaderboardDetail> getRankedDetails(Long quizActivityId) {
        refreshIfNeeded(quizActivityId);
        Set<String> members = stringRedisTemplate.opsForZSet().reverseRange(buildLeaderboardKey(quizActivityId), 0, -1);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream()
                .map(member -> getDetail(quizActivityId, member))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String maskNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return "匿名";
        }
        String value = nickname.trim();
        if (value.length() <= 1) {
            return value + "*";
        }
        if (value.length() == 2) {
            return value.charAt(0) + "*";
        }
        return value.charAt(0) + "***" + value.charAt(value.length() - 1);
    }

    private void refreshIfNeeded(Long quizActivityId) {
        Long size = stringRedisTemplate.opsForZSet().zCard(buildLeaderboardKey(quizActivityId));
        if (size == null || size == 0) {
            refreshFromAttempts(quizActivityId);
        }
    }

    private AppQuizLeaderboardRespVO.RankItem buildMyRank(
            Long quizActivityId,
            String leaderboardKey,
            Long userId,
            List<AppQuizLeaderboardRespVO.RankItem> rankings) {
        if (userId == null) {
            return null;
        }
        for (AppQuizLeaderboardRespVO.RankItem rankItem : rankings) {
            if (Objects.equals(rankItem.getUserId(), userId)) {
                return rankItem;
            }
        }
        Long rank = stringRedisTemplate.opsForZSet().reverseRank(leaderboardKey, String.valueOf(userId));
        if (rank == null) {
            return null;
        }
        LeaderboardDetail detail = getDetail(quizActivityId, String.valueOf(userId));
        return detail == null ? null : convert(rank.intValue() + 1, detail);
    }

    private List<AppQuizLeaderboardRespVO.RankItem> buildRankItems(Long quizActivityId, Set<String> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppQuizLeaderboardRespVO.RankItem> items = new ArrayList<>();
        int index = 1;
        for (String member : members) {
            LeaderboardDetail detail = getDetail(quizActivityId, member);
            if (detail == null) {
                continue;
            }
            items.add(convert(index++, detail));
        }
        return items;
    }

    private AppQuizLeaderboardRespVO.RankItem convert(int rank, LeaderboardDetail detail) {
        AppQuizLeaderboardRespVO.RankItem item = new AppQuizLeaderboardRespVO.RankItem();
        item.setRank(rank);
        item.setUserId(detail.getUserId());
        item.setNickname(maskNickname(detail.getNickname()));
        item.setScore(detail.getScore());
        item.setElapsedMillis(detail.getElapsedMillis());
        item.setSubmittedAt(detail.getSubmittedAt());
        return item;
    }

    private LeaderboardDetail buildDetail(QuizAttemptDO attempt, Long submitSequence) {
        LeaderboardDetail detail = new LeaderboardDetail();
        detail.setAttemptId(attempt.getId());
        detail.setUserId(attempt.getUserId());
        detail.setScore(attempt.getScore());
        detail.setElapsedMillis(attempt.getElapsedMillis());
        detail.setSubmittedAt(attempt.getSubmittedAt());
        detail.setSubmitSequence(submitSequence);
        MemberUserRespDTO user = memberUserApi.getUser(attempt.getUserId());
        detail.setNickname(user == null ? "User" + attempt.getUserId() : user.getNickname());
        return detail;
    }

    private boolean isBetter(LeaderboardDetail left, LeaderboardDetail right) {
        int leftScore = left.getScore() == null ? 0 : left.getScore();
        int rightScore = right.getScore() == null ? 0 : right.getScore();
        if (leftScore != rightScore) {
            return leftScore > rightScore;
        }
        long leftElapsed = left.getElapsedMillis() == null ? Long.MAX_VALUE : left.getElapsedMillis();
        long rightElapsed = right.getElapsedMillis() == null ? Long.MAX_VALUE : right.getElapsedMillis();
        if (leftElapsed != rightElapsed) {
            return leftElapsed < rightElapsed;
        }
        long leftSeq = left.getSubmitSequence() == null ? Long.MAX_VALUE : left.getSubmitSequence();
        long rightSeq = right.getSubmitSequence() == null ? Long.MAX_VALUE : right.getSubmitSequence();
        return leftSeq < rightSeq;
    }

    private LeaderboardDetail getDetail(Long quizActivityId, String member) {
        Object detailJson = stringRedisTemplate.opsForHash().get(buildLeaderboardDetailKey(quizActivityId), member);
        return detailJson == null ? null : JSONUtil.toBean(detailJson.toString(), LeaderboardDetail.class);
    }

    private void saveLeaderboardEntry(Long quizActivityId, LeaderboardDetail detail) {
        String leaderboardKey = buildLeaderboardKey(quizActivityId);
        String detailKey = buildLeaderboardDetailKey(quizActivityId);
        long compositeScore = calculateCompositeScore(detail.getScore(), detail.getElapsedMillis(), detail.getSubmitSequence());
        stringRedisTemplate.opsForZSet().add(leaderboardKey, String.valueOf(detail.getUserId()), compositeScore);
        stringRedisTemplate.opsForHash().put(detailKey, String.valueOf(detail.getUserId()), JSONUtil.toJsonStr(detail));
        stringRedisTemplate.expire(leaderboardKey, 7, TimeUnit.DAYS);
        stringRedisTemplate.expire(detailKey, 7, TimeUnit.DAYS);
    }

    private String buildLeaderboardKey(Long quizActivityId) {
        return LEADERBOARD_KEY_PREFIX + quizActivityId;
    }

    private String buildLeaderboardDetailKey(Long quizActivityId) {
        return LEADERBOARD_DETAIL_KEY_PREFIX + quizActivityId;
    }
}
