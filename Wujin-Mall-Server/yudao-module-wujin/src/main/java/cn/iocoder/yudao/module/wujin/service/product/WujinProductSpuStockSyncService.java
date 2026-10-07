package cn.iocoder.yudao.module.wujin.service.product;

public interface WujinProductSpuStockSyncService {

    int syncProductStock(Long productId, String productName, Integer stockCount);
}
