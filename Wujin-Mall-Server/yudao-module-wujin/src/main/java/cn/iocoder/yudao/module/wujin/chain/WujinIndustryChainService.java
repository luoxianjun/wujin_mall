package cn.iocoder.yudao.module.wujin.chain;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WujinIndustryChainService {

    public List<WujinChainRelation> findUpstream(WujinChainGraph graph, String entityId) {
        List<WujinChainRelation> result = new ArrayList<>();
        for (WujinChainRelation relation : graph.getRelations()) {
            if (relation.getSource().getId().equals(entityId)) {
                result.add(relation);
            }
        }
        return result;
    }

    public List<WujinChainRelation> findDownstream(WujinChainGraph graph, String entityId) {
        List<WujinChainRelation> result = new ArrayList<>();
        for (WujinChainRelation relation : graph.getRelations()) {
            if (relation.getTarget().getId().equals(entityId)) {
                result.add(relation);
            }
        }
        return result;
    }

    public WujinJunctionInsight detectJunction(WujinChainGraph graph, String entityId) {
        Set<String> industries = new LinkedHashSet<>();
        WujinChainEntity entity = graph.getEntity(entityId);
        if (entity != null) {
            industries.addAll(entity.getIndustries());
        }
        for (WujinChainRelation relation : graph.getRelations()) {
            if (relation.getTarget().getId().equals(entityId) && relation.getIndustryContext() != null) {
                industries.add(relation.getIndustryContext());
            }
        }
        return new WujinJunctionInsight(industries.size() >= 2, new ArrayList<>(industries));
    }

    public WujinRiskWarning buildRiskWarning(WujinChainGraph graph, String entityId, String industryContext) {
        WujinChainEntity entity = graph.getEntity(entityId);
        if (entity == null) {
            return WujinRiskWarning.none();
        }
        WujinJunctionInsight insight = detectJunction(graph, entityId);
        if (!insight.isJunction() || !"医疗器械".equals(industryContext)) {
            return WujinRiskWarning.none();
        }
        return new WujinRiskWarning(true,
                entity.getName() + "在轮胎、医疗器械行业存在标准差异，不可直接替代",
                "轮胎级 STR20 因杂质和蛋白指标不同，不可用于医用乳胶手套");
    }

    public WujinTracePath buildTracePath(WujinChainGraph graph, String sourceEntityId, String targetEntityId) {
        WujinChainEntity source = graph.getEntity(sourceEntityId);
        WujinChainEntity target = graph.getEntity(targetEntityId);
        String displayText = source.getName() + " → " + target.getName();
        return new WujinTracePath(displayText, source.getLane(), target.getLane());
    }
}
