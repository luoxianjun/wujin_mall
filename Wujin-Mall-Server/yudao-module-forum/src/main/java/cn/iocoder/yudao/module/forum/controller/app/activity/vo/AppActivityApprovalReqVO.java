package cn.iocoder.yudao.module.forum.controller.app.activity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 活动报名审核请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 活动报名审核请求 VO")
@Data
public class AppActivityApprovalReqVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "报名ID不能为空")
    private Long signUpId;

    @Schema(description = "审核状态：1-通过，2-拒绝", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer approvalStatus;

    @Schema(description = "审核备注", example = "通过")
    private String approvalRemark;

}

