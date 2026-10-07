package cn.iocoder.yudao.module.forum.controller.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 设置用户管理员状态请求 VO")
@Data
public class AdminSetUserAdminReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "是否为管理员", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "管理员状态不能为空")
    private Boolean isAdmin;

}
