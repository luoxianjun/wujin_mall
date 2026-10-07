package cn.iocoder.yudao.module.wujin.category;

import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WujinCategoryTreeServiceTest {

    private final WujinCategoryTreeService service = new WujinCategoryTreeService();

    @Test
    void buildPathUsesParentPathAndCategoryName() {
        WujinCategoryNode root = new WujinCategoryNode("P-ROOT", "汽车配件", WujinLane.PRODUCT, null, 12);
        WujinCategoryNode child = new WujinCategoryNode("P-TIRE", "轮胎", WujinLane.PRODUCT, root, 4);
        WujinCategoryNode leaf = new WujinCategoryNode("P-TIRE-CAR", "乘用车轮胎", WujinLane.PRODUCT, child, 0);

        assertEquals("汽车配件 > 轮胎 > 乘用车轮胎", service.buildPath(leaf));
    }

    @Test
    void validateRejectsCrossLaneParent() {
        WujinCategoryNode materialRoot = new WujinCategoryNode("M-ROOT", "原材料", WujinLane.MATERIAL, null, 10);
        WujinCategoryNode productChild = new WujinCategoryNode("P-TIRE", "轮胎", WujinLane.PRODUCT, materialRoot, 0);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.validateNode(productChild));
        assertEquals("类目父子节点必须属于同一泳道", error.getMessage());
    }

    @Test
    void evaluateHealthDetectsNeedsSplitAndUnbound() {
        WujinCategoryNode crowded = new WujinCategoryNode("M-HELPER", "化工助剂", WujinLane.MATERIAL, null, 68);
        WujinCategoryNode unbound = new WujinCategoryNode("P-CNC", "CNC加工", WujinLane.PROCESS, null, 0);

        assertEquals(WujinCategoryHealthStatus.NEEDS_SPLIT,
                service.evaluateHealth(crowded, Collections.singletonList(new WujinCategoryMappingRule("M-HELPER", "P-TIRE"))));
        assertEquals(WujinCategoryHealthStatus.UNBOUND,
                service.evaluateHealth(unbound, Collections.emptyList()));
    }

    @Test
    void chooseDisplayDepthUsesLaneLimitAndHealthStatus() {
        WujinCategoryDisplayPolicy policy = new WujinCategoryDisplayPolicy(3, 2, 3);

        assertEquals(2, service.chooseDisplayDepth(WujinLane.PRODUCT, WujinCategoryHealthStatus.HEALTHY, policy));
        assertEquals(1, service.chooseDisplayDepth(WujinLane.MATERIAL, WujinCategoryHealthStatus.NEEDS_SPLIT, policy));
        assertEquals(1, service.chooseDisplayDepth(WujinLane.PROCESS, WujinCategoryHealthStatus.UNBOUND, policy));
    }

    @Test
    void summarizeTreeHealthCountsEachStatus() {
        WujinCategoryHealthSummary summary = service.summarizeHealth(Arrays.asList(
                WujinCategoryHealthStatus.HEALTHY,
                WujinCategoryHealthStatus.HEALTHY,
                WujinCategoryHealthStatus.NEEDS_SPLIT,
                WujinCategoryHealthStatus.UNBOUND));

        assertEquals(2, summary.getHealthyCount());
        assertEquals(1, summary.getNeedsSplitCount());
        assertEquals(1, summary.getUnboundCount());
        assertEquals(4, summary.getTotalCount());
    }
}
