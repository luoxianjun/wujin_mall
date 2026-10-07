package cn.iocoder.yudao.module.wujin.audit;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.chain.WujinRiskWarning;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationDraft;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationSubmission;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinRelationAuditServiceTest {

    private final WujinRelationAuditService service = new WujinRelationAuditService();

    @Test
    void precheckBlocksCategoryConflict() {
        WujinRelationAuditRequest request = baseRequest()
                .declaredProductLane(WujinLane.MATERIAL)
                .build();

        WujinRelationAuditDecision decision = service.precheck(request);

        assertEquals(WujinAuditAction.BLOCK, decision.getAction());
        assertEquals(WujinAuditReason.CATEGORY_CONFLICT, decision.getReason());
        assertEquals("商品上架记录必须归属成品泳道", decision.getMessage());
    }

    @Test
    void precheckBlocksHighRiskIndustryConflict() {
        WujinRelationAuditRequest request = baseRequest()
                .riskWarning(new WujinRiskWarning(true, "天然橡胶标准差异", "轮胎级 STR20 不可用于医用乳胶手套"))
                .build();

        WujinRelationAuditDecision decision = service.precheck(request);

        assertEquals(WujinAuditAction.BLOCK, decision.getAction());
        assertEquals(WujinAuditReason.INDUSTRY_STANDARD_CONFLICT, decision.getReason());
        assertTrue(decision.getMessage().contains("轮胎级 STR20"));
    }

    @Test
    void precheckSuggestsExistingEntityWhenSimilarityIsHigh() {
        WujinRelationAuditRequest request = baseRequest()
                .similarEntities(Collections.singletonList(new WujinSimilarEntity("E_CARBON_N990", "炭黑N990", 95)))
                .build();

        WujinRelationAuditDecision decision = service.precheck(request);

        assertEquals(WujinAuditAction.SUGGEST_MERGE, decision.getAction());
        assertEquals(WujinAuditReason.SIMILAR_ENTITY_FOUND, decision.getReason());
        assertEquals("建议修正为现有实体：炭黑N990(E_CARBON_N990)，相似度 95%", decision.getMessage());
    }

    @Test
    void reviewApproveCreatesEffectiveRelation() {
        WujinRelationAuditDecision decision = service.review(WujinAuditReviewCommand.approve("AUD-001", "关系与模板一致"));

        assertEquals(WujinAuditAction.APPROVE, decision.getAction());
        assertEquals(WujinAuditReason.REVIEW_APPROVED, decision.getReason());
        assertEquals("审核通过，关系生效入网：关系与模板一致", decision.getMessage());
    }

    @Test
    void reviewRejectRequiresSupplement() {
        WujinRelationAuditDecision decision = service.review(WujinAuditReviewCommand.reject("AUD-002", "缺少检测报告"));

        assertEquals(WujinAuditAction.REJECT, decision.getAction());
        assertEquals(WujinAuditReason.REVIEW_REJECTED, decision.getReason());
        assertEquals("驳回补充信息：缺少检测报告", decision.getMessage());
    }

    @Test
    void precheckPassesCleanSubmissionToManualReview() {
        WujinRelationAuditRequest request = baseRequest().build();

        WujinRelationAuditDecision decision = service.precheck(request);

        assertEquals(WujinAuditAction.MANUAL_REVIEW, decision.getAction());
        assertEquals(WujinAuditReason.PRECHECK_PASSED, decision.getReason());
        assertEquals("系统初筛通过，进入运营审核", decision.getMessage());
    }

    private WujinRelationAuditRequest.Builder baseRequest() {
        WujinMerchantRelationSubmission submission = WujinMerchantRelationSubmission.builder()
                .productId("SPU-001")
                .productName("高耐磨乘用车轮胎")
                .productLane(WujinLane.PRODUCT)
                .productCategoryId("P-TIRE-CAR")
                .relations(Arrays.asList(
                        new WujinMerchantRelationDraft("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true),
                        new WujinMerchantRelationDraft("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true)))
                .build();
        return WujinRelationAuditRequest.builder()
                .auditId("AUD-001")
                .submission(submission)
                .declaredProductLane(WujinLane.PRODUCT)
                .riskWarning(WujinRiskWarning.none())
                .similarEntities(Collections.emptyList());
    }
}
