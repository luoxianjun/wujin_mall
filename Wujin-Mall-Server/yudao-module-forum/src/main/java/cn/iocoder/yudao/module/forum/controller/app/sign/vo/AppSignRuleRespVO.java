package cn.iocoder.yudao.module.forum.controller.app.sign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 签到规则响应 VO")
@Data
public class AppSignRuleRespVO {

    @Schema(description = "周期类型：1-周；2-月", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer periodType;

    @Schema(description = "连续签到天数下限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer minDays;

    @Schema(description = "连续签到天数上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer maxDays;

    @Schema(description = "命中该规则可获得的积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer points;
}
