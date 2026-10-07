package cn.iocoder.yudao.module.wujin.service.template;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinIndustryTemplateItemAdminServiceImpl.class})
class WujinIndustryTemplateAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinIndustryTemplateItemAdminService templateItemService;

    @Test
    void createTemplatePersistsAndFiltersByIndustry() {
        Long tireTemplateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶", WujinLane.PRODUCT.name()));
        templateService.createTemplate(templateReq("TPL_MEDICAL", "医用乳胶模板", "医用乳胶", WujinLane.PRODUCT.name()));

        WujinIndustryTemplateListReqVO listReqVO = new WujinIndustryTemplateListReqVO();
        listReqVO.setIndustryCode("轮胎橡胶");
        listReqVO.setStatus(0);
        List<WujinIndustryTemplateDO> templates = templateService.getTemplateList(listReqVO);

        assertEquals(1, templates.size());
        assertEquals(tireTemplateId, templates.get(0).getId());
        assertEquals("TPL_TIRE", templates.get(0).getTemplateCode());
        assertEquals(WujinLane.PRODUCT.name(), templates.get(0).getProductLane());
    }

    @Test
    void createTemplateItemPersistsAndFiltersRequiredItems() {
        Long rubberId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "轮胎橡胶,医用乳胶"));
        Long processId = entityService.createEntity(entityReq("E_VULCANIZE", "硫化", WujinLane.PROCESS.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶", WujinLane.PRODUCT.name()));

        Long materialItemId = templateItemService.createTemplateItem(itemReq(templateId, rubberId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true));
        templateItemService.createTemplateItem(itemReq(templateId, processId,
                WujinRelationType.REQUIRES_PROCESS.name(), false));

        WujinIndustryTemplateItemListReqVO listReqVO = new WujinIndustryTemplateItemListReqVO();
        listReqVO.setTemplateId(templateId);
        listReqVO.setRequiredFlag(true);
        List<WujinIndustryTemplateItemDO> items = templateItemService.getTemplateItemList(listReqVO);

        assertEquals(1, items.size());
        assertEquals(materialItemId, items.get(0).getId());
        assertEquals(rubberId, items.get(0).getEntityId());
        assertEquals(WujinRelationType.REQUIRES_MATERIAL.name(), items.get(0).getRelationType());
    }

    @Test
    void createTemplateItemRejectsMissingEntity() {
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶", WujinLane.PRODUCT.name()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> templateItemService.createTemplateItem(itemReq(templateId, 9999L,
                        WujinRelationType.REQUIRES_MATERIAL.name(), true)));

        assertEquals("行业模板关联实体不存在", exception.getMessage());
    }

    @Test
    void batchMigrateTemplateItemsMovesItemsAndOverridesRelationOptions() {
        Long rubberId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long processId = entityService.createEntity(entityReq("E_VULCANIZE", "硫化", WujinLane.PROCESS.name(), "轮胎橡胶"));
        Long sourceTemplateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶", WujinLane.PRODUCT.name()));
        Long targetTemplateId = templateService.createTemplate(templateReq("TPL_TIRE_V2", "轮胎橡胶新版模板", "轮胎橡胶", WujinLane.PRODUCT.name()));
        Long materialItemId = templateItemService.createTemplateItem(itemReq(sourceTemplateId, rubberId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true));
        Long processItemId = templateItemService.createTemplateItem(itemReq(sourceTemplateId, processId,
                WujinRelationType.REQUIRES_PROCESS.name(), false));

        int updatedCount = templateItemService.batchMigrateTemplateItem(batchMigrateReq(
                Arrays.asList(materialItemId, processItemId), targetTemplateId,
                WujinRelationType.REQUIRES_PROCESS.name(), false, 55));

        assertEquals(2, updatedCount);
        WujinIndustryTemplateItemDO materialItem = templateItemService.getTemplateItem(materialItemId);
        assertEquals(targetTemplateId, materialItem.getTemplateId());
        assertEquals(rubberId, materialItem.getEntityId());
        assertEquals(WujinRelationType.REQUIRES_PROCESS.name(), materialItem.getRelationType());
        assertEquals(false, materialItem.getRequiredFlag());
        assertEquals(10, materialItem.getSort());
        assertEquals(55, materialItem.getWeight());
        assertEquals("关键关系", materialItem.getRemark());
        WujinIndustryTemplateItemDO processItem = templateItemService.getTemplateItem(processItemId);
        assertEquals(targetTemplateId, processItem.getTemplateId());
        assertEquals(processId, processItem.getEntityId());
        assertEquals(WujinRelationType.REQUIRES_PROCESS.name(), processItem.getRelationType());
        assertEquals(false, processItem.getRequiredFlag());
        assertEquals(55, processItem.getWeight());
    }

    @Test
    void batchMigrateTemplateItemsRejectsMissingTargetTemplate() {
        Long rubberId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long sourceTemplateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶", WujinLane.PRODUCT.name()));
        Long materialItemId = templateItemService.createTemplateItem(itemReq(sourceTemplateId, rubberId,
                WujinRelationType.REQUIRES_MATERIAL.name(), true));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> templateItemService.batchMigrateTemplateItem(batchMigrateReq(
                        Arrays.asList(materialItemId), 9999L,
                        WujinRelationType.REQUIRES_PROCESS.name(), false, 55)));

        assertEquals("行业模板不存在", exception.getMessage());
    }

    private WujinIndustryTemplateSaveReqVO templateReq(String templateCode, String name, String industryCode, String productLane) {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode(templateCode);
        reqVO.setName(name);
        reqVO.setIndustryCode(industryCode);
        reqVO.setProductLane(productLane);
        reqVO.setStatus(0);
        reqVO.setRemark("平台预设");
        return reqVO;
    }

    private WujinIndustryTemplateItemSaveReqVO itemReq(Long templateId, Long entityId, String relationType, Boolean requiredFlag) {
        WujinIndustryTemplateItemSaveReqVO reqVO = new WujinIndustryTemplateItemSaveReqVO();
        reqVO.setTemplateId(templateId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(relationType);
        reqVO.setRequiredFlag(requiredFlag);
        reqVO.setSort(10);
        reqVO.setWeight(80);
        reqVO.setRemark("关键关系");
        return reqVO;
    }

    private WujinIndustryTemplateItemBatchMigrateReqVO batchMigrateReq(List<Long> ids, Long targetTemplateId,
            String relationType, Boolean requiredFlag, Integer weight) {
        WujinIndustryTemplateItemBatchMigrateReqVO reqVO = new WujinIndustryTemplateItemBatchMigrateReqVO();
        reqVO.setIds(ids);
        reqVO.setTargetTemplateId(targetTemplateId);
        reqVO.setRelationType(relationType);
        reqVO.setRequiredFlag(requiredFlag);
        reqVO.setWeight(weight);
        return reqVO;
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane, String industries) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries(industries);
        reqVO.setStatus(0);
        reqVO.setJunctionFlag(false);
        return reqVO;
    }
}
