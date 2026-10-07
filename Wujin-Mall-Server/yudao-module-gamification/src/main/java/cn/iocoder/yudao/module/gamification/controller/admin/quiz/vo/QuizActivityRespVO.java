package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin quiz activity response")
@Data
public class QuizActivityRespVO {

    @Schema(description = "Primary key", example = "1")
    private Long id;

    @Schema(description = "Forum activity id", example = "1001")
    private Long activityId;

    @Schema(description = "Forum activity title", example = "Campus quiz")
    private String activityTitle;

    @Schema(description = "Forum activity start time")
    private LocalDateTime activityStartTime;

    @Schema(description = "Forum activity end time")
    private LocalDateTime activityEndTime;

    @Schema(description = "Question bank id", example = "2001")
    private Long questionBankId;

    @Schema(description = "Question bank name", example = "History bank")
    private String questionBankName;

    @Schema(description = "Question bank total question count", example = "50")
    private Integer questionBankQuestionCount;

    @Schema(description = "Question bank create time")
    private LocalDateTime questionBankCreateTime;

    @Schema(description = "Question bank update time")
    private LocalDateTime questionBankUpdateTime;

    @Schema(description = "Question count", example = "20")
    private Integer questionCount;

    @Schema(description = "Max attempts", example = "3")
    private Integer maxAttempts;

    @Schema(description = "Duration in seconds", example = "1200")
    private Integer durationSeconds;

    @Schema(description = "Randomize question order", example = "true")
    private Boolean randomQuestionOrder;

    @Schema(description = "Randomize option order", example = "true")
    private Boolean randomOptionOrder;

    @Schema(description = "Leaderboard size", example = "20")
    private Integer leaderboardSize;

    @Schema(description = "Answer reveal mode", example = "AFTER_SUBMIT")
    private String answerRevealMode;

    @Schema(description = "Quiz status", example = "ENABLED")
    private String status;

    @Schema(description = "Reward rules")
    private List<RewardRuleRespVO> rewardRules;

    @Data
    public static class RewardRuleRespVO {
        private Long id;
        private Integer rankStart;
        private Integer rankEnd;
        private String rewardType;
        private Integer pointAmount;
        private String rewardName;
    }
}
