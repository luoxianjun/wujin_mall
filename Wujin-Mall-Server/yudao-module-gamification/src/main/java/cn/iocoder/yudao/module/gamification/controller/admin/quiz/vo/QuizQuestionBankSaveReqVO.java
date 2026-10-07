package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Admin question bank save request")
@Data
public class QuizQuestionBankSaveReqVO {

    @Schema(description = "Primary key", example = "1")
    private Long id;

    @Schema(description = "Question bank name", requiredMode = Schema.RequiredMode.REQUIRED, example = "History")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @Schema(description = "Question bank description", example = "Reusable question bank")
    private String description;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;

    @Schema(description = "Question list")
    @Valid
    @NotEmpty(message = "questions cannot be empty")
    private List<QuestionSaveReqVO> questions;

    @Data
    public static class QuestionSaveReqVO {

        @Schema(description = "Question id", example = "10")
        private Long id;

        @Schema(description = "Question type", requiredMode = Schema.RequiredMode.REQUIRED, example = "SINGLE_CHOICE")
        @NotBlank(message = "questionType cannot be blank")
        private String questionType;

        @Schema(description = "Question content", requiredMode = Schema.RequiredMode.REQUIRED, example = "What is 1+1?")
        @NotBlank(message = "content cannot be blank")
        private String content;

        @Schema(description = "Question image url", example = "https://example.com/question.png")
        private String imageUrl;

        @Schema(description = "Score", example = "5")
        @Min(value = 0, message = "score cannot be less than 0")
        private Integer score;

        @Schema(description = "Explanation", example = "Because 1 + 1 = 2")
        private String explanation;

        @Schema(description = "Sort", example = "1")
        private Integer sort;

        @Schema(description = "Option list")
        @Valid
        @NotEmpty(message = "options cannot be empty")
        private List<OptionSaveReqVO> options;
    }

    @Data
    public static class OptionSaveReqVO {

        @Schema(description = "Option id", example = "100")
        private Long id;

        @Schema(description = "Option key", requiredMode = Schema.RequiredMode.REQUIRED, example = "A")
        @NotBlank(message = "optionKey cannot be blank")
        private String optionKey;

        @Schema(description = "Option content", requiredMode = Schema.RequiredMode.REQUIRED, example = "Option A")
        @NotBlank(message = "content cannot be blank")
        private String content;

        @Schema(description = "Correct flag", example = "false")
        private Boolean isCorrect;

        @Schema(description = "Sort", example = "1")
        private Integer sort;
    }
}
