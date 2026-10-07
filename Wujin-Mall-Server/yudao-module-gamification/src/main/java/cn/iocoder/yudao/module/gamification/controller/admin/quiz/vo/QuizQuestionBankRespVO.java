package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;

@Schema(description = "Admin question bank response")
@Data
public class QuizQuestionBankRespVO {

    @Schema(description = "Primary key", example = "1")
    private Long id;

    @Schema(description = "Question bank name", example = "History")
    private String name;

    @Schema(description = "Question bank description", example = "Reusable question bank")
    private String description;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;

    @Schema(description = "Question count", example = "20")
    private Integer questionCount;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;

    @Schema(description = "Question list")
    private List<QuestionRespVO> questions;

    @Data
    public static class QuestionRespVO {
        private Long id;
        private String questionType;
        private String content;
        private String imageUrl;
        private Integer score;
        private String explanation;
        private Integer sort;
        private List<OptionRespVO> options;
    }

    @Data
    public static class OptionRespVO {
        private Long id;
        private String optionKey;
        private String content;
        private Boolean isCorrect;
        private Integer sort;
    }
}
