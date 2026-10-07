package cn.iocoder.yudao.module.im.client.tencent.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 腾讯 IM 账号删除响应
 *
 * @author codex
 */
@Data
public class TencentImAccountDeleteResp extends TencentImBaseResp {

    @JsonProperty("ResultItem")
    private List<ResultItem> resultItems;

    @Data
    public static class ResultItem {

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultInfo")
        private String resultInfo;

        @JsonProperty("UserID")
        private String userId;

        public boolean isSuccess() {
            return resultCode == null || resultCode == 0;
        }
    }

}
