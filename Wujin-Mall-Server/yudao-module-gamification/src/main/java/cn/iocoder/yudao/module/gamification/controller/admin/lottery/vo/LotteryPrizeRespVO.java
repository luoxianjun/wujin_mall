package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin lottery prize response")
@Data
public class LotteryPrizeRespVO {

    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "Prize name")
    private String name;

    @Schema(description = "Prize type")
    private Integer type;

    @Schema(description = "Points value")
    private Integer value;

    @Schema(description = "Prize image URL")
    private String imageUrl;

    @Schema(description = "Total stock")
    private Integer totalStock;

    @Schema(description = "Remaining stock")
    private Integer remainingStock;

    @Schema(description = "Sort order")
    private Integer sortOrder;

    @Schema(description = "Require address")
    private Boolean requireAddress;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
