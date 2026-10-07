package cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Admin invitation detail response")
@Data
public class InvitationDetailRespVO {

    @Schema(description = "Relation id", example = "1")
    private Long id;

    @Schema(description = "Inviter user id", example = "100")
    private Long inviterId;

    @Schema(description = "Inviter nickname", example = "Alice")
    private String inviterNickname;

    @Schema(description = "Invitee user id", example = "200")
    private Long inviteeId;

    @Schema(description = "Invitee nickname", example = "Bob")
    private String inviteeNickname;

    @Schema(description = "Invitation code", example = "A1B2C3D4")
    private String invitationCode;

    @Schema(description = "Relation status", example = "2")
    private Integer status;

    @Schema(description = "Inviter reward points", example = "20")
    private Integer inviterRewardPoints;

    @Schema(description = "Invitee reward points", example = "10")
    private Integer inviteeRewardPoints;

    @Schema(description = "Registration time")
    private LocalDateTime registerTime;

    @Schema(description = "Verification time")
    private LocalDateTime verifiedTime;
}
