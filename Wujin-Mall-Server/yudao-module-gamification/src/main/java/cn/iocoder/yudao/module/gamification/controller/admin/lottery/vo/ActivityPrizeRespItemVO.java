package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "活动关联奖品响应项")
@Data
public class ActivityPrizeRespItemVO {

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品图片URL")
    private String imageUrl;

    @Schema(description = "奖品总库存")
    private Integer totalStock;

    @Schema(description = "奖品剩余库存")
    private Integer remainingStock;

    @Schema(description = "中奖概率百分比（活动级别）")
    private BigDecimal probability;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
