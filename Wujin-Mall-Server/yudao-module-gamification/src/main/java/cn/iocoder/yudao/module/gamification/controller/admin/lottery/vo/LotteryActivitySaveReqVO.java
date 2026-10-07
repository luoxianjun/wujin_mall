package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin lottery activity save request")
@Data
public class LotteryActivitySaveReqVO {

    @Schema(description = "Primary key (update only)", example = "1")
    private Long id;

    @Schema(description = "Forum activity id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "activityId cannot be null")
    private Long activityId;

    @Schema(description = "Lottery type: 0=scheduled, 1=instant", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "type cannot be null")
    private Integer type;

    @Schema(description = "Scheduled draw time (for type=SCHEDULED)", example = "2026-04-01T12:00:00")
    private LocalDateTime drawTime;

    @Schema(description = "Max draws per day per user", example = "3")
    @Min(value = 0, message = "maxDrawsPerDay >= 0")
    private Integer maxDrawsPerDay;

    @Schema(description = "Max draws total per user", example = "10")
    @Min(value = 0, message = "maxDrawsTotal >= 0")
    private Integer maxDrawsTotal;

    @Schema(description = "Cost type: 0=free, 1=points", example = "0")
    private Integer costType;

    @Schema(description = "Points cost per draw (for costType=POINTS)", example = "10")
    @Min(value = 0, message = "costAmount >= 0")
    private Integer costAmount;

    @Schema(description = "Guarantee draws (0=disabled)", example = "5")
    @Min(value = 0, message = "guaranteeDraws >= 0")
    private Integer guaranteeDraws;

    @Schema(description = "Participation condition: 0=all, 1=enrolled, 2=invited", example = "0")
    private Integer participationCondition;

    @Schema(description = "Status: 0=enabled, 1=disabled", example = "0")
    private Integer status;

    @Schema(description = "关联奖品列表（含概率配置）")
    @Valid
    private List<ActivityPrizeItemVO> prizes;
}
