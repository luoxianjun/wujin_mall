package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditReason;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorTrendRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.monitor.WujinSearchBehaviorLogMapper;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorMetric;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminService;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinSearchBehaviorLogAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinRelationAuditRecordAdminServiceImpl.class,
        WujinMonitorDashboardServiceImpl.class})
class WujinMonitorDashboardServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;
    @Resource
    private WujinMonitorDashboardService dashboardService;
    @Resource
    private WujinSearchBehaviorLogMapper behaviorLogMapper;

    @Test
    void getTrendBucketsSearchLogsByDayWithEmptyDaysAndTopKeywords() throws Exception {
        LocalDate end = LocalDate.of(2026, 10, 7);
        insertLog("轮胎", WujinLane.PRODUCT.name(), true, 5, 1000L, end.atTime(9, 0));
        insertLog("轮胎", WujinLane.PRODUCT.name(), false, 3, 3000L, end.atTime(18, 30));
        insertLog("天然橡胶", WujinLane.MATERIAL.name(), true, 4, 500L, end.minusDays(2).atTime(12, 0));
        insertLog("过期关键词", WujinLane.PRODUCT.name(), true, 5, 100L, end.minusDays(9).atTime(12, 0));

        WujinMonitorTrendRespVO trend = dashboardService.getTrend(end, 7);

        assertEquals(end.minusDays(6), trend.getStartDate());
        assertEquals(end, trend.getEndDate());
        assertEquals(3, trend.getTotalSearchCount());
        assertEquals(7, trend.getPoints().size());
        WujinMonitorTrendRespVO.DailyPoint today = trend.getPoints().get(6);
        assertEquals(end, today.getDate());
        assertEquals(2, today.getSearchCount());
        assertEquals(0.5, today.getChainViewRate(), 0.0001);
        assertEquals(4.0, today.getSearchSatisfaction(), 0.0001);
        assertEquals(2000.0, today.getAverageResponseTimeMillis(), 0.0001);
        assertEquals(0, trend.getPoints().get(5).getSearchCount());
        assertEquals(1, trend.getPoints().get(4).getSearchCount());
        assertEquals("轮胎", trend.getTopKeywords().get(0).getKeyword());
        assertEquals(2, trend.getTopKeywords().get(0).getSearchCount());
        assertEquals(WujinLane.PRODUCT.name(), trend.getLaneStats().get(0).getLane());
        assertEquals(1, dashboardService.getTrend(end, 0).getPoints().size());
        // 应用的 ObjectMapper 注册了 JavaTimeModule，LocalDate 默认输出数组，前端需要 yyyy-MM-dd 字符串
        String json = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(trend);
        assertTrue(json.contains("\"date\":\"2026-10-07\""));
        assertTrue(json.contains("\"startDate\":\"2026-10-01\""));
        assertEquals(90, dashboardService.getTrend(end, 365).getPoints().size());
    }

    private void insertLog(String keyword, String lane, boolean chainViewed, Integer satisfactionScore,
                           Long responseTimeMillis, LocalDateTime createTime) {
        WujinSearchBehaviorLogDO log = new WujinSearchBehaviorLogDO();
        log.setUserId(101L);
        log.setKeyword(keyword);
        log.setIntent(lane);
        log.setResultLane(lane);
        log.setChainViewed(chainViewed);
        log.setClassificationCorrect(true);
        log.setHighRiskWarningTriggered(false);
        log.setSatisfactionScore(satisfactionScore);
        log.setResponseTimeMillis(responseTimeMillis);
        log.setCreateTime(createTime);
        behaviorLogMapper.insert(log);
    }

    @Test
    void getSummaryAggregatesSearchLogsAndAuditRecords() {
        behaviorLogService.createBehaviorLog(logReq("轮胎", WujinLane.PRODUCT.name(), true, true, true, 5, 1200L));
        behaviorLogService.createBehaviorLog(logReq("天然橡胶", WujinLane.MATERIAL.name(), false, true, false, 4, 1800L));
        behaviorLogService.createBehaviorLog(logReq("硫化加工", WujinLane.PROCESS.name(), false, false, true, null, 2400L));
        behaviorLogService.createBehaviorLog(logReq("医用乳胶手套", WujinLane.PRODUCT.name(), true, false, true, 5, 600L));

        Long submissionId = createSubmission();
        auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.APPROVE.name()));
        auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.REJECT.name()));
        auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.APPROVE.name()));
        auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.BLOCK.name()));

        WujinMonitorDashboardRespVO summary = dashboardService.getSummary();

        assertEquals(4, summary.getSearchSampleCount());
        assertEquals(4.67, summary.getSearchSatisfaction(), 0.01);
        assertEquals(0.50, summary.getChainViewRate(), 0.001);
        assertEquals(0.75, summary.getClassificationAccuracy(), 0.001);
        assertEquals(1500.0, summary.getAverageResponseTimeMillis(), 0.001);
        assertEquals(2.0 / 3.0, summary.getRelationAuditPassRate(), 0.001);
        assertEquals(2, summary.getHighRiskWarningCount());
        assertTrue(summary.getAlerts().stream()
                .anyMatch(alert -> WujinMonitorMetric.CLASSIFICATION_ACCURACY.name().equals(alert.getMetric())));
    }

    private Long createSubmission() {
        Long templateId = templateService.createTemplate(templateReq());
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(1001L);
        reqVO.setProductId(2001L);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(10);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(80);
        return submissionService.createSubmission(reqVO);
    }

    private WujinSearchBehaviorLogSaveReqVO logReq(String keyword, String resultLane, Boolean highRiskWarningTriggered,
                                                   Boolean chainViewed, Boolean classificationCorrect,
                                                   Integer satisfactionScore, Long responseTimeMillis) {
        WujinSearchBehaviorLogSaveReqVO reqVO = new WujinSearchBehaviorLogSaveReqVO();
        reqVO.setUserId(101L);
        reqVO.setKeyword(keyword);
        reqVO.setIntent(resultLane);
        reqVO.setResultLane(resultLane);
        reqVO.setIndustryCode("TIRE_RUBBER");
        reqVO.setChainViewed(chainViewed);
        reqVO.setClassificationCorrect(classificationCorrect);
        reqVO.setHighRiskWarningTriggered(highRiskWarningTriggered);
        reqVO.setSatisfactionScore(satisfactionScore);
        reqVO.setResponseTimeMillis(responseTimeMillis);
        return reqVO;
    }

    private WujinRelationAuditRecordSaveReqVO recordReq(Long submissionId, String action) {
        WujinRelationAuditRecordSaveReqVO reqVO = new WujinRelationAuditRecordSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setAuditorId(9001L);
        reqVO.setAction(action);
        reqVO.setReason(WujinAuditAction.APPROVE.name().equals(action)
                ? WujinAuditReason.REVIEW_APPROVED.name() : WujinAuditReason.REVIEW_REJECTED.name());
        reqVO.setComment("运营审核");
        reqVO.setEffectiveFlag(WujinAuditAction.APPROVE.name().equals(action));
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq() {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode("TPL_TIRE");
        reqVO.setName("轮胎橡胶模板");
        reqVO.setIndustryCode("TIRE_RUBBER");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }
}
