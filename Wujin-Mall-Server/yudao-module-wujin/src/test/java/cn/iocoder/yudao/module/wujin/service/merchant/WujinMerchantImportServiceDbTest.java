package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportPreviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportResultRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinMerchantImportServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class, WujinMerchantRelationItemAdminServiceImpl.class,
        WujinMerchantSupplyCapabilityAdminServiceImpl.class, WujinIndustryTemplateAdminServiceImpl.class,
        WujinIndustryTemplateItemAdminServiceImpl.class, WujinChainEntityAdminServiceImpl.class})
class WujinMerchantImportServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinMerchantImportService importService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinIndustryTemplateItemAdminService templateItemService;
    @Resource
    private WujinChainEntityAdminService entityService;

    @Test
    void previewImportRowsBuildsDraftAndValidationErrors() {
        Long templateId = createTemplateWithEntity("TPL_IMPORT_PREVIEW", "M_RUBBER_NATURAL");

        WujinMerchantImportReqVO reqVO = importReq(templateId, Arrays.asList(
                row(3101L, 7101L, "医用橡胶垫片", "天然橡胶", "REQUIRES_MATERIAL", 120, 5, 3),
                row(null, 7102L, "", "不存在材料", "REQUIRES_MATERIAL", null, null, null)
        ));

        WujinMerchantImportPreviewRespVO preview = importService.previewImport(reqVO);

        assertEquals(2, preview.getTotalCount());
        assertEquals(1, preview.getValidCount());
        assertEquals(1, preview.getInvalidCount());
        assertEquals("医用橡胶垫片", preview.getValidRows().get(0).getProductName());
        assertFalse(preview.getInvalidRows().get(0).getErrors().isEmpty());
    }

    @Test
    void importRowsCreatesSubmissionsItemsAndActiveSupplyCapabilities() {
        Long templateId = createTemplateWithEntity("TPL_IMPORT_RUN", "M_RUBBER_IMPORT");

        WujinMerchantImportReqVO reqVO = importReq(templateId, Arrays.asList(
                row(3101L, 7101L, "医用橡胶垫片", "天然橡胶", "REQUIRES_MATERIAL", 120, 5, 3),
                row(3102L, 7102L, "工业橡胶密封圈", "天然橡胶", "REQUIRES_MATERIAL", 80, 10, 7)
        ));

        WujinMerchantImportResultRespVO result = importService.importRows(reqVO);

        assertEquals(2, result.getImportedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(2, result.getSubmissionIds().size());

        WujinMerchantRelationSubmissionListReqVO submissionReqVO = new WujinMerchantRelationSubmissionListReqVO();
        submissionReqVO.setMerchantId(3101L);
        List<WujinMerchantRelationSubmissionDO> submissions = submissionService.getSubmissionList(submissionReqVO);
        assertEquals(1, submissions.size());
        assertEquals(30, submissions.get(0).getAuditStatus());
        assertTrue(submissions.get(0).getRemark().contains("IMPORT"));

        WujinMerchantRelationItemListReqVO itemReqVO = new WujinMerchantRelationItemListReqVO();
        itemReqVO.setSubmissionId(submissions.get(0).getId());
        List<WujinMerchantRelationItemDO> items = itemService.getItemList(itemReqVO);
        assertEquals(1, items.size());
        assertEquals(WujinRelationType.REQUIRES_MATERIAL.name(), items.get(0).getRelationType());

        WujinMerchantSupplyCapabilityListReqVO capabilityReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        capabilityReqVO.setMerchantId(3101L);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(capabilityReqVO);
        assertEquals(1, capabilities.size());
        assertEquals(0, capabilities.get(0).getSupplyStatus());
        assertEquals(120, capabilities.get(0).getStockCount());
    }

    private Long createTemplateWithEntity(String templateCode, String entityCode) {
        Long entityId = entityService.createEntity(entityReq(entityCode, "天然橡胶", WujinLane.MATERIAL.name(), "医疗器械,橡胶"));
        Long templateId = templateService.createTemplate(templateReq(templateCode));
        templateItemService.createTemplateItem(templateItemReq(templateId, entityId));
        return templateId;
    }

    private WujinMerchantImportReqVO importReq(Long templateId, List<WujinMerchantImportReqVO.ImportRow> rows) {
        WujinMerchantImportReqVO reqVO = new WujinMerchantImportReqVO();
        reqVO.setTemplateId(templateId);
        reqVO.setIndustryCode("医疗器械");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setDefaultProductCategoryId(8801L);
        reqVO.setRows(rows);
        return reqVO;
    }

    private WujinMerchantImportReqVO.ImportRow row(Long merchantId, Long productId, String productName,
                                                  String entityName, String relationType, Integer stockCount,
                                                  Integer minOrderQuantity, Integer deliveryDays) {
        WujinMerchantImportReqVO.ImportRow row = new WujinMerchantImportReqVO.ImportRow();
        row.setMerchantId(merchantId);
        row.setProductId(productId);
        row.setProductName(productName);
        row.setProductCategoryId(8801L);
        row.setEntityName(entityName);
        row.setRelationType(relationType);
        row.setStockCount(stockCount);
        row.setMinOrderQuantity(minOrderQuantity);
        row.setDeliveryDays(deliveryDays);
        row.setServiceArea("华东");
        row.setRemark("批量导入");
        return row;
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane, String industries) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries(industries);
        reqVO.setJunctionFlag(false);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq(String templateCode) {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode(templateCode);
        reqVO.setName("导入模板");
        reqVO.setIndustryCode("医疗器械");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinIndustryTemplateItemSaveReqVO templateItemReq(Long templateId, Long entityId) {
        WujinIndustryTemplateItemSaveReqVO reqVO = new WujinIndustryTemplateItemSaveReqVO();
        reqVO.setTemplateId(templateId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(WujinRelationType.REQUIRES_MATERIAL.name());
        reqVO.setRequiredFlag(true);
        reqVO.setSort(1);
        reqVO.setWeight(90);
        return reqVO;
    }
}
