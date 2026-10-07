package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息响应 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 消息响应 VO")
@Data
public class AppMessageRespVO {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long conversationId;

    @Schema(description = "发送人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long senderId;

    @Schema(description = "发送人UID", example = "U123456")
    private String senderUid;

    @Schema(description = "发送人昵称", example = "张三")
    private String senderNickname;

    @Schema(description = "发送人头像", example = "https://example.com/avatar.jpg")
    private String senderAvatar;

    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long receiverId;

    @Schema(description = "消息类型：1-文本，2-图片，3-语音，4-视频", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer messageType;

    @Schema(description = "消息类型名称", example = "文本")
    private String messageTypeName;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好")
    private String content;

    @Schema(description = "是否已读", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

}

