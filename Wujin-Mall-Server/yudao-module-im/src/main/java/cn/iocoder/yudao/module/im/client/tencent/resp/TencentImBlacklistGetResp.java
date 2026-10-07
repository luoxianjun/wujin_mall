package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 腾讯 IM 获取黑名单响应
 *
 * @author forum
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TencentImBlacklistGetResp extends TencentImBaseResp {

    /**
     * 黑名单对象数组
     */
    @JsonProperty("BlackListItem")
    private List<BlackListItem> blackListItem;

    /**
     * 起始位置
     */
    @JsonProperty("StartIndex")
    private Integer startIndex;

    /**
     * 当前 seq 标识，用于下次拉取
     */
    @JsonProperty("CurrentSequence")
    private Integer currentSequence;

    @Data
    public static class BlackListItem {
        /**
         * 黑名单的用户 Identifier
         */
        @JsonProperty("To_Account")
        private String toAccount;

        /**
         * 添加黑名单的时间戳
         */
        @JsonProperty("AddBlackTimeStamp")
        private Long addBlackTimeStamp;
    }

}
