package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 腾讯 IM 发送单聊消息响应 VO")
@Data
public class TencentImSendMsgRespVO {

    @Schema(description = "消息时间戳", example = "1700800000")
    private Long msgTime;

    @Schema(description = "消息唯一标识，用于撤回", example = "1234567890")
    private String msgKey;

    @Schema(description = "客户端唯一标识", example = "abcd-1234")
    private String msgId;
}
