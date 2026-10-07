package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Admin question bank page request")
@Data
@EqualsAndHashCode(callSuper = true)
public class QuizQuestionBankPageReqVO extends PageParam {

    @Schema(description = "Question bank name", example = "History")
    private String name;

    @Schema(description = "Enabled flag", example = "true")
    private Boolean enabled;
}
