package cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Schema(description = "Admin invitation config update request")
@Data
public class InvitationConfigUpdateReqVO {

    @Schema(description = "Compatibility config id", example = "1")
    @NotNull(message = "id cannot be null")
    private Long id;

    @Schema(description = "Inviter reward points", example = "20")
    @NotNull(message = "inviterRewardPoints cannot be null")
    @Min(value = 0, message = "inviterRewardPoints cannot be negative")
    private Integer inviterRewardPoints;

    @Schema(description = "Invitee reward points", example = "10")
    @NotNull(message = "inviteeRewardPoints cannot be null")
    @Min(value = 0, message = "inviteeRewardPoints cannot be negative")
    private Integer inviteeRewardPoints;

    @Schema(description = "Reward mode", example = "both")
    @NotBlank(message = "rewardMode cannot be blank")
    @Pattern(regexp = "both|inviter_only", message = "rewardMode must be both or inviter_only")
    private String rewardMode;

    @Schema(description = "Maximum invitations per inviter", example = "100")
    @NotNull(message = "maxInvitations cannot be null")
    @Min(value = 1, message = "maxInvitations must be greater than 0")
    private Integer maxInvitations;

    @Schema(description = "Invitation feature enabled", example = "true")
    @NotNull(message = "enabled cannot be null")
    private Boolean enabled;

    @Schema(description = "Reward retry limit", example = "3")
    @NotNull(message = "rewardRetryLimit cannot be null")
    @Min(value = 0, message = "rewardRetryLimit cannot be negative")
    private Integer rewardRetryLimit;

    @Schema(description = "IP registration limit", example = "5")
    @NotNull(message = "ipLimit cannot be null")
    @Min(value = 1, message = "ipLimit must be greater than 0")
    private Integer ipLimit;

    @Schema(description = "Device registration limit", example = "3")
    @NotNull(message = "deviceLimit cannot be null")
    @Min(value = 1, message = "deviceLimit must be greater than 0")
    private Integer deviceLimit;
}
