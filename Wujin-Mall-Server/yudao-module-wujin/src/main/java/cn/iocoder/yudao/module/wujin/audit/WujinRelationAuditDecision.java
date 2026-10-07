package cn.iocoder.yudao.module.wujin.audit;

public class WujinRelationAuditDecision {

    private final WujinAuditAction action;
    private final WujinAuditReason reason;
    private final String message;

    public WujinRelationAuditDecision(WujinAuditAction action, WujinAuditReason reason, String message) {
        this.action = action;
        this.reason = reason;
        this.message = message;
    }

    public WujinAuditAction getAction() {
        return action;
    }

    public WujinAuditReason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }
}
