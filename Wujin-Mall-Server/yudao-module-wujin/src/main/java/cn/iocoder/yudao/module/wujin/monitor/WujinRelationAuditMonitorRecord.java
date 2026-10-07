package cn.iocoder.yudao.module.wujin.monitor;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;

public class WujinRelationAuditMonitorRecord {

    private final String auditId;
    private final WujinAuditAction action;

    public WujinRelationAuditMonitorRecord(String auditId, WujinAuditAction action) {
        this.auditId = auditId;
        this.action = action;
    }

    public String getAuditId() {
        return auditId;
    }

    public WujinAuditAction getAction() {
        return action;
    }
}
