package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请海报 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户 APP - 邀请海报 Response VO")
@Data
public class InvitationPosterRespVO {

    @Schema(description = "海报图片URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/poster.png")
    private String posterUrl;

    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ABC12345")
    private String invitationCode;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "缓存过期时间（小时）", example = "24")
    private Integer cacheHours;
}
