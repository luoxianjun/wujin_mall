package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinAppSearchRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinEntryPath;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.search.WujinSearchDecision;
import cn.iocoder.yudao.module.wujin.search.WujinSearchDecisionRequest;
import cn.iocoder.yudao.module.wujin.search.WujinSearchDecisionService;
import cn.iocoder.yudao.module.wujin.search.WujinSearchIntent;
import cn.iocoder.yudao.module.wujin.search.WujinSearchRuleContext;
import cn.iocoder.yudao.module.wujin.search.WujinSearchRuleEngine;
import cn.iocoder.yudao.module.wujin.search.WujinSearchRuleResult;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminService;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryMappingAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchBehaviorLogAdminService;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinSearchRuleConfigAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class WujinAppSearchServiceImpl implements WujinAppSearchService {

    private static final int ENABLED_STATUS = 0;
    private static final int EFFECTIVE_RELATION_STATUS = 1;
    private static final int ENABLED_PRODUCT_STATUS = 1;
    private static final int CATEGORY_SUGGESTION_LIMIT = 8;
    private static final int MIN_CHILDREN_TO_EXPAND = 2;

    private final WujinSearchDecisionService decisionService = new WujinSearchDecisionService();
    private final WujinSearchRuleEngine ruleEngine = new WujinSearchRuleEngine();

    @Resource
    private WujinSearchBehaviorLogAdminService behaviorLogService;
    @Resource
    private WujinSearchRuleConfigAdminService ruleConfigService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService chainEntityRelationService;
    @Resource
    private WujinCategoryAdminService categoryService;
    @Resource
    private WujinCategoryMappingAdminService categoryMappingService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService supplyCapabilityService;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public WujinAppSearchRespVO search(WujinAppSearchReqVO reqVO) {
        WujinSearchIntent intent = resolveIntent(reqVO.getKeyword(), reqVO.getRequestedLane());
        WujinLane requestedLane = parseEnum(WujinLane.class, reqVO.getRequestedLane(), null);
        WujinEntryPath entryPath = parseEnum(WujinEntryPath.class, reqVO.getEntryPath(), WujinEntryPath.DIRECT_SEARCH);
        WujinLane sourceLane = parseEnum(WujinLane.class, reqVO.getSourceLane(), null);
        List<String> riskTags = resolveRiskTags(reqVO.getIndustry());

        WujinSearchDecision decision = decisionService.decide(WujinSearchDecisionRequest.builder()
                .keyword(reqVO.getKeyword())
                .intent(intent)
                .requestedLane(requestedLane)
                .entryPath(entryPath)
                .sourceLane(sourceLane)
                .sourceKeyword(reqVO.getSourceKeyword())
                .industry(reqVO.getIndustry())
                .riskTags(riskTags)
                .childCount(resolveChildCount(reqVO.getKeyword(), intent))
                .build());
        decision = applyRuleConfig(reqVO, decision);

        WujinAppSearchRespVO respVO = new WujinAppSearchRespVO();
        respVO.setKeyword(reqVO.getKeyword());
        respVO.setDefaultLane(decision.getDefaultLane().name());
        respVO.setGranularity(decision.getGranularity());
        respVO.setExplanation(decision.getExplanation());
        respVO.setContextHint(decision.getContextHint());
        respVO.setRiskWarningRequired(decision.isRiskWarningRequired());
        respVO.setRiskWarningText(decision.isRiskWarningRequired()
                ? resolveRiskWarningText(reqVO, decision)
                : null);
        respVO.setTraceHint(buildTraceHint(reqVO, decision));
        List<WujinCategoryDO> searchCategories = getEnabledCategories();
        List<WujinAppSearchRespVO.CategorySuggestion> categorySuggestions =
                buildCategorySuggestions(reqVO, searchCategories);
        List<WujinAppSearchRespVO.CategoryGroup> categoryGroups = buildCategoryGroups(reqVO, searchCategories);
        List<WujinAppSearchRespVO.RelatedProduct> relatedProducts = buildRelatedProducts(
                reqVO, requestedLane, categorySuggestions);
        List<WujinAppSearchRespVO.LaneSummary> laneSummaries = buildLaneSummaries(reqVO, decision.getDefaultLane());
        applyRelatedProductCount(laneSummaries, requestedLane, relatedProducts);
        respVO.setLaneSummaries(laneSummaries);
        respVO.setCategorySuggestions(categorySuggestions);
        respVO.setCategoryGroups(categoryGroups);
        respVO.setRelatedProducts(relatedProducts);

        persistBehaviorLog(reqVO, intent, decision);
        return respVO;
    }

    private List<WujinAppSearchRespVO.RelatedProduct> buildRelatedProducts(
            WujinAppSearchReqVO reqVO, WujinLane requestedLane,
            List<WujinAppSearchRespVO.CategorySuggestion> categorySuggestions) {
        if (requestedLane != WujinLane.MATERIAL) {
            return Collections.emptyList();
        }
        Map<Long, WujinChainEntityDO> materialEntities = resolveMaterialEntities(reqVO, categorySuggestions);
        if (materialEntities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, WujinMerchantSupplyCapabilityDO> capabilityByProductId = new LinkedHashMap<>();
        Map<Long, WujinChainEntityDO> entityByProductId = new HashMap<>();
        for (WujinChainEntityDO materialEntity : materialEntities.values()) {
            WujinMerchantSupplyCapabilityListReqVO capabilityReqVO = new WujinMerchantSupplyCapabilityListReqVO();
            capabilityReqVO.setEntityId(materialEntity.getId());
            capabilityReqVO.setLane(WujinLane.MATERIAL.name());
            capabilityReqVO.setSupplyStatus(ENABLED_STATUS);
            for (WujinMerchantSupplyCapabilityDO capability
                    : supplyCapabilityService.getCapabilityList(capabilityReqVO)) {
                if (capability.getProductId() == null) {
                    continue;
                }
                capabilityByProductId.putIfAbsent(capability.getProductId(), capability);
                entityByProductId.putIfAbsent(capability.getProductId(), materialEntity);
            }
        }
        if (capabilityByProductId.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductSpuRespDTO> products = productSpuApi.getSpuList(capabilityByProductId.keySet());
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ProductSpuRespDTO> productById = new HashMap<>();
        for (ProductSpuRespDTO product : products) {
            if (product != null && ENABLED_PRODUCT_STATUS == defaultInt(product.getStatus(), -1)) {
                productById.put(product.getId(), product);
            }
        }

        List<WujinAppSearchRespVO.RelatedProduct> result = new ArrayList<>();
        for (Map.Entry<Long, WujinMerchantSupplyCapabilityDO> entry : capabilityByProductId.entrySet()) {
            ProductSpuRespDTO product = productById.get(entry.getKey());
            if (product == null) {
                continue;
            }
            WujinMerchantSupplyCapabilityDO capability = entry.getValue();
            WujinChainEntityDO entity = entityByProductId.get(entry.getKey());
            result.add(toRelatedProduct(product, capability, entity));
        }
        return result;
    }

    private Map<Long, WujinChainEntityDO> resolveMaterialEntities(
            WujinAppSearchReqVO reqVO, List<WujinAppSearchRespVO.CategorySuggestion> categorySuggestions) {
        Map<Long, WujinChainEntityDO> materialEntities = new LinkedHashMap<>();
        for (WujinChainEntityDO sourceEntity : resolveSourceEntities(reqVO)) {
            WujinChainEntityRelationListReqVO relationReqVO = new WujinChainEntityRelationListReqVO();
            relationReqVO.setSourceEntityId(sourceEntity.getId());
            relationReqVO.setRelationType(WujinRelationType.REQUIRES_MATERIAL.name());
            relationReqVO.setAuditStatus(EFFECTIVE_RELATION_STATUS);
            for (WujinChainEntityRelationDO relation : chainEntityRelationService.getRelationList(relationReqVO)) {
                WujinChainEntityDO materialEntity = chainEntityService.getEntity(relation.getTargetEntityId());
                if (isEnabledMaterialEntity(materialEntity)) {
                    materialEntities.putIfAbsent(materialEntity.getId(), materialEntity);
                }
            }
        }
        if (!materialEntities.isEmpty()) {
            return materialEntities;
        }

        for (WujinAppSearchRespVO.CategorySuggestion suggestion : categorySuggestions) {
            WujinChainEntityListReqVO entityReqVO = new WujinChainEntityListReqVO();
            entityReqVO.setName(suggestion.getCategoryName());
            entityReqVO.setLane(WujinLane.MATERIAL.name());
            entityReqVO.setStatus(ENABLED_STATUS);
            for (WujinChainEntityDO entity : chainEntityService.getEntityList(entityReqVO)) {
                materialEntities.putIfAbsent(entity.getId(), entity);
            }
        }
        return materialEntities;
    }

    private List<WujinChainEntityDO> resolveSourceEntities(WujinAppSearchReqVO reqVO) {
        Map<Long, WujinChainEntityDO> sourceEntities = new LinkedHashMap<>();
        if (reqVO.getSourceEntityId() != null) {
            addSourceEntity(sourceEntities, chainEntityService.getEntity(reqVO.getSourceEntityId()));
        }
        if (reqVO.getSourceProductId() != null) {
            WujinMerchantSupplyCapabilityListReqVO capabilityReqVO = new WujinMerchantSupplyCapabilityListReqVO();
            capabilityReqVO.setProductId(reqVO.getSourceProductId());
            for (WujinMerchantSupplyCapabilityDO capability
                    : supplyCapabilityService.getCapabilityList(capabilityReqVO)) {
                addSourceEntity(sourceEntities, chainEntityService.getEntity(capability.getEntityId()));
            }
            WujinChainEntityListReqVO productEntityReqVO = new WujinChainEntityListReqVO();
            productEntityReqVO.setEntityCode("PRODUCT_" + reqVO.getSourceProductId());
            for (WujinChainEntityDO entity : chainEntityService.getEntityList(productEntityReqVO)) {
                addSourceEntity(sourceEntities, entity);
            }
        }
        if (!sourceEntities.isEmpty()) {
            return new ArrayList<>(sourceEntities.values());
        }

        String sourceKeyword = isBlank(reqVO.getSourceKeyword()) ? reqVO.getKeyword() : reqVO.getSourceKeyword();
        if (!isBlank(sourceKeyword)) {
            WujinChainEntityListReqVO entityReqVO = new WujinChainEntityListReqVO();
            entityReqVO.setName(sourceKeyword.trim());
            entityReqVO.setLane(WujinLane.PRODUCT.name());
            entityReqVO.setStatus(ENABLED_STATUS);
            for (WujinChainEntityDO entity : chainEntityService.getEntityList(entityReqVO)) {
                addSourceEntity(sourceEntities, entity);
            }
        }
        return new ArrayList<>(sourceEntities.values());
    }

    private void addSourceEntity(Map<Long, WujinChainEntityDO> sourceEntities, WujinChainEntityDO entity) {
        if (entity != null && ENABLED_STATUS == defaultInt(entity.getStatus(), -1)) {
            sourceEntities.putIfAbsent(entity.getId(), entity);
        }
    }

    private boolean isEnabledMaterialEntity(WujinChainEntityDO entity) {
        return entity != null && WujinLane.MATERIAL.name().equals(entity.getLane())
                && ENABLED_STATUS == defaultInt(entity.getStatus(), -1);
    }

    private WujinAppSearchRespVO.RelatedProduct toRelatedProduct(
            ProductSpuRespDTO product, WujinMerchantSupplyCapabilityDO capability,
            WujinChainEntityDO entity) {
        WujinAppSearchRespVO.RelatedProduct relatedProduct = new WujinAppSearchRespVO.RelatedProduct();
        relatedProduct.setId(product.getId());
        relatedProduct.setName(product.getName());
        relatedProduct.setPicUrl(product.getPicUrl());
        relatedProduct.setPrice(product.getPrice());
        relatedProduct.setMarketPrice(product.getMarketPrice());
        relatedProduct.setStock(product.getStock());
        relatedProduct.setMerchantId(capability.getMerchantId());
        relatedProduct.setEntityId(entity == null ? capability.getEntityId() : entity.getId());
        relatedProduct.setEntityName(entity == null ? capability.getProductName() : entity.getName());
        relatedProduct.setLane(capability.getLane());
        relatedProduct.setMinOrderQuantity(capability.getMinOrderQuantity());
        relatedProduct.setDeliveryDays(capability.getDeliveryDays());
        relatedProduct.setServiceArea(capability.getServiceArea());
        return relatedProduct;
    }

    private void applyRelatedProductCount(List<WujinAppSearchRespVO.LaneSummary> summaries,
                                          WujinLane requestedLane,
                                          List<WujinAppSearchRespVO.RelatedProduct> relatedProducts) {
        if (requestedLane != WujinLane.MATERIAL || relatedProducts.isEmpty()) {
            return;
        }
        for (WujinAppSearchRespVO.LaneSummary summary : summaries) {
            if (WujinLane.MATERIAL.name().equals(summary.getLane())) {
                summary.setResultCount(relatedProducts.size());
                summary.setSummary("已找到与当前成品产业链关联的可售原材料商品");
                return;
            }
        }
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private WujinSearchIntent resolveIntent(String keyword, String requestedLane) {
        WujinLane lane = parseEnum(WujinLane.class, requestedLane, null);
        if (lane == WujinLane.MATERIAL) {
            return WujinSearchIntent.MATERIAL;
        }
        if (lane == WujinLane.PROCESS) {
            return WujinSearchIntent.PROCESS;
        }
        if (keyword != null && (keyword.contains("橡胶") || keyword.contains("炭黑") || keyword.contains("材料"))) {
            return WujinSearchIntent.MATERIAL;
        }
        if (keyword != null && (keyword.contains("硫化") || keyword.contains("加工") || keyword.contains("工艺"))) {
            return WujinSearchIntent.PROCESS;
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            return WujinSearchIntent.PRODUCT;
        }
        return WujinSearchIntent.UNKNOWN;
    }

    private int resolveChildCount(String keyword, WujinSearchIntent intent) {
        if (intent == WujinSearchIntent.MATERIAL && keyword != null && keyword.contains("天然橡胶")) {
            return 18;
        }
        if (intent == WujinSearchIntent.PROCESS) {
            return 12;
        }
        return 8;
    }

    private List<String> resolveRiskTags(String industry) {
        if ("医疗器械".equals(industry)) {
            return Arrays.asList("多行业适用", "不可互换", "高风险采购提醒");
        }
        return Collections.emptyList();
    }

    private List<WujinAppSearchRespVO.LaneSummary> buildLaneSummaries(WujinAppSearchReqVO reqVO, WujinLane defaultLane) {
        List<WujinAppSearchRespVO.LaneSummary> summaries = new ArrayList<>();
        addSummary(summaries, WujinLane.PRODUCT, defaultLane,
                resolveLaneResultCount(reqVO, WujinLane.PRODUCT, 227, defaultLane),
                "可交易成品与使用“" + reqVO.getKeyword() + "”的下游商品", "查看成品规格");
        addSummary(summaries, WujinLane.PROCESS, defaultLane,
                resolveLaneResultCount(reqVO, WujinLane.PROCESS, 12, defaultLane),
                "混炼、成型、硫化、检测等加工能力", "查看加工服务");
        addSummary(summaries, WujinLane.MATERIAL, defaultLane,
                resolveLaneResultCount(reqVO, WujinLane.MATERIAL, 18, defaultLane),
                "天然橡胶、炭黑、骨架材料、化工助剂等原材料", "查看规格差异");
        summaries.sort((left, right) -> {
            if (left.getLane().equals(defaultLane.name())) {
                return -1;
            }
            if (right.getLane().equals(defaultLane.name())) {
                return 1;
            }
            return 0;
        });
        return summaries;
    }

    private void addSummary(List<WujinAppSearchRespVO.LaneSummary> summaries, WujinLane lane, WujinLane defaultLane,
                            int resultCount, String summary, String nextAction) {
        summaries.add(new WujinAppSearchRespVO.LaneSummary(lane.name(), laneName(lane),
                resultCount, summary, nextAction));
    }

    private int resolveLaneResultCount(WujinAppSearchReqVO reqVO, WujinLane lane, int fallbackCount, WujinLane defaultLane) {
        List<WujinChainEntityDO> entities = searchEnabledEntities(reqVO, lane);
        if (!entities.isEmpty()) {
            return entities.size();
        }
        return lane == defaultLane ? fallbackCount : Math.max(1, fallbackCount / 2);
    }

    private List<WujinChainEntityDO> searchEnabledEntities(WujinAppSearchReqVO reqVO, WujinLane lane) {
        if (reqVO.getKeyword() == null || reqVO.getKeyword().trim().isEmpty()) {
            return Collections.emptyList();
        }
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setLane(lane.name());
        listReqVO.setName(reqVO.getKeyword());
        listReqVO.setIndustry(reqVO.getIndustry());
        listReqVO.setStatus(ENABLED_STATUS);
        return chainEntityService.getEntityList(listReqVO);
    }

    private List<WujinCategoryDO> getEnabledCategories() {
        WujinCategoryListReqVO listReqVO = new WujinCategoryListReqVO();
        listReqVO.setStatus(ENABLED_STATUS);
        return categoryService.getCategoryList(listReqVO);
    }

    private List<WujinAppSearchRespVO.CategorySuggestion> buildCategorySuggestions(
            WujinAppSearchReqVO reqVO, List<WujinCategoryDO> categories) {
        String keyword = reqVO.getKeyword();
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, WujinCategoryDO> byId = new HashMap<>();
        for (WujinCategoryDO category : categories) {
            byId.put(category.getId(), category);
        }
        WujinLane requestedLane = parseEnum(WujinLane.class, reqVO.getRequestedLane(), WujinLane.PRODUCT);
        List<WujinCategoryDO> keywordMatches = findKeywordMatchedCategories(categories, keyword.trim(), null);
        List<WujinCategoryDO> matchedRoots = findKeywordMatchedCategories(
                categories, keyword.trim(), requestedLane.name());
        if (matchedRoots.isEmpty()) {
            matchedRoots.addAll(resolveMappedCategories(keywordMatches, requestedLane, byId));
        }
        matchedRoots.sort(Comparator.comparing(WujinCategoryDO::getLevel, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getId, Comparator.nullsLast(Long::compareTo)));

        List<WujinAppSearchRespVO.CategorySuggestion> suggestions = new ArrayList<>();
        Set<Long> suggestionIds = new HashSet<>();
        for (WujinCategoryDO root : matchedRoots) {
            List<WujinCategoryDO> descendants = collectDescendants(categories, root);
            if (descendants.isEmpty()) {
                descendants = Collections.singletonList(root);
            }
            for (WujinCategoryDO category : descendants) {
                if (!requestedLane.name().equals(category.getLane()) || !suggestionIds.add(category.getId())) {
                    continue;
                }
                suggestions.add(toCategorySuggestion(category, byId));
                if (suggestions.size() >= CATEGORY_SUGGESTION_LIMIT) {
                    return suggestions;
                }
            }
        }
        return suggestions;
    }

    private List<WujinAppSearchRespVO.CategoryGroup> buildCategoryGroups(
            WujinAppSearchReqVO reqVO, List<WujinCategoryDO> categories) {
        String keyword = reqVO.getKeyword();
        if (isBlank(keyword)) {
            return Collections.emptyList();
        }
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, WujinCategoryDO> byId = new HashMap<>();
        for (WujinCategoryDO category : categories) {
            byId.put(category.getId(), category);
        }
        WujinLane requestedLane = parseEnum(WujinLane.class, reqVO.getRequestedLane(), WujinLane.PRODUCT);
        List<WujinAppSearchRespVO.CategoryGroup> navigationGroups = buildCategoryNavigationGroups(
                reqVO.getCategoryId(), requestedLane, categories, byId);
        if (navigationGroups != null) {
            return navigationGroups;
        }
        List<WujinCategoryDO> keywordMatches = findKeywordMatchedCategories(categories, keyword.trim(), null);
        List<WujinCategoryDO> matchedRoots = findKeywordMatchedCategories(
                categories, keyword.trim(), requestedLane.name());
        if (matchedRoots.isEmpty()) {
            matchedRoots.addAll(resolveMappedCategories(keywordMatches, requestedLane, byId));
        }
        matchedRoots = removeNestedMatchedCategories(matchedRoots, byId);
        matchedRoots.sort(categoryComparator());
        if (matchedRoots.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<WujinAppSearchRespVO.CategorySuggestion>> grouped = new LinkedHashMap<>();
        Set<Long> categoryIds = new HashSet<>();
        int[] itemCount = new int[] {0};
        for (WujinCategoryDO root : matchedRoots) {
            addCategoryToGroup(grouped, categoryIds, itemCount, root, byId);
        }

        // Ambiguous matches stay broad. Only one clear category may reveal its next level.
        if (matchedRoots.size() == 1 && itemCount[0] < CATEGORY_SUGGESTION_LIMIT) {
            WujinCategoryDO root = matchedRoots.get(0);
            List<WujinCategoryDO> children = findDirectChildren(categories, root);
            if (shouldExpandNextLevel(root, children)) {
                for (WujinCategoryDO child : children) {
                    addCategoryToGroup(grouped, categoryIds, itemCount, child, byId);
                    if (itemCount[0] >= CATEGORY_SUGGESTION_LIMIT) {
                        break;
                    }
                }
            }
        }

        List<WujinAppSearchRespVO.CategoryGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<WujinAppSearchRespVO.CategorySuggestion>> entry : grouped.entrySet()) {
            groups.add(new WujinAppSearchRespVO.CategoryGroup(
                    entry.getKey(), categoryLevelName(entry.getKey()), entry.getValue()));
        }
        return groups;
    }

    private List<WujinAppSearchRespVO.CategoryGroup> buildCategoryNavigationGroups(
            Long selectedCategoryId, WujinLane requestedLane, List<WujinCategoryDO> categories,
            Map<Long, WujinCategoryDO> byId) {
        if (selectedCategoryId == null) {
            return null;
        }
        WujinCategoryDO selected = byId.get(selectedCategoryId);
        if (selected == null || !requestedLane.name().equals(selected.getLane())) {
            return null;
        }
        WujinCategoryDO parent = byId.get(selected.getParentId());
        if (parent == null || !selected.getLane().equals(parent.getLane())) {
            return null;
        }

        Map<Integer, List<WujinAppSearchRespVO.CategorySuggestion>> grouped = new LinkedHashMap<>();
        Set<Long> categoryIds = new HashSet<>();
        int[] itemCount = new int[] {0};
        addCategoryToGroup(grouped, categoryIds, itemCount, parent, byId);

        List<WujinCategoryDO> siblings = findDirectChildren(categories, parent);
        int remaining = CATEGORY_SUGGESTION_LIMIT - itemCount[0];
        List<WujinCategoryDO> visibleSiblings = new ArrayList<>(
                siblings.subList(0, Math.min(remaining, siblings.size())));
        if (!visibleSiblings.contains(selected) && !visibleSiblings.isEmpty()) {
            visibleSiblings.set(visibleSiblings.size() - 1, selected);
            visibleSiblings.sort(categoryComparator());
        }
        for (WujinCategoryDO sibling : visibleSiblings) {
            addCategoryToGroup(grouped, categoryIds, itemCount, sibling, byId);
        }

        List<WujinAppSearchRespVO.CategoryGroup> groups = new ArrayList<>();
        for (Map.Entry<Integer, List<WujinAppSearchRespVO.CategorySuggestion>> entry : grouped.entrySet()) {
            groups.add(new WujinAppSearchRespVO.CategoryGroup(
                    entry.getKey(), categoryLevelName(entry.getKey()), entry.getValue()));
        }
        return groups;
    }

    private List<WujinCategoryDO> removeNestedMatchedCategories(List<WujinCategoryDO> matches,
                                                                 Map<Long, WujinCategoryDO> byId) {
        Set<Long> matchedIds = new HashSet<>();
        for (WujinCategoryDO match : matches) {
            matchedIds.add(match.getId());
        }
        List<WujinCategoryDO> roots = new ArrayList<>();
        for (WujinCategoryDO match : matches) {
            WujinCategoryDO parent = byId.get(match.getParentId());
            boolean nested = false;
            while (parent != null) {
                if (matchedIds.contains(parent.getId())) {
                    nested = true;
                    break;
                }
                parent = byId.get(parent.getParentId());
            }
            if (!nested) {
                roots.add(match);
            }
        }
        return roots;
    }

    private List<WujinCategoryDO> findDirectChildren(List<WujinCategoryDO> categories, WujinCategoryDO parent) {
        List<WujinCategoryDO> children = new ArrayList<>();
        for (WujinCategoryDO category : categories) {
            if (parent.getId().equals(category.getParentId()) && parent.getLane().equals(category.getLane())) {
                children.add(category);
            }
        }
        children.sort(categoryComparator());
        return children;
    }

    private boolean shouldExpandNextLevel(WujinCategoryDO root, List<WujinCategoryDO> children) {
        int level = defaultInt(root.getLevel(), 1);
        int displayDepth = defaultInt(root.getDisplayDepth(), level);
        return displayDepth > level && children.size() >= MIN_CHILDREN_TO_EXPAND;
    }

    private void addCategoryToGroup(
            Map<Integer, List<WujinAppSearchRespVO.CategorySuggestion>> grouped,
            Set<Long> categoryIds, int[] itemCount, WujinCategoryDO category,
            Map<Long, WujinCategoryDO> byId) {
        if (itemCount[0] >= CATEGORY_SUGGESTION_LIMIT || !categoryIds.add(category.getId())) {
            return;
        }
        int level = defaultInt(category.getLevel(), 1);
        grouped.computeIfAbsent(level, ignored -> new ArrayList<>()).add(toCategorySuggestion(category, byId));
        itemCount[0]++;
    }

    private Comparator<WujinCategoryDO> categoryComparator() {
        return Comparator.comparing(WujinCategoryDO::getLevel, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getId, Comparator.nullsLast(Long::compareTo));
    }

    private String categoryLevelName(Integer level) {
        if (Integer.valueOf(1).equals(level)) {
            return "一级分类";
        }
        if (Integer.valueOf(2).equals(level)) {
            return "二级分类";
        }
        if (Integer.valueOf(3).equals(level)) {
            return "三级分类";
        }
        return defaultInt(level, 1) + "级分类";
    }

    private List<WujinCategoryDO> findKeywordMatchedCategories(List<WujinCategoryDO> categories,
                                                                String keyword, String lane) {
        List<WujinCategoryDO> matches = new ArrayList<>();
        for (WujinCategoryDO category : categories) {
            if (lane != null && !lane.equals(category.getLane())) {
                continue;
            }
            boolean nameMatched = category.getName() != null && category.getName().contains(keyword);
            boolean descriptionMatched = category.getDescription() != null
                    && category.getDescription().contains(keyword);
            if (nameMatched || descriptionMatched) {
                matches.add(category);
            }
        }
        return matches;
    }

    private List<WujinCategoryDO> resolveMappedCategories(List<WujinCategoryDO> sourceCategories,
                                                           WujinLane requestedLane,
                                                           Map<Long, WujinCategoryDO> byId) {
        List<WujinCategoryDO> mappedCategories = new ArrayList<>();
        Set<Long> mappedIds = new HashSet<>();
        for (WujinCategoryDO source : sourceCategories) {
            WujinCategoryMappingListReqVO outgoingReqVO = new WujinCategoryMappingListReqVO();
            outgoingReqVO.setSourceCategoryId(source.getId());
            outgoingReqVO.setTargetLane(requestedLane.name());
            outgoingReqVO.setStatus(ENABLED_STATUS);
            for (WujinCategoryMappingDO mapping : categoryMappingService.getMappingList(outgoingReqVO)) {
                addMappedCategory(mappedCategories, mappedIds, byId.get(mapping.getTargetCategoryId()), requestedLane);
            }

            WujinCategoryMappingListReqVO incomingReqVO = new WujinCategoryMappingListReqVO();
            incomingReqVO.setTargetCategoryId(source.getId());
            incomingReqVO.setSourceLane(requestedLane.name());
            incomingReqVO.setStatus(ENABLED_STATUS);
            for (WujinCategoryMappingDO mapping : categoryMappingService.getMappingList(incomingReqVO)) {
                addMappedCategory(mappedCategories, mappedIds, byId.get(mapping.getSourceCategoryId()), requestedLane);
            }
        }
        return mappedCategories;
    }

    private void addMappedCategory(List<WujinCategoryDO> mappedCategories, Set<Long> mappedIds,
                                   WujinCategoryDO category, WujinLane requestedLane) {
        if (category != null && requestedLane.name().equals(category.getLane()) && mappedIds.add(category.getId())) {
            mappedCategories.add(category);
        }
    }

    private List<WujinCategoryDO> collectDescendants(List<WujinCategoryDO> categories, WujinCategoryDO root) {
        List<WujinCategoryDO> descendants = new ArrayList<>();
        for (WujinCategoryDO category : categories) {
            if (isDescendantOf(category, root, categories)) {
                descendants.add(category);
            }
        }
        descendants.sort(Comparator.comparing(WujinCategoryDO::getLevel, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WujinCategoryDO::getId, Comparator.nullsLast(Long::compareTo)));
        return descendants;
    }

    private boolean isDescendantOf(WujinCategoryDO category, WujinCategoryDO root, List<WujinCategoryDO> categories) {
        Long parentId = category.getParentId();
        while (parentId != null && !WujinCategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            if (parentId.equals(root.getId())) {
                return true;
            }
            WujinCategoryDO parent = findCategory(categories, parentId);
            parentId = parent == null ? null : parent.getParentId();
        }
        return false;
    }

    private WujinCategoryDO findCategory(List<WujinCategoryDO> categories, Long id) {
        for (WujinCategoryDO category : categories) {
            if (id != null && id.equals(category.getId())) {
                return category;
            }
        }
        return null;
    }

    private WujinAppSearchRespVO.CategorySuggestion toCategorySuggestion(WujinCategoryDO category,
                                                                         Map<Long, WujinCategoryDO> byId) {
        return new WujinAppSearchRespVO.CategorySuggestion(category.getId(), category.getName(),
                buildCategoryPath(category, byId), category.getLane(), laneName(parseEnum(WujinLane.class,
                category.getLane(), WujinLane.PRODUCT)), category.getLevel());
    }

    private String buildCategoryPath(WujinCategoryDO category, Map<Long, WujinCategoryDO> byId) {
        List<String> names = new ArrayList<>();
        WujinCategoryDO current = category;
        while (current != null && current.getName() != null) {
            names.add(0, current.getName());
            Long parentId = current.getParentId();
            current = parentId == null || WujinCategoryDO.PARENT_ID_ROOT.equals(parentId) ? null : byId.get(parentId);
        }
        return String.join(" > ", names);
    }

    private String buildTraceHint(WujinAppSearchReqVO reqVO, WujinSearchDecision decision) {
        if (reqVO.getSourceKeyword() != null && !reqVO.getSourceKeyword().trim().isEmpty()) {
            return "搜索路径：" + reqVO.getSourceKeyword() + " → " + reqVO.getKeyword();
        }
        if (decision.getDefaultLane() == WujinLane.MATERIAL) {
            return "可从“" + reqVO.getKeyword() + "”反查下游成品，也可继续查看规格和行业差异";
        }
        return "可从“" + reqVO.getKeyword() + "”继续下钻原材料和加工工艺";
    }

    private void persistBehaviorLog(WujinAppSearchReqVO reqVO, WujinSearchIntent intent, WujinSearchDecision decision) {
        WujinSearchBehaviorLogSaveReqVO logReqVO = new WujinSearchBehaviorLogSaveReqVO();
        logReqVO.setUserId(reqVO.getUserId());
        logReqVO.setKeyword(reqVO.getKeyword());
        logReqVO.setIntent(intent.name());
        logReqVO.setResultLane(decision.getDefaultLane().name());
        logReqVO.setIndustryCode(reqVO.getIndustry());
        logReqVO.setChainViewed(Boolean.TRUE.equals(reqVO.getChainViewed()));
        logReqVO.setClassificationCorrect(true);
        logReqVO.setHighRiskWarningTriggered(decision.isRiskWarningRequired());
        logReqVO.setSatisfactionScore(reqVO.getSatisfactionScore());
        logReqVO.setResponseTimeMillis(reqVO.getResponseTimeMillis() == null ? 0L : reqVO.getResponseTimeMillis());
        behaviorLogService.createBehaviorLog(logReqVO);
    }

    private WujinSearchDecision applyRuleConfig(WujinAppSearchReqVO reqVO, WujinSearchDecision decision) {
        WujinSearchRuleContext context = new WujinSearchRuleContext(reqVO.getKeyword(), decision.getDefaultLane(),
                reqVO.getIndustry(), parseEnum(WujinEntryPath.class, reqVO.getEntryPath(), WujinEntryPath.DIRECT_SEARCH),
                parseEnum(WujinLane.class, reqVO.getSourceLane(), null), reqVO.getSourceKeyword());
        WujinSearchRuleResult result = ruleEngine.apply(context, decision.getGranularity(), decision.getDefaultLane(),
                decision.isRiskWarningRequired(), decision.getRiskWarningText(), decision.getExplanation(),
                resolveEnabledRules());
        return new WujinSearchDecision(result.getLane(), result.getGranularity(), result.getExplanation(),
                decision.getContextHint(), result.isRiskWarningRequired(), result.getRiskWarningText());
    }

    private List<WujinSearchRuleConfigDO> resolveEnabledRules() {
        WujinSearchRuleConfigListReqVO listReqVO = new WujinSearchRuleConfigListReqVO();
        listReqVO.setStatus(ENABLED_STATUS);
        return ruleConfigService.getRuleConfigList(listReqVO);
    }

    private String resolveRiskWarningText(WujinAppSearchReqVO reqVO, WujinSearchDecision decision) {
        if (decision.getRiskWarningText() != null && !decision.getRiskWarningText().trim().isEmpty()) {
            return decision.getRiskWarningText();
        }
        return reqVO.getKeyword() + "在不同行业存在标准差异，请确认应用场景后再联系供应商";
    }

    private String laneName(WujinLane lane) {
        if (lane == WujinLane.MATERIAL) {
            return "原材料";
        }
        if (lane == WujinLane.PROCESS) {
            return "加工";
        }
        return "成品";
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }
}
