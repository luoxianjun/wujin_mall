package cn.iocoder.yudao.module.forum.service.text;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.forum.config.ForumTextAuditProperties;
import com.aliyun.imageaudit20191230.Client;
import com.aliyun.imageaudit20191230.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.TEXT_AUDIT_FAILED;

/**
 * 阿里云文本审核实现
 * 使用阿里云 imageaudit ScanText API 进行内容安全检测
 *
 * @author codex
 */
@Service
@Slf4j
public class ForumTextAuditServiceImpl implements ForumTextAuditService {

    /** 阿里云建议 - 拦截 */
    private static final String SUGGESTION_BLOCK = "block";

    @Resource
    private ForumTextAuditProperties textAuditProperties;

    // ========== 无 openid 的方法（兼容旧调用，阿里云不需要 openid）==========

    @Override
    public void audit(String content, String biz) {
        if (!Boolean.TRUE.equals(textAuditProperties.getEnabled())) {
            return;
        }
        if (StrUtil.isBlank(content)) {
            return;
        }
        try {
            TextAuditResult result = doScanText(content, biz);
            if (!result.isPassed()) {
                // 审核失败，只提示内容违规，不显示具体原因
                throw ServiceExceptionUtil.exception(TEXT_AUDIT_FAILED);
            }
        } catch (Exception ex) {
            if (ex instanceof ServiceException) {
                throw (ServiceException) ex;
            }
            log.error("[audit][阿里云文本审核异常，biz={}]", biz, ex);
        }
    }

    @Override
    public TextAuditResult auditSilently(String content, String biz) {
        if (!Boolean.TRUE.equals(textAuditProperties.getEnabled()) || StrUtil.isBlank(content)) {
            return TextAuditResult.pass();
        }
        try {
            return doScanText(content, biz);
        } catch (Exception ex) {
            log.error("[auditSilently][阿里云文本审核异常，biz={}]", biz, ex);
            return TextAuditResult.pass();
        }
    }

    // ========== 带 openid 的方法（保留签名兼容，阿里云不依赖 openid）==========

    @Override
    public void audit(String content, String openid, String biz) {
        audit(content, biz);
    }

    @Override
    public TextAuditResult auditSilently(String content, String openid, String biz) {
        return auditSilently(content, biz);
    }

    // ========== 核心方法 ==========

    private TextAuditResult doScanText(String content, String biz) throws Exception {
        Client client = createClient();
        if (client == null) {
            return TextAuditResult.pass();
        }

        ScanTextRequest request = new ScanTextRequest();
        ScanTextRequest.ScanTextRequestTasks task = new ScanTextRequest.ScanTextRequestTasks().setContent(content);
        request.setTasks(Collections.singletonList(task));

        List<String> labels = textAuditProperties.getLabels();
        if (labels == null || labels.isEmpty()) {
            labels = Arrays.asList("spam", "ad", "politics", "abuse", "porn");
        }
        List<ScanTextRequest.ScanTextRequestLabels> labelObjs = labels.stream()
                .map(l -> new ScanTextRequest.ScanTextRequestLabels().setLabel(l))
                .collect(Collectors.toList());
        request.setLabels(labelObjs);

        log.info("[doScanText][调用阿里云文本审核，biz={} contentLength={}]", biz, content.length());
        ScanTextResponse response = client.scanTextWithOptions(request, new RuntimeOptions());
        return parseScanTextResponse(response, biz);
    }

    private TextAuditResult parseScanTextResponse(ScanTextResponse response, String biz) {
        if (response == null || response.getBody() == null) {
            return TextAuditResult.pass();
        }
        ScanTextResponseBody body = response.getBody();
        ScanTextResponseBody.ScanTextResponseBodyData data = body.getData();
        if (data == null) {
            return TextAuditResult.pass();
        }
        List<ScanTextResponseBody.ScanTextResponseBodyDataElements> elements = data.getElements();
        if (elements == null || elements.isEmpty()) {
            return TextAuditResult.pass();
        }
        for (ScanTextResponseBody.ScanTextResponseBodyDataElements elem : elements) {
            List<ScanTextResponseBody.ScanTextResponseBodyDataElementsResults> results = elem.getResults();
            if (results == null || results.isEmpty()) {
                continue;
            }
            for (ScanTextResponseBody.ScanTextResponseBodyDataElementsResults r : results) {
                String suggestion = r.getSuggestion();
                if (SUGGESTION_BLOCK.equalsIgnoreCase(suggestion)) {
                    String label = r.getLabel() != null ? r.getLabel() : "违规内容";
                    log.warn("[parseScanTextResponse][阿里云文本审核未通过，biz={} label={}]", biz, label);
                    return TextAuditResult.reject("内容不合规：" + label);
                }
            }
        }
        return TextAuditResult.pass();
    }

    private Client createClient() {
        if (StrUtil.isBlank(textAuditProperties.getAccessKeyId()) || StrUtil.isBlank(textAuditProperties.getAccessKeySecret())) {
            log.warn("[createClient][阿里云文本审核未配置 accessKeyId/accessKeySecret]");
            return null;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(textAuditProperties.getAccessKeyId())
                    .setAccessKeySecret(textAuditProperties.getAccessKeySecret());
            config.endpoint = StrUtil.isNotBlank(textAuditProperties.getEndpoint()) ? textAuditProperties.getEndpoint() : "imageaudit.cn-shanghai.aliyuncs.com";
            return new Client(config);
        } catch (Exception ex) {
            log.error("[createClient][创建阿里云文本审核客户端失败]", ex);
            return null;
        }
    }
}
