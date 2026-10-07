package cn.iocoder.yudao.module.gamification.controller.app.invitation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请人信息 Response VO（免登录，用于接受邀请页面展示）
 */
@Schema(description = "用户 APP - 邀请人信息 Response VO")
@Data
public class InviterInfoRespVO {

    @Schema(description = "邀请人昵称", example = "张三")
    private String nickname;

    @Schema(description = "邀请人头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}
