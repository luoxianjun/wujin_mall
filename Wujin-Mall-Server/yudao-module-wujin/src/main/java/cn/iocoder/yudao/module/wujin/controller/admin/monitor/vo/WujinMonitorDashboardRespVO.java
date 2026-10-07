package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 五金运营看板汇总 Response VO")
public class WujinMonitorDashboardRespVO {

    private Double searchSatisfaction;
    private Double chainViewRate;
    private Double classificationAccuracy;
    private Double averageResponseTimeMillis;
    private Double relationAuditPassRate;
    private Integer highRiskWarningCount;
    private Integer searchSampleCount;
    private Integer auditSampleCount;
    private List<Alert> alerts = new ArrayList<>();

    public Double getSearchSatisfaction() {
        return searchSatisfaction;
    }

    public void setSearchSatisfaction(Double searchSatisfaction) {
        this.searchSatisfaction = searchSatisfaction;
    }

    public Double getChainViewRate() {
        return chainViewRate;
    }

    public void setChainViewRate(Double chainViewRate) {
        this.chainViewRate = chainViewRate;
    }

    public Double getClassificationAccuracy() {
        return classificationAccuracy;
    }

    public void setClassificationAccuracy(Double classificationAccuracy) {
        this.classificationAccuracy = classificationAccuracy;
    }

    public Double getAverageResponseTimeMillis() {
        return averageResponseTimeMillis;
    }

    public void setAverageResponseTimeMillis(Double averageResponseTimeMillis) {
        this.averageResponseTimeMillis = averageResponseTimeMillis;
    }

    public Double getRelationAuditPassRate() {
        return relationAuditPassRate;
    }

    public void setRelationAuditPassRate(Double relationAuditPassRate) {
        this.relationAuditPassRate = relationAuditPassRate;
    }

    public Integer getHighRiskWarningCount() {
        return highRiskWarningCount;
    }

    public void setHighRiskWarningCount(Integer highRiskWarningCount) {
        this.highRiskWarningCount = highRiskWarningCount;
    }

    public Integer getSearchSampleCount() {
        return searchSampleCount;
    }

    public void setSearchSampleCount(Integer searchSampleCount) {
        this.searchSampleCount = searchSampleCount;
    }

    public Integer getAuditSampleCount() {
        return auditSampleCount;
    }

    public void setAuditSampleCount(Integer auditSampleCount) {
        this.auditSampleCount = auditSampleCount;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts == null ? new ArrayList<>() : alerts;
    }

    public static class Alert {

        private String metric;
        private String message;

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
