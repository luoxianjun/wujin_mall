package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin lottery activity response")
@Data
public class LotteryActivityRespVO {

    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "Forum activity id")
    private Long activityId;

    @Schema(description = "Lottery type: 0=scheduled, 1=instant")
    private Integer type;

    @Schema(description = "Scheduled draw time")
    private LocalDateTime drawTime;

    @Schema(description = "Max draws per day")
    private Integer maxDrawsPerDay;

    @Schema(description = "Max draws total")
    private Integer maxDrawsTotal;

    @Schema(description = "Cost type: 0=free, 1=points")
    private Integer costType;

    @Schema(description = "Points cost per draw")
    private Integer costAmount;

    @Schema(description = "Guarantee draws")
    private Integer guaranteeDraws;

    @Schema(description = "Participation condition")
    private Integer participationCondition;

    @Schema(description = "Status: 0=enabled, 1=disabled")
    private Integer status;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "关联奖品列表（含概率）")
    private List<ActivityPrizeRespItemVO> prizes;
}
