package cn.iocoder.yudao.module.gamification.service.quiz;

import cn.iocoder.yudao.module.gamification.controller.app.quiz.vo.AppQuizLeaderboardRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizLeaderboardService {

    void recordSubmission(QuizAttemptDO attempt);

    AppQuizLeaderboardRespVO getLeaderboard(Long quizActivityId, Long userId);

    Long nextSubmitSequence(Long quizActivityId);

    long calculateCompositeScore(Integer score, Long elapsedMillis, Long submitSequence);

    void refreshFromAttempts(Long quizActivityId);

    List<LeaderboardDetail> getRankedDetails(Long quizActivityId);

    String maskNickname(String nickname);

    @Data
    class LeaderboardDetail {
        private Long attemptId;
        private Long userId;
        private String nickname;
        private Integer score;
        private Long elapsedMillis;
        private Long submitSequence;
        private LocalDateTime submittedAt;
    }
}
