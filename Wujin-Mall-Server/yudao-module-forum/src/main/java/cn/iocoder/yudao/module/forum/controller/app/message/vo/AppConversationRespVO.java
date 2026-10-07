package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 会话响应 VO")
@Data
public class AppConversationRespVO {

    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "对方用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long otherUserId;

    @Schema(description = "对方用户UID", example = "U123456")
    private String otherUserUid;

    @Schema(description = "对方用户昵称", example = "张三")
    private String otherUserNickname;

    @Schema(description = "对方用户头像", example = "https://example.com/avatar.jpg")
    private String otherUserAvatar;

    @Schema(description = "最后一条消息内容", example = "你好")
    private String lastMessageContent;

    @Schema(description = "最后一条消息时间", example = "2024-01-01 12:00:00")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数", example = "5")
    private Integer unreadCount;

}

