package cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Admin quiz activity page request")
@Data
@EqualsAndHashCode(callSuper = true)
public class QuizActivityPageReqVO extends PageParam {

    @Schema(description = "Forum activity id", example = "1001")
    private Long activityId;

    @Schema(description = "Question bank id", example = "2001")
    private Long questionBankId;

    @Schema(description = "Quiz config status", example = "ENABLED")
    private String status;
}
