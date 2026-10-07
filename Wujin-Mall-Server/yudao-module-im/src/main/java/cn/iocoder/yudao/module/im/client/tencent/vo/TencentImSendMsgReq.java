package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 腾讯 IM 单聊发消息请求
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TencentImSendMsgReq {

    @JsonProperty("SyncOtherMachine")
    private Integer syncOtherMachine;

    @JsonProperty("From_Account")
    private String fromAccount;

    @JsonProperty("To_Account")
    private String toAccount;

    @JsonProperty("OnlineOnlyFlag")
    private Integer onlineOnlyFlag;

    @JsonProperty("MsgSeq")
    private Integer msgSeq;

    @JsonProperty("MsgRandom")
    private Integer msgRandom;

    @JsonProperty("ForbidCallbackControl")
    private List<String> forbidCallbackControl;

    @JsonProperty("SendMsgControl")
    private List<String> sendMsgControl;

    @JsonProperty("MsgBody")
    private List<MsgBodyItem> msgBody;

    @JsonProperty("CloudCustomData")
    private String cloudCustomData;

    @JsonProperty("SupportMessageExtension")
    private Integer supportMessageExtension;

    @JsonProperty("OfflinePushInfo")
    private Map<String, Object> offlinePushInfo;

    @JsonProperty("IsNeedReadReceipt")
    private Integer isNeedReadReceipt;

    @Data
    public static class MsgBodyItem {
        @JsonProperty("MsgType")
        private String msgType;

        @JsonProperty("MsgContent")
        private Map<String, Object> msgContent;
    }
}
