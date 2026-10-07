package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuCreateReqDTO;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import cn.iocoder.yudao.module.wujin.merchant.WujinIndustryTemplate;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantAuditDecision;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantAuditRoute;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantCompletenessScore;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationDraft;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationService;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationSubmission;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class WujinMerchantRelationSubmitServiceImpl implements WujinMerchantRelationSubmitService {

    private static final int AUDIT_STATUS_WAIT_REVIEW = 10;
    private static final int AUDIT_STATUS_AUTO_APPROVED = 20;
    private static final int ENTITY_STATUS_ENABLED = 0;
    private static final int ENTITY_STATUS_DISABLED = 1;
    private static final int RELATION_AUDIT_STATUS_EFFECTIVE = 1;
    private static final int SUPPLY_STATUS_ENABLED = 0;
    private static final int SUPPLY_STATUS_DISABLED = 1;
    private static final Long DEFAULT_PRODUCT_BRAND_ID = 1L;
    private static final String DEFAULT_PRODUCT_PIC_URL = "/static/icons/local/search-empty.png";

    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinIndustryTemplateItemAdminService templateItemService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService chainRelationService;
    @Resource
    private ProductSpuApi productSpuApi;

    private final WujinMerchantRelationService relationService = new WujinMerchantRelationService();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WujinMerchantRelationSubmitRespVO submitRelation(WujinMerchantRelationSubmitReqVO reqVO) {
        reqVO.setMerchantId(resolveMerchantId(reqVO));
        Long productId = resolveProductId(reqVO);
        reqVO.setProductId(productId);

        WujinIndustryTemplateDO template = templateService.getTemplate(reqVO.getTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("商家关系申报模板不存在");
        }
        List<WujinIndustryTemplateItemDO> templateItems = getTemplateItems(reqVO.getTemplateId());
        if (templateItems.isEmpty()) {
            throw new IllegalArgumentException("行业模板尚未配置关系项");
        }
        normalizeCustomRelations(reqVO, template);

        List<WujinMerchantRelationDraft> relationDrafts = buildRelationDrafts(templateItems, reqVO.getCustomRelations());
        WujinMerchantRelationSubmission submission = WujinMerchantRelationSubmission.builder()
                .productId(String.valueOf(reqVO.getProductId()))
                .productName(reqVO.getProductName())
                .productLane(WujinLane.PRODUCT)
                .productCategoryId(reqVO.getProductCategoryId() == null ? null : String.valueOf(reqVO.getProductCategoryId()))
                .relations(relationDrafts)
                .build();
        submission.setCertificationCount(reqVO.getCertificationCount() == null ? 0 : reqVO.getCertificationCount());
        submission.setHasApplicationDescription(Boolean.TRUE.equals(reqVO.getHasApplicationDescription()));

        WujinIndustryTemplate domainTemplate = buildDomainTemplate(template, templateItems);
        WujinMerchantAuditDecision auditDecision = relationService.decideAuditRoute(submission, domainTemplate);
        WujinMerchantCompletenessScore completenessScore = relationService.calculateCompleteness(submission);
        Integer auditStatus = auditStatus(auditDecision.getRoute());

        Long submissionId = submissionService.createSubmission(submissionReq(reqVO, auditStatus, auditDecision, completenessScore));
        copyTemplateItems(submissionId, templateItems);
        createCustomItems(submissionId, reqVO.getCustomRelations());
        Long supplyEntityId = syncSupplyCapability(reqVO, template, auditDecision);
        if (auditDecision.getRoute() == WujinMerchantAuditRoute.AUTO_APPROVE) {
            createEffectiveRelations(submissionId, supplyEntityId, template);
        }

        WujinMerchantRelationSubmitRespVO respVO = new WujinMerchantRelationSubmitRespVO();
        respVO.setSubmissionId(submissionId);
        respVO.setProductId(productId);
        respVO.setAuditStatus(auditStatus);
        respVO.setAuditRoute(auditDecision.getRoute().name());
        respVO.setAuditReason(auditDecision.getReason());
        respVO.setCompletenessScore(completenessScore.getScore());
        respVO.setCompletenessSuggestion(completenessScore.getSuggestion());
        respVO.setCopiedTemplateItemCount(templateItems.size());
        return respVO;
    }

    private void normalizeCustomRelations(WujinMerchantRelationSubmitReqVO reqVO, WujinIndustryTemplateDO template) {
        if (reqVO.getCustomRelations() == null) {
            return;
        }
        for (WujinMerchantRelationSubmitReqVO.RelationItem item : reqVO.getCustomRelations()) {
            if (item.getEntityId() != null) {
                continue;
            }
            if (isBlank(item.getEntityName())) {
                throw new IllegalArgumentException("关联原材料或加工工艺不能为空");
            }
            item.setEntityId(createPendingCustomEntity(reqVO, template, item));
        }
    }

    private Long createPendingCustomEntity(WujinMerchantRelationSubmitReqVO reqVO, WujinIndustryTemplateDO template,
                                           WujinMerchantRelationSubmitReqVO.RelationItem item) {
        String entityCode = customEntityCode(reqVO, item);
        WujinChainEntityDO existing = getEntityByCode(entityCode);
        if (existing != null) {
            return existing.getId();
        }
        WujinChainEntitySaveReqVO entityReqVO = new WujinChainEntitySaveReqVO();
        entityReqVO.setEntityCode(entityCode);
        entityReqVO.setName(item.getEntityName().trim());
        entityReqVO.setLane(resolveEntityLane(item.getRelationType()));
        entityReqVO.setIndustries(template.getIndustryCode());
        entityReqVO.setJunctionFlag(false);
        entityReqVO.setRiskNote("商家发布自定义，待平台审核");
        entityReqVO.setStatus(ENTITY_STATUS_DISABLED);
        return entityService.createEntity(entityReqVO);
    }

    private WujinChainEntityDO getEntityByCode(String entityCode) {
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setEntityCode(entityCode);
        List<WujinChainEntityDO> entities = entityService.getEntityList(listReqVO);
        return entities.isEmpty() ? null : entities.get(0);
    }

    private String customEntityCode(WujinMerchantRelationSubmitReqVO reqVO,
                                    WujinMerchantRelationSubmitReqVO.RelationItem item) {
        String seed = reqVO.getMerchantId() + "_" + reqVO.getProductId() + "_" + item.getRelationType()
                + "_" + item.getEntityName().trim();
        return "MERCHANT_CUSTOM_" + Integer.toHexString(seed.hashCode()).toUpperCase();
    }

    private String resolveEntityLane(String relationType) {
        if (WujinRelationType.REQUIRES_PROCESS.name().equals(relationType)) {
            return WujinLane.PROCESS.name();
        }
        return WujinLane.MATERIAL.name();
    }

    private Long resolveMerchantId(WujinMerchantRelationSubmitReqVO reqVO) {
        if (reqVO.getMerchantId() != null) {
            return reqVO.getMerchantId();
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            throw new IllegalArgumentException("商家编号不能为空");
        }
        return loginUserId;
    }

    private Long resolveProductId(WujinMerchantRelationSubmitReqVO reqVO) {
        if (reqVO.getProductId() != null) {
            return reqVO.getProductId();
        }
        return productSpuApi.createSpu(productCreateReq(reqVO));
    }

    private ProductSpuCreateReqDTO productCreateReq(WujinMerchantRelationSubmitReqVO reqVO) {
        ProductSpuCreateReqDTO createReqDTO = new ProductSpuCreateReqDTO();
        createReqDTO.setName(reqVO.getProductName());
        createReqDTO.setKeyword(reqVO.getProductName());
        createReqDTO.setIntroduction("五金商城商家发布：" + reqVO.getProductName());
        createReqDTO.setDescription("五金商城商家发布商品，关联三泳道产业链关系。");
        createReqDTO.setCategoryId(reqVO.getProductCategoryId());
        createReqDTO.setBrandId(reqVO.getProductBrandId() == null ? DEFAULT_PRODUCT_BRAND_ID : reqVO.getProductBrandId());
        createReqDTO.setPicUrl(normalizeProductPicUrl(reqVO.getProductPicUrl()));
        createReqDTO.setPrice(defaultInt(reqVO.getProductPrice()));
        createReqDTO.setMarketPrice(defaultInt(reqVO.getProductMarketPrice()));
        createReqDTO.setCostPrice(defaultInt(reqVO.getProductCostPrice()));
        createReqDTO.setStock(defaultInt(reqVO.getProductStock()));
        return createReqDTO;
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeProductPicUrl(String value) {
        if (isBlank(value)) {
            return DEFAULT_PRODUCT_PIC_URL;
        }
        String trimmed = value.trim();
        return isBlockedPlaceholderUrl(trimmed) ? DEFAULT_PRODUCT_PIC_URL : trimmed;
    }

    private boolean isBlockedPlaceholderUrl(String value) {
        String lower = value.toLowerCase();
        return lower.matches("^(https?:)?//([^/]+\\.)?example\\.com([/:?#].*)?$")
                || lower.matches("^(https?:)?//([^/]+\\.)?placeholder\\.com([/:?#].*)?$")
                || lower.matches("^(https?:)?//via\\.placeholder\\.com([/:?#].*)?$");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private List<WujinIndustryTemplateItemDO> getTemplateItems(Long templateId) {
        WujinIndustryTemplateItemListReqVO listReqVO = new WujinIndustryTemplateItemListReqVO();
        listReqVO.setTemplateId(templateId);
        return templateItemService.getTemplateItemList(listReqVO);
    }

    private List<WujinMerchantRelationDraft> buildRelationDrafts(List<WujinIndustryTemplateItemDO> templateItems,
                                                                 List<WujinMerchantRelationSubmitReqVO.RelationItem> customRelations) {
        List<WujinMerchantRelationDraft> relationDrafts = new ArrayList<>();
        for (WujinIndustryTemplateItemDO item : templateItems) {
            relationDrafts.add(new WujinMerchantRelationDraft(String.valueOf(item.getEntityId()),
                    WujinRelationType.valueOf(item.getRelationType()), true));
        }
        if (customRelations != null) {
            for (WujinMerchantRelationSubmitReqVO.RelationItem item : customRelations) {
                relationDrafts.add(new WujinMerchantRelationDraft(String.valueOf(item.getEntityId()),
                        WujinRelationType.valueOf(item.getRelationType()), false));
            }
        }
        return relationDrafts;
    }

    private WujinIndustryTemplate buildDomainTemplate(WujinIndustryTemplateDO template,
                                                       List<WujinIndustryTemplateItemDO> templateItems) {
        List<String> materialEntityIds = new ArrayList<>();
        List<String> processEntityIds = new ArrayList<>();
        for (WujinIndustryTemplateItemDO item : templateItems) {
            if (!Boolean.TRUE.equals(item.getRequiredFlag())) {
                continue;
            }
            if (WujinRelationType.REQUIRES_MATERIAL.name().equals(item.getRelationType())) {
                materialEntityIds.add(String.valueOf(item.getEntityId()));
            } else if (WujinRelationType.REQUIRES_PROCESS.name().equals(item.getRelationType())) {
                processEntityIds.add(String.valueOf(item.getEntityId()));
            }
        }
        return new WujinIndustryTemplate(String.valueOf(template.getId()), materialEntityIds, processEntityIds);
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(WujinMerchantRelationSubmitReqVO reqVO,
                                                                   Integer auditStatus,
                                                                   WujinMerchantAuditDecision auditDecision,
                                                                   WujinMerchantCompletenessScore completenessScore) {
        WujinMerchantRelationSubmissionSaveReqVO saveReqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        saveReqVO.setMerchantId(reqVO.getMerchantId());
        saveReqVO.setProductId(reqVO.getProductId());
        saveReqVO.setProductName(reqVO.getProductName());
        saveReqVO.setProductLane(WujinLane.PRODUCT.name());
        saveReqVO.setProductCategoryId(reqVO.getProductCategoryId());
        saveReqVO.setTemplateId(reqVO.getTemplateId());
        saveReqVO.setAuditStatus(auditStatus);
        saveReqVO.setAuditRoute(auditDecision.getRoute().name());
        saveReqVO.setCompletenessScore(completenessScore.getScore());
        saveReqVO.setRemark(auditDecision.getReason());
        return saveReqVO;
    }

    private void copyTemplateItems(Long submissionId, List<WujinIndustryTemplateItemDO> templateItems) {
        for (WujinIndustryTemplateItemDO item : templateItems) {
            WujinMerchantRelationItemSaveReqVO itemReqVO = new WujinMerchantRelationItemSaveReqVO();
            itemReqVO.setSubmissionId(submissionId);
            itemReqVO.setEntityId(item.getEntityId());
            itemReqVO.setRelationType(item.getRelationType());
            itemReqVO.setFromTemplate(true);
            itemReqVO.setRequiredFlag(item.getRequiredFlag());
            itemReqVO.setRemark(item.getRemark());
            itemService.createItem(itemReqVO);
        }
    }

    private void createCustomItems(Long submissionId, List<WujinMerchantRelationSubmitReqVO.RelationItem> customRelations) {
        if (customRelations == null) {
            return;
        }
        for (WujinMerchantRelationSubmitReqVO.RelationItem item : customRelations) {
            WujinMerchantRelationItemSaveReqVO itemReqVO = new WujinMerchantRelationItemSaveReqVO();
            itemReqVO.setSubmissionId(submissionId);
            itemReqVO.setEntityId(item.getEntityId());
            itemReqVO.setRelationType(item.getRelationType());
            itemReqVO.setFromTemplate(false);
            itemReqVO.setRequiredFlag(Boolean.TRUE.equals(item.getRequiredFlag()));
            itemReqVO.setRemark(item.getRemark());
            itemService.createItem(itemReqVO);
        }
    }

    private Long syncSupplyCapability(WujinMerchantRelationSubmitReqVO reqVO, WujinIndustryTemplateDO template,
                                      WujinMerchantAuditDecision auditDecision) {
        int supplyStatus = auditDecision.getRoute() == WujinMerchantAuditRoute.AUTO_APPROVE
                ? SUPPLY_STATUS_ENABLED : SUPPLY_STATUS_DISABLED;
        WujinChainEntityDO supplyEntity = resolveSupplyEntity(reqVO, template);
        capabilityService.saveOrUpdateCapability(capabilityReq(reqVO, template, supplyEntity, supplyStatus));
        return supplyEntity.getId();
    }

    private WujinChainEntityDO resolveSupplyEntity(WujinMerchantRelationSubmitReqVO reqVO,
                                                   WujinIndustryTemplateDO template) {
        if (reqVO.getSupplyEntityId() != null) {
            WujinChainEntityDO entity = entityService.getEntity(reqVO.getSupplyEntityId());
            if (entity == null || !Integer.valueOf(ENTITY_STATUS_ENABLED).equals(entity.getStatus())) {
                throw new IllegalArgumentException("商品供应内容不存在或未启用");
            }
            return entity;
        }

        String entityCode = "PRODUCT_" + reqVO.getProductId();
        WujinChainEntityDO existing = getEntityByCode(entityCode);
        if (existing != null) {
            return existing;
        }
        WujinChainEntitySaveReqVO entityReqVO = new WujinChainEntitySaveReqVO();
        entityReqVO.setEntityCode(entityCode);
        entityReqVO.setName(reqVO.getProductName());
        entityReqVO.setLane(WujinLane.PRODUCT.name());
        entityReqVO.setIndustries(template.getIndustryCode());
        entityReqVO.setJunctionFlag(false);
        entityReqVO.setRiskNote("商品发布时自动创建的供应实体");
        entityReqVO.setStatus(ENTITY_STATUS_ENABLED);
        return entityService.getEntity(entityService.createEntity(entityReqVO));
    }

    private void createEffectiveRelations(Long submissionId, Long sourceEntityId,
                                          WujinIndustryTemplateDO template) {
        WujinMerchantRelationItemListReqVO itemReqVO = new WujinMerchantRelationItemListReqVO();
        itemReqVO.setSubmissionId(submissionId);
        for (WujinMerchantRelationItemDO item : itemService.getItemList(itemReqVO)) {
            WujinChainEntityRelationListReqVO existingReqVO = new WujinChainEntityRelationListReqVO();
            existingReqVO.setSourceEntityId(sourceEntityId);
            existingReqVO.setTargetEntityId(item.getEntityId());
            existingReqVO.setRelationType(item.getRelationType());
            existingReqVO.setAuditStatus(RELATION_AUDIT_STATUS_EFFECTIVE);
            if (!chainRelationService.getRelationList(existingReqVO).isEmpty()) {
                continue;
            }
            WujinChainEntityRelationSaveReqVO relationReqVO = new WujinChainEntityRelationSaveReqVO();
            relationReqVO.setSourceEntityId(sourceEntityId);
            relationReqVO.setTargetEntityId(item.getEntityId());
            relationReqVO.setRelationType(item.getRelationType());
            relationReqVO.setWeight(Boolean.TRUE.equals(item.getRequiredFlag()) ? 80 : 50);
            relationReqVO.setIndustryContext(template.getIndustryCode());
            relationReqVO.setAuditStatus(RELATION_AUDIT_STATUS_EFFECTIVE);
            relationReqVO.setAuditRemark("模板关系自动审核通过");
            chainRelationService.createRelation(relationReqVO);
        }
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(WujinMerchantRelationSubmitReqVO reqVO,
                                                                 WujinIndustryTemplateDO template,
                                                                 WujinChainEntityDO entity,
                                                                 Integer supplyStatus) {
        WujinMerchantSupplyCapabilitySaveReqVO saveReqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        saveReqVO.setMerchantId(reqVO.getMerchantId());
        saveReqVO.setProductId(reqVO.getProductId());
        saveReqVO.setProductName(reqVO.getProductName());
        saveReqVO.setEntityId(entity.getId());
        saveReqVO.setLane(entity.getLane());
        saveReqVO.setIndustry(template.getIndustryCode());
        saveReqVO.setSupplyStatus(supplyStatus);
        saveReqVO.setStockCount(reqVO.getProductStock());
        saveReqVO.setMinOrderQuantity(reqVO.getSupplyMinOrderQuantity());
        saveReqVO.setDeliveryDays(reqVO.getSupplyDeliveryDays());
        saveReqVO.setServiceArea(reqVO.getSupplyServiceArea());
        saveReqVO.setRemark(isBlank(reqVO.getSupplyRemark())
                ? "商品发布自动同步：" + entity.getName()
                : reqVO.getSupplyRemark().trim());
        return saveReqVO;
    }

    private Integer auditStatus(WujinMerchantAuditRoute route) {
        return route == WujinMerchantAuditRoute.AUTO_APPROVE ? AUDIT_STATUS_AUTO_APPROVED : AUDIT_STATUS_WAIT_REVIEW;
    }
}
