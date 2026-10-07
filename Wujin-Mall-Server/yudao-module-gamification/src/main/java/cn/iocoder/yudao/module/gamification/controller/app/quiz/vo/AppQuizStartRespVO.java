package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App quiz start response")
@Data
public class AppQuizStartRespVO {

    private Long quizActivityId;
    private Integer attemptNo;
    private Integer durationSeconds;
    private Integer maxAttempts;
    private String answerRevealMode;
    private List<Question> questions;

    @Data
    public static class Question {
        private Long id;
        private String questionType;
        private String content;
        private String imageUrl;
        private Integer score;
        private List<Option> options;
    }

    @Data
    public static class Option {
        private String optionKey;
        private String content;
    }
}
