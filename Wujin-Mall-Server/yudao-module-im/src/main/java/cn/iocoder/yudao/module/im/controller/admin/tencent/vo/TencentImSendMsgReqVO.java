package cn.iocoder.yudao.module.im.controller.admin.tencent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 腾讯 IM 发送单聊消息请求 VO")
@Data
public class TencentImSendMsgReqVO {

    @Schema(description = "自定义发送方账号，不填则使用管理员账号发送", example = "10001")
    private String fromAccount;

    @Schema(description = "接收方账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20001")
    @NotBlank(message = "接收方账号不能为空")
    private String toAccount;

    @Schema(description = "消息随机数（32位无符号整数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotNull(message = "MsgRandom 不能为空")
    private Integer msgRandom;

    @Schema(description = "是否仅在线可见（0:存历史，1:仅在线）", example = "0")
    private Integer onlineOnlyFlag;

    @Schema(description = "是否同步其它端：1同步，2不同步 From，3不同步 To", example = "1")
    private Integer syncOtherMachine;

    @Schema(description = "消息序列号（32位无符号整数）", example = "1")
    private Integer msgSeq;

    @Schema(description = "消息发送控制选项", example = "[\"NoUnread\",\"NoLastMsg\"]")
    private List<String> sendMsgControl;

    @Schema(description = "消息回调禁止开关", example = "[\"ForbidBeforeSendMsgCallback\"]")
    private List<String> forbidCallbackControl;

    @Schema(description = "消息体，按腾讯 IM 文档格式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息体不能为空")
    @Valid
    private List<MsgBodyItem> msgBody;

    @Schema(description = "云端自定义数据", example = "{\"biz\":\"forum\"}")
    private String cloudCustomData;

    @Schema(description = "是否支持消息扩展，0/1", example = "0")
    private Integer supportMessageExtension;

    @Schema(description = "离线推送信息", example = "{\"Title\":\"通知\",\"Desc\":\"内容\"}")
    private Map<String, Object> offlinePushInfo;

    @Schema(description = "是否需要已读回执，0/1", example = "0")
    private Integer isNeedReadReceipt;

    @Data
    public static class MsgBodyItem {
        @Schema(description = "消息类型，如 TIMTextElem", requiredMode = Schema.RequiredMode.REQUIRED, example = "TIMTextElem")
        @NotBlank(message = "消息类型不能为空")
        private String msgType;

        @Schema(description = "消息内容，按消息类型传递", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"Text\":\"hello\"}")
        @NotNull(message = "消息内容不能为空")
        private Map<String, Object> msgContent;
    }
}
