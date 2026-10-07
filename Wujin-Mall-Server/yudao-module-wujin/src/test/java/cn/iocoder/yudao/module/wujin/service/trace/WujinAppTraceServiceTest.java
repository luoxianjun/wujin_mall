package cn.iocoder.yudao.module.wujin.service.trace;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphRespVO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinAppTraceServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class})
class WujinAppTraceServiceTest extends BaseDbUnitTest {

    @Resource
    private WujinAppTraceService traceService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @Test
    void getTraceGraphIncludesApprovedSecondHopRelation() {
        Long productId = chainEntityService.createEntity(entityReq("P_TIRE_CAR", "乘用车轮胎",
                WujinLane.PRODUCT.name(), "橡胶,汽车"));
        Long processId = chainEntityService.createEntity(entityReq("C_TIRE_VULCANIZE", "硫化成型",
                WujinLane.PROCESS.name(), "橡胶,汽车"));
        Long materialId = chainEntityService.createEntity(entityReq("M_TIRE_RUBBER", "天然橡胶",
                WujinLane.MATERIAL.name(), "橡胶,汽车"));
        relationService.createRelation(relationReq(productId, processId, WujinRelationType.REQUIRES_PROCESS.name(), 35));
        relationService.createRelation(relationReq(processId, materialId, WujinRelationType.REQUIRES_MATERIAL.name(), 52));

        WujinTraceGraphReqVO reqVO = new WujinTraceGraphReqVO();
        reqVO.setKeyword("乘用车轮胎");
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setIndustry("橡胶");

        WujinTraceGraphRespVO graph = traceService.getTraceGraph(reqVO);

        assertTrue(graph.getTitle().contains("乘用车轮胎"));
        assertNotNull(graph.getCurrentBatch());
        assertEquals(new HashSet<>(Arrays.asList(WujinLane.PRODUCT.name(), WujinLane.PROCESS.name(),
                        WujinLane.MATERIAL.name())),
                graph.getNodes().stream().map(WujinTraceGraphRespVO.TraceNode::getLane).collect(Collectors.toSet()));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> edge.getFromName().equals("乘用车轮胎")
                && edge.getToName().equals("硫化成型")
                && edge.getRelation().contains(WujinRelationType.REQUIRES_PROCESS.name())));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> edge.getFromName().equals("硫化成型")
                && edge.getToName().equals("天然橡胶")
                && edge.getRelation().contains("52")));
    }

    @Test
    void getTraceGraphCanExpandThirdHopRelations() {
        Long productId = chainEntityService.createEntity(entityReq("P_BEARING", "深沟球轴承",
                WujinLane.PRODUCT.name(), "五金,轴承"));
        Long partId = chainEntityService.createEntity(entityReq("C_BEARING_RING", "轴承套圈",
                WujinLane.PROCESS.name(), "五金,轴承"));
        Long materialId = chainEntityService.createEntity(entityReq("M_BEARING_STEEL", "轴承钢GCr15",
                WujinLane.MATERIAL.name(), "五金,轴承"));
        Long upstreamId = chainEntityService.createEntity(entityReq("M_BEARING_UPSTREAM", "精密锻造坯料",
                WujinLane.PROCESS.name(), "五金,轴承"));
        relationService.createRelation(relationReq(productId, partId, WujinRelationType.REQUIRES_PROCESS.name(), 28));
        relationService.createRelation(relationReq(partId, materialId, WujinRelationType.REQUIRES_MATERIAL.name(), 46));
        relationService.createRelation(relationReq(materialId, upstreamId, WujinRelationType.REQUIRES_PROCESS.name(), 18));

        WujinTraceGraphReqVO reqVO = new WujinTraceGraphReqVO();
        reqVO.setKeyword("深沟球轴承");
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setIndustry("五金");

        WujinTraceGraphRespVO graph = traceService.getTraceGraph(reqVO);

        assertTrue(graph.getNodes().stream().anyMatch(node -> "精密锻造坯料".equals(node.getName())));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> edge.getFromName().equals("轴承钢GCr15")
                && edge.getToName().equals("精密锻造坯料")));
    }

    @Test
    void getTraceGraphFallsBackToBomTreeWhenNoEntityExists() {
        WujinTraceGraphReqVO reqVO = new WujinTraceGraphReqVO();
        reqVO.setKeyword("未知商品");
        reqVO.setLane(WujinLane.PRODUCT.name());

        WujinTraceGraphRespVO graph = traceService.getTraceGraph(reqVO);

        assertEquals("未知商品 BOM 溯源", graph.getTitle());
        assertTrue(graph.getSummary().contains("当前成品为起点"));
        assertEquals("当前成品树根节点", graph.getOutputMeta());
        assertTrue(graph.getNodes().size() >= 6);
        assertTrue(graph.getNodes().stream().anyMatch(node -> "内圈".equals(node.getName())));
        assertTrue(graph.getNodes().stream().anyMatch(node -> "保持架".equals(node.getName())));
        assertTrue(graph.getNodes().stream().anyMatch(node -> "轴承钢GCr15".equals(node.getName())));
        assertTrue(graph.getNodes().stream().anyMatch(node -> "精密锻造 / 退火".equals(node.getName())));
        assertTrue(graph.getNodes().stream().anyMatch(node -> "特钢厂炉批".equals(node.getName())));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> "未知商品".equals(edge.getFromName())
                && "内圈".equals(edge.getToName())
                && edge.getRelation().contains("直接构成")));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> "轴承钢GCr15".equals(edge.getFromName())
                && "精密锻造 / 退火".equals(edge.getToName())));
        assertTrue(graph.getEdges().stream().anyMatch(edge -> "精密锻造 / 退火".equals(edge.getFromName())
                && "特钢厂炉批".equals(edge.getToName())));
        assertEquals("WJ-DEMO", graph.getCurrentBatch().getBatchNo());
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

    private WujinChainEntityRelationSaveReqVO relationReq(Long sourceEntityId, Long targetEntityId,
                                                          String relationType, Integer costRatio) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(sourceEntityId);
        reqVO.setTargetEntityId(targetEntityId);
        reqVO.setRelationType(relationType);
        reqVO.setWeight(100);
        reqVO.setCostRatio(costRatio);
        reqVO.setIndustryContext("橡胶");
        reqVO.setAuditStatus(30);
        reqVO.setAuditRemark("测试通过");
        return reqVO;
    }
}
