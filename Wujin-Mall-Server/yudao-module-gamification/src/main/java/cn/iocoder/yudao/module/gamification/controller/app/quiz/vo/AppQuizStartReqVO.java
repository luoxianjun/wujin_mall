package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "App quiz start request")
@Data
public class AppQuizStartReqVO {

    @Schema(description = "Quiz activity id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "quizActivityId cannot be null")
    private Long quizActivityId;
}
