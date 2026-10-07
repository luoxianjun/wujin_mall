package cn.iocoder.yudao.module.infra.framework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Infra 图片审核配置
 *
 * @author codex
 */
@Data
@Component
@ConfigurationProperties("infra.image-audit")
public class InfraImageAuditProperties {

    /**
     * 是否开启图片审核
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
     * 审核接口域名
     */
    private String endpoint = "imageaudit.cn-shanghai.aliyuncs.com";

    /**
     * 审核场景，例如：porn、terrorism、logo、ad
     */
    private List<String> scenes = new ArrayList<>(Arrays.asList("porn"));

}
