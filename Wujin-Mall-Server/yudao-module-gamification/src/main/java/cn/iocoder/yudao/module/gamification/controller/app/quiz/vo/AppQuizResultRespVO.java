package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App quiz result response")
@Data
public class AppQuizResultRespVO {

    private String status;
    private Integer score;
    private Long elapsedMillis;
    private Integer rank;
    private Integer leaderboardSize;
    private LocalDateTime submittedAt;
    private List<QuestionResult> questionResults;

    @Data
    public static class QuestionResult {
        private Long questionId;
        private String content;
        private List<String> selectedOptionKeys;
        private Boolean correct;
        private Integer earnedScore;
        private List<String> correctAnswer;
        private String explanation;
    }
}
