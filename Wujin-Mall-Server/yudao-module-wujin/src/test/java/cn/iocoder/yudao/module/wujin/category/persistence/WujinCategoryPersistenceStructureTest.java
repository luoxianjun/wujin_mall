package cn.iocoder.yudao.module.wujin.category.persistence;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wujin.category.WujinCategoryHealthStatus;
import cn.iocoder.yudao.module.wujin.controller.admin.category.WujinCategoryController;
import cn.iocoder.yudao.module.wujin.controller.admin.category.WujinCategoryMappingController;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMappingMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WujinCategoryPersistenceStructureTest {

    @Test
    void categoryDoKeepsThreeLaneTreeFields() {
        WujinCategoryDO category = WujinCategoryDO.builder()
                .id(1L)
                .parentId(WujinCategoryDO.PARENT_ID_ROOT)
                .lane(WujinLane.PRODUCT.name())
                .code("P-TIRE-CAR")
                .name("乘用车轮胎")
                .level(3)
                .sort(10)
                .status(0)
                .displayDepth(2)
                .healthStatus(WujinCategoryHealthStatus.HEALTHY.name())
                .build();

        assertTrue(BaseDO.class.isAssignableFrom(WujinCategoryDO.class));
        assertEquals("wujin_category", WujinCategoryDO.class.getAnnotation(TableName.class).value());
        assertEquals(WujinCategoryDO.PARENT_ID_ROOT, category.getParentId());
        assertEquals(WujinLane.PRODUCT.name(), category.getLane());
        assertEquals(WujinCategoryHealthStatus.HEALTHY.name(), category.getHealthStatus());
    }

    @Test
    void categoryMappingDoKeepsCrossLaneRelationFields() {
        WujinCategoryMappingDO mapping = WujinCategoryMappingDO.builder()
                .id(10L)
                .sourceCategoryId(1L)
                .sourceLane(WujinLane.PRODUCT.name())
                .targetCategoryId(2L)
                .targetLane(WujinLane.MATERIAL.name())
                .mappingType("REQUIRES_MATERIAL")
                .confidence(95)
                .status(0)
                .build();

        assertTrue(BaseDO.class.isAssignableFrom(WujinCategoryMappingDO.class));
        assertEquals("wujin_category_mapping", WujinCategoryMappingDO.class.getAnnotation(TableName.class).value());
        assertEquals(WujinLane.PRODUCT.name(), mapping.getSourceLane());
        assertEquals(WujinLane.MATERIAL.name(), mapping.getTargetLane());
        assertEquals("REQUIRES_MATERIAL", mapping.getMappingType());
    }

    @Test
    void mapperAndControllerRoutesFollowYudaoAdminConvention() {
        assertNotNull(WujinCategoryMapper.class.getAnnotation(Mapper.class));
        assertNotNull(WujinCategoryMappingMapper.class.getAnnotation(Mapper.class));
        assertEquals("/wujin/category", WujinCategoryController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/wujin/category-mapping", WujinCategoryMappingController.class.getAnnotation(RequestMapping.class).value()[0]);
    }
}
