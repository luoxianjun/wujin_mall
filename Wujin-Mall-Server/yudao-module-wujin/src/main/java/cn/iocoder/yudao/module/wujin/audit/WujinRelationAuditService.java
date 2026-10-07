package cn.iocoder.yudao.module.wujin.audit;

import cn.iocoder.yudao.module.wujin.chain.WujinRiskWarning;
import cn.iocoder.yudao.module.wujin.search.WujinLane;

public class WujinRelationAuditService {

    private static final int SIMILAR_ENTITY_THRESHOLD = 90;

    public WujinRelationAuditDecision precheck(WujinRelationAuditRequest request) {
        if (request.getDeclaredProductLane() != WujinLane.PRODUCT) {
            return new WujinRelationAuditDecision(WujinAuditAction.BLOCK,
                    WujinAuditReason.CATEGORY_CONFLICT,
                    "商品上架记录必须归属成品泳道");
        }

        WujinRiskWarning riskWarning = request.getRiskWarning();
        if (riskWarning.isRequired()) {
            return new WujinRelationAuditDecision(WujinAuditAction.BLOCK,
                    WujinAuditReason.INDUSTRY_STANDARD_CONFLICT,
                    riskWarning.getMessage());
        }

        WujinSimilarEntity similarEntity = findBestSimilarEntity(request);
        if (similarEntity != null) {
            return new WujinRelationAuditDecision(WujinAuditAction.SUGGEST_MERGE,
                    WujinAuditReason.SIMILAR_ENTITY_FOUND,
                    "建议修正为现有实体：" + similarEntity.getName()
                            + "(" + similarEntity.getEntityId() + ")，相似度 "
                            + similarEntity.getSimilarity() + "%");
        }

        return new WujinRelationAuditDecision(WujinAuditAction.MANUAL_REVIEW,
                WujinAuditReason.PRECHECK_PASSED,
                "系统初筛通过，进入运营审核");
    }

    public WujinRelationAuditDecision review(WujinAuditReviewCommand command) {
        if (command.getAction() == WujinAuditAction.APPROVE) {
            return new WujinRelationAuditDecision(WujinAuditAction.APPROVE,
                    WujinAuditReason.REVIEW_APPROVED,
                    "审核通过，关系生效入网：" + command.getComment());
        }
        return new WujinRelationAuditDecision(WujinAuditAction.REJECT,
                WujinAuditReason.REVIEW_REJECTED,
                "驳回补充信息：" + command.getComment());
    }

    private WujinSimilarEntity findBestSimilarEntity(WujinRelationAuditRequest request) {
        WujinSimilarEntity best = null;
        for (WujinSimilarEntity similarEntity : request.getSimilarEntities()) {
            if (similarEntity.getSimilarity() < SIMILAR_ENTITY_THRESHOLD) {
                continue;
            }
            if (best == null || similarEntity.getSimilarity() > best.getSimilarity()) {
                best = similarEntity;
            }
        }
        return best;
    }
}
