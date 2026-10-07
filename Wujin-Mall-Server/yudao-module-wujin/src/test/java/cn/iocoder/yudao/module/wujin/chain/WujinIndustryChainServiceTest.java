package cn.iocoder.yudao.module.wujin.chain;

import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinIndustryChainServiceTest {

    private final WujinIndustryChainService service = new WujinIndustryChainService();

    @Test
    void findUpstreamReturnsMaterialsAndProcessesForProduct() {
        WujinChainGraph graph = sampleGraph();

        List<WujinChainRelation> upstream = service.findUpstream(graph, "E_TIRE");

        assertEquals(3, upstream.size());
        assertTrue(upstream.stream().anyMatch(relation -> relation.getTarget().getId().equals("E_RUBBER")
                && relation.getRelationType() == WujinRelationType.REQUIRES_MATERIAL));
        assertTrue(upstream.stream().anyMatch(relation -> relation.getTarget().getId().equals("E_VULCANIZE")
                && relation.getRelationType() == WujinRelationType.REQUIRES_PROCESS));
    }

    @Test
    void findDownstreamReturnsProductsThatUseMaterial() {
        WujinChainGraph graph = sampleGraph();

        List<WujinChainRelation> downstream = service.findDownstream(graph, "E_RUBBER");

        assertEquals(2, downstream.size());
        assertTrue(downstream.stream().anyMatch(relation -> relation.getSource().getId().equals("E_TIRE")));
        assertTrue(downstream.stream().anyMatch(relation -> relation.getSource().getId().equals("E_GLOVE")));
    }

    @Test
    void detectJunctionMarksEntityUsedByMultipleIndustries() {
        WujinChainGraph graph = sampleGraph();

        WujinJunctionInsight insight = service.detectJunction(graph, "E_RUBBER");

        assertTrue(insight.isJunction());
        assertEquals(2, insight.getIndustries().size());
        assertTrue(insight.getIndustries().contains("轮胎"));
        assertTrue(insight.getIndustries().contains("医疗器械"));
    }

    @Test
    void buildRiskWarningForNonInterchangeableMedicalMaterial() {
        WujinChainGraph graph = sampleGraph();

        WujinRiskWarning warning = service.buildRiskWarning(graph, "E_RUBBER", "医疗器械");

        assertTrue(warning.isRequired());
        assertEquals("天然橡胶在轮胎、医疗器械行业存在标准差异，不可直接替代", warning.getTitle());
        assertEquals("轮胎级 STR20 因杂质和蛋白指标不同，不可用于医用乳胶手套", warning.getMessage());
    }

    @Test
    void buildTracePathKeepsSourceAndTargetContext() {
        WujinChainGraph graph = sampleGraph();

        WujinTracePath path = service.buildTracePath(graph, "E_TIRE", "E_RUBBER");

        assertEquals("轮胎 → 天然橡胶", path.getDisplayText());
        assertEquals(WujinLane.PRODUCT, path.getSourceLane());
        assertEquals(WujinLane.MATERIAL, path.getTargetLane());
    }

    @Test
    void noWarningForSingleIndustryEntity() {
        WujinChainGraph graph = sampleGraph();

        WujinRiskWarning warning = service.buildRiskWarning(graph, "E_CARBON_BLACK", "轮胎");

        assertFalse(warning.isRequired());
    }

    private WujinChainGraph sampleGraph() {
        WujinChainEntity tire = new WujinChainEntity("E_TIRE", "轮胎", WujinLane.PRODUCT, Arrays.asList("轮胎"));
        WujinChainEntity glove = new WujinChainEntity("E_GLOVE", "医用乳胶手套", WujinLane.PRODUCT, Arrays.asList("医疗器械"));
        WujinChainEntity rubber = new WujinChainEntity("E_RUBBER", "天然橡胶", WujinLane.MATERIAL, Arrays.asList("轮胎", "医疗器械"));
        WujinChainEntity carbonBlack = new WujinChainEntity("E_CARBON_BLACK", "炭黑", WujinLane.MATERIAL, Arrays.asList("轮胎"));
        WujinChainEntity vulcanize = new WujinChainEntity("E_VULCANIZE", "硫化工艺", WujinLane.PROCESS, Arrays.asList("轮胎", "医疗器械"));
        WujinChainEntity latex = new WujinChainEntity("E_LATEX", "医用级天然橡胶浓缩胶乳", WujinLane.MATERIAL, Arrays.asList("医疗器械"));

        return new WujinChainGraph(
                Arrays.asList(tire, glove, rubber, carbonBlack, vulcanize, latex),
                Arrays.asList(
                        new WujinChainRelation(tire, rubber, WujinRelationType.REQUIRES_MATERIAL, "轮胎"),
                        new WujinChainRelation(tire, carbonBlack, WujinRelationType.REQUIRES_MATERIAL, "轮胎"),
                        new WujinChainRelation(tire, vulcanize, WujinRelationType.REQUIRES_PROCESS, "轮胎"),
                        new WujinChainRelation(glove, rubber, WujinRelationType.REQUIRES_MATERIAL, "医疗器械"),
                        new WujinChainRelation(glove, latex, WujinRelationType.REQUIRES_MATERIAL, "医疗器械"),
                        new WujinChainRelation(glove, vulcanize, WujinRelationType.REQUIRES_PROCESS, "医疗器械")
                ));
    }
}
