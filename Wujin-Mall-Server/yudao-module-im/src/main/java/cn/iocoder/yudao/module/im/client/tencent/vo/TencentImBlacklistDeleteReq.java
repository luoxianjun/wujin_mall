package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 腾讯 IM 删除黑名单请求
 *
 * @author forum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TencentImBlacklistDeleteReq {

    /**
     * 请求删除黑名单的用户的 Identifier
     */
    @JsonProperty("From_Account")
    private String fromAccount;

    /**
     * 待删除黑名单的用户 Identifier 列表，单次请求的 To_Account 数不得超过1000
     */
    @JsonProperty("To_Account")
    private List<String> toAccount;

}
