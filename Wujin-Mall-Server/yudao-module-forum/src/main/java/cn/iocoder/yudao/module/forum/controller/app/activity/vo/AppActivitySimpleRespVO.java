package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 热门活动简要信息 VO
 */
@Schema(description = "用户 APP - 热门活动简要信息 VO")
@Data
public class AppActivitySimpleRespVO {

    @Schema(description = "活动ID", example = "1")
    private Long id;

    @Schema(description = "活动标题", example = "校园公益活动")
    private String title;

    @Schema(description = "活动描述", example = "一起参加校园公益志愿服务")
    private String description;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.png")
    private String coverImage;

    @Schema(description = "报名要求", example = "需携带学生证，提前10分钟到场")
    private String requirements;

    @Schema(description = "报名是否需要积分", example = "false")
    private Boolean needPoint;

    @Schema(description = "报名所需积分", example = "10")
    private Integer pointAmount;

    @Schema(description = "活动地点", example = "体育馆")
    private String location;

    @Schema(description = "活动开始时间", example = "2024-01-01 10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间", example = "2024-01-01 12:00:00")
    private LocalDateTime endTime;

    @Schema(description = "Whether the activity has a quiz", example = "true")
    private Boolean hasQuiz;

    @Schema(description = "Quiz activity id", example = "1001")
    private Long quizActivityId;

    @Schema(description = "Whether the current user can join the quiz", example = "false")
    private Boolean canJoinQuiz;

    @Schema(description = "Quiz config status", example = "ENABLED")
    private String quizStatus;

    @Schema(description = "Quiz question count", example = "20")
    private Integer quizQuestionCount;

    @Schema(description = "Quiz max attempts", example = "3")
    private Integer quizMaxAttempts;

    @Schema(description = "Quiz attempts already used by the current user", example = "1")
    private Integer quizUsedAttempts;

    @Schema(description = "Quiz duration in seconds", example = "1200")
    private Integer quizDurationSeconds;

    @Schema(description = "Quiz leaderboard size", example = "10")
    private Integer quizLeaderboardSize;

    @Schema(description = "Quiz answer reveal mode", example = "AFTER_SUBMIT")
    private String quizAnswerRevealMode;

}
