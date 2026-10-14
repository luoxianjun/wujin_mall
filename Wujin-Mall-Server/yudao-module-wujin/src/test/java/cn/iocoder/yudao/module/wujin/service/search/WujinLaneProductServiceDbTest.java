package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.brand.ProductBrandApi;
import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductPageRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeServiceImpl;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminService;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Import({WujinLaneProductServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class, WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinCategoryAdminServiceImpl.class, WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinProductAttributeServiceImpl.class, WujinAttributeDictionaryAdminServiceImpl.class})
class WujinLaneProductServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinLaneProductService laneProductService;
    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinCategoryAdminService categoryService;
    @MockBean
    private ProductSpuApi productSpuApi;
    @MockBean
    private ProductBrandApi productBrandApi;

    private final Map<Long, ProductSpuRespDTO> products = new LinkedHashMap<>();
    private Long tireEntityId;

    @BeforeEach
    void setUpChain() {
        tireEntityId = entityService.createEntity(entityReq("P_TIRE", "轮胎", WujinLane.PRODUCT.name()));
        Long rubberId = entityService.createEntity(entityReq("M_RUBBER", "天然橡胶", WujinLane.MATERIAL.name()));
        Long blackId = entityService.createEntity(entityReq("M_BLACK", "炭黑", WujinLane.MATERIAL.name()));
        Long vulcanizeId = entityService.createEntity(entityReq("W_VULCANIZE", "硫化", WujinLane.PROCESS.name()));
        relationService.createRelation(relationReq(tireEntityId, rubberId, WujinRelationType.REQUIRES_MATERIAL, 1));
        relationService.createRelation(relationReq(tireEntityId, blackId, WujinRelationType.REQUIRES_MATERIAL, 1));
        relationService.createRelation(relationReq(tireEntityId, vulcanizeId, WujinRelationType.REQUIRES_PROCESS, 1));

        addProduct(9001L, "米其林 205/55R16 乘用车轮胎", 2000L, 39900, 30);
        addProduct(9206L, "米其林123", 2000L, 123, 1);
        addProduct(9300L, "朝阳 小推车轮胎 4.00-8", 2001L, 8800, 5);
        addProduct(9201L, "泰国 STR20 轮胎级天然橡胶", 2020L, 1250000, 3);
        addProduct(9203L, "橡胶用炭黑 N330", 2022L, 680000, 2);
        capability(9001L, "米其林 205/55R16 乘用车轮胎", tireEntityId, WujinLane.PRODUCT);
        capability(9206L, "米其林123", tireEntityId, WujinLane.PRODUCT);
        capability(9300L, "朝阳 小推车轮胎 4.00-8", tireEntityId, WujinLane.PRODUCT);
        // 历史数据中成品商品也挂了原材料供应能力，仍只能出现在成品泳道
        capability(9001L, "天然橡胶供应", rubberId, WujinLane.MATERIAL);
        capability(9201L, "泰国 STR20 轮胎级天然橡胶", rubberId, WujinLane.MATERIAL);
        capability(9203L, "橡胶用炭黑 N330", blackId, WujinLane.MATERIAL);

        when(productSpuApi.getSpuList(anyCollection())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().map(products::get).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        });
        when(productSpuApi.getEnabledSpuListByKeyword(anyString(), anyInt())).thenAnswer(invocation -> {
            String keyword = invocation.getArgument(0);
            return products.values().stream().filter(spu -> spu.getName().contains(keyword))
                    .collect(Collectors.toList());
        });
        when(productBrandApi.getBrandList(anyCollection())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            List<ProductBrandRespDTO> brands = new ArrayList<>();
            for (Long id : ids) {
                ProductBrandRespDTO brand = new ProductBrandRespDTO();
                brand.setId(id);
                brand.setName(id == 2000L ? "米其林" : "品牌" + id);
                brands.add(brand);
            }
            return brands;
        });
    }

    @Test
    void sameKeywordProductsAreSeparatedByLane() {
        WujinLaneProductPageRespVO productLane = laneProductService.getLaneProductPage(req("米其林123", WujinLane.PRODUCT));
        assertEquals(Collections.singletonList(9206L), ids(productLane));
        assertEquals("米其林", productLane.getList().get(0).getBrandName());
        assertEquals(2, productLane.getList().get(0).getMaterialCount());

        WujinLaneProductPageRespVO processLane = laneProductService.getLaneProductPage(req("米其林123", WujinLane.PROCESS));
        assertTrue(processLane.getList().isEmpty());
        assertEquals("硫化", processLane.getUpstreamEntities().get(0).getName());
        assertEquals("已找到上游加工工艺，暂无可承接的加工服务，可提交寻源需求", processLane.getEmptyHint());

        WujinLaneProductPageRespVO materialLane = laneProductService.getLaneProductPage(req("米其林123", WujinLane.MATERIAL));
        assertEquals(Arrays.asList(9201L, 9203L), ids(materialLane).stream().sorted().collect(Collectors.toList()));
        assertTrue(materialLane.getUpstreamEntities().stream().anyMatch(entity -> "天然橡胶".equals(entity.getName())));
    }

    @Test
    void tireKeywordKeepsMaterialProductsOutOfProductLane() {
        WujinLaneProductPageRespVO productLane = laneProductService.getLaneProductPage(req("轮胎", WujinLane.PRODUCT));

        assertEquals(Arrays.asList(9001L, 9300L, 9206L), ids(productLane));
        Map<String, Integer> counts = laneProductService.countLaneProducts("轮胎", null);
        assertEquals(3, counts.get(WujinLane.PRODUCT.name()));
        assertEquals(0, counts.get(WujinLane.PROCESS.name()));
        assertEquals(2, counts.get(WujinLane.MATERIAL.name()));
    }

    @Test
    void unknownKeywordReturnsEmptyListsWithFriendlyHint() {
        WujinLaneProductPageRespVO productLane = laneProductService.getLaneProductPage(req("士大夫但是犯得上", WujinLane.PRODUCT));
        WujinLaneProductPageRespVO materialLane = laneProductService.getLaneProductPage(req("士大夫但是犯得上", WujinLane.MATERIAL));

        assertEquals(0L, productLane.getTotal());
        assertEquals("未找到相关结果，试试其他关键词", productLane.getEmptyHint());
        assertEquals("暂无上游关联的原材料", materialLane.getEmptyHint());
        assertTrue(laneProductService.countLaneProducts("士大夫但是犯得上", null).values().stream()
                .allMatch(count -> count == 0));
    }

    @Test
    void categoryAndBrandFiltersKeepTheOriginalKeyword() {
        Long rootId = categoryService.createCategory(categoryReq("P", "五金成品", WujinCategoryDO.PARENT_ID_ROOT));
        Long tireId = categoryService.createCategory(categoryReq("P.TIRE", "轮胎", rootId));
        Long carId = categoryService.createCategory(categoryReq("P.TIRE.CAR", "乘用车轮胎", tireId));
        categoryService.createCategory(categoryReq("P.TIRE.TROLLEY", "小推车轮胎", tireId));

        WujinLaneProductReqVO carReq = req("轮胎", WujinLane.PRODUCT);
        carReq.setCategoryId(carId);
        assertEquals(Collections.singletonList(9001L), ids(laneProductService.getLaneProductPage(carReq)));

        WujinLaneProductReqVO tireReq = req("轮胎", WujinLane.PRODUCT);
        tireReq.setCategoryId(tireId);
        WujinLaneProductPageRespVO tirePage = laneProductService.getLaneProductPage(tireReq);
        assertEquals(3L, tirePage.getTotal());
        assertEquals("米其林", tirePage.getBrands().get(0).getBrandName());
        assertEquals(2, tirePage.getBrands().get(0).getProductCount());

        tireReq.setBrandId(2001L);
        assertEquals(Collections.singletonList(9300L), ids(laneProductService.getLaneProductPage(tireReq)));

        WujinLaneProductReqVO emptyReq = req("轮胎", WujinLane.PRODUCT);
        emptyReq.setCategoryId(carId);
        emptyReq.setBrandId(2001L);
        assertEquals("当前筛选条件下暂无商品，试试其他分类或品牌",
                laneProductService.getLaneProductPage(emptyReq).getEmptyHint());
    }

    @Test
    void sortingAndPagingBeyondLastPage() {
        WujinLaneProductReqVO priceReq = req("轮胎", WujinLane.PRODUCT);
        priceReq.setSort(WujinLaneProductReqVO.SORT_PRICE_ASC);
        assertEquals(Arrays.asList(9206L, 9300L, 9001L), ids(laneProductService.getLaneProductPage(priceReq)));

        WujinLaneProductReqVO pageReq = req("轮胎", WujinLane.PRODUCT);
        pageReq.setPageSize(2);
        pageReq.setPageNo(5);
        WujinLaneProductPageRespVO page = laneProductService.getLaneProductPage(pageReq);
        assertEquals(3L, page.getTotal());
        assertTrue(page.getList().isEmpty());
        assertNull(page.getEmptyHint());
    }

    private List<Long> ids(WujinLaneProductPageRespVO page) {
        return page.getList().stream().map(WujinLaneProductPageRespVO.Item::getId).collect(Collectors.toList());
    }

    private WujinLaneProductReqVO req(String keyword, WujinLane lane) {
        WujinLaneProductReqVO reqVO = new WujinLaneProductReqVO();
        reqVO.setKeyword(keyword);
        reqVO.setLane(lane.name());
        return reqVO;
    }

    private void addProduct(Long id, String name, Long brandId, Integer price, Integer salesCount) {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(id);
        spu.setName(name);
        spu.setBrandId(brandId);
        spu.setPrice(price);
        spu.setSalesCount(salesCount);
        spu.setStatus(1);
        products.put(id, spu);
    }

    private void capability(Long productId, String productName, Long entityId, WujinLane lane) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(3001L);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setEntityId(entityId);
        reqVO.setLane(lane.name());
        reqVO.setSupplyStatus(0);
        reqVO.setMinOrderQuantity(10);
        reqVO.setDeliveryDays(3);
        capabilityService.createCapability(reqVO);
    }

    private WujinChainEntitySaveReqVO entityReq(String code, String name, String lane) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(code);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries("汽车");
        reqVO.setJunctionFlag(false);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntityRelationSaveReqVO relationReq(Long source, Long target, WujinRelationType type,
                                                          Integer auditStatus) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(source);
        reqVO.setTargetEntityId(target);
        reqVO.setRelationType(type.name());
        reqVO.setWeight(100);
        reqVO.setCostRatio(20);
        reqVO.setIndustryContext("TIRE");
        reqVO.setAuditStatus(auditStatus);
        return reqVO;
    }

    private WujinCategorySaveReqVO categoryReq(String code, String name, Long parentId) {
        WujinCategorySaveReqVO reqVO = new WujinCategorySaveReqVO();
        reqVO.setParentId(parentId);
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setCode(code);
        reqVO.setName(name);
        reqVO.setStatus(0);
        reqVO.setDisplayDepth(3);
        reqVO.setHealthStatus("HEALTHY");
        return reqVO;
    }
}
