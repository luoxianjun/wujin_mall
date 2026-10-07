package cn.iocoder.yudao.module.wujin.monitor;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinMonitorServiceTest {

    private final WujinMonitorService service = new WujinMonitorService();

    @Test
    void summarizeCoreKpisFromSearchAndAuditRecords() {
        WujinMonitorSummary summary = service.summarize(Arrays.asList(
                        WujinSearchMonitorRecord.builder()
                                .keyword("轮胎")
                                .satisfactionScore(5)
                                .chainViewed(true)
                                .classificationCorrect(true)
                                .responseTimeMillis(1200)
                                .highRiskWarningTriggered(false)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("天然橡胶")
                                .satisfactionScore(4)
                                .chainViewed(true)
                                .classificationCorrect(false)
                                .responseTimeMillis(1800)
                                .highRiskWarningTriggered(true)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("硫化加工")
                                .chainViewed(false)
                                .classificationCorrect(true)
                                .responseTimeMillis(2400)
                                .highRiskWarningTriggered(false)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("医用乳胶手套")
                                .satisfactionScore(5)
                                .chainViewed(false)
                                .classificationCorrect(true)
                                .responseTimeMillis(600)
                                .highRiskWarningTriggered(true)
                                .build()),
                Arrays.asList(
                        new WujinRelationAuditMonitorRecord("AUD-001", WujinAuditAction.APPROVE),
                        new WujinRelationAuditMonitorRecord("AUD-002", WujinAuditAction.REJECT),
                        new WujinRelationAuditMonitorRecord("AUD-003", WujinAuditAction.APPROVE),
                        new WujinRelationAuditMonitorRecord("AUD-004", WujinAuditAction.BLOCK)));

        assertEquals(4.67, summary.getSearchSatisfaction(), 0.01);
        assertEquals(0.50, summary.getChainViewRate(), 0.001);
        assertEquals(0.75, summary.getClassificationAccuracy(), 0.001);
        assertEquals(1500.0, summary.getAverageResponseTimeMillis(), 0.001);
        assertEquals(2.0 / 3.0, summary.getRelationAuditPassRate(), 0.001);
        assertEquals(2, summary.getHighRiskWarningCount());
        assertTrue(summary.hasAlert(WujinMonitorMetric.CLASSIFICATION_ACCURACY));
        assertFalse(summary.hasAlert(WujinMonitorMetric.RESPONSE_TIME));
    }

    @Test
    void alertWhenKpisMissPrdTargets() {
        WujinMonitorSummary summary = service.summarize(Arrays.asList(
                        WujinSearchMonitorRecord.builder()
                                .keyword("轮胎")
                                .satisfactionScore(4)
                                .chainViewed(true)
                                .classificationCorrect(true)
                                .responseTimeMillis(2500)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("天然橡胶")
                                .satisfactionScore(4)
                                .chainViewed(false)
                                .classificationCorrect(false)
                                .responseTimeMillis(2000)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("医用乳胶手套")
                                .satisfactionScore(4)
                                .chainViewed(false)
                                .classificationCorrect(true)
                                .responseTimeMillis(2300)
                                .build(),
                        WujinSearchMonitorRecord.builder()
                                .keyword("钢钉")
                                .satisfactionScore(4)
                                .chainViewed(false)
                                .classificationCorrect(true)
                                .responseTimeMillis(2200)
                                .build()),
                Collections.singletonList(new WujinRelationAuditMonitorRecord("AUD-001", WujinAuditAction.APPROVE)));

        assertEquals("搜索满意度低于目标 4.5，当前 4.00",
                summary.getAlert(WujinMonitorMetric.SEARCH_SATISFACTION).getMessage());
        assertEquals("制造链查看率低于目标 30%，当前 25.00%",
                summary.getAlert(WujinMonitorMetric.CHAIN_VIEW_RATE).getMessage());
        assertEquals("分类准确率低于目标 90%，当前 75.00%",
                summary.getAlert(WujinMonitorMetric.CLASSIFICATION_ACCURACY).getMessage());
        assertEquals("平均响应时间超过目标 2000ms，当前 2250ms",
                summary.getAlert(WujinMonitorMetric.RESPONSE_TIME).getMessage());
    }

    @Test
    void summarizeEmptyRecordsAsZeroWithoutAlerts() {
        WujinMonitorSummary summary = service.summarize(Collections.emptyList(), Collections.emptyList());

        assertEquals(0.0, summary.getSearchSatisfaction(), 0.001);
        assertEquals(0.0, summary.getChainViewRate(), 0.001);
        assertEquals(0.0, summary.getClassificationAccuracy(), 0.001);
        assertEquals(0.0, summary.getAverageResponseTimeMillis(), 0.001);
        assertEquals(0.0, summary.getRelationAuditPassRate(), 0.001);
        assertEquals(0, summary.getHighRiskWarningCount());
        assertTrue(summary.getAlerts().isEmpty());
    }
}
