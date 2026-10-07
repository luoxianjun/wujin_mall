package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSpuApiImplTest {

    @InjectMocks
    private ProductSpuApiImpl productSpuApi;

    @Mock
    private ProductSpuService spuService;

    @Test
    void createSpuMapsApiRequestToProductSaveRequest() {
        ProductSpuCreateReqDTO reqDTO = new ProductSpuCreateReqDTO();
        reqDTO.setName("高耐磨乘用车轮胎");
        reqDTO.setCategoryId(3001L);
        reqDTO.setBrandId(1L);
        reqDTO.setKeyword("轮胎 耐磨 五金");
        reqDTO.setIntroduction("适用于乘用车维修场景");
        reqDTO.setDescription("商家发布联动创建的五金商品");
        reqDTO.setPicUrl("https://example.com/wujin-tyre.png");
        reqDTO.setPrice(19900);
        reqDTO.setMarketPrice(22900);
        reqDTO.setCostPrice(12800);
        reqDTO.setStock(60);

        when(spuService.createSpu(org.mockito.ArgumentMatchers.any(ProductSpuSaveReqVO.class))).thenReturn(9001L);

        Long spuId = productSpuApi.createSpu(reqDTO);

        assertEquals(9001L, spuId);
        ArgumentCaptor<ProductSpuSaveReqVO> captor = ArgumentCaptor.forClass(ProductSpuSaveReqVO.class);
        verify(spuService).createSpu(captor.capture());
        ProductSpuSaveReqVO saveReqVO = captor.getValue();
        assertEquals("高耐磨乘用车轮胎", saveReqVO.getName());
        assertEquals(3001L, saveReqVO.getCategoryId());
        assertEquals(1L, saveReqVO.getBrandId());
        assertEquals("轮胎 耐磨 五金", saveReqVO.getKeyword());
        assertEquals("适用于乘用车维修场景", saveReqVO.getIntroduction());
        assertEquals("商家发布联动创建的五金商品", saveReqVO.getDescription());
        assertEquals("https://example.com/wujin-tyre.png", saveReqVO.getPicUrl());
        assertEquals(Collections.singletonList("https://example.com/wujin-tyre.png"), saveReqVO.getSliderPicUrls());
        assertEquals(false, saveReqVO.getSpecType());
        assertEquals(Integer.valueOf(0), saveReqVO.getGiveIntegral());
        assertEquals(false, saveReqVO.getSubCommissionType());
        assertEquals(1, saveReqVO.getSkus().size());
        assertEquals("高耐磨乘用车轮胎", saveReqVO.getSkus().get(0).getName());
        assertEquals(Integer.valueOf(19900), saveReqVO.getSkus().get(0).getPrice());
        assertEquals(Integer.valueOf(60), saveReqVO.getSkus().get(0).getStock());
    }
}
