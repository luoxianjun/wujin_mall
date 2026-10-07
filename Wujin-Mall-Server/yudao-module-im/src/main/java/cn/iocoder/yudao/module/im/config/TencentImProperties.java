package cn.iocoder.yudao.module.im.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯 IM 配置
 *
 * @author codex
 */
@Data
@Component
@ConfigurationProperties(prefix = "im.tencent")
public class TencentImProperties {

    /**
     * 是否启用腾讯 IM 能力
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * 腾讯 IM 应用的 SDKAppID
     */
    private Long sdkAppId;

    /**
     * 生成 UserSig 的密钥
     */
    private String secretKey;

    /**
     * 管理员帐号，作为接口调用的 Identifier
     */
    private String adminIdentifier;

    /**
     * UserSig 有效期（秒）
     */
    private Long expireSeconds = 604800L;

    /**
     * 请求域名
     */
    private String baseUrl = "https://console.tim.qq.com";

}
