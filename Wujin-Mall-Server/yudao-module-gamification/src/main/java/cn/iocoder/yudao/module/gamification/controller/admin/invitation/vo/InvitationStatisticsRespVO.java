package cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Admin invitation statistics response")
@Data
public class InvitationStatisticsRespVO {

    @Schema(description = "Total invitations", example = "100")
    private Long totalInvitations;

    @Schema(description = "Successful invitations", example = "60")
    private Long successfulInvitations;

    @Schema(description = "Pending invitations", example = "30")
    private Long pendingInvitations;

    @Schema(description = "Failed invitations", example = "10")
    private Long failedInvitations;

    @Schema(description = "Today's invitations", example = "6")
    private Long todayInvitations;

    @Schema(description = "Today's reward points", example = "120")
    private Long todayRewardPoints;

    @Schema(description = "Total reward points", example = "900")
    private Long totalRewardPoints;

    @Schema(description = "Distinct inviters with at least one relation", example = "25")
    private Long activeInviters;

    @Schema(description = "Successful invitations / total invitations * 100", example = "60.0")
    private Double successRate;
}
