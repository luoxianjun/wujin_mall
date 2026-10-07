package cn.iocoder.yudao.module.forum.service.text;

/**
 * 论坛文本审核 Service
 *
 * @author codex
 */
public interface ForumTextAuditService {

    /**
     * 审核文本内容，未通过时抛出业务异常
     *
     * @param content 文本内容
     * @param biz     业务描述，用于日志
     */
    void audit(String content, String biz);

    /**
     * 审核文本内容，返回审核结果（不抛异常）
     * 用于需要静默处理敏感内容的场景
     *
     * @param content 文本内容
     * @param biz     业务描述，用于日志
     * @return 审核结果，包含是否通过和拒绝原因
     */
    TextAuditResult auditSilently(String content, String biz);

    /**
     * 审核文本内容（带openid），未通过时抛出业务异常
     * 用于微信小程序场景
     *
     * @param content 文本内容
     * @param openid  用户的微信openid
     * @param biz     业务描述，用于日志
     */
    void audit(String content, String openid, String biz);

    /**
     * 审核文本内容（带openid），返回审核结果（不抛异常）
     * 用于微信小程序场景
     *
     * @param content 文本内容
     * @param openid  用户的微信openid
     * @param biz     业务描述，用于日志
     * @return 审核结果，包含是否通过和拒绝原因
     */
    TextAuditResult auditSilently(String content, String openid, String biz);

    /**
     * 文本审核结果
     */
    class TextAuditResult {
        private boolean passed;
        private String rejectReason;

        public TextAuditResult(boolean passed, String rejectReason) {
            this.passed = passed;
            this.rejectReason = rejectReason;
        }

        public static TextAuditResult pass() {
            return new TextAuditResult(true, null);
        }

        public static TextAuditResult reject(String reason) {
            return new TextAuditResult(false, reason);
        }

        public boolean isPassed() {
            return passed;
        }

        public String getRejectReason() {
            return rejectReason;
        }
    }

}
