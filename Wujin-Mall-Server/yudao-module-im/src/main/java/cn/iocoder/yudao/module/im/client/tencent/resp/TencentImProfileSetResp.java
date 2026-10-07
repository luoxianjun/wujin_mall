package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 腾讯 IM 设置资料响应
 *
 * @author codex
 */
@Data
public class TencentImProfileSetResp extends TencentImBaseResp {

    @JsonProperty("ErrorDisplay")
    private String errorDisplay;

}
