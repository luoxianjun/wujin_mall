package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 腾讯 IM 设置资料请求 VO")
@Data
public class TencentImProfileSetReqVO {

    @Schema(description = "需要设置资料的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "资料字段列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "资料字段列表不能为空")
    @Valid
    private List<TencentImProfileItemReqVO> items;

}
