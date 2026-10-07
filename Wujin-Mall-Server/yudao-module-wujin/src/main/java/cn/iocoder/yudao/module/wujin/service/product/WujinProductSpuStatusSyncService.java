package cn.iocoder.yudao.module.wujin.service.product;

public interface WujinProductSpuStatusSyncService {

    int syncProductStatus(Long productId, String productName, Integer productStatus);
}
