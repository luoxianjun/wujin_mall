package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

@Schema(description = "Admin lottery prize save request")
@Data
public class LotteryPrizeSaveReqVO {

    @Schema(description = "Primary key (update only)", example = "1")
    private Long id;

    @Schema(description = "Prize name", requiredMode = Schema.RequiredMode.REQUIRED, example = "100积分")
    @NotBlank(message = "name cannot be empty")
    private String name;

    @Schema(description = "Prize type: 0=points, 1=coupon, 2=physical, 3=virtual, 4=thank_you", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "type cannot be null")
    private Integer type;

    @Schema(description = "Points value (for type=POINTS)", example = "100")
    private Integer value;

    @Schema(description = "Prize image URL")
    private String imageUrl;

    @Schema(description = "Total stock", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "totalStock cannot be null")
    @Min(value = 0, message = "totalStock >= 0")
    private Integer totalStock;

    @Schema(description = "Sort order", example = "1")
    private Integer sortOrder;

    @Schema(description = "Whether winner must provide address (physical)", example = "false")
    private Boolean requireAddress;
}
