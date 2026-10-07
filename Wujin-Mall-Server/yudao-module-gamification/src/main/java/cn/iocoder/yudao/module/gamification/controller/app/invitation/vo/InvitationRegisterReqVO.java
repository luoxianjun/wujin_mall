package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 邀请注册请求 VO
 *
 * @author gamification
 */
@Schema(description = "小程序 - 邀请注册请求")
@Data
public class InvitationRegisterReqVO {

    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC12345")
    @NotBlank(message = "邀请码不能为空")
    private String invitationCode;

    @Schema(description = "设备信息", example = "iPhone 13 Pro")
    private String deviceInfo;
}
