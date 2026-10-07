package cn.iocoder.yudao.module.infra.service.file;

import java.util.List;

/**
 * 图片审核 Service 接口
 *
 * @author codex
 */
public interface InfraImageAuditService {

    /**
     * 审核图片
     *
     * @param content     图片内容
     * @param fileName    文件名
     * @param contentType 文件类型
     */
    void auditImage(byte[] content, String fileName, String contentType);

    /**
     * 审核图片URL列表（同步审核，审核失败时抛出异常）
     *
     * @param imageUrls 图片URL列表
     * @param biz       业务描述，用于日志
     */
    void auditImageUrls(List<String> imageUrls, String biz);

    /**
     * 静默审核图片URL列表（不抛异常，返回审核结果）
     *
     * @param imageUrls 图片URL列表
     * @param biz       业务描述，用于日志
     * @return 审核结果
     */
    ImageAuditResult auditImageUrlsSilently(List<String> imageUrls, String biz);

    /**
     * 审核图片（带openid，微信小程序场景）
     *
     * @param content     图片内容
     * @param fileName    文件名
     * @param contentType 文件类型
     * @param openid      用户的微信openid
     */
    void auditImage(byte[] content, String fileName, String contentType, String openid);

    /**
     * 审核图片URL列表（带openid，微信小程序场景，同步审核，审核失败时抛出异常）
     *
     * @param imageUrls 图片URL列表
     * @param openid    用户的微信openid
     * @param biz       业务描述，用于日志
     */
    void auditImageUrls(List<String> imageUrls, String openid, String biz);

    /**
     * 静默审核图片URL列表（带openid，微信小程序场景）
     *
     * @param imageUrls 图片URL列表
     * @param openid    用户的微信openid
     * @param biz       业务描述，用于日志
     * @return 审核结果
     */
    ImageAuditResult auditImageUrlsSilently(List<String> imageUrls, String openid, String biz);

    /**
     * 图片审核结果
     */
    class ImageAuditResult {
        private boolean passed;
        private String rejectReason;

        public ImageAuditResult(boolean passed, String rejectReason) {
            this.passed = passed;
            this.rejectReason = rejectReason;
        }

        public static ImageAuditResult pass() {
            return new ImageAuditResult(true, null);
        }

        public static ImageAuditResult reject(String reason) {
            return new ImageAuditResult(false, reason);
        }

        public boolean isPassed() {
            return passed;
        }

        public String getRejectReason() {
            return rejectReason;
        }
    }

}
