package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金监控指标快照列表查询 Request VO")
public class WujinMonitorSnapshotListReqVO {

    private String metric;
    private String auditAction;
    private Boolean alertFlag;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    public Boolean getAlertFlag() {
        return alertFlag;
    }

    public void setAlertFlag(Boolean alertFlag) {
        this.alertFlag = alertFlag;
    }
}
