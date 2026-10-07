package cn.iocoder.yudao.module.wujin.search;

import java.util.Comparator;
import java.util.List;

public class WujinSearchDecisionService {

    public WujinSearchDecision decide(WujinSearchDecisionRequest request) {
        WujinLane lane = resolveLane(request);
        int granularity = resolveGranularity(request, lane);
        boolean riskWarningRequired = hasRiskWarning(request);
        String explanation = riskWarningRequired ? "该材料跨多个行业使用，请确认应用场景后查看供应商"
                : resolveExplanation(request, lane);
        return new WujinSearchDecision(lane, granularity, explanation, resolveContextHint(request), riskWarningRequired);
    }

    public int adaptGranularity(int childCount, int requestedLevel) {
        if (childCount > 50 && requestedLevel > 2) {
            return 2;
        }
        if (childCount < 5 && requestedLevel == 1) {
            return 2;
        }
        return requestedLevel;
    }

    public WujinCategoryMapping resolveCategoryConflict(List<WujinCategoryMapping> mappings, List<WujinLane> contextLanes) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        if (contextLanes != null) {
            for (WujinCategoryMapping mapping : mappings) {
                if (contextLanes.contains(mapping.getLane())) {
                    return mapping;
                }
            }
        }
        return mappings.stream()
                .max(Comparator.comparingInt(WujinCategoryMapping::getWeight))
                .orElse(null);
    }

    private WujinLane resolveLane(WujinSearchDecisionRequest request) {
        if (request.getRequestedLane() != null) {
            return request.getRequestedLane();
        }
        if (request.getIntent() == WujinSearchIntent.MATERIAL) {
            return WujinLane.MATERIAL;
        }
        if (request.getIntent() == WujinSearchIntent.PROCESS) {
            return WujinLane.PROCESS;
        }
        return WujinLane.PRODUCT;
    }

    private int resolveGranularity(WujinSearchDecisionRequest request, WujinLane lane) {
        if (request.getEntryPath() == WujinEntryPath.UPSTREAM_JUMP
                && request.getSourceLane() == WujinLane.PRODUCT
                && lane == WujinLane.MATERIAL) {
            return adaptGranularity(request.getChildCount(), 1);
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.PRODUCT) {
            return adaptGranularity(request.getChildCount(), 3);
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.MATERIAL) {
            return adaptGranularity(request.getChildCount(), 3);
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.PROCESS) {
            return adaptGranularity(request.getChildCount(), 2);
        }
        return adaptGranularity(request.getChildCount(), lane == WujinLane.PRODUCT ? 3 : 2);
    }

    private boolean hasRiskWarning(WujinSearchDecisionRequest request) {
        return request.getRiskTags().contains("不可互换")
                || request.getRiskTags().contains("高风险采购提醒")
                || request.getRiskTags().contains("多行业适用")
                || "医疗器械".equals(request.getIndustry());
    }

    private String resolveExplanation(WujinSearchDecisionRequest request, WujinLane lane) {
        if (request.getEntryPath() == WujinEntryPath.UPSTREAM_JUMP
                && request.getSourceLane() == WujinLane.PRODUCT
                && lane == WujinLane.MATERIAL) {
            return "系统判定：您从" + request.getSourceKeyword() + "查看所需材料，先展示材料大类，便于快速浏览";
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.PRODUCT) {
            return "系统判定：您正在查找成品规格，优先展示最细粒度结果";
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.MATERIAL) {
            return "系统判定：您把“" + request.getKeyword() + "”当作具体采购对象，展示规格型号";
        }
        if (request.getEntryPath() == WujinEntryPath.DIRECT_SEARCH && lane == WujinLane.PROCESS) {
            return "系统判定：您正在查找加工工艺，展示工艺能力和适用行业";
        }
        return "系统判定：根据当前上下文展示" + laneName(lane) + "结果";
    }

    private String resolveContextHint(WujinSearchDecisionRequest request) {
        if (request.getSourceKeyword() == null || request.getSourceKeyword().trim().isEmpty()) {
            return null;
        }
        return "来自：" + request.getSourceKeyword() + " → " + request.getKeyword();
    }

    private String laneName(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "加工";
        }
        if (lane == WujinLane.MATERIAL) {
            return "原材料";
        }
        return "成品";
    }
}
