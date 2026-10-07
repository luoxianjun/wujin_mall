package cn.iocoder.yudao.module.im.client.tencent.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 腾讯 IM 账号导入请求
 *
 * @author codex
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TencentImAccountImportReq {

    /**
     * 账号，建议使用系统内唯一的用户标识
     */
    @JsonProperty("Identifier")
    private String identifier;

    /**
     * 昵称
     */
    @JsonProperty("Nick")
    private String nick;

    /**
     * 头像
     */
    @JsonProperty("FaceUrl")
    private String faceUrl;

}
