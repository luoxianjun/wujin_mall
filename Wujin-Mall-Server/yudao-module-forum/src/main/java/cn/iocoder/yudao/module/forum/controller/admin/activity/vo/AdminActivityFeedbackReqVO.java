package cn.iocoder.yudao.module.forum.controller.admin.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 活动报名点评请求 VO")
@Data
public class AdminActivityFeedbackReqVO {

    @Schema(description = "报名记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报名记录ID不能为空")
    private Long signUpId;

    @Schema(description = "点评/回顾/反馈", requiredMode = Schema.RequiredMode.REQUIRED, example = "组织很到位")
    @NotBlank(message = "点评内容不能为空")
    private String feedback;

}
