package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 腾讯 IM 账号删除请求
 *
 * @author codex
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TencentImAccountDeleteReq {

    @JsonProperty("DeleteItem")
    private List<DeleteItem> deleteItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteItem {

        @JsonProperty("UserID")
        private String userId;

    }

}
