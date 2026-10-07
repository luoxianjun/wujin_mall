package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 腾讯 IM 获取黑名单请求
 *
 * @author forum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TencentImBlacklistGetReq {

    /**
     * 请求拉取黑名单的用户的 Identifier
     */
    @JsonProperty("From_Account")
    private String fromAccount;

    /**
     * 分页拉取的起始位置，第一页填0
     */
    @JsonProperty("StartIndex")
    private Integer startIndex;

    /**
     * 每页最多拉取的黑名单数，最大值为100
     */
    @JsonProperty("MaxLimited")
    private Integer maxLimited;

    /**
     * 上一次拉黑名单时后台返回给客户端的 Seq，初次拉取时为0
     */
    @JsonProperty("LastSequence")
    private Integer lastSequence;

}
