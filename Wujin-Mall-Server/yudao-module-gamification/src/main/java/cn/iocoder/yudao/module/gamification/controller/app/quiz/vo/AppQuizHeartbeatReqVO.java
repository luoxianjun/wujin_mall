package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "App quiz heartbeat request")
@Data
public class AppQuizHeartbeatReqVO {

    @NotNull(message = "quizActivityId cannot be null")
    private Long quizActivityId;

    @NotNull(message = "attemptNo cannot be null")
    private Integer attemptNo;

    @Schema(description = "Whether the client explicitly exits the quiz", example = "false")
    private Boolean exitQuiz;
}
