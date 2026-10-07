package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.infra.framework.file.config.InfraImageAuditProperties;
import com.aliyun.imageaudit20191230.Client;
import com.aliyun.imageaudit20191230.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.IMAGE_AUDIT_FAILED;

/**
 * 阿里云图片审核实现
 * 使用阿里云 imageaudit ScanImage API 进行内容安全检测
 *
 * @author codex
 */
@Service
@Slf4j
public class InfraImageAuditServiceImpl implements InfraImageAuditService {

    /** 阿里云建议 - 拦截 */
    private static final String SUGGESTION_BLOCK = "block";

    @Resource
    private InfraImageAuditProperties properties;

    // ========== 无 openid 的方法（兼容旧调用，阿里云不需要 openid）==========

    @Override
    public void auditImage(byte[] content, String fileName, String contentType) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || content == null || content.length == 0) {
            return;
        }
        try {
            ImageAuditResult result = doAuditImageBytes(content, fileName);
            if (!result.isPassed()) {
                // 审核失败（内容违规），抛出异常阻止上传
                throw new IllegalArgumentException(result.getRejectReason());
            }
        } catch (IllegalArgumentException ex) {
            // 审核失败异常，重新抛出以阻止上传
            throw ex;
        } catch (Exception ex) {
            // 审核服务异常（网络问题、服务不可用等），记录日志但不阻止上传
            log.warn("[auditImage][阿里云图片审核服务异常，fileName={}]", fileName, ex);
        }
    }

    @Override
    public void auditImageUrls(List<String> imageUrls, String biz) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || CollUtil.isEmpty(imageUrls)) {
            return;
        }
        try {
            ImageAuditResult result = doAuditImageUrls(imageUrls, biz);
            if (!result.isPassed()) {
                // 审核失败（内容违规），抛出标准错误码，只提示内容违规，不显示具体原因
                throw ServiceExceptionUtil.exception(IMAGE_AUDIT_FAILED);
            }
        } catch (Exception ex) {
            // 如果是ServiceException（审核失败），重新抛出
            if (ex instanceof cn.iocoder.yudao.framework.common.exception.ServiceException) {
                throw ex;
            }
            // 审核服务异常（网络问题、服务不可用等），记录日志但不阻止操作
            log.warn("[auditImageUrls][阿里云图片审核服务异常，biz={}]", biz, ex);
        }
    }

    @Override
    public ImageAuditResult auditImageUrlsSilently(List<String> imageUrls, String biz) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || CollUtil.isEmpty(imageUrls)) {
            return ImageAuditResult.pass();
        }
        return doAuditImageUrls(imageUrls, biz);
    }

    // ========== 带 openid 的方法（保留签名兼容，阿里云不依赖 openid）==========

    @Override
    public void auditImage(byte[] content, String fileName, String contentType, String openid) {
        auditImage(content, fileName, contentType);
    }

    @Override
    public void auditImageUrls(List<String> imageUrls, String openid, String biz) {
        auditImageUrls(imageUrls, biz);
    }

    @Override
    public ImageAuditResult auditImageUrlsSilently(List<String> imageUrls, String openid, String biz) {
        return auditImageUrlsSilently(imageUrls, biz);
    }

    // ========== 核心方法 ==========

    private ImageAuditResult doAuditImageBytes(byte[] content, String fileName) {
        Client client = createClient();
        if (client == null) {
            return ImageAuditResult.pass();
        }
        List<String> scenes = properties.getScenes() != null ? properties.getScenes() : Collections.singletonList("porn");
        try {
            ScanImageAdvanceRequest request = new ScanImageAdvanceRequest();
            ScanImageAdvanceRequest.ScanImageAdvanceRequestTask task = new ScanImageAdvanceRequest.ScanImageAdvanceRequestTask();
            task.setDataId(fileName != null ? fileName : "bin-" + System.currentTimeMillis());
            task.setImageURLObject(new ByteArrayInputStream(content));
            request.setTask(Collections.singletonList(task));
            request.setScene(scenes);

            ScanImageResponse resp = client.scanImageAdvance(request, new RuntimeOptions());
            return parseScanImageResponse(resp, "auditImage");
        } catch (Exception ex) {
            log.error("[doAuditImageBytes][阿里云图片审核异常，fileName={}]", fileName, ex);
            return ImageAuditResult.pass();
        }
    }

    private ImageAuditResult doAuditImageUrls(List<String> imageUrls, String biz) {
        Client client = createClient();
        if (client == null) {
            return ImageAuditResult.pass();
        }
        List<String> scenes = properties.getScenes() != null ? properties.getScenes() : Collections.singletonList("porn");

        for (String imageUrl : imageUrls) {
            if (StrUtil.isBlank(imageUrl)) {
                continue;
            }
            try {
                ScanImageRequest request = new ScanImageRequest();
                ScanImageRequest.ScanImageRequestTask task = new ScanImageRequest.ScanImageRequestTask();
                task.setImageURL(imageUrl);
                task.setDataId("biz-" + biz + "-" + imageUrl.hashCode());
                request.setTask(Collections.singletonList(task));
                request.setScene(scenes);

                ScanImageResponse resp = client.scanImageWithOptions(request, new RuntimeOptions());
                ImageAuditResult result = parseScanImageResponse(resp, biz);
                if (!result.isPassed()) {
                    return result;
                }
            } catch (Exception ex) {
                log.error("[doAuditImageUrls][阿里云图片审核异常，biz={} url={}]", biz, imageUrl, ex);
                return ImageAuditResult.reject("图片审核服务异常，请稍后重试");
            }
        }
        return ImageAuditResult.pass();
    }

    private ImageAuditResult parseScanImageResponse(ScanImageResponse response, String biz) {
        if (response == null || response.getBody() == null) {
            return ImageAuditResult.pass();
        }
        ScanImageResponseBody body = response.getBody();
        ScanImageResponseBody.ScanImageResponseBodyData data = body.getData();
        if (data == null) {
            return ImageAuditResult.pass();
        }
        List<ScanImageResponseBody.ScanImageResponseBodyDataResults> results = data.getResults();
        if (CollUtil.isEmpty(results)) {
            return ImageAuditResult.pass();
        }
        for (ScanImageResponseBody.ScanImageResponseBodyDataResults r : results) {
            List<ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults> subResults = r.getSubResults();
            if (CollUtil.isEmpty(subResults)) {
                continue;
            }
            for (ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults sr : subResults) {
                String suggestion = sr.getSuggestion();
                if (SUGGESTION_BLOCK.equalsIgnoreCase(suggestion)) {
                    String label = sr.getLabel() != null ? sr.getLabel() : "违规内容";
                    log.warn("[parseScanImageResponse][阿里云图片审核未通过，biz={} label={}]", biz, label);
                    return ImageAuditResult.reject("图片未通过审核：" + label);
                }
            }
        }
        return ImageAuditResult.pass();
    }

    private Client createClient() {
        if (StrUtil.isBlank(properties.getAccessKeyId()) || StrUtil.isBlank(properties.getAccessKeySecret())) {
            log.warn("[createClient][阿里云图片审核未配置 accessKeyId/accessKeySecret]");
            return null;
        }
        try {
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret());
            config.endpoint = StrUtil.isNotBlank(properties.getEndpoint()) ? properties.getEndpoint() : "imageaudit.cn-shanghai.aliyuncs.com";
            return new Client(config);
        } catch (Exception ex) {
            log.error("[createClient][创建阿里云图片审核客户端失败]", ex);
            return null;
        }
    }
}
