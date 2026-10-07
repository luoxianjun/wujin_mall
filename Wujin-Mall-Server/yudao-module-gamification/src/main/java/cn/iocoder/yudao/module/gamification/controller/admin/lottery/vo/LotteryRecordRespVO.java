package cn.iocoder.yudao.module.gamification.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin lottery record response")
@Data
public class LotteryRecordRespVO {

    @Schema(description = "Primary key")
    private Long id;

    @Schema(description = "Lottery activity id")
    private Long lotteryActivityId;

    @Schema(description = "User id")
    private Long userId;

    @Schema(description = "Forum uid")
    private String uid;

    @Schema(description = "Prize name")
    private String prizeName;

    @Schema(description = "Prize type")
    private Integer prizeType;

    @Schema(description = "Whether won")
    private Boolean won;

    @Schema(description = "Draw time")
    private LocalDateTime drawTime;

    @Schema(description = "Whether delivered")
    private Boolean delivered;

    @Schema(description = "Delivery address")
    private String deliveryAddress;

    @Schema(description = "User nickname")
    private String userNickname;

    @Schema(description = "Activity name")
    private String activityName;
}
