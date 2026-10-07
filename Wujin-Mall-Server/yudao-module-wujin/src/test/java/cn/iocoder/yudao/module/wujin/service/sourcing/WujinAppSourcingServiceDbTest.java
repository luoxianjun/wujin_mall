package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadDispatchReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo.WujinMerchantSourcingLeadHandleReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.sourcing.WujinSourcingLeadMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadProgressRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityRespVO;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinAppSourcingServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class, WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinMerchantRelationItemAdminServiceImpl.class,
        WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinAttributeDictionaryAdminServiceImpl.class, WujinProductAttributeServiceImpl.class,
        WujinSourcingLeadAdminServiceImpl.class, WujinMerchantSourcingLeadServiceImpl.class})
class WujinAppSourcingServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinAppSourcingService sourcingService;
    @Resource
    private WujinSourcingLeadAdminService sourcingLeadAdminService;
    @Resource
    private WujinMerchantSourcingLeadService merchantSourcingLeadService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinSourcingLeadMapper sourcingLeadMapper;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private WujinProductAttributeService productAttributeService;

    @Test
    void getSupplierCandidatesUsesEnabledChainEntities() {
        chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶", WujinLane.MATERIAL.name(), "橡胶,汽车"));
        chainEntityService.createEntity(entityReq("M_RUBBER_BLACK", "炭黑补强剂", WujinLane.MATERIAL.name(), "橡胶"));
        chainEntityService.createEntity(entityReq("P_TIRE", "轮胎成品", WujinLane.PRODUCT.name(), "橡胶"));

        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword("橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("橡胶");

        List<WujinSupplierCandidateRespVO> candidates = sourcingService.getSupplierCandidates(reqVO);

        assertEquals(2, candidates.size());
        assertEquals("天然橡胶供应协作商", candidates.get(0).getSupplierName());
        assertEquals(WujinLane.MATERIAL.name(), candidates.get(0).getLane());
        assertTrue(candidates.get(0).getMatchScore() >= candidates.get(1).getMatchScore());
        assertTrue(candidates.get(0).getMainProducts().contains("天然橡胶"));
        assertTrue(candidates.get(0).getServiceNote().contains("橡胶"));
    }

    @Test
    void getSupplierCandidatesPrioritizesApprovedMerchantRelations() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq());
        Long approvedSubmissionId = submissionService.createSubmission(submissionReq(3001L, 5001L,
                "高耐磨乘用车轮胎", templateId, 30, 96));
        itemService.createItem(itemReq(approvedSubmissionId, materialEntityId));
        Long pendingSubmissionId = submissionService.createSubmission(submissionReq(3002L, 5002L,
                "待审核轮胎商品", templateId, 10, 98));
        itemService.createItem(itemReq(pendingSubmissionId, materialEntityId));

        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword("天然橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("轮胎橡胶");

        List<WujinSupplierCandidateRespVO> candidates = sourcingService.getSupplierCandidates(reqVO);

        assertFalse(candidates.isEmpty());
        assertEquals(3001L, candidates.get(0).getId());
        assertEquals(materialEntityId, candidates.get(0).getEntityId());
        assertEquals("天然橡胶", candidates.get(0).getEntityName());
        assertEquals("商家3001 · 高耐磨乘用车轮胎", candidates.get(0).getSupplierName());
        assertTrue(candidates.get(0).getServiceNote().contains("已通过关系申报"));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.getSupplierName().contains("3002")));
    }

    @Test
    void getSupplierCandidatesPrioritizesActiveSupplyCapabilitiesOverRelationSubmissions() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq());
        Long approvedSubmissionId = submissionService.createSubmission(submissionReq(3001L, 5001L,
                "关系申报轮胎商品", templateId, 30, 86));
        itemService.createItem(itemReq(approvedSubmissionId, materialEntityId));
        createSupplyCapability(3009L, 9001L, "现货天然橡胶原料", materialEntityId, 0);
        createSupplyCapability(3010L, 9002L, "停供天然橡胶原料", materialEntityId, 1);

        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword("天然橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("轮胎橡胶");

        List<WujinSupplierCandidateRespVO> candidates = sourcingService.getSupplierCandidates(reqVO);

        assertFalse(candidates.isEmpty());
        assertEquals(3009L, candidates.get(0).getId());
        assertEquals("商家3009 · 现货天然橡胶原料", candidates.get(0).getSupplierName());
        assertTrue(candidates.get(0).getServiceNote().contains("供应能力索引"));
        assertTrue(candidates.get(0).getServiceNote().contains("库存"));
        assertTrue(candidates.stream().noneMatch(candidate -> candidate.getSupplierName().contains("3010")));
    }

    @Test
    void merchantCanStopSupplyCapabilityAndRemoveItFromSupplierCandidates() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long capabilityId = createSupplyCapability(3009L, 9001L, "现货天然橡胶原料", materialEntityId, 0);

        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword("天然橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("轮胎橡胶");
        assertTrue(sourcingService.getSupplierCandidates(reqVO).stream()
                .anyMatch(candidate -> Long.valueOf(3009L).equals(candidate.getId())));

        updateSupplyCapability(capabilityId, 3009L, 9001L, "现货天然橡胶原料", materialEntityId, 1);

        assertTrue(sourcingService.getSupplierCandidates(reqVO).stream()
                .noneMatch(candidate -> Long.valueOf(3009L).equals(candidate.getId())));
    }

    @Test
    void submitLeadPersistsContactRequirementAndSelectedSupplier() {
        WujinSourcingLeadSubmitReqVO reqVO = new WujinSourcingLeadSubmitReqVO();
        reqVO.setUserId(201L);
        reqVO.setKeyword("天然橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setSourceKeyword("轮胎");
        reqVO.setIndustry("橡胶");
        reqVO.setSupplierId(11L);
        reqVO.setSupplierName("天然橡胶供应协作商");
        reqVO.setContactName("张三");
        reqVO.setContactPhone("13800000000");
        reqVO.setRequirement("需要天然橡胶样品，先询价 2 吨。");

        WujinSourcingLeadSubmitRespVO respVO = sourcingService.submitLead(reqVO);

        assertNotNull(respVO.getLeadId());
        assertEquals("SUBMITTED", respVO.getLeadStatus());
        assertFalse(respVO.getMessage().isEmpty());

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(respVO.getLeadId());
        assertNotNull(lead);
        assertEquals(201L, lead.getUserId());
        assertEquals("天然橡胶", lead.getKeyword());
        assertEquals(WujinLane.MATERIAL.name(), lead.getLane());
        assertEquals("轮胎", lead.getSourceKeyword());
        assertEquals("天然橡胶供应协作商", lead.getSupplierName());
        assertEquals("13800000000", lead.getContactPhone());
        assertEquals("SUBMITTED", lead.getLeadStatus());
    }

    @Test
    void platformDispatchesLeadAndMerchantHandlesIt() {
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(leadReq());

        WujinSourcingLeadListReqVO pendingReqVO = new WujinSourcingLeadListReqVO();
        pendingReqVO.setDispatchStatus("PENDING");
        List<WujinSourcingLeadDO> pendingLeads = sourcingLeadAdminService.getLeadList(pendingReqVO);
        assertEquals(1, pendingLeads.size());

        WujinSourcingLeadDispatchReqVO dispatchReqVO = new WujinSourcingLeadDispatchReqVO();
        dispatchReqVO.setLeadId(submitted.getLeadId());
        dispatchReqVO.setMerchantId(3001L);
        dispatchReqVO.setDispatchRemark("优先分发给华东材料商");
        sourcingLeadAdminService.dispatchLead(dispatchReqVO);

        WujinSourcingLeadListReqVO merchantListReqVO = new WujinSourcingLeadListReqVO();
        merchantListReqVO.setMerchantId(3001L);
        List<WujinSourcingLeadDO> merchantLeads = merchantSourcingLeadService.getLeadList(merchantListReqVO);
        assertEquals(1, merchantLeads.size());
        assertEquals("DISPATCHED", merchantLeads.get(0).getDispatchStatus());
        assertEquals("ASSIGNED", merchantLeads.get(0).getLeadStatus());

        WujinMerchantSourcingLeadHandleReqVO handleReqVO = new WujinMerchantSourcingLeadHandleReqVO();
        handleReqVO.setLeadId(submitted.getLeadId());
        handleReqVO.setMerchantId(3001L);
        handleReqVO.setHandleAction("CONTACTED");
        handleReqVO.setHandleRemark("已电话联系客户，准备报价");
        merchantSourcingLeadService.handleLead(handleReqVO);

        WujinSourcingLeadDO handledLead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals("CONTACTED", handledLead.getLeadStatus());
        assertEquals("DISPATCHED", handledLead.getDispatchStatus());
        assertEquals("已电话联系客户，准备报价", handledLead.getHandleRemark());
    }

    @Test
    void merchantLeadDeepFlowRequiresOrderedStatusTransitionsAndRecordsProcessTime() {
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(leadReq());
        WujinSourcingLeadDispatchReqVO dispatchReqVO = new WujinSourcingLeadDispatchReqVO();
        dispatchReqVO.setLeadId(submitted.getLeadId());
        dispatchReqVO.setMerchantId(3001L);
        dispatchReqVO.setDispatchRemark("分发给商家处理");
        sourcingLeadAdminService.dispatchLead(dispatchReqVO);

        WujinMerchantSourcingLeadHandleReqVO quoteBeforeContactReqVO = handleReq(submitted.getLeadId(), 3001L,
                "QUOTED", "未联系直接报价");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> merchantSourcingLeadService.handleLead(quoteBeforeContactReqVO));
        assertEquals("线索状态不允许从 ASSIGNED 流转到 QUOTED", exception.getMessage());

        merchantSourcingLeadService.handleLead(handleReq(submitted.getLeadId(), 3001L,
                "CONTACTED", "已联系客户"));
        merchantSourcingLeadService.handleLead(handleReq(submitted.getLeadId(), 3001L,
                "QUOTED", "已发送报价"));
        merchantSourcingLeadService.handleLead(handleReq(submitted.getLeadId(), 3001L,
                "CONVERTED", "客户确认下单"));

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals("CONVERTED", lead.getLeadStatus());
        assertEquals("客户确认下单", lead.getHandleRemark());
        assertNotNull(lead.getFirstContactTime());
        assertNotNull(lead.getQuotedTime());
        assertNotNull(lead.getConvertedTime());
        assertTrue(lead.getProcessDurationMinutes() >= 0);
    }

    @Test
    void platformReportsLeadConversionByMerchantAndLane() {
        WujinSourcingLeadSubmitRespVO converted = sourcingService.submitLead(leadReq());
        dispatchAndHandle(converted.getLeadId(), 3001L, "CONTACTED", "已联系客户");
        merchantSourcingLeadService.handleLead(handleReq(converted.getLeadId(), 3001L, "QUOTED", "已报价"));
        merchantSourcingLeadService.handleLead(handleReq(converted.getLeadId(), 3001L, "CONVERTED", "已转化"));

        WujinSourcingLeadSubmitReqVO lostReq = leadReq();
        lostReq.setUserId(203L);
        lostReq.setKeyword("天然橡胶");
        WujinSourcingLeadSubmitRespVO lost = sourcingService.submitLead(lostReq);
        dispatchAndHandle(lost.getLeadId(), 3001L, "CONTACTED", "已联系客户");
        merchantSourcingLeadService.handleLead(handleReq(lost.getLeadId(), 3001L, "LOST", "客户暂缓采购"));

        WujinSourcingLeadConversionReportReqVO reportReqVO = new WujinSourcingLeadConversionReportReqVO();
        reportReqVO.setStartDate(LocalDate.now().minusDays(1));
        reportReqVO.setEndDate(LocalDate.now().plusDays(1));
        reportReqVO.setMerchantId(3001L);
        reportReqVO.setLane(WujinLane.MATERIAL.name());

        WujinSourcingLeadConversionReportRespVO report = sourcingLeadAdminService.getConversionReport(reportReqVO);

        assertEquals(2, report.getTotalLeadCount());
        assertEquals(2, report.getContactedCount());
        assertEquals(1, report.getQuotedCount());
        assertEquals(1, report.getConvertedCount());
        assertEquals(1, report.getLostCount());
        assertEquals("50.00%", report.getConversionRate());
        assertEquals(1, report.getMerchantStats().size());
        assertEquals(3001L, report.getMerchantStats().get(0).getMerchantId());
        assertEquals(1, report.getLaneStats().size());
        assertEquals(WujinLane.MATERIAL.name(), report.getLaneStats().get(0).getLane());
    }

    @Test
    void platformAutoDispatchesLeadToBestApprovedMerchantCandidate() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq());
        Long approvedSubmissionId = submissionService.createSubmission(submissionReq(3001L, 5001L,
                "高耐磨乘用车轮胎", templateId, 30, 96));
        itemService.createItem(itemReq(approvedSubmissionId, materialEntityId));

        WujinSourcingLeadSubmitReqVO reqVO = leadReq();
        reqVO.setKeyword("天然橡胶");
        reqVO.setIndustry("轮胎橡胶");
        reqVO.setSupplierId(null);
        reqVO.setSupplierName(null);
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(reqVO);

        sourcingLeadAdminService.autoDispatchLead(submitted.getLeadId());

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals(3001L, lead.getMerchantId());
        assertEquals(3001L, lead.getSupplierId());
        assertEquals("商家3001 · 高耐磨乘用车轮胎", lead.getSupplierName());
        assertEquals("DISPATCHED", lead.getDispatchStatus());
        assertEquals("ASSIGNED", lead.getLeadStatus());
        assertTrue(lead.getDispatchRemark().contains("自动匹配"));
        assertTrue(lead.getDispatchRemark().contains("匹配分"));
    }

    @Test
    void platformAutoDispatchesLeadToActiveSupplyCapabilityBeforeRelationCandidate() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq());
        Long approvedSubmissionId = submissionService.createSubmission(submissionReq(3001L, 5001L,
                "关系申报轮胎商品", templateId, 30, 96));
        itemService.createItem(itemReq(approvedSubmissionId, materialEntityId));
        createSupplyCapability(3009L, 9001L, "现货天然橡胶原料", materialEntityId, 0);

        WujinSourcingLeadSubmitReqVO reqVO = leadReq();
        reqVO.setKeyword("天然橡胶");
        reqVO.setIndustry("轮胎橡胶");
        reqVO.setSupplierId(null);
        reqVO.setSupplierName(null);
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(reqVO);

        sourcingLeadAdminService.autoDispatchLead(submitted.getLeadId());

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals(3009L, lead.getMerchantId());
        assertEquals(3009L, lead.getSupplierId());
        assertEquals("商家3009 · 现货天然橡胶原料", lead.getSupplierName());
        assertEquals("DISPATCHED", lead.getDispatchStatus());
        assertEquals("ASSIGNED", lead.getLeadStatus());
        assertTrue(lead.getDispatchRemark().contains("供应能力索引"));
        assertTrue(lead.getDispatchRemark().contains("现货天然橡胶原料"));
    }

    @Test
    void platformAutoDispatchRejectsWhenOnlyFallbackEntityCandidateExists() {
        chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));

        WujinSourcingLeadSubmitReqVO reqVO = leadReq();
        reqVO.setKeyword("天然橡胶");
        reqVO.setIndustry("轮胎橡胶");
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(reqVO);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> sourcingLeadAdminService.autoDispatchLead(submitted.getLeadId()));

        assertEquals("暂无可自动分发的审核通过商家", exception.getMessage());
        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals("PENDING", lead.getDispatchStatus());
        assertEquals("SUBMITTED", lead.getLeadStatus());
        assertEquals(null, lead.getMerchantId());
    }

    @Test
    void submitterSeesLeadProgressStepsWhileOtherUsersAreRejected() {
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(leadReq());

        WujinSourcingLeadProgressRespVO submittedProgress = sourcingService.getLeadProgress(submitted.getLeadId(), 202L);
        assertEquals("SUBMITTED", submittedProgress.getLeadStatus());
        assertEquals("已提交", submittedProgress.getLeadStatusName());
        assertEquals(5, submittedProgress.getSteps().size());
        assertTrue(submittedProgress.getSteps().get(0).getDone());
        assertTrue(submittedProgress.getSteps().get(1).getActive());
        assertFalse(submittedProgress.getSteps().get(1).getDone());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> sourcingService.getLeadProgress(submitted.getLeadId(), 999L));
        assertEquals("无权查看该寻源线索", exception.getMessage());
        assertThrows(IllegalArgumentException.class, () -> sourcingService.getLeadProgress(submitted.getLeadId(), null));

        WujinSourcingLeadDispatchReqVO dispatchReqVO = new WujinSourcingLeadDispatchReqVO();
        dispatchReqVO.setLeadId(submitted.getLeadId());
        dispatchReqVO.setMerchantId(3001L);
        sourcingLeadAdminService.dispatchLead(dispatchReqVO);
        merchantSourcingLeadService.handleLead(handleReq(submitted.getLeadId(), "CONTACTED"));
        WujinMerchantSourcingLeadHandleReqVO quoteReqVO = handleReq(submitted.getLeadId(), "QUOTED");
        quoteReqVO.setQuotedAmount(1250000);
        merchantSourcingLeadService.handleLead(quoteReqVO);

        WujinSourcingLeadProgressRespVO quotedProgress = sourcingService.getLeadProgress(submitted.getLeadId(), 202L);
        assertEquals("已报价", quotedProgress.getLeadStatusName());
        assertEquals(1250000, quotedProgress.getQuotedAmount());
        assertTrue(quotedProgress.getSteps().get(1).getDone());
        assertTrue(quotedProgress.getSteps().get(2).getDone());
        assertTrue(quotedProgress.getSteps().get(3).getDescription().contains("12500.00"));
        assertTrue(quotedProgress.getSteps().get(4).getActive());
    }

    @Test
    void myLeadListOnlyReturnsCurrentUserLeadsNewestFirst() {
        Long first = sourcingService.submitLead(leadReq()).getLeadId();
        WujinSourcingLeadSubmitReqVO second = leadReq();
        second.setKeyword("天然橡胶");
        Long secondId = sourcingService.submitLead(second).getLeadId();
        WujinSourcingLeadSubmitReqVO other = leadReq();
        other.setUserId(303L);
        sourcingService.submitLead(other);

        List<WujinSourcingLeadProgressRespVO> leads = sourcingService.getMyLeadList(202L);
        assertEquals(2, leads.size());
        assertEquals(secondId, leads.get(0).getLeadId());
        assertEquals(first, leads.get(1).getLeadId());
        assertEquals(null, leads.get(0).getSteps());
        assertTrue(sourcingService.getMyLeadList(null).isEmpty());
    }

    @Test
    void merchantHandleLeadPersistsFollowUpFields() {
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(leadReq());
        WujinSourcingLeadDispatchReqVO dispatchReqVO = new WujinSourcingLeadDispatchReqVO();
        dispatchReqVO.setLeadId(submitted.getLeadId());
        dispatchReqVO.setMerchantId(3001L);
        sourcingLeadAdminService.dispatchLead(dispatchReqVO);
        assertNotNull(sourcingLeadMapper.selectById(submitted.getLeadId()).getDispatchTime());

        LocalDateTime nextFollowTime = LocalDateTime.of(2026, 10, 9, 10, 0);
        WujinMerchantSourcingLeadHandleReqVO handleReqVO = handleReq(submitted.getLeadId(), "CONTACTED");
        handleReqVO.setFollowStage("SAMPLE");
        handleReqVO.setNextFollowTime(nextFollowTime);
        handleReqVO.setWinProbability(60);
        merchantSourcingLeadService.handleLead(handleReqVO);

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals("SAMPLE", lead.getFollowStage());
        assertEquals(nextFollowTime, lead.getNextFollowTime());
        assertEquals(60, lead.getWinProbability());

        merchantSourcingLeadService.handleLead(handleReq(submitted.getLeadId(), "LOST"));
        WujinSourcingLeadDO lost = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals(0, lost.getWinProbability());
        assertEquals("SAMPLE", lost.getFollowStage());
    }

    @Test
    void supplierCapabilitySummarizesMerchantActiveCapabilitiesAndApprovedTags() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        createSupplyCapability(3009L, 9001L, "现货天然橡胶原料", materialEntityId, 0);
        createSupplyCapability(3009L, 9002L, "停供橡胶原料", materialEntityId, 1);
        productAttributeService.submitCustomTags(null, 3009L, 9001L, "现货天然橡胶原料",
                Collections.singletonList("SGS认证"), null);
        WujinProductCustomTagReviewReqVO reviewReqVO = new WujinProductCustomTagReviewReqVO();
        reviewReqVO.setId(productAttributeService.getCustomTagList(new WujinProductCustomTagListReqVO()).get(0).getId());
        reviewReqVO.setAction("APPROVE");
        productAttributeService.reviewCustomTag(reviewReqVO, 1L);

        WujinSupplierCapabilityReqVO reqVO = new WujinSupplierCapabilityReqVO();
        reqVO.setSupplierId(3009L);
        reqVO.setSupplierType("MERCHANT");
        reqVO.setKeyword("天然橡胶");
        WujinSupplierCapabilityRespVO capability = sourcingService.getSupplierCapability(reqVO);

        assertEquals("商家3009 · 现货天然橡胶原料", capability.getSupplierName());
        assertEquals(1, capability.getCapabilities().size());
        assertEquals("天然橡胶", capability.getCapabilities().get(0).getEntityName());
        assertEquals(96, capability.getMatchScore());
        assertEquals(1, capability.getLanes().size());
        assertEquals(WujinLane.MATERIAL.name(), capability.getLanes().get(0).getValue());
        assertTrue(capability.getLanes().get(0).getNote().contains("3 天"));
        assertTrue(capability.getMainCapabilities().contains("天然橡胶"));
        assertEquals(Collections.singletonList("SGS认证"), capability.getApprovedTags());
        assertTrue(capability.getServiceNote().contains("华东"));

        reqVO.setSupplierId(4040L);
        WujinSupplierCapabilityRespVO empty = sourcingService.getSupplierCapability(reqVO);
        assertTrue(empty.getCapabilities().isEmpty());
        assertEquals(0, empty.getMatchScore());
    }

    @Test
    void supplierCapabilityForPlatformCandidateUsesChainEntity() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));

        WujinSupplierCapabilityReqVO reqVO = new WujinSupplierCapabilityReqVO();
        reqVO.setSupplierId(materialEntityId);
        reqVO.setSupplierType("PLATFORM");
        reqVO.setKeyword("天然橡胶");
        reqVO.setIndustry("轮胎橡胶");
        WujinSupplierCapabilityRespVO capability = sourcingService.getSupplierCapability(reqVO);

        assertEquals("天然橡胶供应协作商", capability.getSupplierName());
        assertEquals("PLATFORM", capability.getSupplierType());
        assertEquals(WujinLane.MATERIAL.name(), capability.getLanes().get(0).getValue());
        assertTrue(capability.getCapabilities().isEmpty());
    }

    @Test
    void supplierCandidatesExposeSupplierTypeAndAutoDispatchIgnoresIdCollision() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        // 商家编号与实体编号相同，旧逻辑会把商家候选误判为平台兜底候选
        createSupplyCapability(materialEntityId, 9001L, "现货天然橡胶原料", materialEntityId, 0);

        WujinSupplierCandidateReqVO candidateReqVO = new WujinSupplierCandidateReqVO();
        candidateReqVO.setKeyword("天然橡胶");
        candidateReqVO.setLane(WujinLane.MATERIAL.name());
        candidateReqVO.setIndustry("轮胎橡胶");
        List<WujinSupplierCandidateRespVO> candidates = sourcingService.getSupplierCandidates(candidateReqVO);
        assertEquals("MERCHANT", candidates.get(0).getSupplierType());
        assertEquals("PLATFORM", candidates.get(candidates.size() - 1).getSupplierType());

        WujinSourcingLeadSubmitReqVO reqVO = leadReq();
        reqVO.setKeyword("天然橡胶");
        reqVO.setIndustry("轮胎橡胶");
        WujinSourcingLeadSubmitRespVO submitted = sourcingService.submitLead(reqVO);
        sourcingLeadAdminService.autoDispatchLead(submitted.getLeadId());

        WujinSourcingLeadDO lead = sourcingLeadMapper.selectById(submitted.getLeadId());
        assertEquals(materialEntityId, lead.getMerchantId());
        assertEquals("DISPATCHED", lead.getDispatchStatus());
    }

    @Test
    void supplierCandidatesKeepOneCardPerMerchantAndEntity() {
        Long materialEntityId = chainEntityService.createEntity(entityReq("M_RUBBER_NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq());
        Long approvedSubmissionId = submissionService.createSubmission(submissionReq(3009L, 9001L,
                "现货天然橡胶原料", templateId, 30, 90));
        itemService.createItem(itemReq(approvedSubmissionId, materialEntityId));
        createSupplyCapability(3009L, 9001L, "现货天然橡胶原料", materialEntityId, 0);

        WujinSupplierCandidateReqVO reqVO = new WujinSupplierCandidateReqVO();
        reqVO.setKeyword("天然橡胶");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("轮胎橡胶");
        List<WujinSupplierCandidateRespVO> candidates = sourcingService.getSupplierCandidates(reqVO);

        assertEquals(1, candidates.stream()
                .filter(candidate -> "MERCHANT".equals(candidate.getSupplierType())
                        && Long.valueOf(3009L).equals(candidate.getId()))
                .count());
        assertTrue(candidates.get(0).getServiceNote().contains("供应能力索引"));
    }

    private WujinMerchantSourcingLeadHandleReqVO handleReq(Long leadId, String action) {
        WujinMerchantSourcingLeadHandleReqVO reqVO = new WujinMerchantSourcingLeadHandleReqVO();
        reqVO.setLeadId(leadId);
        reqVO.setMerchantId(3001L);
        reqVO.setHandleAction(action);
        reqVO.setHandleRemark("跟进：" + action);
        return reqVO;
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane, String industries) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries(industries);
        reqVO.setJunctionFlag(false);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq() {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode("TPL_TIRE_RUBBER");
        reqVO.setName("轮胎橡胶模板");
        reqVO.setIndustryCode("轮胎橡胶");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(Long merchantId, Long productId, String productName,
                                                                   Long templateId, Integer auditStatus,
                                                                   Integer completenessScore) {
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(merchantId);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(auditStatus);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(completenessScore);
        reqVO.setRemark("商家提交");
        return reqVO;
    }

    private WujinMerchantRelationItemSaveReqVO itemReq(Long submissionId, Long entityId) {
        WujinMerchantRelationItemSaveReqVO reqVO = new WujinMerchantRelationItemSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(WujinRelationType.REQUIRES_MATERIAL.name());
        reqVO.setFromTemplate(true);
        reqVO.setRequiredFlag(true);
        reqVO.setRemark("商家可承接天然橡胶供应");
        return reqVO;
    }

    private WujinSourcingLeadSubmitReqVO leadReq() {
        WujinSourcingLeadSubmitReqVO reqVO = new WujinSourcingLeadSubmitReqVO();
        reqVO.setUserId(202L);
        reqVO.setKeyword("炭黑补强剂");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setSourceKeyword("橡胶密封圈");
        reqVO.setIndustry("橡胶");
        reqVO.setSupplierName("炭黑补强剂供应协作商");
        reqVO.setContactName("李四");
        reqVO.setContactPhone("13900000000");
        reqVO.setRequirement("需要炭黑补强剂报价，月采 5 吨。");
        return reqVO;
    }

    private WujinMerchantSourcingLeadHandleReqVO handleReq(Long leadId, Long merchantId, String action, String remark) {
        WujinMerchantSourcingLeadHandleReqVO reqVO = new WujinMerchantSourcingLeadHandleReqVO();
        reqVO.setLeadId(leadId);
        reqVO.setMerchantId(merchantId);
        reqVO.setHandleAction(action);
        reqVO.setHandleRemark(remark);
        return reqVO;
    }

    private void dispatchAndHandle(Long leadId, Long merchantId, String action, String remark) {
        WujinSourcingLeadDispatchReqVO dispatchReqVO = new WujinSourcingLeadDispatchReqVO();
        dispatchReqVO.setLeadId(leadId);
        dispatchReqVO.setMerchantId(merchantId);
        dispatchReqVO.setDispatchRemark("分发给商家");
        sourcingLeadAdminService.dispatchLead(dispatchReqVO);
        merchantSourcingLeadService.handleLead(handleReq(leadId, merchantId, action, remark));
    }

    private Long createSupplyCapability(Long merchantId, Long productId, String productName, Long entityId,
                                        Integer supplyStatus) {
        try {
            Object service = applicationContext.getBean(Class.forName(
                    "cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService"));
            Object reqVO = buildSupplyCapabilityReq(null, merchantId, productId, productName, entityId, supplyStatus);
            Class<?> reqClass = reqVO.getClass();
            Method method = service.getClass().getMethod("createCapability", reqClass);
            return (Long) method.invoke(service, reqVO);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new AssertionError("应提供五金商家供应能力索引服务", ex);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError("应提供五金商家供应能力索引 VO 与服务接口", ex);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("五金商家供应能力索引创建失败", ex);
        }
    }

    private void updateSupplyCapability(Long id, Long merchantId, Long productId, String productName, Long entityId,
                                        Integer supplyStatus) {
        try {
            Object service = applicationContext.getBean(Class.forName(
                    "cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService"));
            Object reqVO = buildSupplyCapabilityReq(id, merchantId, productId, productName, entityId, supplyStatus);
            Method method = service.getClass().getMethod("updateCapability", reqVO.getClass());
            method.invoke(service, reqVO);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new AssertionError("应提供五金商家供应能力索引服务", ex);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError("应提供五金商家供应能力更新服务", ex);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError("应提供五金商家供应能力索引 VO 与服务接口", ex);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("五金商家供应能力索引更新失败", ex);
        }
    }

    private Object buildSupplyCapabilityReq(Long id, Long merchantId, Long productId, String productName, Long entityId,
                                            Integer supplyStatus)
            throws ReflectiveOperationException, ClassNotFoundException {
        Class<?> reqClass = Class.forName(
                "cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO");
        Object reqVO = reqClass.getDeclaredConstructor().newInstance();
        if (id != null) {
            set(reqClass, reqVO, "setId", id);
        }
        set(reqClass, reqVO, "setMerchantId", merchantId);
        set(reqClass, reqVO, "setProductId", productId);
        set(reqClass, reqVO, "setProductName", productName);
        set(reqClass, reqVO, "setEntityId", entityId);
        set(reqClass, reqVO, "setLane", WujinLane.MATERIAL.name());
        set(reqClass, reqVO, "setIndustry", "轮胎橡胶");
        set(reqClass, reqVO, "setSupplyStatus", supplyStatus);
        set(reqClass, reqVO, "setStockCount", 120);
        set(reqClass, reqVO, "setMinOrderQuantity", 5);
        set(reqClass, reqVO, "setDeliveryDays", 3);
        set(reqClass, reqVO, "setServiceArea", "华东");
        set(reqClass, reqVO, "setRemark", "来自商城 SPU 供给索引");
        return reqVO;
    }

    private void set(Class<?> reqClass, Object reqVO, String methodName, Object value)
            throws ReflectiveOperationException {
        Method method = reqClass.getMethod(methodName, value.getClass());
        method.invoke(reqVO, value);
    }

}
