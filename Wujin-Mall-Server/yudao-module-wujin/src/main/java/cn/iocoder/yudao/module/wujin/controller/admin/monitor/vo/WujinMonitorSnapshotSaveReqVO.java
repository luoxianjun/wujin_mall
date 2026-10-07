package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金监控指标快照新增/更新 Request VO")
public class WujinMonitorSnapshotSaveReqVO {

    private Long id;
    @NotBlank(message = "指标名称不能为空")
    private String metric;
    @NotNull(message = "指标值不能为空")
    private Integer metricValue;
    private Integer thresholdValue;
    private String auditAction;
    @NotNull(message = "是否告警不能为空")
    private Boolean alertFlag;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Integer getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(Integer metricValue) {
        this.metricValue = metricValue;
    }

    public Integer getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Integer thresholdValue) {
        this.thresholdValue = thresholdValue;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
