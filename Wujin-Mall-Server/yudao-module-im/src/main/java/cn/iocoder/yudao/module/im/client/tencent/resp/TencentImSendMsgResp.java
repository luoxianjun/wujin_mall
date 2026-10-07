package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 腾讯 IM 单聊消息响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TencentImSendMsgResp extends TencentImBaseResp {

    @JsonProperty("MsgTime")
    private Long msgTime;

    @JsonProperty("MsgKey")
    private String msgKey;

    @JsonProperty("MsgId")
    private String msgId;
}
