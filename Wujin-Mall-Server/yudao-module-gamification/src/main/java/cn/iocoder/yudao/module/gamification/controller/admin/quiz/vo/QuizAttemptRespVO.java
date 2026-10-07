package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin quiz attempt response")
@Data
public class QuizAttemptRespVO {

    @Schema(description = "Attempt id", example = "1")
    private Long id;

    @Schema(description = "Quiz activity id", example = "1")
    private Long quizActivityId;

    @Schema(description = "Forum activity id", example = "104")
    private Long activityId;

    @Schema(description = "Forum activity title")
    private String activityTitle;

    @Schema(description = "User id", example = "100")
    private Long userId;

    @Schema(description = "Forum uid", example = "U123456")
    private String uid;

    @Schema(description = "User nickname")
    private String userNickname;

    @Schema(description = "User mobile")
    private String userMobile;

    @Schema(description = "Activity rank", example = "1")
    private Integer activityRank;

    @Schema(description = "Registration remark")
    private String signUpRemark;

    @Schema(description = "Attempt number", example = "1")
    private Integer attemptNo;

    @Schema(description = "Status", example = "SUBMITTED")
    private String status;

    @Schema(description = "Score", example = "80")
    private Integer score;

    @Schema(description = "Elapsed time in ms", example = "60000")
    private Long elapsedMillis;

    @Schema(description = "Started at")
    private LocalDateTime startedAt;

    @Schema(description = "Submitted at")
    private LocalDateTime submittedAt;

    @Schema(description = "Invalidated reason")
    private String invalidatedReason;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
