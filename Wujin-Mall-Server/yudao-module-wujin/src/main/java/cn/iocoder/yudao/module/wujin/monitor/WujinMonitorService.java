package cn.iocoder.yudao.module.wujin.monitor;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WujinMonitorService {

    private static final double TARGET_SEARCH_SATISFACTION = 4.5D;
    private static final double TARGET_CHAIN_VIEW_RATE = 0.30D;
    private static final double TARGET_CLASSIFICATION_ACCURACY = 0.90D;
    private static final double TARGET_AVERAGE_RESPONSE_MILLIS = 2000D;

    public WujinMonitorSummary summarize(List<WujinSearchMonitorRecord> searchRecords,
                                         List<WujinRelationAuditMonitorRecord> auditRecords) {
        List<WujinSearchMonitorRecord> searches = searchRecords == null ? Collections.emptyList() : searchRecords;
        List<WujinRelationAuditMonitorRecord> audits = auditRecords == null ? Collections.emptyList() : auditRecords;

        SearchKpi searchKpi = calculateSearchKpi(searches);
        AuditKpi auditKpi = calculateAuditKpi(audits);
        List<WujinMonitorAlert> alerts = buildAlerts(searchKpi);

        return new WujinMonitorSummary(searchKpi.searchSatisfaction,
                searchKpi.chainViewRate,
                searchKpi.classificationAccuracy,
                searchKpi.averageResponseTimeMillis,
                auditKpi.relationAuditPassRate,
                searchKpi.highRiskWarningCount,
                alerts);
    }

    private SearchKpi calculateSearchKpi(List<WujinSearchMonitorRecord> searches) {
        int satisfactionCount = 0;
        int satisfactionTotal = 0;
        int chainViewCount = 0;
        int classificationCount = 0;
        int classificationCorrectCount = 0;
        int responseCount = 0;
        long responseTotal = 0L;
        int highRiskWarningCount = 0;

        for (WujinSearchMonitorRecord search : searches) {
            if (search.getSatisfactionScore() != null) {
                satisfactionCount++;
                satisfactionTotal += search.getSatisfactionScore();
            }
            if (search.isChainViewed()) {
                chainViewCount++;
            }
            if (search.getClassificationCorrect() != null) {
                classificationCount++;
                if (search.getClassificationCorrect()) {
                    classificationCorrectCount++;
                }
            }
            if (search.getResponseTimeMillis() > 0) {
                responseCount++;
                responseTotal += search.getResponseTimeMillis();
            }
            if (search.isHighRiskWarningTriggered()) {
                highRiskWarningCount++;
            }
        }

        double searchSatisfaction = satisfactionCount == 0 ? 0D : (double) satisfactionTotal / satisfactionCount;
        double chainViewRate = searches.isEmpty() ? 0D : (double) chainViewCount / searches.size();
        double classificationAccuracy = classificationCount == 0 ? 0D : (double) classificationCorrectCount / classificationCount;
        double averageResponseTimeMillis = responseCount == 0 ? 0D : (double) responseTotal / responseCount;
        return new SearchKpi(searchSatisfaction,
                chainViewRate,
                classificationAccuracy,
                averageResponseTimeMillis,
                highRiskWarningCount,
                satisfactionCount,
                !searches.isEmpty(),
                classificationCount,
                responseCount);
    }

    private AuditKpi calculateAuditKpi(List<WujinRelationAuditMonitorRecord> audits) {
        int reviewedCount = 0;
        int approvedCount = 0;
        for (WujinRelationAuditMonitorRecord audit : audits) {
            if (audit.getAction() == WujinAuditAction.APPROVE || audit.getAction() == WujinAuditAction.REJECT) {
                reviewedCount++;
                if (audit.getAction() == WujinAuditAction.APPROVE) {
                    approvedCount++;
                }
            }
        }
        return new AuditKpi(reviewedCount == 0 ? 0D : (double) approvedCount / reviewedCount);
    }

    private List<WujinMonitorAlert> buildAlerts(SearchKpi searchKpi) {
        List<WujinMonitorAlert> alerts = new ArrayList<>();
        if (searchKpi.satisfactionCount > 0 && searchKpi.searchSatisfaction < TARGET_SEARCH_SATISFACTION) {
            alerts.add(new WujinMonitorAlert(WujinMonitorMetric.SEARCH_SATISFACTION,
                    "搜索满意度低于目标 4.5，当前 " + formatDecimal(searchKpi.searchSatisfaction)));
        }
        if (searchKpi.hasSearchRecords && searchKpi.chainViewRate < TARGET_CHAIN_VIEW_RATE) {
            alerts.add(new WujinMonitorAlert(WujinMonitorMetric.CHAIN_VIEW_RATE,
                    "制造链查看率低于目标 30%，当前 " + formatPercent(searchKpi.chainViewRate)));
        }
        if (searchKpi.classificationCount > 0 && searchKpi.classificationAccuracy < TARGET_CLASSIFICATION_ACCURACY) {
            alerts.add(new WujinMonitorAlert(WujinMonitorMetric.CLASSIFICATION_ACCURACY,
                    "分类准确率低于目标 90%，当前 " + formatPercent(searchKpi.classificationAccuracy)));
        }
        if (searchKpi.responseCount > 0 && searchKpi.averageResponseTimeMillis > TARGET_AVERAGE_RESPONSE_MILLIS) {
            alerts.add(new WujinMonitorAlert(WujinMonitorMetric.RESPONSE_TIME,
                    "平均响应时间超过目标 2000ms，当前 "
                            + String.format(Locale.ROOT, "%.0fms", searchKpi.averageResponseTimeMillis)));
        }
        return alerts;
    }

    private String formatDecimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.2f%%", value * 100);
    }

    private static class SearchKpi {
        private final double searchSatisfaction;
        private final double chainViewRate;
        private final double classificationAccuracy;
        private final double averageResponseTimeMillis;
        private final int highRiskWarningCount;
        private final int satisfactionCount;
        private final boolean hasSearchRecords;
        private final int classificationCount;
        private final int responseCount;

        private SearchKpi(double searchSatisfaction, double chainViewRate, double classificationAccuracy,
                          double averageResponseTimeMillis, int highRiskWarningCount, int satisfactionCount,
                          boolean hasSearchRecords, int classificationCount, int responseCount) {
            this.searchSatisfaction = searchSatisfaction;
            this.chainViewRate = chainViewRate;
            this.classificationAccuracy = classificationAccuracy;
            this.averageResponseTimeMillis = averageResponseTimeMillis;
            this.highRiskWarningCount = highRiskWarningCount;
            this.satisfactionCount = satisfactionCount;
            this.hasSearchRecords = hasSearchRecords;
            this.classificationCount = classificationCount;
            this.responseCount = responseCount;
        }
    }

    private static class AuditKpi {
        private final double relationAuditPassRate;

        private AuditKpi(double relationAuditPassRate) {
            this.relationAuditPassRate = relationAuditPassRate;
        }
    }
}
