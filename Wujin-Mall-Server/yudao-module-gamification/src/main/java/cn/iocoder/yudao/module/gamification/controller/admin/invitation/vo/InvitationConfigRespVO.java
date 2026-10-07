package cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Admin invitation config response")
@Data
public class InvitationConfigRespVO {

    @Schema(description = "Compatibility config id", example = "1")
    private Long id;

    @Schema(description = "Inviter reward points", example = "20")
    private Integer inviterRewardPoints;

    @Schema(description = "Invitee reward points", example = "10")
    private Integer inviteeRewardPoints;

    @Schema(description = "Reward mode", example = "both")
    private String rewardMode;

    @Schema(description = "Maximum invitations per inviter", example = "100")
    private Integer maxInvitations;

    @Schema(description = "Invitation feature enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Reward retry limit", example = "3")
    private Integer rewardRetryLimit;

    @Schema(description = "IP registration limit", example = "5")
    private Integer ipLimit;

    @Schema(description = "Device registration limit", example = "3")
    private Integer deviceLimit;
}
