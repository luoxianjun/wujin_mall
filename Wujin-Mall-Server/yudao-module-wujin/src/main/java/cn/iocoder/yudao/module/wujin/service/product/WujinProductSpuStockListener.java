package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.product.event.spu.ProductSpuStockUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class WujinProductSpuStockListener {

    @Resource
    private WujinProductSpuStockSyncService syncService;

    @EventListener
    public void onProductSpuStockUpdated(ProductSpuStockUpdatedEvent event) {
        int updatedCount = syncService.syncProductStock(event.getSpuId(), event.getSpuName(), event.getNewStock());
        log.info("[onProductSpuStockUpdated][同步五金供应能力库存完成，spuId={}, stockIncrCount={}, newStock={}, updatedCount={}]",
                event.getSpuId(), event.getStockIncrCount(), event.getNewStock(), updatedCount);
    }
}
