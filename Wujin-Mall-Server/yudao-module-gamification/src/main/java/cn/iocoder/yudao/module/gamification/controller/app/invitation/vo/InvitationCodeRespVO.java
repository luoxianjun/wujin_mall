package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请码响应 VO
 *
 * @author gamification
 */
@Schema(description = "小程序 - 邀请码响应")
@Data
public class InvitationCodeRespVO {

    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC12345")
    private String code;

    @Schema(description = "累计邀请人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer totalInvitations;

    @Schema(description = "有效邀请人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Integer validInvitations;

    @Schema(description = "邀请链接", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/invite?code=ABC12345")
    private String inviteUrl;

    @Schema(description = "二维码图片URL", example = "https://example.com/qrcode/ABC12345.png")
    private String qrCodeUrl;
}
