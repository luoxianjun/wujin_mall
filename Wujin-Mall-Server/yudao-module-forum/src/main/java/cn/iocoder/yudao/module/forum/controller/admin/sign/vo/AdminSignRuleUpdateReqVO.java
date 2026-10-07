package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 修改签到规则请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminSignRuleUpdateReqVO extends AdminSignRuleBaseVO {

    @Schema(description = "规则ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "规则ID不能为空")
    private Long id;
}

