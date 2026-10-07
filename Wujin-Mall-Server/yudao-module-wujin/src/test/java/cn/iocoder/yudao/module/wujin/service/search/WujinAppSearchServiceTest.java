package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import cn.iocoder.yudao.module.wujin.search.WujinEntryPath;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.search.WujinSearchRuleContext;
import cn.iocoder.yudao.module.wujin.search.WujinSearchRuleEngine;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminService;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryMappingAdminService;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryMappingAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchBehaviorLogAdminService;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchBehaviorLogAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchRuleConfigAdminService;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchRuleConfigAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@Import({WujinAppSearchServiceImpl.class, WujinSearchBehaviorLogAdminServiceImpl.class,
        WujinSearchRuleConfigAdminServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class, WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinCategoryAdminServiceImpl.class, WujinCategoryMappingAdminServiceImpl.class})
class WujinAppSearchServiceTest extends BaseDbUnitTest {

    @Resource
    private WujinAppSearchService appSearchService;
    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinSearchRuleConfigAdminService ruleConfigService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinCategoryAdminService categoryService;
    @Resource
    private WujinCategoryMappingAdminService categoryMappingService;
    @Resource
    private WujinChainEntityRelationAdminService chainEntityRelationService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService supplyCapabilityService;
    @MockBean
    private ProductSpuApi productSpuApi;

    @Test
    void searchMaterialKeywordReturnsThreeLaneTraceAndPersistsBehaviorLog() {
        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(101L);
        reqVO.setKeyword("天然橡胶");
        reqVO.setRequestedLane(WujinLane.MATERIAL.name());
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setIndustry("医疗器械");
        reqVO.setChainViewed(true);
        reqVO.setSatisfactionScore(5);
        reqVO.setResponseTimeMillis(640L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(WujinLane.MATERIAL.name(), respVO.getDefaultLane());
        assertEquals(3, respVO.getGranularity());
        assertTrue(respVO.getRiskWarningRequired());
        assertTrue(respVO.getExplanation().contains("跨多个行业"));
        assertEquals(3, respVO.getLaneSummaries().size());
        assertEquals("原材料", respVO.getLaneSummaries().get(0).getLaneName());
        assertTrue(respVO.getLaneSummaries().get(0).getResultCount() > 0);
        assertNotNull(respVO.getTraceHint());
        assertTrue(respVO.getTraceHint().contains("天然橡胶"));

        WujinSearchBehaviorLogListReqVO listReqVO = new WujinSearchBehaviorLogListReqVO();
        listReqVO.setKeyword("天然橡胶");
        List<WujinSearchBehaviorLogDO> logs = behaviorLogService.getBehaviorLogList(listReqVO);
        assertEquals(1, logs.size());
        assertEquals(WujinLane.MATERIAL.name(), logs.get(0).getResultLane());
        assertTrue(logs.get(0).getHighRiskWarningTriggered());
        assertTrue(logs.get(0).getChainViewed());
    }

    @Test
    void searchUsesEnabledGranularityRuleConfig() {
        ruleConfigService.createRuleConfig(ruleReq("GRANULARITY_LIMIT", WujinLane.MATERIAL.name(), "RUBBER", "2", 100, 0));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(102L);
        reqVO.setKeyword("天然橡胶");
        reqVO.setRequestedLane(WujinLane.MATERIAL.name());
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setIndustry("RUBBER");
        reqVO.setChainViewed(false);
        reqVO.setResponseTimeMillis(720L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(2, respVO.getGranularity());
        assertTrue(respVO.getExplanation().contains("搜索规则配置"));
    }

    @Test
    void searchUsesPersistedChainEntitiesAsLaneSummaryCounts() {
        chainEntityService.createEntity(entityReq("P_TIRE_CAR", "乘用车轮胎", WujinLane.PRODUCT.name(), "汽车,橡胶"));
        chainEntityService.createEntity(entityReq("P_TIRE_ENGINEERING", "工程机械轮胎", WujinLane.PRODUCT.name(), "工程机械,橡胶"));
        chainEntityService.createEntity(entityReq("M_TIRE_RUBBER", "轮胎胎面橡胶", WujinLane.MATERIAL.name(), "汽车,橡胶"));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(103L);
        reqVO.setKeyword("轮胎");
        reqVO.setRequestedLane(WujinLane.PRODUCT.name());
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setIndustry("橡胶");
        reqVO.setResponseTimeMillis(580L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(2, laneSummary(respVO, WujinLane.PRODUCT).getResultCount());
        assertEquals(1, laneSummary(respVO, WujinLane.MATERIAL).getResultCount());
    }

    @Test
    void searchReturnsDescendantCategorySuggestionsForBroadKeyword() {
        Long clothingId = categoryService.createCategory(categoryReq("P-CLOTHING", "衣服",
                WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT, 1, 10));
        Long menId = categoryService.createCategory(categoryReq("P-CLOTHING-MEN", "男装",
                WujinLane.PRODUCT.name(), clothingId, 2, 10));
        Long womenId = categoryService.createCategory(categoryReq("P-CLOTHING-WOMEN", "女装",
                WujinLane.PRODUCT.name(), clothingId, 2, 20));
        categoryService.createCategory(categoryReq("P-CLOTHING-MEN-SUIT", "西服",
                WujinLane.PRODUCT.name(), menId, 3, 10));
        categoryService.createCategory(categoryReq("P-CLOTHING-WOMEN-DRESS", "裙子",
                WujinLane.PRODUCT.name(), womenId, 3, 10));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(106L);
        reqVO.setKeyword("衣服");
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setResponseTimeMillis(360L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(4, respVO.getCategorySuggestions().size());
        assertTrue(categoryPaths(respVO).contains("衣服 > 男装"));
        assertTrue(categoryPaths(respVO).contains("衣服 > 男装 > 西服"));
        assertTrue(categoryPaths(respVO).contains("衣服 > 女装"));
        assertTrue(categoryPaths(respVO).contains("衣服 > 女装 > 裙子"));
        assertEquals(WujinLane.PRODUCT.name(), respVO.getCategorySuggestions().get(0).getLane());
        assertNotNull(respVO.getCategorySuggestions().get(0).getCategoryId());
        assertEquals(2, respVO.getCategoryGroups().size());
        assertEquals(1, respVO.getCategoryGroups().get(0).getLevel());
        assertEquals("一级分类", respVO.getCategoryGroups().get(0).getLevelName());
        assertEquals("衣服", respVO.getCategoryGroups().get(0).getCategories().get(0).getCategoryName());
        assertEquals(2, respVO.getCategoryGroups().get(1).getLevel());
        assertEquals("二级分类", respVO.getCategoryGroups().get(1).getLevelName());
        assertEquals(2, respVO.getCategoryGroups().get(1).getCategories().size());
        assertTrue(respVO.getCategoryGroups().get(1).getCategories().stream()
                .anyMatch(item -> "男装".equals(item.getCategoryName())));
        assertTrue(respVO.getCategoryGroups().get(1).getCategories().stream()
                .anyMatch(item -> "女装".equals(item.getCategoryName())));
    }

    @Test
    void searchKeepsOneCategoryLevelWhenDisplayDepthStopsAtMatchedLevel() {
        WujinCategorySaveReqVO safetyReqVO = categoryReq("P-SAFETY", "安全防护",
                WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT, 1, 10);
        safetyReqVO.setDisplayDepth(1);
        Long safetyId = categoryService.createCategory(safetyReqVO);
        categoryService.createCategory(categoryReq("P-SAFETY-GLOVE", "防护手套",
                WujinLane.PRODUCT.name(), safetyId, 2, 10));
        categoryService.createCategory(categoryReq("P-SAFETY-HELMET", "安全帽",
                WujinLane.PRODUCT.name(), safetyId, 2, 20));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(108L);
        reqVO.setKeyword("安全防护");
        reqVO.setRequestedLane(WujinLane.PRODUCT.name());
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setResponseTimeMillis(280L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(1, respVO.getCategoryGroups().size());
        assertEquals("一级分类", respVO.getCategoryGroups().get(0).getLevelName());
        assertEquals(1, respVO.getCategoryGroups().get(0).getCategories().size());
        assertEquals("安全防护", respVO.getCategoryGroups().get(0).getCategories().get(0).getCategoryName());
    }

    @Test
    void selectingPassengerTireKeepsParentAndSiblingCategoriesVisible() {
        Long vehicleId = categoryService.createCategory(categoryReq("P-VEHICLE", "汽配",
                WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT, 1, 10));
        Long tireId = categoryService.createCategory(categoryReq("P-TIRE-NAV", "轮胎",
                WujinLane.PRODUCT.name(), vehicleId, 2, 10));
        Long passengerTireId = categoryService.createCategory(categoryReq(
                "P-TIRE-NAV-PASSENGER", "乘用车轮胎", WujinLane.PRODUCT.name(), tireId, 3, 10));
        categoryService.createCategory(categoryReq("P-TIRE-NAV-TROLLEY", "小推车轮胎",
                WujinLane.PRODUCT.name(), tireId, 3, 20));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(109L);
        reqVO.setKeyword("乘用车轮胎");
        reqVO.setRequestedLane(WujinLane.PRODUCT.name());
        reqVO.setEntryPath("CATEGORY_SUGGESTION");
        reqVO.setCategoryId(passengerTireId);
        reqVO.setCategoryPath("汽配 > 轮胎 > 乘用车轮胎");
        reqVO.setResponseTimeMillis(260L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(2, respVO.getCategoryGroups().size());
        assertEquals(2, respVO.getCategoryGroups().get(0).getLevel());
        assertEquals(1, respVO.getCategoryGroups().get(0).getCategories().size());
        assertEquals("轮胎", respVO.getCategoryGroups().get(0).getCategories().get(0).getCategoryName());
        assertEquals(3, respVO.getCategoryGroups().get(1).getLevel());
        assertEquals(2, respVO.getCategoryGroups().get(1).getCategories().size());
        assertTrue(respVO.getCategoryGroups().get(1).getCategories().stream()
                .anyMatch(item -> "乘用车轮胎".equals(item.getCategoryName())));
        assertTrue(respVO.getCategoryGroups().get(1).getCategories().stream()
                .anyMatch(item -> "小推车轮胎".equals(item.getCategoryName())));
    }

    @Test
    void searchReturnsMappedMaterialCategoriesForMaterialLane() {
        Long tireId = categoryService.createCategory(categoryReq("P-TIRE", "轮胎",
                WujinLane.PRODUCT.name(), WujinCategoryDO.PARENT_ID_ROOT, 1, 10));
        Long rubberId = categoryService.createCategory(categoryReq("M-RUBBER", "橡胶材料",
                WujinLane.MATERIAL.name(), WujinCategoryDO.PARENT_ID_ROOT, 1, 10));
        categoryService.createCategory(categoryReq("M-RUBBER-NATURAL", "天然橡胶",
                WujinLane.MATERIAL.name(), rubberId, 2, 10));
        categoryService.createCategory(categoryReq("M-RUBBER-SYNTHETIC", "合成橡胶",
                WujinLane.MATERIAL.name(), rubberId, 2, 20));
        categoryMappingService.createMapping(mappingReq(tireId, WujinLane.PRODUCT.name(),
                rubberId, WujinLane.MATERIAL.name()));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(107L);
        reqVO.setKeyword("轮胎");
        reqVO.setRequestedLane(WujinLane.MATERIAL.name());
        reqVO.setEntryPath(WujinEntryPath.LANE_SWITCH.name());
        reqVO.setResponseTimeMillis(330L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(2, respVO.getCategorySuggestions().size());
        assertTrue(categoryPaths(respVO).contains("橡胶材料 > 天然橡胶"));
        assertTrue(categoryPaths(respVO).contains("橡胶材料 > 合成橡胶"));
        assertTrue(respVO.getCategorySuggestions().stream()
                .allMatch(item -> WujinLane.MATERIAL.name().equals(item.getLane())));
    }

    @Test
    void switchingTireSearchToMaterialReturnsSellableMaterialProducts() {
        Long tireEntityId = chainEntityService.createEntity(entityReq(
                "P_TIRE_PRODUCTS", "轮胎", WujinLane.PRODUCT.name(), "轮胎"));
        Long rubberEntityId = chainEntityService.createEntity(entityReq(
                "M_NATURAL_RUBBER_PRODUCTS", "天然橡胶", WujinLane.MATERIAL.name(), "轮胎"));
        chainEntityRelationService.createRelation(relationReq(tireEntityId, rubberEntityId));
        supplyCapabilityService.createCapability(capabilityReq(rubberEntityId, 9201L));

        ProductSpuRespDTO rubberProduct = new ProductSpuRespDTO();
        rubberProduct.setId(9201L);
        rubberProduct.setName("泰国 STR20 天然橡胶");
        rubberProduct.setPicUrl("/static/test/natural-rubber.png");
        rubberProduct.setPrice(1250000);
        rubberProduct.setMarketPrice(1300000);
        rubberProduct.setStock(200);
        rubberProduct.setStatus(1);
        when(productSpuApi.getSpuList(anyCollection())).thenReturn(Collections.singletonList(rubberProduct));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setKeyword("轮胎");
        reqVO.setRequestedLane(WujinLane.MATERIAL.name());
        reqVO.setEntryPath(WujinEntryPath.LANE_SWITCH.name());
        reqVO.setSourceLane(WujinLane.PRODUCT.name());
        reqVO.setSourceKeyword("轮胎");

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(1, respVO.getRelatedProducts().size());
        assertEquals(9201L, respVO.getRelatedProducts().get(0).getId());
        assertEquals("天然橡胶", respVO.getRelatedProducts().get(0).getEntityName());
        assertEquals(1, laneSummary(respVO, WujinLane.MATERIAL).getResultCount());
    }

    @Test
    void searchAppliesExtensibleLaneOverrideRuleTypeWithConditionExpression() {
        ruleConfigService.createRuleConfig(ruleReq("LANE_OVERRIDE", null, "医疗器械",
                "lane=MATERIAL;when=keyword contains '医用' and industry == '医疗器械'", 200, 0));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(104L);
        reqVO.setKeyword("医用密封垫片");
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setIndustry("医疗器械");
        reqVO.setResponseTimeMillis(410L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertEquals(WujinLane.MATERIAL.name(), respVO.getDefaultLane());
        assertTrue(respVO.getExplanation().contains("LANE_OVERRIDE"));
    }

    @Test
    void searchAppliesCustomRiskWarningRuleTypeWithComplexCondition() {
        ruleConfigService.createRuleConfig(ruleReq("RISK_WARNING", WujinLane.MATERIAL.name(), "RUBBER",
                "text=医疗级橡胶不可与普通橡胶直接互换;when=(keyword contains '橡胶' and lane == 'MATERIAL')", 120, 0));

        WujinAppSearchReqVO reqVO = new WujinAppSearchReqVO();
        reqVO.setUserId(105L);
        reqVO.setKeyword("橡胶材料");
        reqVO.setRequestedLane(WujinLane.MATERIAL.name());
        reqVO.setEntryPath(WujinEntryPath.DIRECT_SEARCH.name());
        reqVO.setIndustry("RUBBER");
        reqVO.setResponseTimeMillis(390L);

        WujinAppSearchRespVO respVO = appSearchService.search(reqVO);

        assertTrue(respVO.getRiskWarningRequired());
        assertEquals("医疗级橡胶不可与普通橡胶直接互换", respVO.getRiskWarningText());
        assertTrue(respVO.getExplanation().contains("RISK_WARNING"));
    }

    @Test
    void ruleEngineCachesCompiledConditionExpressions() {
        WujinSearchRuleEngine engine = new WujinSearchRuleEngine();
        WujinSearchRuleConfigDO rule = new WujinSearchRuleConfigDO();
        rule.setRuleType("GRANULARITY_LIMIT");
        rule.setLane(WujinLane.MATERIAL.name());
        rule.setIndustryCode("RUBBER");
        rule.setRuleValue("2;when=keyword contains '橡胶' and industry == 'RUBBER'");
        rule.setWeight(10);
        rule.setStatus(0);
        List<WujinSearchRuleConfigDO> rules = new ArrayList<>();
        rules.add(rule);

        WujinSearchRuleContext context = new WujinSearchRuleContext("天然橡胶", WujinLane.MATERIAL,
                "RUBBER", WujinEntryPath.DIRECT_SEARCH, null, null);

        engine.apply(context, 3, WujinLane.MATERIAL, false, null, "base", rules);
        engine.apply(context, 3, WujinLane.MATERIAL, false, null, "base", rules);

        assertEquals(1, engine.getCompiledConditionCacheSize());
    }

    private WujinAppSearchRespVO.LaneSummary laneSummary(WujinAppSearchRespVO respVO, WujinLane lane) {
        return respVO.getLaneSummaries().stream()
                .filter(summary -> lane.name().equals(summary.getLane()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未返回泳道摘要：" + lane.name()));
    }

    private List<String> categoryPaths(WujinAppSearchRespVO respVO) {
        List<String> paths = new ArrayList<>();
        for (WujinAppSearchRespVO.CategorySuggestion suggestion : respVO.getCategorySuggestions()) {
            paths.add(suggestion.getCategoryPath());
        }
        return paths;
    }

    private WujinSearchRuleConfigSaveReqVO ruleReq(String ruleType, String lane, String industryCode,
                                                   String ruleValue, Integer weight, Integer status) {
        WujinSearchRuleConfigSaveReqVO reqVO = new WujinSearchRuleConfigSaveReqVO();
        reqVO.setRuleType(ruleType);
        reqVO.setLane(lane);
        reqVO.setIndustryCode(industryCode);
        reqVO.setRuleValue(ruleValue);
        reqVO.setWeight(weight);
        reqVO.setStatus(status);
        return reqVO;
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

    private WujinCategorySaveReqVO categoryReq(String code, String name, String lane, Long parentId,
                                               Integer level, Integer sort) {
        WujinCategorySaveReqVO reqVO = new WujinCategorySaveReqVO();
        reqVO.setParentId(parentId);
        reqVO.setLane(lane);
        reqVO.setCode(code);
        reqVO.setName(name);
        reqVO.setLevel(level);
        reqVO.setSort(sort);
        reqVO.setStatus(0);
        reqVO.setDisplayDepth(3);
        reqVO.setHealthStatus("HEALTHY");
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
        reqVO.setConfidence(100);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntityRelationSaveReqVO relationReq(Long sourceEntityId, Long targetEntityId) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(sourceEntityId);
        reqVO.setTargetEntityId(targetEntityId);
        reqVO.setRelationType(WujinRelationType.REQUIRES_MATERIAL.name());
        reqVO.setWeight(100);
        reqVO.setCostRatio(45);
        reqVO.setIndustryContext("TIRE");
        reqVO.setAuditStatus(1);
        return reqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(Long entityId, Long productId) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(2001L);
        reqVO.setProductId(productId);
        reqVO.setProductName("泰国 STR20 天然橡胶");
        reqVO.setEntityId(entityId);
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("TIRE");
        reqVO.setSupplyStatus(0);
        reqVO.setStockCount(200);
        reqVO.setMinOrderQuantity(1);
        reqVO.setDeliveryDays(7);
        reqVO.setServiceArea("全国");
        return reqVO;
    }
}
