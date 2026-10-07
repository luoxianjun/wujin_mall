package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.wujin.dal.mysql.supply.WujinMerchantSupplyCapabilityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

@Service
@Validated
public class WujinProductSpuStockSyncServiceImpl implements WujinProductSpuStockSyncService {

    @Resource
    private WujinMerchantSupplyCapabilityMapper capabilityMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncProductStock(Long productId, String productName, Integer stockCount) {
        if (productId == null || stockCount == null) {
            return 0;
        }
        return capabilityMapper.updateStockCountByProductId(productId, stockCount);
    }
}
