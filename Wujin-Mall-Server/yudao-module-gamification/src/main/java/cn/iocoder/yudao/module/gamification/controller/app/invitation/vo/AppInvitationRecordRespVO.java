package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "小程序 - 邀请记录响应")
@Data
public class AppInvitationRecordRespVO {

    @Schema(description = "邀请关系 ID", example = "1")
    private Long id;

    @Schema(description = "被邀请人用户 ID", example = "9")
    private Long inviteeId;

    @Schema(description = "被邀请人昵称", example = "小星")
    private String inviteeNickname;

    @Schema(description = "邀请时间")
    private LocalDateTime registerTime;

    @Schema(description = "是否已完成学校邮箱认证", example = "true")
    private Boolean verified;

    @Schema(description = "是否已完成邀请任务", example = "false")
    private Boolean completed;

    @Schema(description = "原始邀请状态", example = "1")
    private Integer status;
}
