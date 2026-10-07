package cn.iocoder.yudao.module.wujin.category;

import cn.iocoder.yudao.module.wujin.search.WujinLane;

import java.util.List;

public class WujinCategoryTreeService {

    public String buildPath(WujinCategoryNode node) {
        if (node.getParent() == null) {
            return node.getName();
        }
        return buildPath(node.getParent()) + " > " + node.getName();
    }

    public void validateNode(WujinCategoryNode node) {
        if (node.getParent() != null && node.getParent().getLane() != node.getLane()) {
            throw new IllegalArgumentException("类目父子节点必须属于同一泳道");
        }
    }

    public WujinCategoryHealthStatus evaluateHealth(WujinCategoryNode node, List<WujinCategoryMappingRule> mappingRules) {
        if (node.getChildCount() > 50) {
            return WujinCategoryHealthStatus.NEEDS_SPLIT;
        }
        if (!hasMapping(node, mappingRules)) {
            return WujinCategoryHealthStatus.UNBOUND;
        }
        return WujinCategoryHealthStatus.HEALTHY;
    }

    public int chooseDisplayDepth(WujinLane lane, WujinCategoryHealthStatus status, WujinCategoryDisplayPolicy policy) {
        if (status == WujinCategoryHealthStatus.UNBOUND) {
            return 1;
        }
        if (status == WujinCategoryHealthStatus.NEEDS_SPLIT) {
            return 1;
        }
        if (lane == WujinLane.PRODUCT) {
            return Math.min(policy.getProductMaxDepth(), 2);
        }
        if (lane == WujinLane.PROCESS) {
            return Math.min(policy.getProcessMaxDepth(), 2);
        }
        return policy.getMaterialMaxDepth();
    }

    public WujinCategoryHealthSummary summarizeHealth(List<WujinCategoryHealthStatus> statuses) {
        int healthy = 0;
        int needsSplit = 0;
        int unbound = 0;
        for (WujinCategoryHealthStatus status : statuses) {
            if (status == WujinCategoryHealthStatus.HEALTHY) {
                healthy++;
            } else if (status == WujinCategoryHealthStatus.NEEDS_SPLIT) {
                needsSplit++;
            } else if (status == WujinCategoryHealthStatus.UNBOUND) {
                unbound++;
            }
        }
        return new WujinCategoryHealthSummary(healthy, needsSplit, unbound, statuses.size());
    }

    private boolean hasMapping(WujinCategoryNode node, List<WujinCategoryMappingRule> mappingRules) {
        if (mappingRules == null) {
            return false;
        }
        for (WujinCategoryMappingRule rule : mappingRules) {
            if (rule.matches(node.getId())) {
                return true;
            }
        }
        return false;
    }
}
