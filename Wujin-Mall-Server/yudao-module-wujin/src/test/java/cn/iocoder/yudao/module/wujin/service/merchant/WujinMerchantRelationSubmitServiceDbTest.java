package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuCreateReqDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantAuditRoute;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionarySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminService;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinIndustryTemplateItemAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinMerchantRelationItemAdminServiceImpl.class,
        WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinAttributeDictionaryAdminServiceImpl.class,
        WujinProductAttributeServiceImpl.class,
        WujinMerchantRelationSubmitServiceImpl.class})
class WujinMerchantRelationSubmitServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
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
    private WujinMerchantRelationSubmitService submitService;
    @Resource
    private WujinAttributeDictionaryAdminService attributeDictionaryService;
    @Resource
    private WujinProductAttributeService productAttributeService;
    @MockBean
    private ProductSpuApi productSpuApi;

    @Test
    void submitProductPublishPersistsStandardAttributesAndPendingCustomTags() {
        Long templateId = createTireTemplate("ATTR");
        attributeDictionaryService.createAttribute(attributeReq("SPEC", "规格型号", "TEXT", true));
        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setStandardAttributes(Arrays.asList(standardAttribute("规格型号", "205/55R16"),
                standardAttribute("产地", "山东")));
        reqVO.setCustomTags(Arrays.asList("静音", "耐磨", "静音"));
        reqVO.setCustomTagReviewNote("第三方检测报告已上传");

        WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(reqVO);

        assertEquals(2, result.getStandardAttributeCount());
        assertEquals(2, result.getPendingCustomTagCount());
        List<WujinProductAttributeValueDO> values = productAttributeService.getAttributeValueList(
                result.getSubmissionId(), null);
        assertEquals("SPEC", values.get(0).getAttributeCode());
        assertEquals("205/55R16", values.get(0).getAttributeValue());
        WujinProductCustomTagListReqVO tagListReqVO = new WujinProductCustomTagListReqVO();
        tagListReqVO.setSubmissionId(result.getSubmissionId());
        assertEquals(2, productAttributeService.getCustomTagList(tagListReqVO).size());
        assertEquals("第三方检测报告已上传",
                productAttributeService.getCustomTagList(tagListReqVO).get(0).getReviewNote());
    }

    @Test
    void submitProductPublishRejectsMissingRequiredStandardAttributeBeforeCreatingSpu() {
        Long templateId = createTireTemplate("ATTR_REQUIRED");
        attributeDictionaryService.createAttribute(attributeReq("SPEC", "规格型号", "TEXT", true));
        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setProductId(null);
        reqVO.setStandardAttributes(Collections.singletonList(standardAttribute("产地", "山东")));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submitService.submitRelation(reqVO));
        assertEquals("标准属性「规格型号」为必填项", exception.getMessage());
        verify(productSpuApi, never()).createSpu(any(ProductSpuCreateReqDTO.class));
    }

    @Test
    void submitRelationWithoutStandardAttributesSkipsDictionaryValidation() {
        Long templateId = createTireTemplate("ATTR_SKIP");
        attributeDictionaryService.createAttribute(attributeReq("SPEC", "规格型号", "TEXT", true));

        WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(submitReq(templateId));

        assertEquals(0, result.getStandardAttributeCount());
        assertEquals(0, result.getPendingCustomTagCount());
    }

    private Long createTireTemplate(String suffix) {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_" + suffix, "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_" + suffix, "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_" + suffix, "轮胎属性模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));
        return templateId;
    }

    private WujinAttributeDictionarySaveReqVO attributeReq(String code, String name, String valueType, boolean required) {
        WujinAttributeDictionarySaveReqVO reqVO = new WujinAttributeDictionarySaveReqVO();
        reqVO.setCode(code);
        reqVO.setName(name);
        reqVO.setGroupName("成品属性");
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setValueType(valueType);
        reqVO.setRequiredFlag(required);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinMerchantRelationSubmitReqVO.StandardAttribute standardAttribute(String name, String value) {
        WujinMerchantRelationSubmitReqVO.StandardAttribute attribute = new WujinMerchantRelationSubmitReqVO.StandardAttribute();
        attribute.setName(name);
        attribute.setValue(value);
        return attribute;
    }

    @Test
    void submitRelationCreatesSubmissionCopiesTemplateItemsAndComputesRoute() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));

        WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(submitReq(templateId));

        WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(result.getSubmissionId());
        assertEquals(1001L, submission.getMerchantId());
        assertEquals(2001L, submission.getProductId());
        assertEquals(WujinLane.PRODUCT.name(), submission.getProductLane());
        assertEquals(20, submission.getAuditStatus());
        assertEquals(WujinMerchantAuditRoute.AUTO_APPROVE.name(), submission.getAuditRoute());
        assertEquals(100, submission.getCompletenessScore());
        assertEquals("平台模板关系完整保留，可随商品审核自动生效", submission.getRemark());

        WujinMerchantRelationItemListReqVO itemListReqVO = new WujinMerchantRelationItemListReqVO();
        itemListReqVO.setSubmissionId(result.getSubmissionId());
        List<WujinMerchantRelationItemDO> items = itemService.getItemList(itemListReqVO);
        assertEquals(2, items.size());
        assertEquals(2, result.getCopiedTemplateItemCount());
        assertEquals(WujinMerchantAuditRoute.AUTO_APPROVE.name(), result.getAuditRoute());
        assertEquals(100, result.getCompletenessScore());
        assertEquals("关系信息完整，可作为标杆候选", result.getCompletenessSuggestion());
    }

    @Test
    void submitAutoApprovedRelationSyncsSupplyCapabilitiesWithoutDuplicates() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_CAP", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_CAP", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_CAP", "轮胎供应能力模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));

        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setProductStock(60);
        reqVO.setSupplyMinOrderQuantity(10);
        reqVO.setSupplyDeliveryDays(3);
        reqVO.setSupplyServiceArea("全国");
        reqVO.setSupplyRemark("支持定制包装，现货当天发出");
        submitService.submitRelation(reqVO);
        submitService.submitRelation(reqVO);

        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setMerchantId(1001L);
        listReqVO.setProductId(2001L);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(listReqVO);
        assertEquals(1, capabilities.size());
        assertEquals(1, capabilities.stream()
                .filter(capability -> Integer.valueOf(60).equals(capability.getStockCount())
                        && Integer.valueOf(10).equals(capability.getMinOrderQuantity())
                        && Integer.valueOf(3).equals(capability.getDeliveryDays())
                        && "全国".equals(capability.getServiceArea())
                        && "支持定制包装，现货当天发出".equals(capability.getRemark()))
                .count());
        assertEquals(1, capabilities.stream()
                .filter(capability -> WujinLane.PRODUCT.name().equals(capability.getLane())
                        && Integer.valueOf(0).equals(capability.getSupplyStatus())
                        && "TIRE_RUBBER".equals(capability.getIndustry()))
                .count());
    }

    @Test
    void submitAutoApprovedRelationKeepsMerchantMaintainedSupplyFields() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_KEEP", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_KEEP", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_KEEP", "轮胎保留能力模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));
        capabilityService.createCapability(capabilityReq(materialEntityId));

        submitService.submitRelation(submitReq(templateId));

        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setMerchantId(1001L);
        listReqVO.setProductId(2001L);
        listReqVO.setEntityId(materialEntityId);
        WujinMerchantSupplyCapabilityDO capability = capabilityService.getCapabilityList(listReqVO).get(0);
        assertEquals(88, capability.getStockCount());
        assertEquals(6, capability.getMinOrderQuantity());
        assertEquals(4, capability.getDeliveryDays());
        assertEquals("华东", capability.getServiceArea());
    }

    @Test
    void submitProductPublishCreatesStandardSpuWhenProductIdMissing() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_SPU", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_SPU", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_SPU", "轮胎商城商品模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));
        when(productSpuApi.createSpu(org.mockito.ArgumentMatchers.any(ProductSpuCreateReqDTO.class))).thenReturn(91001L);

        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setProductId(null);
        WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(reqVO);

        assertEquals(91001L, result.getProductId());
        WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(result.getSubmissionId());
        assertEquals(91001L, submission.getProductId());
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setMerchantId(1001L);
        listReqVO.setProductId(91001L);
        assertEquals(1, capabilityService.getCapabilityList(listReqVO).size());

        ArgumentCaptor<ProductSpuCreateReqDTO> captor = ArgumentCaptor.forClass(ProductSpuCreateReqDTO.class);
        verify(productSpuApi).createSpu(captor.capture());
        ProductSpuCreateReqDTO createReqDTO = captor.getValue();
        assertEquals("高耐磨乘用车轮胎", createReqDTO.getName());
        assertEquals(3001L, createReqDTO.getCategoryId());
        assertEquals("高耐磨乘用车轮胎", createReqDTO.getKeyword());
        assertEquals("五金商城商家发布：高耐磨乘用车轮胎", createReqDTO.getIntroduction());
        assertFalse(createReqDTO.getPicUrl().contains("example.com"));
    }

    @Test
    void submitRelationUsesLoginUserWhenMerchantIdMissing() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_LOGIN", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_LOGIN", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_LOGIN", "轮胎登录商家模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));
        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setMerchantId(null);
        LoginUser loginUser = new LoginUser();
        loginUser.setId(88001L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                loginUser, null, Collections.emptyList()));

        try {
            WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(reqVO);

            WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(result.getSubmissionId());
            assertEquals(88001L, submission.getMerchantId());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void submitRelationCreatesPendingEntityForCustomMaterialOrProcessName() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER_CUSTOM", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE_CUSTOM", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE_CUSTOM", "轮胎自定义关系模板", "TIRE_RUBBER"));
        templateItemService.createTemplateItem(templateItemReq(templateId, materialEntityId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true, 10));
        templateItemService.createTemplateItem(templateItemReq(templateId, processEntityId,
                WujinRelationType.REQUIRES_PROCESS.name(), true, 20));
        WujinMerchantRelationSubmitReqVO.RelationItem customItem = new WujinMerchantRelationSubmitReqVO.RelationItem();
        customItem.setEntityName("高耐磨芳纶帘线");
        customItem.setRelationType(WujinRelationType.REQUIRES_MATERIAL.name());
        customItem.setRequiredFlag(false);
        customItem.setRemark("轮胎骨架增强材料");
        WujinMerchantRelationSubmitReqVO reqVO = submitReq(templateId);
        reqVO.setCustomRelations(Collections.singletonList(customItem));
        reqVO.setProductStock(45);
        reqVO.setSupplyMinOrderQuantity(5);
        reqVO.setSupplyDeliveryDays(7);
        reqVO.setSupplyServiceArea("华东");

        WujinMerchantRelationSubmitRespVO result = submitService.submitRelation(reqVO);
        assertEquals(WujinMerchantAuditRoute.MANUAL_REVIEW.name(), result.getAuditRoute());

        WujinMerchantRelationItemListReqVO itemListReqVO = new WujinMerchantRelationItemListReqVO();
        itemListReqVO.setSubmissionId(result.getSubmissionId());
        List<WujinMerchantRelationItemDO> items = itemService.getItemList(itemListReqVO);
        WujinMerchantRelationItemDO customRelation = items.stream()
                .filter(item -> !Boolean.TRUE.equals(item.getFromTemplate()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("custom relation item missing"));
        assertEquals(WujinRelationType.REQUIRES_MATERIAL.name(), customRelation.getRelationType());
        assertEquals(false, customRelation.getRequiredFlag());
        assertEquals("轮胎骨架增强材料", customRelation.getRemark());

        assertEquals("高耐磨芳纶帘线", entityService.getEntity(customRelation.getEntityId()).getName());
        assertEquals(WujinLane.MATERIAL.name(), entityService.getEntity(customRelation.getEntityId()).getLane());
        assertEquals(1, entityService.getEntity(customRelation.getEntityId()).getStatus());

        WujinMerchantSupplyCapabilityListReqVO capabilityListReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        capabilityListReqVO.setProductId(2001L);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(capabilityListReqVO);
        assertEquals(1, capabilities.size());
        assertEquals(1, capabilities.stream()
                .filter(capability -> Integer.valueOf(1).equals(capability.getSupplyStatus())
                        && Integer.valueOf(45).equals(capability.getStockCount())
                        && Integer.valueOf(5).equals(capability.getMinOrderQuantity())
                        && Integer.valueOf(7).equals(capability.getDeliveryDays())
                        && "华东".equals(capability.getServiceArea()))
                .count());
    }

    private WujinMerchantRelationSubmitReqVO submitReq(Long templateId) {
        WujinMerchantRelationSubmitReqVO reqVO = new WujinMerchantRelationSubmitReqVO();
        reqVO.setMerchantId(1001L);
        reqVO.setProductId(2001L);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setCertificationCount(2);
        reqVO.setHasApplicationDescription(true);
        return reqVO;
    }

    private WujinIndustryTemplateItemSaveReqVO templateItemReq(Long templateId, Long entityId, String relationType,
                                                               Boolean requiredFlag, Integer sort) {
        WujinIndustryTemplateItemSaveReqVO reqVO = new WujinIndustryTemplateItemSaveReqVO();
        reqVO.setTemplateId(templateId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(relationType);
        reqVO.setRequiredFlag(requiredFlag);
        reqVO.setSort(sort);
        reqVO.setWeight(80);
        reqVO.setRemark("模板关系");
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq(String templateCode, String name, String industryCode) {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode(templateCode);
        reqVO.setName(name);
        reqVO.setIndustryCode(industryCode);
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries("TIRE_RUBBER");
        reqVO.setStatus(0);
        reqVO.setJunctionFlag(false);
        return reqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(Long entityId) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(1001L);
        reqVO.setProductId(2001L);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setEntityId(entityId);
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("TIRE_RUBBER");
        reqVO.setSupplyStatus(0);
        reqVO.setStockCount(88);
        reqVO.setMinOrderQuantity(6);
        reqVO.setDeliveryDays(4);
        reqVO.setServiceArea("华东");
        reqVO.setRemark("商家维护");
        return reqVO;
    }
}
