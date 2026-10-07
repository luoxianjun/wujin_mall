package cn.iocoder.yudao.module.wujin.merchant;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinMerchantRelationServiceTest {

    private final WujinMerchantRelationService service = new WujinMerchantRelationService();

    @Test
    void validateProductSubmissionRequiresAtLeastOneMaterial() {
        WujinMerchantRelationSubmission submission = WujinMerchantRelationSubmission.builder()
                .productId("SPU-001")
                .productName("高耐磨乘用车轮胎")
                .productLane(WujinLane.PRODUCT)
                .relations(Collections.singletonList(new WujinMerchantRelationDraft("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true)))
                .build();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.validateSubmission(submission));
        assertEquals("成品上架必须至少绑定一个原材料类目", error.getMessage());
    }

    @Test
    void decideAuditRouteAutoApprovesWhenAllRequiredTemplateRelationsKept() {
        WujinMerchantRelationSubmission submission = tireSubmission(true,
                relation("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_CARBON_BLACK", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true));
        WujinIndustryTemplate template = new WujinIndustryTemplate("T-TIRE", Arrays.asList("E_RUBBER", "E_CARBON_BLACK"),
                Collections.singletonList("E_VULCANIZE"));

        WujinMerchantAuditDecision decision = service.decideAuditRoute(submission, template);

        assertEquals(WujinMerchantAuditRoute.AUTO_APPROVE, decision.getRoute());
        assertEquals("平台模板关系完整保留，可随商品审核自动生效", decision.getReason());
    }

    @Test
    void decideAuditRouteRequiresManualReviewWhenRequiredMaterialRemoved() {
        WujinMerchantRelationSubmission submission = tireSubmission(true,
                relation("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true));
        WujinIndustryTemplate template = new WujinIndustryTemplate("T-TIRE", Arrays.asList("E_RUBBER", "E_CARBON_BLACK"),
                Collections.singletonList("E_VULCANIZE"));

        WujinMerchantAuditDecision decision = service.decideAuditRoute(submission, template);

        assertEquals(WujinMerchantAuditRoute.MANUAL_REVIEW, decision.getRoute());
        assertEquals("缺少平台模板必选材料：E_CARBON_BLACK", decision.getReason());
    }

    @Test
    void decideAuditRouteRequiresManualReviewWhenMerchantAddsCustomRelation() {
        WujinMerchantRelationSubmission submission = tireSubmission(true,
                relation("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_CARBON_BLACK", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_SPECIAL_BLACK", WujinRelationType.REQUIRES_MATERIAL, false),
                relation("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true));
        WujinIndustryTemplate template = new WujinIndustryTemplate("T-TIRE", Arrays.asList("E_RUBBER", "E_CARBON_BLACK"),
                Collections.singletonList("E_VULCANIZE"));

        WujinMerchantAuditDecision decision = service.decideAuditRoute(submission, template);

        assertEquals(WujinMerchantAuditRoute.MANUAL_REVIEW, decision.getRoute());
        assertEquals("商家新增非模板关系，需运营审核", decision.getReason());
    }

    @Test
    void calculateCompletenessRewardsMaterialsProcessesCertificationsAndDescriptions() {
        WujinMerchantRelationSubmission submission = tireSubmission(true,
                relation("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_CARBON_BLACK", WujinRelationType.REQUIRES_MATERIAL, true),
                relation("E_VULCANIZE", WujinRelationType.REQUIRES_PROCESS, true));
        submission.setCertificationCount(2);
        submission.setHasApplicationDescription(true);

        WujinMerchantCompletenessScore score = service.calculateCompleteness(submission);

        assertEquals(100, score.getScore());
        assertTrue(score.isBenchmarkCandidate());
    }

    @Test
    void calculateCompletenessShowsMissingParts() {
        WujinMerchantRelationSubmission submission = tireSubmission(false,
                relation("E_RUBBER", WujinRelationType.REQUIRES_MATERIAL, true));

        WujinMerchantCompletenessScore score = service.calculateCompleteness(submission);

        assertEquals(40, score.getScore());
        assertFalse(score.isBenchmarkCandidate());
        assertEquals("缺少加工工艺、认证信息、应用说明", score.getSuggestion());
    }

    private WujinMerchantRelationSubmission tireSubmission(boolean hasCategory, WujinMerchantRelationDraft... relations) {
        return WujinMerchantRelationSubmission.builder()
                .productId("SPU-001")
                .productName("高耐磨乘用车轮胎")
                .productLane(WujinLane.PRODUCT)
                .productCategoryId(hasCategory ? "P-TIRE-CAR" : null)
                .relations(Arrays.asList(relations))
                .build();
    }

    private WujinMerchantRelationDraft relation(String entityId, WujinRelationType type, boolean fromTemplate) {
        return new WujinMerchantRelationDraft(entityId, type, fromTemplate);
    }
}
