package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.product.event.spu.ProductSpuStatusUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class WujinProductSpuStatusListener {

    @Resource
    private WujinProductSpuStatusSyncService syncService;

    @EventListener
    public void onProductSpuStatusUpdated(ProductSpuStatusUpdatedEvent event) {
        int updatedCount = syncService.syncProductStatus(event.getSpuId(), event.getSpuName(), event.getNewStatus());
        log.info("[onProductSpuStatusUpdated][同步五金供应能力状态完成，spuId={}, newStatus={}, updatedCount={}]",
                event.getSpuId(), event.getNewStatus(), updatedCount);
    }
}
