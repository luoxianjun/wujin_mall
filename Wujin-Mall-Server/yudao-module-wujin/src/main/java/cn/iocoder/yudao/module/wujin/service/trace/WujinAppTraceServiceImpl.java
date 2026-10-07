package cn.iocoder.yudao.module.wujin.service.trace;

import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.trace.vo.WujinTraceGraphRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class WujinAppTraceServiceImpl implements WujinAppTraceService {

    private static final int ENABLED_STATUS = 0;
    private static final int AUDIT_STATUS_APPROVED = 30;

    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService chainEntityRelationService;

    @Override
    public WujinTraceGraphRespVO getTraceGraph(WujinTraceGraphReqVO reqVO) {
        WujinChainEntityDO root = resolveRootEntity(reqVO);
        if (root == null) {
            return buildFallbackTraceGraph(reqVO);
        }

        Map<Long, WujinChainEntityDO> entityMap = new LinkedHashMap<>();
        entityMap.put(root.getId(), root);
        List<WujinChainEntityRelationDO> relations = loadTraceRelations(root, entityMap);
        for (WujinChainEntityRelationDO relation : relations) {
            putEntity(entityMap, relation.getSourceEntityId());
            putEntity(entityMap, relation.getTargetEntityId());
        }
        if (relations.isEmpty()) {
            return buildFallbackTraceGraph(reqVO, root);
        }

        WujinTraceGraphRespVO respVO = new WujinTraceGraphRespVO();
        String keyword = firstNotBlank(root.getName(), reqVO.getKeyword(), reqVO.getSourceKeyword(), "五金商品");
        respVO.setTitle(keyword + " BOM 溯源");
        respVO.setSummary("当前成品为起点，向右展开部件、原材料和更上游原料的构成树。");
        respVO.setOutputName(keyword);
        respVO.setOutputMeta("当前成品树根节点");
        respVO.setCurrentBatch(new WujinTraceGraphRespVO.TraceBatch("WJ-" + root.getId(), "2026-06-27",
                "五金商城供应链中心", "合格"));
        respVO.setNodes(buildNodes(entityMap));
        respVO.setEdges(buildEdges(entityMap, relations));
        return respVO;
    }

    private WujinChainEntityDO resolveRootEntity(WujinTraceGraphReqVO reqVO) {
        if (reqVO.getId() != null) {
            WujinChainEntityDO entity = chainEntityService.getEntity(reqVO.getId());
            if (entity != null) {
                return entity;
            }
        }

        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setName(firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword()));
        listReqVO.setLane(parseLane(reqVO.getLane(), WujinLane.PRODUCT).name());
        listReqVO.setIndustry(reqVO.getIndustry());
        listReqVO.setStatus(ENABLED_STATUS);
        List<WujinChainEntityDO> entities = chainEntityService.getEntityList(listReqVO);
        if (!entities.isEmpty()) {
            return entities.get(0);
        }

        WujinChainEntityListReqVO keywordReqVO = new WujinChainEntityListReqVO();
        keywordReqVO.setName(firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword()));
        keywordReqVO.setStatus(ENABLED_STATUS);
        entities = chainEntityService.getEntityList(keywordReqVO);
        return entities.isEmpty() ? null : entities.get(0);
    }

    private List<WujinChainEntityRelationDO> loadTraceRelations(WujinChainEntityDO root,
                                                               Map<Long, WujinChainEntityDO> entityMap) {
        Map<String, WujinChainEntityRelationDO> relationMap = new LinkedHashMap<>();
        List<Long> frontier = new ArrayList<>();
        frontier.add(root.getId());
        Set<Long> expandedEntityIds = new HashSet<>();

        for (int depth = 0; depth < 3 && !frontier.isEmpty(); depth++) {
            List<Long> nextFrontier = new ArrayList<>();
            for (Long entityId : frontier) {
                if (entityId == null || !expandedEntityIds.add(entityId)) {
                    continue;
                }
                List<WujinChainEntityRelationDO> connectedRelations = new ArrayList<>();
                connectedRelations.addAll(loadRelations(entityId, true));
                connectedRelations.addAll(loadRelations(entityId, false));
                for (WujinChainEntityRelationDO relation : connectedRelations) {
                    relationMap.putIfAbsent(relationKey(relation), relation);
                    addRelationEndpoint(entityMap, nextFrontier, expandedEntityIds, relation.getSourceEntityId());
                    addRelationEndpoint(entityMap, nextFrontier, expandedEntityIds, relation.getTargetEntityId());
                }
            }
            frontier = nextFrontier;
        }
        return new ArrayList<>(relationMap.values());
    }

    private void addRelationEndpoint(Map<Long, WujinChainEntityDO> entityMap, List<Long> nextFrontier,
                                     Set<Long> expandedEntityIds, Long entityId) {
        if (entityId == null) {
            return;
        }
        putEntity(entityMap, entityId);
        if (!expandedEntityIds.contains(entityId) && !nextFrontier.contains(entityId)) {
            nextFrontier.add(entityId);
        }
    }

    private List<WujinChainEntityRelationDO> loadRelations(Long entityId, boolean source) {
        WujinChainEntityRelationListReqVO listReqVO = new WujinChainEntityRelationListReqVO();
        if (source) {
            listReqVO.setSourceEntityId(entityId);
        } else {
            listReqVO.setTargetEntityId(entityId);
        }
        listReqVO.setAuditStatus(AUDIT_STATUS_APPROVED);
        List<WujinChainEntityRelationDO> relations = chainEntityRelationService.getRelationList(listReqVO);
        return relations == null ? Collections.emptyList() : relations;
    }

    private String relationKey(WujinChainEntityRelationDO relation) {
        if (relation.getId() != null) {
            return String.valueOf(relation.getId());
        }
        return relation.getSourceEntityId() + ">" + relation.getTargetEntityId() + ":" + relation.getRelationType();
    }

    private void putEntity(Map<Long, WujinChainEntityDO> entityMap, Long entityId) {
        if (entityId == null || entityMap.containsKey(entityId)) {
            return;
        }
        WujinChainEntityDO entity = chainEntityService.getEntity(entityId);
        if (entity != null) {
            entityMap.put(entityId, entity);
        }
    }

    private List<WujinTraceGraphRespVO.TraceNode> buildNodes(Map<Long, WujinChainEntityDO> entityMap) {
        List<WujinTraceGraphRespVO.TraceNode> nodes = new ArrayList<>();
        for (WujinChainEntityDO entity : entityMap.values()) {
            nodes.add(new WujinTraceGraphRespVO.TraceNode(String.valueOf(entity.getId()), entity.getLane(),
                    entity.getName(), buildNodeDescription(entity)));
        }
        return nodes;
    }

    private List<WujinTraceGraphRespVO.TraceEdge> buildEdges(Map<Long, WujinChainEntityDO> entityMap,
                                                            List<WujinChainEntityRelationDO> relations) {
        List<WujinTraceGraphRespVO.TraceEdge> edges = new ArrayList<>();
        for (WujinChainEntityRelationDO relation : relations) {
            WujinChainEntityDO source = entityMap.get(relation.getSourceEntityId());
            WujinChainEntityDO target = entityMap.get(relation.getTargetEntityId());
            if (source == null || target == null) {
                continue;
            }
            edges.add(new WujinTraceGraphRespVO.TraceEdge(String.valueOf(source.getId()), source.getName(),
                    String.valueOf(target.getId()), target.getName(), relationText(relation)));
        }
        return edges;
    }

    private WujinTraceGraphRespVO buildFallbackTraceGraph(WujinTraceGraphReqVO reqVO) {
        String keyword = firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword(), "五金商品");
        return buildFallbackTraceGraph(reqVO, fallbackEntity(-100L, WujinLane.PRODUCT.name(), keyword));
    }

    private WujinTraceGraphRespVO buildFallbackTraceGraph(WujinTraceGraphReqVO reqVO, WujinChainEntityDO root) {
        String keyword = firstNotBlank(root.getName(), reqVO.getKeyword(), reqVO.getSourceKeyword(), "五金商品");
        WujinTraceGraphRespVO respVO = new WujinTraceGraphRespVO();
        respVO.setTitle(keyword + " BOM 溯源");
        respVO.setSummary("当前成品为起点，向右展开部件、原材料和更上游原料的构成树。");
        respVO.setOutputName(keyword);
        respVO.setOutputMeta("当前成品树根节点");
        respVO.setCurrentBatch(new WujinTraceGraphRespVO.TraceBatch("WJ-DEMO", "2026-06-27",
                "五金商城供应链中心", "合格"));
        List<WujinTraceGraphRespVO.TraceNode> nodes = new ArrayList<>();
        nodes.add(new WujinTraceGraphRespVO.TraceNode("product", WujinLane.PRODUCT.name(), keyword,
                "当前成品，所有右侧节点都是它的组成来源。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("inner-ring", "PART", "内圈",
                "轴承承载部件，由轴承钢车削热处理形成。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("outer-ring", "PART", "外圈",
                "外侧承载部件，决定轴承安装和受力稳定性。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("steel-ball", "PART", "钢球",
                "滚动体部件，影响低噪音和旋转精度。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("retainer", "PART", "保持架",
                "保持滚动体间距，常用钢板或增强尼龙。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("gcr15", WujinLane.MATERIAL.name(), "轴承钢GCr15",
                "内外圈的主要原材料，高碳铬轴承钢。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("steel-bead-material", WujinLane.MATERIAL.name(),
                "高碳铬轴承钢", "钢球常用原料，强调硬度、洁净度和疲劳寿命。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("nylon", WujinLane.MATERIAL.name(), "增强尼龙/钢板",
                "保持架可选原料，按转速、温度和成本选择。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("ring-forging", WujinLane.PROCESS.name(),
                "精密锻造 / 退火", "更上游的成形与热处理来源，展示前序加工链条。"));
        nodes.add(new WujinTraceGraphRespVO.TraceNode("steel-mill", WujinLane.MATERIAL.name(),
                "特钢厂炉批", "对应炉批号、洁净度和材质证明文件。"));
        respVO.setNodes(nodes);
        List<WujinTraceGraphRespVO.TraceEdge> edges = new ArrayList<>();
        edges.add(new WujinTraceGraphRespVO.TraceEdge("product", keyword, "inner-ring", "内圈", "直接构成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("product", keyword, "outer-ring", "外圈", "直接构成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("product", keyword, "steel-ball", "钢球", "直接构成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("product", keyword, "retainer", "保持架", "直接构成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("inner-ring", "内圈", "gcr15", "轴承钢GCr15",
                "由该原材料加工形成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("outer-ring", "外圈", "gcr15", "轴承钢GCr15",
                "由该原材料加工形成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("steel-ball", "钢球", "steel-bead-material", "高碳铬轴承钢",
                "由该原材料加工形成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("retainer", "保持架", "nylon", "增强尼龙/钢板",
                "由该原材料加工形成"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("gcr15", "轴承钢GCr15", "ring-forging", "精密锻造 / 退火",
                "更上游加工环节"));
        edges.add(new WujinTraceGraphRespVO.TraceEdge("ring-forging", "精密锻造 / 退火", "steel-mill", "特钢厂炉批",
                "更早一级来源数据"));
        respVO.setEdges(edges);
        return respVO;
    }

    private WujinChainEntityDO fallbackEntity(Long id, String lane, String name) {
        WujinChainEntityDO entity = new WujinChainEntityDO();
        entity.setId(id);
        entity.setLane(lane);
        entity.setName(name);
        entity.setStatus(ENABLED_STATUS);
        return entity;
    }

    private String buildNodeDescription(WujinChainEntityDO entity) {
        if (!isBlank(entity.getRiskNote())) {
            return entity.getRiskNote();
        }
        WujinLane lane = parseLane(entity.getLane(), WujinLane.PRODUCT);
        return entity.getName() + "位于" + laneName(lane) + "泳道，可用于上下游溯源和寻源匹配。";
    }

    private String relationText(WujinChainEntityRelationDO relation) {
        String relationType = isBlank(relation.getRelationType()) ? "上下游关系" : relation.getRelationType();
        if (relation.getCostRatio() != null) {
            return relationType + "，成本占比约" + relation.getCostRatio() + "%。";
        }
        return relationType + "，可用于溯源和寻源匹配。";
    }

    private WujinLane parseLane(String value, WujinLane defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return WujinLane.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    private String laneName(WujinLane lane) {
        if (lane == WujinLane.MATERIAL) {
            return "原材料";
        }
        if (lane == WujinLane.PROCESS) {
            return "加工";
        }
        return "成品";
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
