package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSpuStockEventTest {

    @InjectMocks
    private ProductSpuServiceImpl productSpuService;

    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private ProductSkuService productSkuService;
    @Mock
    private ProductBrandService brandService;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void updateSpuStockPublishesStockUpdatedEventWithLatestStock() throws Exception {
        ProductSpuDO updatedSpu = ProductSpuDO.builder()
                .id(9001L)
                .name("高耐磨乘用车轮胎")
                .stock(86)
                .build();
        when(productSpuMapper.selectById(9001L)).thenReturn(updatedSpu);

        productSpuService.updateSpuStock(Collections.singletonMap(9001L, -4));

        verify(productSpuMapper).updateStock(9001L, -4);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        Object event = eventCaptor.getValue();
        assertEquals("cn.iocoder.yudao.module.product.event.spu.ProductSpuStockUpdatedEvent",
                event.getClass().getName());
        assertEquals(9001L, getter(event, "getSpuId"));
        assertEquals("高耐磨乘用车轮胎", getter(event, "getSpuName"));
        assertEquals(-4, getter(event, "getStockIncrCount"));
        assertEquals(86, getter(event, "getNewStock"));
    }

    private Object getter(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
