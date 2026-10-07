package cn.iocoder.yudao.module.forum.controller.admin.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdminSignRuleBaseVO {

    @Schema(description = "周期类型：1=周，2=月", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "周期类型不能为空")
    private Integer periodType;

    @Schema(description = "连续天数下限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "连续天数下限不能为空")
    private Integer minDays;

    @Schema(description = "连续天数上限（含）", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "连续天数上限不能为空")
    private Integer maxDays;

    @Schema(description = "积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "积分不能为空")
    private Integer points;
}

