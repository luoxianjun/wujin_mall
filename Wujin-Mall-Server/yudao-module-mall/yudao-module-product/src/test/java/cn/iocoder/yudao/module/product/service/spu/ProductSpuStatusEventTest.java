package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateStatusReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSpuStatusEventTest {

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
    void updateSpuStatusPublishesStatusChangedEvent() throws Exception {
        ProductSpuDO spu = ProductSpuDO.builder()
                .id(9001L)
                .name("高耐磨乘用车轮胎")
                .status(ProductSpuStatusEnum.ENABLE.getStatus())
                .build();
        when(productSpuMapper.selectById(9001L)).thenReturn(spu);

        ProductSpuUpdateStatusReqVO reqVO = new ProductSpuUpdateStatusReqVO();
        reqVO.setId(9001L);
        reqVO.setStatus(ProductSpuStatusEnum.DISABLE.getStatus());
        productSpuService.updateSpuStatus(reqVO);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        Object event = eventCaptor.getValue();
        assertEquals("cn.iocoder.yudao.module.product.event.spu.ProductSpuStatusUpdatedEvent",
                event.getClass().getName());
        assertEquals(9001L, getter(event, "getSpuId"));
        assertEquals(ProductSpuStatusEnum.ENABLE.getStatus(), getter(event, "getOldStatus"));
        assertEquals(ProductSpuStatusEnum.DISABLE.getStatus(), getter(event, "getNewStatus"));
    }

    private Object getter(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
