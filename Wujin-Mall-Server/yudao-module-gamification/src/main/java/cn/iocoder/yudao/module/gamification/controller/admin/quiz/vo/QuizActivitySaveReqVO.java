package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "Admin quiz activity save request")
@Data
public class QuizActivitySaveReqVO {

    @Schema(description = "Primary key", example = "1")
    private Long id;

    @Schema(description = "Forum activity id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "activityId cannot be null")
    private Long activityId;

    @Schema(description = "Question bank id", requiredMode = Schema.RequiredMode.REQUIRED, example = "2001")
    @NotNull(message = "questionBankId cannot be null")
    private Long questionBankId;

    @Schema(description = "Question count", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "questionCount cannot be null")
    @Min(value = 1, message = "questionCount must be greater than 0")
    private Integer questionCount;

    @Schema(description = "Max attempts", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "maxAttempts cannot be null")
    @Min(value = 1, message = "maxAttempts must be greater than 0")
    private Integer maxAttempts;

    @Schema(description = "Duration in seconds", requiredMode = Schema.RequiredMode.REQUIRED, example = "1200")
    @NotNull(message = "durationSeconds cannot be null")
    @Min(value = 1, message = "durationSeconds must be greater than 0")
    private Integer durationSeconds;

    @Schema(description = "Randomize question order", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "randomQuestionOrder cannot be null")
    private Boolean randomQuestionOrder;

    @Schema(description = "Randomize option order", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "randomOptionOrder cannot be null")
    private Boolean randomOptionOrder;

    @Schema(description = "Leaderboard size", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "leaderboardSize cannot be null")
    @Min(value = 0, message = "leaderboardSize cannot be less than 0")
    private Integer leaderboardSize;

    @Schema(description = "Answer reveal mode", requiredMode = Schema.RequiredMode.REQUIRED, example = "AFTER_SUBMIT")
    @NotNull(message = "answerRevealMode cannot be null")
    private String answerRevealMode;

    @Schema(description = "Quiz status", requiredMode = Schema.RequiredMode.REQUIRED, example = "ENABLED")
    @NotNull(message = "status cannot be null")
    private String status;

    @Schema(description = "Reward rules")
    @Valid
    private List<RewardRuleSaveReqVO> rewardRules;

    @Schema(description = "Reward rule save request")
    @Data
    public static class RewardRuleSaveReqVO {

        @Schema(description = "Primary key", example = "1")
        private Long id;

        @Schema(description = "Rank start", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "rankStart cannot be null")
        @Min(value = 1, message = "rankStart must be greater than 0")
        private Integer rankStart;

        @Schema(description = "Rank end", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        @NotNull(message = "rankEnd cannot be null")
        @Min(value = 1, message = "rankEnd must be greater than 0")
        private Integer rankEnd;

        @Schema(description = "Reward type", requiredMode = Schema.RequiredMode.REQUIRED, example = "POINTS")
        @NotNull(message = "rewardType cannot be null")
        private String rewardType;

        @Schema(description = "Point amount", example = "100")
        @Min(value = 0, message = "pointAmount cannot be less than 0")
        private Integer pointAmount;

        @Schema(description = "Reward name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Top 3 reward")
        @NotNull(message = "rewardName cannot be null")
        private String rewardName;
    }
}
