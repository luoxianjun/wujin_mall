package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinMerchantSupplyCapabilityAdminServiceImpl.class,
        WujinProductSpuStockSyncServiceImpl.class})
class WujinProductSpuStockSyncServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinProductSpuStockSyncService spuStockSyncService;

    @Test
    void syncProductStockOverwritesCapabilityStockCountForSameProduct() {
        Long productId = 9001L;
        Long entityId = entityService.createEntity(entityReq("M_RUBBER_STOCK_SYNC", "天然橡胶"));
        capabilityService.createCapability(capabilityReq(3001L, productId, "高耐磨乘用车轮胎", entityId, 120));
        capabilityService.createCapability(capabilityReq(3002L, productId, "高耐磨乘用车轮胎", entityId, 60));
        capabilityService.createCapability(capabilityReq(3003L, 9002L, "其它商品", entityId, 33));

        int updatedCount = spuStockSyncService.syncProductStock(productId, "高耐磨乘用车轮胎", 86);

        assertEquals(2, updatedCount);
        assertEquals(Arrays.asList(86, 86), stockCounts(productId));
        assertEquals(Arrays.asList(33), stockCounts(9002L));
    }

    @Test
    void syncProductStockIgnoresNullProductOrStock() {
        assertEquals(0, spuStockSyncService.syncProductStock(null, "高耐磨乘用车轮胎", 86));
        assertEquals(0, spuStockSyncService.syncProductStock(9001L, "高耐磨乘用车轮胎", null));
    }

    private List<Integer> stockCounts(Long productId) {
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setProductId(productId);
        return capabilityService.getCapabilityList(listReqVO).stream()
                .map(WujinMerchantSupplyCapabilityDO::getStockCount)
                .sorted()
                .collect(Collectors.toList());
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustries("TIRE_RUBBER");
        reqVO.setStatus(0);
        reqVO.setJunctionFlag(false);
        return reqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(Long merchantId, Long productId, String productName,
                                                                 Long entityId, Integer stockCount) {
        WujinMerchantSupplyCapabilitySaveReqVO reqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        reqVO.setMerchantId(merchantId);
        reqVO.setProductId(productId);
        reqVO.setProductName(productName);
        reqVO.setEntityId(entityId);
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("TIRE_RUBBER");
        reqVO.setSupplyStatus(0);
        reqVO.setStockCount(stockCount);
        reqVO.setMinOrderQuantity(5);
        reqVO.setDeliveryDays(3);
        reqVO.setServiceArea("华东");
        reqVO.setRemark("来自商城 SPU 供给索引");
        return reqVO;
    }
}
