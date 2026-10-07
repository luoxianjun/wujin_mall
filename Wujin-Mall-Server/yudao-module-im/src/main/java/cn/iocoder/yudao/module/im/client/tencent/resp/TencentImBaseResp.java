package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 腾讯 IM 通用响应
 *
 * @author codex
 */
@Data
public class TencentImBaseResp {

    @JsonProperty("ActionStatus")
    private String actionStatus;

    @JsonProperty("ErrorCode")
    private Integer errorCode;

    @JsonProperty("ErrorInfo")
    private String errorInfo;

    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(actionStatus) && (errorCode == null || errorCode == 0);
    }

}
