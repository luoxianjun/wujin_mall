package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 腾讯 IM 添加黑名单响应
 *
 * @author forum
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TencentImBlacklistAddResp extends TencentImBaseResp {

    /**
     * 添加黑名单失败的用户列表
     */
    @JsonProperty("Fail_Account")
    private List<String> failAccount;

    /**
     * 添加黑名单的详细结果
     */
    @JsonProperty("ResultItem")
    private List<ResultItem> resultItem;

    @Data
    public static class ResultItem {
        /**
         * 被添加黑名单的用户 Identifier
         */
        @JsonProperty("To_Account")
        private String toAccount;

        /**
         * 结果码，0表示成功，非0表示失败
         */
        @JsonProperty("ResultCode")
        private Integer resultCode;

        /**
         * 结果描述
         */
        @JsonProperty("ResultInfo")
        private String resultInfo;
    }

}
