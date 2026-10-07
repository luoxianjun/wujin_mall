package cn.iocoder.yudao.module.wujin.monitor;

import java.util.Collections;
import java.util.List;

public class WujinMonitorSummary {

    private final double searchSatisfaction;
    private final double chainViewRate;
    private final double classificationAccuracy;
    private final double averageResponseTimeMillis;
    private final double relationAuditPassRate;
    private final int highRiskWarningCount;
    private final List<WujinMonitorAlert> alerts;

    public WujinMonitorSummary(double searchSatisfaction, double chainViewRate, double classificationAccuracy,
                               double averageResponseTimeMillis, double relationAuditPassRate,
                               int highRiskWarningCount, List<WujinMonitorAlert> alerts) {
        this.searchSatisfaction = searchSatisfaction;
        this.chainViewRate = chainViewRate;
        this.classificationAccuracy = classificationAccuracy;
        this.averageResponseTimeMillis = averageResponseTimeMillis;
        this.relationAuditPassRate = relationAuditPassRate;
        this.highRiskWarningCount = highRiskWarningCount;
        this.alerts = alerts == null ? Collections.emptyList() : Collections.unmodifiableList(alerts);
    }

    public double getSearchSatisfaction() {
        return searchSatisfaction;
    }

    public double getChainViewRate() {
        return chainViewRate;
    }

    public double getClassificationAccuracy() {
        return classificationAccuracy;
    }

    public double getAverageResponseTimeMillis() {
        return averageResponseTimeMillis;
    }

    public double getRelationAuditPassRate() {
        return relationAuditPassRate;
    }

    public int getHighRiskWarningCount() {
        return highRiskWarningCount;
    }

    public List<WujinMonitorAlert> getAlerts() {
        return alerts;
    }

    public boolean hasAlert(WujinMonitorMetric metric) {
        return getAlert(metric) != null;
    }

    public WujinMonitorAlert getAlert(WujinMonitorMetric metric) {
        for (WujinMonitorAlert alert : alerts) {
            if (alert.getMetric() == metric) {
                return alert;
            }
        }
        return null;
    }
}
