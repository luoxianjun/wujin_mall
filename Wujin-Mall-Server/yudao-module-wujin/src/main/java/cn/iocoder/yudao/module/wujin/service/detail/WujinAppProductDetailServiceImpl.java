package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.module.product.api.brand.ProductBrandApi;
import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinProductDetailRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class WujinAppProductDetailServiceImpl implements WujinAppProductDetailService {

    private static final int PRODUCT_STATUS_ENABLE = 1;
    private static final int SUPPLY_STATUS_ACTIVE = 0;
    private static final int ENABLED_STATUS = 0;
    private static final int RELATED_PRODUCT_LIMIT = 6;
    private static final List<Integer> EFFECTIVE_RELATION_STATUSES = Arrays.asList(1, 20, 30);

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductBrandApi productBrandApi;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinProductAttributeService productAttributeService;

    @Override
    public WujinProductDetailRespVO getProductDetail(Long productId) {
        WujinProductDetailRespVO respVO = new WujinProductDetailRespVO();
        respVO.setId(productId);
        ProductSpuRespDTO spu = productId == null ? null : productSpuApi.getSpu(productId);
        if (spu == null || spu.getStatus() == null || spu.getStatus() != PRODUCT_STATUS_ENABLE) {
            respVO.setExists(false);
            respVO.setName(spu == null ? null : spu.getName());
            return respVO;
        }

        respVO.setExists(true);
        respVO.setName(spu.getName());
        respVO.setPicUrl(spu.getPicUrl());
        respVO.setSliderPicUrls(spu.getSliderPicUrls());
        respVO.setPrice(spu.getPrice());
        respVO.setMarketPrice(spu.getMarketPrice());
        respVO.setStock(spu.getStock());
        respVO.setSalesCount(defaultInt(spu.getSalesCount()) + defaultInt(spu.getVirtualSalesCount()));
        respVO.setIntroduction(spu.getIntroduction());
        respVO.setDescription(spu.getDescription());
        respVO.setBrandName(brandName(spu.getBrandId()));

        WujinMerchantSupplyCapabilityDO capability = primaryCapability(productId);
        WujinChainEntityDO entity = capability == null ? null : chainEntityService.getEntity(capability.getEntityId());
        respVO.setLane(capability == null ? WujinLane.PRODUCT.name() : capability.getLane());
        if (capability != null) {
            respVO.setMerchantId(capability.getMerchantId());
            respVO.setEntityId(capability.getEntityId());
            respVO.setMinOrderQuantity(capability.getMinOrderQuantity());
            respVO.setDeliveryDays(capability.getDeliveryDays());
            respVO.setServiceArea(capability.getServiceArea());
        }
        if (entity != null) {
            respVO.setEntityName(entity.getName());
            respVO.setRiskNote(entity.getRiskNote());
        }

        respVO.setSpecs(buildSpecs(productId, respVO));
        respVO.setTags(productAttributeService.getApprovedTagNames(productId));
        respVO.setUpstreamMaterials(entity == null ? Collections.<WujinProductDetailRespVO.EntityRef>emptyList()
                : upstream(entity.getId(), WujinRelationType.REQUIRES_MATERIAL, WujinLane.MATERIAL));
        respVO.setUpstreamProcesses(entity == null ? Collections.<WujinProductDetailRespVO.EntityRef>emptyList()
                : upstream(entity.getId(), WujinRelationType.REQUIRES_PROCESS, WujinLane.PROCESS));
        respVO.setRelatedProducts(capability == null
                ? Collections.<WujinProductDetailRespVO.RelatedProduct>emptyList()
                : relatedProducts(productId, capability));
        return respVO;
    }

    /**
     * 商品主供应能力：优先成品泳道、启用中的供应能力
     */
    private WujinMerchantSupplyCapabilityDO primaryCapability(Long productId) {
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setProductId(productId);
        WujinMerchantSupplyCapabilityDO best = null;
        int bestScore = -1;
        for (WujinMerchantSupplyCapabilityDO capability : capabilityService.getCapabilityList(listReqVO)) {
            int score = (WujinLane.PRODUCT.name().equals(capability.getLane()) ? 2 : 0)
                    + (Integer.valueOf(SUPPLY_STATUS_ACTIVE).equals(capability.getSupplyStatus()) ? 1 : 0);
            if (score > bestScore) {
                best = capability;
                bestScore = score;
            }
        }
        return best;
    }

    private List<WujinProductDetailRespVO.Spec> buildSpecs(Long productId, WujinProductDetailRespVO detail) {
        Map<String, String> specs = new LinkedHashMap<>();
        for (WujinProductAttributeValueDO value : productAttributeService.getAttributeValueList(null, productId)) {
            if (!isBlank(value.getAttributeName()) && !isBlank(value.getAttributeValue())) {
                specs.putIfAbsent(value.getAttributeName(), value.getAttributeValue());
            }
        }
        putIfPresent(specs, "品牌", detail.getBrandName());
        putIfPresent(specs, "产业链品类", detail.getEntityName());
        putIfPresent(specs, "服务区域", detail.getServiceArea());
        List<WujinProductDetailRespVO.Spec> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            result.add(new WujinProductDetailRespVO.Spec(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private List<WujinProductDetailRespVO.EntityRef> upstream(Long entityId, WujinRelationType relationType,
                                                             WujinLane lane) {
        WujinChainEntityRelationListReqVO listReqVO = new WujinChainEntityRelationListReqVO();
        listReqVO.setSourceEntityId(entityId);
        listReqVO.setRelationType(relationType.name());
        Set<Long> seen = new LinkedHashSet<>();
        List<WujinProductDetailRespVO.EntityRef> refs = new ArrayList<>();
        for (WujinChainEntityRelationDO relation : relationService.getRelationList(listReqVO)) {
            if (!EFFECTIVE_RELATION_STATUSES.contains(relation.getAuditStatus())
                    || !seen.add(relation.getTargetEntityId())) {
                continue;
            }
            WujinChainEntityDO target = chainEntityService.getEntity(relation.getTargetEntityId());
            if (target == null || !lane.name().equals(target.getLane())
                    || !Integer.valueOf(ENABLED_STATUS).equals(target.getStatus())) {
                continue;
            }
            WujinProductDetailRespVO.EntityRef ref = new WujinProductDetailRespVO.EntityRef();
            ref.setId(target.getId());
            ref.setName(target.getName());
            ref.setLane(target.getLane());
            refs.add(ref);
        }
        return refs;
    }

    /**
     * 同一产业链品类、同一泳道下的其他在售商品
     */
    private List<WujinProductDetailRespVO.RelatedProduct> relatedProducts(Long productId,
                                                                         WujinMerchantSupplyCapabilityDO capability) {
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setEntityId(capability.getEntityId());
        listReqVO.setLane(capability.getLane());
        listReqVO.setSupplyStatus(SUPPLY_STATUS_ACTIVE);
        Set<Long> productIds = new LinkedHashSet<>();
        for (WujinMerchantSupplyCapabilityDO sibling : capabilityService.getCapabilityList(listReqVO)) {
            if (sibling.getProductId() != null && !productId.equals(sibling.getProductId())) {
                productIds.add(sibling.getProductId());
            }
        }
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<WujinProductDetailRespVO.RelatedProduct> related = new ArrayList<>();
        for (ProductSpuRespDTO spu : productSpuApi.getSpuList(productIds)) {
            if (spu == null || !Integer.valueOf(PRODUCT_STATUS_ENABLE).equals(spu.getStatus())) {
                continue;
            }
            WujinProductDetailRespVO.RelatedProduct item = new WujinProductDetailRespVO.RelatedProduct();
            item.setId(spu.getId());
            item.setName(spu.getName());
            item.setPicUrl(spu.getPicUrl());
            item.setPrice(spu.getPrice());
            related.add(item);
            if (related.size() >= RELATED_PRODUCT_LIMIT) {
                break;
            }
        }
        return related;
    }

    private String brandName(Long brandId) {
        if (brandId == null) {
            return null;
        }
        for (ProductBrandRespDTO brand : productBrandApi.getBrandList(Collections.singleton(brandId))) {
            if (brand != null && brandId.equals(brand.getId())) {
                return brand.getName();
            }
        }
        return null;
    }

    private void putIfPresent(Map<String, String> specs, String label, String value) {
        if (!isBlank(value)) {
            specs.putIfAbsent(label, value);
        }
    }

    private static int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
