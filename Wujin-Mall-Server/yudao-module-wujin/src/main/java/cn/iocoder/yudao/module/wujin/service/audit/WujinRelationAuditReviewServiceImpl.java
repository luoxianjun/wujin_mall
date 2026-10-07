package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditReason;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.supply.WujinMerchantSupplyCapabilityMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinRelationAuditReviewServiceImpl implements WujinRelationAuditReviewService {

    private static final int AUDIT_STATUS_APPROVED = 30;
    private static final int AUDIT_STATUS_REJECTED = 40;
    private static final int RELATION_AUDIT_STATUS_EFFECTIVE = 1;
    private static final int SUPPLY_STATUS_ENABLED = 0;

    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantSupplyCapabilityMapper capabilityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WujinRelationAuditReviewRespVO review(WujinRelationAuditReviewReqVO reqVO) {
        WujinAuditAction action = WujinAuditAction.valueOf(reqVO.getAction());
        WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(reqVO.getSubmissionId());
        if (submission == null) {
            throw new IllegalArgumentException("审核记录关联申报不存在");
        }

        boolean approved = action == WujinAuditAction.APPROVE;
        Long productEntityId = null;
        int effectiveRelationCount = 0;
        if (approved) {
            productEntityId = resolveSupplyEntity(submission);
            effectiveRelationCount = createEffectiveRelations(submission, productEntityId, reqVO.getComment());
        }

        Long auditRecordId = auditRecordService.createAuditRecord(auditRecordReq(reqVO, action, approved));
        updateSubmissionStatus(submission, approved);
        if (approved) {
            capabilityMapper.updateSupplyStatusByProductId(submission.getProductId(), SUPPLY_STATUS_ENABLED);
        }

        WujinRelationAuditReviewRespVO respVO = new WujinRelationAuditReviewRespVO();
        respVO.setSubmissionId(submission.getId());
        respVO.setAuditRecordId(auditRecordId);
        respVO.setAction(action.name());
        respVO.setReason(approved ? WujinAuditReason.REVIEW_APPROVED.name() : WujinAuditReason.REVIEW_REJECTED.name());
        respVO.setEffectiveFlag(approved);
        respVO.setProductEntityId(productEntityId);
        respVO.setEffectiveRelationCount(effectiveRelationCount);
        return respVO;
    }

    private Long ensureProductEntity(WujinMerchantRelationSubmissionDO submission) {
        String entityCode = productEntityCode(submission.getProductId());
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setEntityCode(entityCode);
        List<WujinChainEntityDO> existing = entityService.getEntityList(listReqVO);
        if (!existing.isEmpty()) {
            return existing.get(0).getId();
        }

        WujinIndustryTemplateDO template = templateService.getTemplate(submission.getTemplateId());
        WujinChainEntitySaveReqVO entityReqVO = new WujinChainEntitySaveReqVO();
        entityReqVO.setEntityCode(entityCode);
        entityReqVO.setName(submission.getProductName());
        entityReqVO.setLane(WujinLane.PRODUCT.name());
        entityReqVO.setIndustries(template == null ? null : template.getIndustryCode());
        entityReqVO.setJunctionFlag(false);
        entityReqVO.setStatus(0);
        entityReqVO.setRiskNote("商家商品审核通过后自动入网");
        return entityService.createEntity(entityReqVO);
    }

    private Long resolveSupplyEntity(WujinMerchantRelationSubmissionDO submission) {
        for (WujinMerchantSupplyCapabilityDO capability
                : capabilityMapper.selectListByProductId(submission.getProductId())) {
            WujinChainEntityDO entity = entityService.getEntity(capability.getEntityId());
            if (entity != null) {
                return entity.getId();
            }
        }
        return ensureProductEntity(submission);
    }

    private int createEffectiveRelations(WujinMerchantRelationSubmissionDO submission, Long productEntityId, String comment) {
        WujinMerchantRelationItemListReqVO listReqVO = new WujinMerchantRelationItemListReqVO();
        listReqVO.setSubmissionId(submission.getId());
        List<WujinMerchantRelationItemDO> items = itemService.getItemList(listReqVO);

        int count = 0;
        WujinIndustryTemplateDO template = templateService.getTemplate(submission.getTemplateId());
        for (WujinMerchantRelationItemDO item : items) {
            WujinChainEntityRelationSaveReqVO relationReqVO = new WujinChainEntityRelationSaveReqVO();
            relationReqVO.setSourceEntityId(productEntityId);
            relationReqVO.setTargetEntityId(item.getEntityId());
            relationReqVO.setRelationType(item.getRelationType());
            relationReqVO.setWeight(Boolean.TRUE.equals(item.getRequiredFlag()) ? 80 : 50);
            relationReqVO.setCostRatio(null);
            relationReqVO.setIndustryContext(template == null ? null : template.getIndustryCode());
            relationReqVO.setAuditStatus(RELATION_AUDIT_STATUS_EFFECTIVE);
            relationReqVO.setAuditRemark(comment);
            relationService.createRelation(relationReqVO);
            count++;
        }
        return count;
    }

    private WujinRelationAuditRecordSaveReqVO auditRecordReq(WujinRelationAuditReviewReqVO reqVO, WujinAuditAction action,
                                                             boolean approved) {
        WujinRelationAuditRecordSaveReqVO recordReqVO = new WujinRelationAuditRecordSaveReqVO();
        recordReqVO.setSubmissionId(reqVO.getSubmissionId());
        recordReqVO.setAuditorId(reqVO.getAuditorId());
        recordReqVO.setAction(action.name());
        recordReqVO.setReason(approved ? WujinAuditReason.REVIEW_APPROVED.name() : WujinAuditReason.REVIEW_REJECTED.name());
        recordReqVO.setComment(reqVO.getComment());
        recordReqVO.setEffectiveFlag(approved);
        return recordReqVO;
    }

    private void updateSubmissionStatus(WujinMerchantRelationSubmissionDO submission, boolean approved) {
        WujinMerchantRelationSubmissionSaveReqVO updateReqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        updateReqVO.setId(submission.getId());
        updateReqVO.setMerchantId(submission.getMerchantId());
        updateReqVO.setProductId(submission.getProductId());
        updateReqVO.setProductName(submission.getProductName());
        updateReqVO.setProductLane(submission.getProductLane());
        updateReqVO.setProductCategoryId(submission.getProductCategoryId());
        updateReqVO.setTemplateId(submission.getTemplateId());
        updateReqVO.setAuditStatus(approved ? AUDIT_STATUS_APPROVED : AUDIT_STATUS_REJECTED);
        updateReqVO.setAuditRoute(submission.getAuditRoute());
        updateReqVO.setCompletenessScore(submission.getCompletenessScore());
        updateReqVO.setRemark(submission.getRemark());
        submissionService.updateSubmission(updateReqVO);
    }

    private String productEntityCode(Long productId) {
        return "PRODUCT_" + productId;
    }
}
