package cn.iocoder.yudao.module.wujin.audit;

public class WujinAuditReviewCommand {

    private final String auditId;
    private final WujinAuditAction action;
    private final String comment;

    private WujinAuditReviewCommand(String auditId, WujinAuditAction action, String comment) {
        this.auditId = auditId;
        this.action = action;
        this.comment = comment;
    }

    public static WujinAuditReviewCommand approve(String auditId, String comment) {
        return new WujinAuditReviewCommand(auditId, WujinAuditAction.APPROVE, comment);
    }

    public static WujinAuditReviewCommand reject(String auditId, String comment) {
        return new WujinAuditReviewCommand(auditId, WujinAuditAction.REJECT, comment);
    }

    public String getAuditId() {
        return auditId;
    }

    public WujinAuditAction getAction() {
        return action;
    }

    public String getComment() {
        return comment;
    }
}
