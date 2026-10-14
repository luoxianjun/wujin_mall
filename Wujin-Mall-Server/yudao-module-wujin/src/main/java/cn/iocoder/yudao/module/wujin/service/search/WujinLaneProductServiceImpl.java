package cn.iocoder.yudao.module.wujin.service.search;

import cn.iocoder.yudao.module.product.api.brand.ProductBrandApi;
import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductPageRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.search.vo.WujinLaneProductReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.category.WujinCategoryAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class WujinLaneProductServiceImpl implements WujinLaneProductService {

    static final int KEYWORD_PRODUCT_LIMIT = 200;
    private static final int ENABLED_STATUS = 0;
    private static final int SUPPLY_STATUS_ACTIVE = 0;
    private static final int PRODUCT_STATUS_ENABLE = 1;
    /**
     * 产业链关系生效状态：1 为关系表生效值，20/30 为历史数据沿用的审核通过值
     */
    static final List<Integer> EFFECTIVE_RELATION_STATUSES = Arrays.asList(1, 20, 30);
    private static final int MIN_CATEGORY_NAME_LENGTH = 2;

    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinCategoryAdminService categoryService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinProductAttributeService productAttributeService;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductBrandApi productBrandApi;

    @Override
    public WujinLaneProductPageRespVO getLaneProductPage(WujinLaneProductReqVO reqVO) {
        WujinLane lane = parseLane(reqVO.getLane());
        String keyword = reqVO.getKeyword().trim();
        LaneContext context = new LaneContext(capabilityService.getCapabilityList(
                new WujinMerchantSupplyCapabilityListReqVO()));
        LaneCandidates candidates = collectCandidates(keyword, lane, reqVO.getSourceProductId(), context);

        List<Candidate> list = candidates.products;
        boolean filtered = false;
        if (reqVO.getCategoryId() != null) {
            list = filterByCategory(list, reqVO.getCategoryId(), context);
            filtered = true;
        }
        List<WujinLaneProductPageRespVO.BrandFacet> brands = buildBrandFacets(list);
        if (reqVO.getBrandId() != null) {
            List<Candidate> brandMatched = new ArrayList<>();
            for (Candidate candidate : list) {
                if (reqVO.getBrandId().equals(candidate.spu.getBrandId())) {
                    brandMatched.add(candidate);
                }
            }
            list = brandMatched;
            filtered = true;
        }
        list.sort(comparator(reqVO.getSort(), keyword));

        int pageSize = reqVO.getPageSize() == null ? 10 : reqVO.getPageSize();
        int pageNo = reqVO.getPageNo() == null ? 1 : reqVO.getPageNo();
        int from = Math.min(list.size(), (pageNo - 1) * pageSize);
        int to = Math.min(list.size(), from + pageSize);

        WujinLaneProductPageRespVO respVO = new WujinLaneProductPageRespVO();
        respVO.setLane(lane.name());
        respVO.setTotal((long) list.size());
        respVO.setList(toItems(list.subList(from, to), lane, context));
        respVO.setBrands(brands);
        respVO.setUpstreamEntities(toEntityRefs(candidates.entities));
        respVO.setEmptyHint(list.isEmpty() ? emptyHint(lane, candidates.entities.isEmpty(), filtered) : null);
        return respVO;
    }

    @Override
    public Map<String, Integer> countLaneProducts(String keyword, Long sourceProductId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (WujinLane lane : WujinLane.values()) {
            counts.put(lane.name(), 0);
        }
        if (isBlank(keyword)) {
            return counts;
        }
        LaneContext context = new LaneContext(capabilityService.getCapabilityList(
                new WujinMerchantSupplyCapabilityListReqVO()));
        for (WujinLane lane : WujinLane.values()) {
            counts.put(lane.name(), collectCandidates(keyword.trim(), lane, sourceProductId, context).products.size());
        }
        return counts;
    }

    private LaneCandidates collectCandidates(String keyword, WujinLane lane, Long sourceProductId,
                                             LaneContext context) {
        LaneCandidates result = new LaneCandidates();
        Map<Long, ProductSpuRespDTO> spuById = new LinkedHashMap<>();
        Set<Long> entityIds = new HashSet<>();
        if (lane == WujinLane.PRODUCT) {
            for (ProductSpuRespDTO spu : productSpuApi.getEnabledSpuListByKeyword(keyword, KEYWORD_PRODUCT_LIMIT)) {
                spuById.put(spu.getId(), spu);
            }
            for (WujinChainEntityDO entity : findEntities(keyword, WujinLane.PRODUCT)) {
                entityIds.add(entity.getId());
            }
        } else {
            for (WujinChainEntityDO entity : findEntities(keyword, lane)) {
                result.entities.putIfAbsent(entity.getId(), entity);
            }
            WujinRelationType relationType = lane == WujinLane.MATERIAL
                    ? WujinRelationType.REQUIRES_MATERIAL : WujinRelationType.REQUIRES_PROCESS;
            for (WujinChainEntityDO source : findSourceEntities(keyword, sourceProductId, context)) {
                for (WujinChainEntityDO target : findUpstreamEntities(source.getId(), relationType, lane, context)) {
                    result.entities.putIfAbsent(target.getId(), target);
                }
            }
            entityIds.addAll(result.entities.keySet());
        }

        Set<Long> productIds = new LinkedHashSet<>(spuById.keySet());
        for (WujinMerchantSupplyCapabilityDO capability : context.capabilities) {
            if (!isActiveIn(capability, lane) || capability.getProductId() == null) {
                continue;
            }
            if (entityIds.contains(capability.getEntityId()) || contains(capability.getProductName(), keyword)) {
                productIds.add(capability.getProductId());
            }
        }
        List<Long> missingIds = new ArrayList<>();
        for (Long productId : productIds) {
            if (!spuById.containsKey(productId)) {
                missingIds.add(productId);
            }
        }
        if (!missingIds.isEmpty()) {
            for (ProductSpuRespDTO spu : productSpuApi.getSpuList(missingIds)) {
                if (spu != null) {
                    spuById.put(spu.getId(), spu);
                }
            }
        }

        for (Long productId : productIds) {
            ProductSpuRespDTO spu = spuById.get(productId);
            if (spu == null || spu.getStatus() == null || spu.getStatus() != PRODUCT_STATUS_ENABLE) {
                continue;
            }
            if (context.homeLane(productId) != lane) {
                continue;
            }
            result.products.add(new Candidate(spu, context.laneCapability(productId, lane)));
        }
        return result;
    }

    private List<WujinChainEntityDO> findEntities(String keyword, WujinLane lane) {
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setName(keyword);
        listReqVO.setLane(lane.name());
        listReqVO.setStatus(ENABLED_STATUS);
        return chainEntityService.getEntityList(listReqVO);
    }

    private List<WujinChainEntityDO> findSourceEntities(String keyword, Long sourceProductId, LaneContext context) {
        Map<Long, WujinChainEntityDO> sources = new LinkedHashMap<>();
        if (sourceProductId != null) {
            for (WujinMerchantSupplyCapabilityDO capability : context.capabilities) {
                if (sourceProductId.equals(capability.getProductId())
                        && WujinLane.PRODUCT.name().equals(capability.getLane())) {
                    WujinChainEntityDO entity = context.entity(capability.getEntityId(), chainEntityService);
                    if (entity != null) {
                        sources.putIfAbsent(entity.getId(), entity);
                    }
                }
            }
        }
        for (WujinChainEntityDO entity : findEntities(keyword, WujinLane.PRODUCT)) {
            sources.putIfAbsent(entity.getId(), entity);
        }
        // 关键词命中成品商品名时，沿该商品的成品实体继续查上游
        for (WujinMerchantSupplyCapabilityDO capability : context.capabilities) {
            if (WujinLane.PRODUCT.name().equals(capability.getLane())
                    && contains(capability.getProductName(), keyword)) {
                WujinChainEntityDO entity = context.entity(capability.getEntityId(), chainEntityService);
                if (entity != null && ENABLED_STATUS == defaultInt(entity.getStatus(), -1)) {
                    sources.putIfAbsent(entity.getId(), entity);
                }
            }
        }
        return new ArrayList<>(sources.values());
    }

    private List<WujinChainEntityDO> findUpstreamEntities(Long sourceEntityId, WujinRelationType relationType,
                                                          WujinLane lane, LaneContext context) {
        WujinChainEntityRelationListReqVO relationReqVO = new WujinChainEntityRelationListReqVO();
        relationReqVO.setSourceEntityId(sourceEntityId);
        relationReqVO.setRelationType(relationType.name());
        List<WujinChainEntityDO> targets = new ArrayList<>();
        for (WujinChainEntityRelationDO relation : relationService.getRelationList(relationReqVO)) {
            if (!EFFECTIVE_RELATION_STATUSES.contains(relation.getAuditStatus())) {
                continue;
            }
            WujinChainEntityDO target = context.entity(relation.getTargetEntityId(), chainEntityService);
            if (target != null && lane.name().equals(target.getLane())
                    && ENABLED_STATUS == defaultInt(target.getStatus(), -1)) {
                targets.add(target);
            }
        }
        return targets;
    }

    private List<Candidate> filterByCategory(List<Candidate> candidates, Long categoryId, LaneContext context) {
        WujinCategoryListReqVO listReqVO = new WujinCategoryListReqVO();
        listReqVO.setStatus(ENABLED_STATUS);
        List<WujinCategoryDO> categories = categoryService.getCategoryList(listReqVO);
        Set<Long> subtreeIds = new HashSet<>();
        Set<String> subtreeNames = new HashSet<>();
        collectSubtree(categories, categoryId, subtreeIds, subtreeNames);
        if (subtreeIds.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Set<Long>> submissionCategories = new HashMap<>();
        for (WujinMerchantRelationSubmissionDO submission
                : submissionService.getSubmissionList(new WujinMerchantRelationSubmissionListReqVO())) {
            if (submission.getProductId() != null && submission.getProductCategoryId() != null) {
                submissionCategories.computeIfAbsent(submission.getProductId(), ignored -> new HashSet<>())
                        .add(submission.getProductCategoryId());
            }
        }

        List<Candidate> matched = new ArrayList<>();
        for (Candidate candidate : candidates) {
            if (matchesCategory(candidate, subtreeIds, subtreeNames, submissionCategories, context)) {
                matched.add(candidate);
            }
        }
        return matched;
    }

    private void collectSubtree(List<WujinCategoryDO> categories, Long rootId, Set<Long> ids, Set<String> names) {
        for (WujinCategoryDO category : categories) {
            if (rootId.equals(category.getId()) && ids.add(category.getId())) {
                if (category.getName() != null && category.getName().trim().length() >= MIN_CATEGORY_NAME_LENGTH) {
                    names.add(category.getName().trim());
                }
                for (WujinCategoryDO child : categories) {
                    if (rootId.equals(child.getParentId())) {
                        collectSubtree(categories, child.getId(), ids, names);
                    }
                }
            }
        }
    }

    private boolean matchesCategory(Candidate candidate, Set<Long> subtreeIds, Set<String> subtreeNames,
                                    Map<Long, Set<Long>> submissionCategories, LaneContext context) {
        Set<Long> declared = submissionCategories.get(candidate.spu.getId());
        if (declared != null) {
            for (Long categoryId : declared) {
                if (subtreeIds.contains(categoryId)) {
                    return true;
                }
            }
        }
        for (String name : subtreeNames) {
            if (contains(candidate.spu.getName(), name)) {
                return true;
            }
        }
        if (candidate.capability != null) {
            WujinChainEntityDO entity = context.entity(candidate.capability.getEntityId(), chainEntityService);
            return entity != null && subtreeNames.contains(entity.getName());
        }
        return false;
    }

    private List<WujinLaneProductPageRespVO.BrandFacet> buildBrandFacets(List<Candidate> candidates) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            Long brandId = candidate.spu.getBrandId();
            if (brandId != null) {
                counts.put(brandId, counts.containsKey(brandId) ? counts.get(brandId) + 1 : 1);
            }
        }
        if (counts.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> names = brandNames(counts.keySet());
        List<WujinLaneProductPageRespVO.BrandFacet> facets = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
            if (!names.containsKey(entry.getKey())) {
                continue;
            }
            WujinLaneProductPageRespVO.BrandFacet facet = new WujinLaneProductPageRespVO.BrandFacet();
            facet.setBrandId(entry.getKey());
            facet.setBrandName(names.get(entry.getKey()));
            facet.setProductCount(entry.getValue());
            facets.add(facet);
        }
        facets.sort((left, right) -> right.getProductCount().compareTo(left.getProductCount()));
        return facets;
    }

    private Map<Long, String> brandNames(Set<Long> brandIds) {
        Map<Long, String> names = new HashMap<>();
        if (brandIds.isEmpty()) {
            return names;
        }
        for (ProductBrandRespDTO brand : productBrandApi.getBrandList(brandIds)) {
            if (brand != null && brand.getName() != null) {
                names.put(brand.getId(), brand.getName());
            }
        }
        return names;
    }

    private Comparator<Candidate> comparator(String sort, String keyword) {
        Comparator<Candidate> bySales = Comparator.comparing(Candidate::salesCount).reversed();
        Comparator<Candidate> byId = Comparator.comparing((Candidate candidate) -> candidate.spu.getId(),
                Comparator.nullsLast(Comparator.reverseOrder()));
        if (WujinLaneProductReqVO.SORT_PRICE_ASC.equals(sort)) {
            return Comparator.comparing(Candidate::price).thenComparing(byId);
        }
        if (WujinLaneProductReqVO.SORT_PRICE_DESC.equals(sort)) {
            return Comparator.comparing(Candidate::price).reversed().thenComparing(byId);
        }
        if (WujinLaneProductReqVO.SORT_SALES.equals(sort)) {
            return bySales.thenComparing(byId);
        }
        // 默认：商品名直接命中关键词的排在前面
        return Comparator.comparing((Candidate candidate) -> contains(candidate.spu.getName(), keyword) ? 0 : 1)
                .thenComparing(bySales).thenComparing(byId);
    }

    private List<WujinLaneProductPageRespVO.Item> toItems(List<Candidate> page, WujinLane lane, LaneContext context) {
        Set<Long> brandIds = new HashSet<>();
        for (Candidate candidate : page) {
            if (candidate.spu.getBrandId() != null) {
                brandIds.add(candidate.spu.getBrandId());
            }
        }
        Map<Long, String> brands = brandNames(brandIds);
        List<WujinLaneProductPageRespVO.Item> items = new ArrayList<>();
        for (Candidate candidate : page) {
            ProductSpuRespDTO spu = candidate.spu;
            WujinLaneProductPageRespVO.Item item = new WujinLaneProductPageRespVO.Item();
            item.setId(spu.getId());
            item.setName(spu.getName());
            item.setPicUrl(spu.getPicUrl());
            item.setPrice(spu.getPrice());
            item.setMarketPrice(spu.getMarketPrice());
            item.setStock(spu.getStock());
            item.setSalesCount(candidate.salesCount());
            item.setBrandId(spu.getBrandId());
            item.setBrandName(brands.get(spu.getBrandId()));
            item.setLane(lane.name());
            item.setTags(productAttributeService.getApprovedTagNames(spu.getId()));
            WujinMerchantSupplyCapabilityDO capability = candidate.capability;
            if (capability != null) {
                WujinChainEntityDO entity = context.entity(capability.getEntityId(), chainEntityService);
                item.setMerchantId(capability.getMerchantId());
                item.setEntityId(capability.getEntityId());
                item.setEntityName(entity == null ? null : entity.getName());
                item.setMinOrderQuantity(capability.getMinOrderQuantity());
                item.setDeliveryDays(capability.getDeliveryDays());
                item.setServiceArea(capability.getServiceArea());
                if (lane == WujinLane.PRODUCT) {
                    item.setMaterialCount(findUpstreamEntities(capability.getEntityId(),
                            WujinRelationType.REQUIRES_MATERIAL, WujinLane.MATERIAL, context).size());
                }
            }
            items.add(item);
        }
        return items;
    }

    private List<WujinLaneProductPageRespVO.EntityRef> toEntityRefs(Map<Long, WujinChainEntityDO> entities) {
        List<WujinLaneProductPageRespVO.EntityRef> refs = new ArrayList<>();
        for (WujinChainEntityDO entity : entities.values()) {
            WujinLaneProductPageRespVO.EntityRef ref = new WujinLaneProductPageRespVO.EntityRef();
            ref.setId(entity.getId());
            ref.setName(entity.getName());
            ref.setLane(entity.getLane());
            refs.add(ref);
        }
        return refs;
    }

    private String emptyHint(WujinLane lane, boolean noEntities, boolean filtered) {
        if (filtered) {
            return "当前筛选条件下暂无商品，试试其他分类或品牌";
        }
        if (lane == WujinLane.MATERIAL) {
            return noEntities ? "暂无上游关联的原材料" : "已找到上游原材料，暂无在售商品，可提交寻源需求";
        }
        if (lane == WujinLane.PROCESS) {
            return noEntities ? "暂无上游关联的加工工艺" : "已找到上游加工工艺，暂无可承接的加工服务，可提交寻源需求";
        }
        return "未找到相关结果，试试其他关键词";
    }

    private boolean isActiveIn(WujinMerchantSupplyCapabilityDO capability, WujinLane lane) {
        return lane.name().equals(capability.getLane())
                && SUPPLY_STATUS_ACTIVE == defaultInt(capability.getSupplyStatus(), -1);
    }

    private WujinLane parseLane(String lane) {
        if (isBlank(lane)) {
            return WujinLane.PRODUCT;
        }
        try {
            return WujinLane.valueOf(lane.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return WujinLane.PRODUCT;
        }
    }

    private static boolean contains(String text, String keyword) {
        return text != null && keyword != null && !keyword.isEmpty() && text.contains(keyword);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static int defaultInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static class Candidate {

        private final ProductSpuRespDTO spu;
        private final WujinMerchantSupplyCapabilityDO capability;

        Candidate(ProductSpuRespDTO spu, WujinMerchantSupplyCapabilityDO capability) {
            this.spu = spu;
            this.capability = capability;
        }

        int salesCount() {
            return defaultInt(spu.getSalesCount(), 0) + defaultInt(spu.getVirtualSalesCount(), 0);
        }

        int price() {
            return defaultInt(spu.getPrice(), Integer.MAX_VALUE);
        }
    }

    private static class LaneCandidates {

        private final List<Candidate> products = new ArrayList<>();
        private final Map<Long, WujinChainEntityDO> entities = new LinkedHashMap<>();
    }

    /**
     * 一次请求内共享的供应能力索引与实体缓存
     */
    private static class LaneContext {

        private final List<WujinMerchantSupplyCapabilityDO> capabilities;
        private final Map<Long, WujinChainEntityDO> entityCache = new HashMap<>();

        LaneContext(List<WujinMerchantSupplyCapabilityDO> capabilities) {
            this.capabilities = capabilities == null
                    ? Collections.<WujinMerchantSupplyCapabilityDO>emptyList() : capabilities;
        }

        /**
         * 商品归属泳道：有成品能力即为成品，其次加工、原材料；未建立供应能力索引的商城商品视为成品
         */
        WujinLane homeLane(Long productId) {
            Set<String> lanes = new HashSet<>();
            for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
                if (productId.equals(capability.getProductId()) && capability.getLane() != null) {
                    lanes.add(capability.getLane());
                }
            }
            if (lanes.isEmpty() || lanes.contains(WujinLane.PRODUCT.name())) {
                return WujinLane.PRODUCT;
            }
            return lanes.contains(WujinLane.PROCESS.name()) ? WujinLane.PROCESS : WujinLane.MATERIAL;
        }

        WujinMerchantSupplyCapabilityDO laneCapability(Long productId, WujinLane lane) {
            WujinMerchantSupplyCapabilityDO fallback = null;
            for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
                if (!productId.equals(capability.getProductId()) || !lane.name().equals(capability.getLane())) {
                    continue;
                }
                if (SUPPLY_STATUS_ACTIVE == defaultInt(capability.getSupplyStatus(), -1)) {
                    return capability;
                }
                fallback = fallback == null ? capability : fallback;
            }
            return fallback;
        }

        WujinChainEntityDO entity(Long entityId, WujinChainEntityAdminService entityService) {
            if (entityId == null) {
                return null;
            }
            if (!entityCache.containsKey(entityId)) {
                entityCache.put(entityId, entityService.getEntity(entityId));
            }
            return entityCache.get(entityId);
        }
    }
}
