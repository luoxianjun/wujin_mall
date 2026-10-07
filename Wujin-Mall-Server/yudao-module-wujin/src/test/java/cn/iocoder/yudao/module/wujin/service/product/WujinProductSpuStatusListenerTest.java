package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.product.event.spu.ProductSpuStatusUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WujinProductSpuStatusListenerTest {

    @InjectMocks
    private WujinProductSpuStatusListener listener;

    @Mock
    private WujinProductSpuStatusSyncService syncService;

    @Test
    void onProductSpuStatusUpdatedDelegatesToSyncService() {
        ProductSpuStatusUpdatedEvent event = new ProductSpuStatusUpdatedEvent(9001L, "高耐磨乘用车轮胎", 1, 0);

        listener.onProductSpuStatusUpdated(event);

        verify(syncService).syncProductStatus(9001L, "高耐磨乘用车轮胎", 0);
    }
}
