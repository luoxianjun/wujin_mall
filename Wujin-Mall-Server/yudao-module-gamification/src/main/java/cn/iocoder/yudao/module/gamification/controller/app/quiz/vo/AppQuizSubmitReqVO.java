package cn.iocoder.yudao.module.gamification.controller.app.quiz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "App quiz submit request")
@Data
public class AppQuizSubmitReqVO {

    @NotNull(message = "quizActivityId cannot be null")
    private Long quizActivityId;

    @NotNull(message = "attemptNo cannot be null")
    private Integer attemptNo;

    @Valid
    @NotEmpty(message = "answers cannot be empty")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "questionId cannot be null")
        private Long questionId;

        private List<String> selectedOptionKeys;

        private Boolean marked;
    }
}
