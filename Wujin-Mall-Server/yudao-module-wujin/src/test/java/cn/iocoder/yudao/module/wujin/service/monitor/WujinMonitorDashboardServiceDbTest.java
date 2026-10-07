package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditReason;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.monitor.WujinMonitorMetric;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminService;
import cn.iocoder.yudao.module.wujin.service.audit.WujinRelationAuditRecordAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

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
