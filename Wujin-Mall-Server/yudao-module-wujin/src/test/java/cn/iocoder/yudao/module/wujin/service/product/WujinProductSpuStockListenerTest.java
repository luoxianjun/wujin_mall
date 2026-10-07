package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.product.event.spu.ProductSpuStockUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WujinProductSpuStockListenerTest {

    @InjectMocks
    private WujinProductSpuStockListener listener;

    @Mock
    private WujinProductSpuStockSyncService syncService;

    @Test
    void onProductSpuStockUpdatedDelegatesToSyncService() {
        ProductSpuStockUpdatedEvent event = new ProductSpuStockUpdatedEvent(9001L,
                "高耐磨乘用车轮胎", -4, 86);

        listener.onProductSpuStockUpdated(event);

        verify(syncService).syncProductStock(9001L, "高耐磨乘用车轮胎", 86);
    }
}
