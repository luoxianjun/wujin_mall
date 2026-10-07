package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 腾讯 IM 资料字段设置项 VO")
@Data
public class TencentImProfileItemReqVO {

    @Schema(description = "资料字段 Tag，例如 Tag_Profile_IM_Nick", requiredMode = Schema.RequiredMode.REQUIRED, example = "Tag_Profile_IM_Nick")
    @NotBlank(message = "资料字段 Tag 不能为空")
    private String tag;

    @Schema(description = "资料字段值", requiredMode = Schema.RequiredMode.REQUIRED, example = "MyNickName")
    @NotNull(message = "资料字段值不能为空")
    private Object value;

}
