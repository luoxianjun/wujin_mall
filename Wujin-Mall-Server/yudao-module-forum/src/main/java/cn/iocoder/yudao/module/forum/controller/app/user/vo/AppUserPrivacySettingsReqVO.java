package cn.iocoder.yudao.module.forum.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户隐私设置更新请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 隐私设置更新请求 VO")
@Data
public class AppUserPrivacySettingsReqVO {

    @Schema(description = "是否允许私聊", example = "true")
    private Boolean allowPrivateChat;

    @Schema(description = "是否接收系统消息", example = "true")
    private Boolean allowSystemMessage;

    @Schema(description = "是否隐藏认证学校信息", example = "false")
    private Boolean hideSchoolInfo;

}
