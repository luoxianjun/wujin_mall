package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Admin quiz attempt page request")
@Data
@EqualsAndHashCode(callSuper = true)
public class QuizAttemptPageReqVO extends PageParam {

    @Schema(description = "Quiz activity id", example = "1")
    private Long quizActivityId;

    @Schema(description = "Forum activity id", example = "104")
    private Long activityId;

    @Schema(description = "User id", example = "1")
    private Long userId;

    @Schema(description = "Status", example = "SUBMITTED")
    private String status;

    @Schema(description = "User mobile", example = "13800000000")
    private String userMobile;

    @Schema(description = "User nickname (fuzzy)", example = "test")
    private String userNickname;

    @Schema(description = "Submitted time range start")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] submittedAt;
}
