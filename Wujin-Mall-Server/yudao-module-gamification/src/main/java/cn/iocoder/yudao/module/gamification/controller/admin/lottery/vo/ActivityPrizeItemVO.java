package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "活动关联奖品项（创建/更新活动时使用）")
@Data
public class ActivityPrizeItemVO {

    @Schema(description = "奖品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "prizeId cannot be null")
    private Long prizeId;

    @Schema(description = "中奖概率百分比（0-100）", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "probability cannot be null")
    private BigDecimal probability;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;
}
