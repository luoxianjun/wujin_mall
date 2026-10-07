package cn.iocoder.yudao.module.forum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 论坛文本审核配置（阿里云内容安全 - ScanText）
 *
 * @author codex
 */
@Data
@Component
@ConfigurationProperties("forum.text-audit")
public class ForumTextAuditProperties {

    /**
     * 是否开启文本审核
     */
    private Boolean enabled = Boolean.FALSE;

    /**
     * 阿里云账号 AccessKey ID
     */
    private String accessKeyId;

    /**
     * 阿里云账号 AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 审核接口域名（与图片审核同产品，默认同 endpoint）
     */
    private String endpoint = "imageaudit.cn-shanghai.aliyuncs.com";

    /**
     * 审核标签列表，例如 spam、ad、politics、abuse、porn、terrorism、flood、contraband
     */
    private List<String> labels = new ArrayList<>(Arrays.asList("spam", "ad", "politics", "abuse", "porn"));

}
