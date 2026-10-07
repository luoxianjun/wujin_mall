package cn.iocoder.yudao.module.wujin.search;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinSearchDecisionServiceTest {

    private final WujinSearchDecisionService service = new WujinSearchDecisionService();

    @Test
    void decideDirectProductSearchUsesProductLaneAndFinestGranularity() {
        WujinSearchDecision decision = service.decide(WujinSearchDecisionRequest.builder()
                .keyword("轮胎")
                .intent(WujinSearchIntent.PRODUCT)
                .entryPath(WujinEntryPath.DIRECT_SEARCH)
                .requestedLane(WujinLane.PRODUCT)
                .childCount(12)
                .build());

        assertEquals(WujinLane.PRODUCT, decision.getDefaultLane());
        assertEquals(3, decision.getGranularity());
        assertEquals("系统判定：您正在查找成品规格，优先展示最细粒度结果", decision.getExplanation());
        assertFalse(decision.isRiskWarningRequired());
    }

    @Test
    void decideDirectMaterialSearchUsesMaterialLaneAndFinestGranularity() {
        WujinSearchDecision decision = service.decide(WujinSearchDecisionRequest.builder()
                .keyword("天然橡胶")
                .intent(WujinSearchIntent.MATERIAL)
                .entryPath(WujinEntryPath.DIRECT_SEARCH)
                .requestedLane(WujinLane.MATERIAL)
                .childCount(18)
                .build());

        assertEquals(WujinLane.MATERIAL, decision.getDefaultLane());
        assertEquals(3, decision.getGranularity());
        assertEquals("系统判定：您把“天然橡胶”当作具体采购对象，展示规格型号", decision.getExplanation());
    }

    @Test
    void decideProductToMaterialJumpShowsCoarseUpstreamView() {
        WujinSearchDecision decision = service.decide(WujinSearchDecisionRequest.builder()
                .keyword("天然橡胶")
                .intent(WujinSearchIntent.MATERIAL)
                .entryPath(WujinEntryPath.UPSTREAM_JUMP)
                .sourceLane(WujinLane.PRODUCT)
                .requestedLane(WujinLane.MATERIAL)
                .sourceKeyword("轮胎")
                .childCount(12)
                .build());

        assertEquals(WujinLane.MATERIAL, decision.getDefaultLane());
        assertEquals(1, decision.getGranularity());
        assertEquals("系统判定：您从轮胎查看所需材料，先展示材料大类，便于快速浏览", decision.getExplanation());
        assertEquals("来自：轮胎 → 天然橡胶", decision.getContextHint());
    }

    @Test
    void decideMedicalMaterialViewForcesRiskWarning() {
        WujinSearchDecision decision = service.decide(WujinSearchDecisionRequest.builder()
                .keyword("医用乳胶手套")
                .intent(WujinSearchIntent.PRODUCT)
                .entryPath(WujinEntryPath.LANE_SWITCH)
                .requestedLane(WujinLane.MATERIAL)
                .industry("医疗器械")
                .riskTags(Arrays.asList("多行业适用", "不可互换"))
                .childCount(8)
                .build());

        assertEquals(WujinLane.MATERIAL, decision.getDefaultLane());
        assertTrue(decision.isRiskWarningRequired());
        assertEquals("该材料跨多个行业使用，请确认应用场景后查看供应商", decision.getExplanation());
    }

    @Test
    void adaptiveGranularityCapsLargeCategoryAndExpandsSparseCategory() {
        assertEquals(2, service.adaptGranularity(68, 3));
        assertEquals(2, service.adaptGranularity(3, 1));
        assertEquals(2, service.adaptGranularity(12, 2));
    }

    @Test
    void resolveCategoryPrefersContextThenFallsBackToHighestWeight() {
        WujinCategoryMapping productContext = new WujinCategoryMapping("C-01", "成品/汽车配件/轮胎", WujinLane.PRODUCT, 80);
        WujinCategoryMapping materialContext = new WujinCategoryMapping("M-01", "原材料/橡胶材料/天然橡胶", WujinLane.MATERIAL, 60);
        WujinCategoryMapping heavierFallback = new WujinCategoryMapping("P-02", "成品/工业配件/输送带", WujinLane.PRODUCT, 95);

        assertEquals(materialContext, service.resolveCategoryConflict(
                Arrays.asList(productContext, materialContext, heavierFallback),
                Collections.singletonList(WujinLane.MATERIAL)));
        assertEquals(heavierFallback, service.resolveCategoryConflict(
                Arrays.asList(productContext, materialContext, heavierFallback),
                Collections.singletonList(WujinLane.PROCESS)));
    }
}
