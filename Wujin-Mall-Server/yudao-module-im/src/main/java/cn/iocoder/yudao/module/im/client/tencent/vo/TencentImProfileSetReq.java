package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 腾讯 IM 设置资料请求
 *
 * @author codex
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TencentImProfileSetReq {

    @JsonProperty("From_Account")
    private String fromAccount;

    @JsonProperty("ProfileItem")
    private List<ProfileItem> profileItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileItem {

        @JsonProperty("Tag")
        private String tag;

        @JsonProperty("Value")
        private Object value;

    }

}
