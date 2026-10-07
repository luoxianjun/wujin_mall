package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinMerchantRelationItemAdminServiceImpl.class,
        WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinProductSpuStatusSyncServiceImpl.class})
class WujinProductSpuStatusSyncServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinProductSpuStatusSyncService spuStatusSyncService;

    @Test
    void syncProductStatusDisablesCapabilitiesWhenSpuLeavesShelf() {
        Long productId = 9001L;
        Long entityId = entityService.createEntity(entityReq("M_RUBBER_STATUS_OFF", "天然橡胶"));
        capabilityService.createCapability(capabilityReq(3001L, productId, "高耐磨乘用车轮胎", entityId, 0));
        capabilityService.createCapability(capabilityReq(3002L, productId, "高耐磨乘用车轮胎", entityId, 0));
        capabilityService.createCapability(capabilityReq(3003L, 9002L, "其它商品", entityId, 0));

        int updatedCount = spuStatusSyncService.syncProductStatus(productId, "高耐磨乘用车轮胎", 0);

        assertEquals(2, updatedCount);
        assertEquals(2, countCapabilities(productId, 1));
        assertEquals(1, countCapabilities(9002L, 0));
    }

    @Test
    void syncProductStatusEnablesCapabilitiesWhenSpuIsListedAgain() {
        Long productId = 9003L;
        Long entityId = entityService.createEntity(entityReq("M_RUBBER_STATUS_ON", "天然橡胶"));
        capabilityService.createCapability(capabilityReq(3001L, productId, "高耐磨乘用车轮胎", entityId, 1));
        capabilityService.createCapability(capabilityReq(3002L, productId, "高耐磨乘用车轮胎", entityId, 1));

        int updatedCount = spuStatusSyncService.syncProductStatus(productId, "高耐磨乘用车轮胎", 1);

        assertEquals(2, updatedCount);
        assertEquals(2, countCapabilities(productId, 0));
    }

    @Test
    void syncProductStatusRebuildsCapabilitiesFromApprovedRelationSubmissionWhenListed() {
        Long productId = 9004L;
        Long materialEntityId = entityService.createEntity(entityReq("M_RUBBER_REBUILD", "天然橡胶"));
        Long processEntityId = entityService.createEntity(entityReq("P_VULCANIZE_REBUILD", "硫化工艺", WujinLane.PROCESS.name()));
        Long templateId = templateService.createTemplate(templateReq("TPL_REBUILD", "TIRE_RUBBER"));
        Long submissionId = submissionService.createSubmission(submissionReq(3001L, productId,
                "高耐磨乘用车轮胎", templateId, 30));
        itemService.createItem(itemReq(submissionId, materialEntityId, WujinRelationType.REQUIRES_MATERIAL.name()));
        itemService.createItem(itemReq(submissionId, processEntityId, WujinRelationType.REQUIRES_PROCESS.name()));

        int updatedCount = spuStatusSyncService.syncProductStatus(productId, "高耐磨乘用车轮胎", 1);

        assertEquals(2, updatedCount);
        assertEquals(2, countCapabilities(productId, 0));
    }

    private long countCapabilities(Long productId, Integer supplyStatus) {
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setProductId(productId);
        listReqVO.setSupplyStatus(supplyStatus);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(listReqVO);
        return capabilities.size();
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name) {
        return entityReq(entityCode, name, WujinLane.MATERIAL.name());
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

    private WujinIndustryTemplateSaveReqVO templateReq(String templateCode, String industryCode) {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode(templateCode);
        reqVO.setName("轮胎橡胶模板");
        reqVO.setIndustryCode(industryCode);
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(Long merchantId, Long productId, String productName,
                                                                   Long templateId, Integer auditStatus) {
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(merchantId);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(auditStatus);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(96);
        reqVO.setRemark("平台审核通过");
        return reqVO;
    }

    private WujinMerchantRelationItemSaveReqVO itemReq(Long submissionId, Long entityId, String relationType) {
        WujinMerchantRelationItemSaveReqVO reqVO = new WujinMerchantRelationItemSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(relationType);
        reqVO.setFromTemplate(true);
        reqVO.setRequiredFlag(true);
        reqVO.setRemark("审核通过关系");
        return reqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(Long merchantId, Long productId, String productName,
                                                                 Long entityId, Integer supplyStatus) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(merchantId);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setEntityId(entityId);
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("TIRE_RUBBER");
        reqVO.setSupplyStatus(supplyStatus);
        reqVO.setStockCount(120);
        reqVO.setMinOrderQuantity(5);
        reqVO.setDeliveryDays(3);
        reqVO.setServiceArea("华东");
        reqVO.setRemark("来自商城 SPU 供给索引");
        return reqVO;
    }
}
