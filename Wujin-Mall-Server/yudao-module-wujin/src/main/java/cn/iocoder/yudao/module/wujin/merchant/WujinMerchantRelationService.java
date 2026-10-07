package cn.iocoder.yudao.module.wujin.merchant;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.search.WujinLane;

import java.util.ArrayList;
import java.util.List;

public class WujinMerchantRelationService {

    public void validateSubmission(WujinMerchantRelationSubmission submission) {
        if (submission.getProductLane() != WujinLane.PRODUCT) {
            throw new IllegalArgumentException("商家发布商品必须归属成品泳道");
        }
        if (!hasRelationType(submission, WujinRelationType.REQUIRES_MATERIAL)) {
            throw new IllegalArgumentException("成品上架必须至少绑定一个原材料类目");
        }
    }

    public WujinMerchantAuditDecision decideAuditRoute(WujinMerchantRelationSubmission submission, WujinIndustryTemplate template) {
        validateSubmission(submission);
        for (String materialEntityId : template.getRequiredMaterialEntityIds()) {
            if (!hasTemplateRelation(submission, materialEntityId, WujinRelationType.REQUIRES_MATERIAL)) {
                return new WujinMerchantAuditDecision(WujinMerchantAuditRoute.MANUAL_REVIEW,
                        "缺少平台模板必选材料：" + materialEntityId);
            }
        }
        for (String processEntityId : template.getRequiredProcessEntityIds()) {
            if (!hasTemplateRelation(submission, processEntityId, WujinRelationType.REQUIRES_PROCESS)) {
                return new WujinMerchantAuditDecision(WujinMerchantAuditRoute.MANUAL_REVIEW,
                        "缺少平台模板必选工艺：" + processEntityId);
            }
        }
        for (WujinMerchantRelationDraft relation : submission.getRelations()) {
            if (!relation.isFromTemplate()) {
                return new WujinMerchantAuditDecision(WujinMerchantAuditRoute.MANUAL_REVIEW,
                        "商家新增非模板关系，需运营审核");
            }
        }
        return new WujinMerchantAuditDecision(WujinMerchantAuditRoute.AUTO_APPROVE,
                "平台模板关系完整保留，可随商品审核自动生效");
    }

    public WujinMerchantCompletenessScore calculateCompleteness(WujinMerchantRelationSubmission submission) {
        int score = 0;
        List<String> missing = new ArrayList<>();
        if (hasRelationType(submission, WujinRelationType.REQUIRES_MATERIAL)) {
            score += 40;
        } else {
            missing.add("原材料");
        }
        if (hasRelationType(submission, WujinRelationType.REQUIRES_PROCESS)) {
            score += 25;
        } else {
            missing.add("加工工艺");
        }
        if (submission.getCertificationCount() > 0) {
            score += 20;
        } else {
            missing.add("认证信息");
        }
        if (submission.isHasApplicationDescription()) {
            score += 15;
        } else {
            missing.add("应用说明");
        }
        String suggestion = missing.isEmpty() ? "关系信息完整，可作为标杆候选" : "缺少" + String.join("、", missing);
        return new WujinMerchantCompletenessScore(score, score >= 90, suggestion);
    }

    private boolean hasRelationType(WujinMerchantRelationSubmission submission, WujinRelationType relationType) {
        for (WujinMerchantRelationDraft relation : submission.getRelations()) {
            if (relation.getRelationType() == relationType) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTemplateRelation(WujinMerchantRelationSubmission submission, String entityId, WujinRelationType relationType) {
        for (WujinMerchantRelationDraft relation : submission.getRelations()) {
            if (entityId.equals(relation.getEntityId())
                    && relation.getRelationType() == relationType
                    && relation.isFromTemplate()) {
                return true;
            }
        }
        return false;
    }
}
