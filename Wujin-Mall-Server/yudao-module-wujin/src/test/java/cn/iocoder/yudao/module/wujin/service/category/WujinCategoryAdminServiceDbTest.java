package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.module.wujin.category.WujinCategoryHealthStatus.HEALTHY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({WujinCategoryAdminServiceImpl.class, WujinCategoryMappingAdminServiceImpl.class})
class WujinCategoryAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinCategoryAdminService categoryService;
    @Resource
    private WujinCategoryMappingAdminService mappingService;

    @Test
    void createCategoryPersistsAndFiltersByLane() {
        Long productId = categoryService.createCategory(categoryReq("P-TIRE-CAR", "乘用车轮胎", WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT));
        categoryService.createCategory(categoryReq("M-RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), WujinCategoryDO.PARENT_ID_ROOT));

        WujinCategoryListReqVO listReqVO = new WujinCategoryListReqVO();
        listReqVO.setLane(WujinLane.PRODUCT.name());
        List<WujinCategoryDO> categories = categoryService.getCategoryList(listReqVO);

        assertEquals(1, categories.size());
        assertEquals(productId, categories.get(0).getId());
        assertEquals(WujinLane.PRODUCT.name(), categories.get(0).getLane());
        assertEquals(HEALTHY.name(), categories.get(0).getHealthStatus());
    }

    @Test
    void createCategoryRejectsCrossLaneParent() {
        Long productParentId = categoryService.createCategory(categoryReq("P-TIRE", "轮胎", WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT));
        WujinCategorySaveReqVO child = categoryReq("M-RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), productParentId);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(child));

        assertEquals("类目父子节点必须属于同一泳道", exception.getMessage());
    }

    @Test
    void createMappingPersistsAndRejectsSameLaneMapping() {
        Long productId = categoryService.createCategory(categoryReq("P-TIRE-CAR", "乘用车轮胎", WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT));
        Long materialId = categoryService.createCategory(categoryReq("M-RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), WujinCategoryDO.PARENT_ID_ROOT));
        Long mappingId = mappingService.createMapping(mappingReq(productId, WujinLane.PRODUCT.name(), materialId, WujinLane.MATERIAL.name()));

        WujinCategoryMappingListReqVO listReqVO = new WujinCategoryMappingListReqVO();
        listReqVO.setSourceLane(WujinLane.PRODUCT.name());
        List<WujinCategoryMappingDO> mappings = mappingService.getMappingList(listReqVO);

        assertEquals(1, mappings.size());
        assertEquals(mappingId, mappings.get(0).getId());
        assertEquals("REQUIRES_MATERIAL", mappings.get(0).getMappingType());

        WujinCategoryMappingSaveReqVO sameLane = mappingReq(productId, WujinLane.PRODUCT.name(), productId, WujinLane.PRODUCT.name());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> mappingService.createMapping(sameLane));
        assertEquals("跨泳道映射必须连接不同泳道", exception.getMessage());
    }

    @Test
    void batchMigrateCategoryUpdatesParentLaneLevelAndDisplayDepth() {
        Long processParentId = categoryService.createCategory(categoryReq("P-PROCESS", "加工工艺", WujinLane.PROCESS.name(),
                WujinCategoryDO.PARENT_ID_ROOT));
        Long productChildId = categoryService.createCategory(categoryReq("P-TIRE-CAR", "乘用车轮胎", WujinLane.PRODUCT.name(),
                WujinCategoryDO.PARENT_ID_ROOT));
        Long productPartId = categoryService.createCategory(categoryReq("P-TIRE-PART", "轮胎配件", WujinLane.PRODUCT.name(),
                WujinCategoryDO.PARENT_ID_ROOT));

        int updatedCount = categoryService.batchMigrateCategory(batchMigrateReq(Arrays.asList(productChildId, productPartId),
                processParentId, WujinLane.PROCESS.name(), 2, 3, "NEEDS_SPLIT"));

        assertEquals(2, updatedCount);
        WujinCategoryDO migratedChild = categoryService.getCategory(productChildId);
        assertEquals(processParentId, migratedChild.getParentId());
        assertEquals(WujinLane.PROCESS.name(), migratedChild.getLane());
        assertEquals(2, migratedChild.getLevel());
        assertEquals(3, migratedChild.getDisplayDepth());
        assertEquals("NEEDS_SPLIT", migratedChild.getHealthStatus());
        WujinCategoryDO migratedPart = categoryService.getCategory(productPartId);
        assertEquals(processParentId, migratedPart.getParentId());
        assertEquals(WujinLane.PROCESS.name(), migratedPart.getLane());
    }

    @Test
    void batchMigrateCategoryRejectsCrossLaneParent() {
        Long productParentId = categoryService.createCategory(categoryReq("P-TIRE", "轮胎", WujinLane.PRODUCT.name(),
                WujinCategoryDO.PARENT_ID_ROOT));
        Long materialId = categoryService.createCategory(categoryReq("M-RUBBER", "天然橡胶", WujinLane.MATERIAL.name(),
                WujinCategoryDO.PARENT_ID_ROOT));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoryService.batchMigrateCategory(batchMigrateReq(Arrays.asList(materialId), productParentId,
                        WujinLane.MATERIAL.name(), 2, 2, HEALTHY.name())));

        assertEquals("类目父子节点必须属于同一泳道", exception.getMessage());
    }

    private WujinCategorySaveReqVO categoryReq(String code, String name, String lane, Long parentId) {
        WujinCategorySaveReqVO reqVO = new WujinCategorySaveReqVO();
        reqVO.setParentId(parentId);
        reqVO.setLane(lane);
        reqVO.setCode(code);
        reqVO.setName(name);
        reqVO.setLevel(WujinCategoryDO.PARENT_ID_ROOT.equals(parentId) ? 1 : 2);
        reqVO.setSort(10);
        reqVO.setStatus(0);
        reqVO.setDisplayDepth(2);
        reqVO.setHealthStatus(HEALTHY.name());
        return reqVO;
    }

    private WujinCategoryMappingSaveReqVO mappingReq(Long sourceCategoryId, String sourceLane,
                                                     Long targetCategoryId, String targetLane) {
        WujinCategoryMappingSaveReqVO reqVO = new WujinCategoryMappingSaveReqVO();
        reqVO.setSourceCategoryId(sourceCategoryId);
        reqVO.setSourceLane(sourceLane);
        reqVO.setTargetCategoryId(targetCategoryId);
        reqVO.setTargetLane(targetLane);
        reqVO.setMappingType("REQUIRES_MATERIAL");
        reqVO.setConfidence(95);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinCategoryBatchMigrateReqVO batchMigrateReq(List<Long> ids, Long targetParentId, String targetLane,
                                                           Integer targetLevel, Integer displayDepth,
                                                           String healthStatus) {
        WujinCategoryBatchMigrateReqVO reqVO = new WujinCategoryBatchMigrateReqVO();
        reqVO.setIds(ids);
        reqVO.setTargetParentId(targetParentId);
        reqVO.setTargetLane(targetLane);
        reqVO.setTargetLevel(targetLevel);
        reqVO.setDisplayDepth(displayDepth);
        reqVO.setHealthStatus(healthStatus);
        return reqVO;
    }
}
