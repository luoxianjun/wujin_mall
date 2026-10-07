package cn.iocoder.yudao.module.forum.controller.app.message.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送私信请求 VO
 *
 * @author forum
 */
@Schema(description = "用户 APP - 发送私信请求 VO")
@Data
public class AppSendMessageReqVO {

    @Schema(description = "接收人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "接收人ID不能为空")
    private Long receiverId;

    @Schema(description = "消息类型：1-文本，2-图片，3-语音，4-视频", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好")
    @NotBlank(message = "消息内容不能为空")
    private String content;

}

