package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.brand.ProductBrandApi;
import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinProductDetailRespVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@Import({WujinAppProductDetailServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class, WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinProductAttributeServiceImpl.class, WujinAttributeDictionaryAdminServiceImpl.class})
class WujinAppProductDetailServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinAppProductDetailService productDetailService;
    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinProductAttributeService productAttributeService;
    @MockBean
    private ProductSpuApi productSpuApi;
    @MockBean
    private ProductBrandApi productBrandApi;

    @Test
    void productDetailUsesRealProductAttributesAndUpstream() {
        Long tireId = entityService.createEntity(entityReq("P_TIRE", "轮胎", WujinLane.PRODUCT.name(), "注意轮胎负载等级"));
        Long rubberId = entityService.createEntity(entityReq("M_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), null));
        Long vulcanizeId = entityService.createEntity(entityReq("W_VULCANIZE", "硫化", WujinLane.PROCESS.name(), null));
        relationService.createRelation(relationReq(tireId, rubberId, WujinRelationType.REQUIRES_MATERIAL));
        relationService.createRelation(relationReq(tireId, vulcanizeId, WujinRelationType.REQUIRES_PROCESS));
        capabilityService.createCapability(capabilityReq(9206L, "米其林123", tireId));
        capabilityService.createCapability(capabilityReq(9001L, "米其林 205/55R16 乘用车轮胎", tireId));
        WujinMerchantRelationSubmitReqVO.StandardAttribute spec = new WujinMerchantRelationSubmitReqVO.StandardAttribute();
        spec.setName("规格型号");
        spec.setValue("205/55R16");
        productAttributeService.saveStandardAttributes(601L, 3001L, 9206L, WujinLane.PRODUCT.name(),
                Collections.singletonList(spec));

        ProductSpuRespDTO product = spu(9206L, "米其林123", 1);
        product.setBrandId(2000L);
        when(productSpuApi.getSpu(9206L)).thenReturn(product);
        when(productSpuApi.getSpuList(anyCollection())).thenReturn(Arrays.asList(spu(9001L, "米其林 205/55R16 乘用车轮胎", 1)));
        ProductBrandRespDTO brand = new ProductBrandRespDTO();
        brand.setId(2000L);
        brand.setName("米其林");
        when(productBrandApi.getBrandList(anyCollection())).thenReturn(Collections.singletonList(brand));

        WujinProductDetailRespVO detail = productDetailService.getProductDetail(9206L);

        assertTrue(detail.getExists());
        assertEquals("米其林123", detail.getName());
        assertEquals("米其林", detail.getBrandName());
        assertEquals("轮胎", detail.getEntityName());
        assertEquals("注意轮胎负载等级", detail.getRiskNote());
        assertEquals(Arrays.asList("规格型号", "品牌", "产业链品类", "服务区域"),
                detail.getSpecs().stream().map(WujinProductDetailRespVO.Spec::getLabel).collect(Collectors.toList()));
        assertEquals("205/55R16", detail.getSpecs().get(0).getValue());
        assertFalse(detail.getSpecs().stream().anyMatch(item -> item.getValue().contains("Q235")));
        assertEquals("天然橡胶", detail.getUpstreamMaterials().get(0).getName());
        assertEquals("硫化", detail.getUpstreamProcesses().get(0).getName());
        assertEquals(9001L, detail.getRelatedProducts().get(0).getId());
        assertEquals(10, detail.getMinOrderQuantity());
    }

    @Test
    void missingOrOffShelfProductIsReportedAsNotExisting() {
        assertFalse(productDetailService.getProductDetail(404L).getExists());
        when(productSpuApi.getSpu(9002L)).thenReturn(spu(9002L, "已下架螺栓", 0));
        WujinProductDetailRespVO detail = productDetailService.getProductDetail(9002L);
        assertFalse(detail.getExists());
        assertEquals("已下架螺栓", detail.getName());
    }

    private ProductSpuRespDTO spu(Long id, String name, Integer status) {
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(id);
        spu.setName(name);
        spu.setPrice(123);
        spu.setStatus(status);
        return spu;
    }

    private WujinChainEntitySaveReqVO entityReq(String code, String name, String lane, String riskNote) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(code);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries("汽车");
        reqVO.setRiskNote(riskNote);
        reqVO.setJunctionFlag(false);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntityRelationSaveReqVO relationReq(Long source, Long target, WujinRelationType type) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(source);
        reqVO.setTargetEntityId(target);
        reqVO.setRelationType(type.name());
        reqVO.setWeight(100);
        reqVO.setCostRatio(20);
        reqVO.setIndustryContext("TIRE");
        reqVO.setAuditStatus(1);
        return reqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(Long productId, String productName, Long entityId) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(3001L);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setEntityId(entityId);
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setSupplyStatus(0);
        reqVO.setMinOrderQuantity(10);
        reqVO.setDeliveryDays(2);
        reqVO.setServiceArea("华东");
        return reqVO;
    }
}
