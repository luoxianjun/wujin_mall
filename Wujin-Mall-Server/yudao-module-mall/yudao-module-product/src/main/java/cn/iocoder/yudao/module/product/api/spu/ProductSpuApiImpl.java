package cn.iocoder.yudao.module.product.api.spu;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuCreateReqDTO;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 商品 SPU API 接口实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuService spuService;

    @Override
    public Long createSpu(ProductSpuCreateReqDTO createReqDTO) {
        return spuService.createSpu(buildSaveReqVO(createReqDTO));
    }

    @Override
    public List<ProductSpuRespDTO> getSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.getSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.validateSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public ProductSpuRespDTO getSpu(Long id) {
        ProductSpuDO spu = spuService.getSpu(id);
        return BeanUtils.toBean(spu, ProductSpuRespDTO.class);
    }

    private ProductSpuSaveReqVO buildSaveReqVO(ProductSpuCreateReqDTO createReqDTO) {
        ProductSpuSaveReqVO saveReqVO = new ProductSpuSaveReqVO();
        saveReqVO.setName(createReqDTO.getName());
        saveReqVO.setKeyword(createReqDTO.getKeyword());
        saveReqVO.setIntroduction(createReqDTO.getIntroduction());
        saveReqVO.setDescription(createReqDTO.getDescription());
        saveReqVO.setCategoryId(createReqDTO.getCategoryId());
        saveReqVO.setBrandId(createReqDTO.getBrandId());
        saveReqVO.setPicUrl(createReqDTO.getPicUrl());
        saveReqVO.setSliderPicUrls(Collections.singletonList(createReqDTO.getPicUrl()));
        saveReqVO.setSort(0);
        saveReqVO.setSpecType(false);
        saveReqVO.setDeliveryTypes(Collections.singletonList(1));
        saveReqVO.setGiveIntegral(0);
        saveReqVO.setSubCommissionType(false);
        saveReqVO.setVirtualSalesCount(0);
        saveReqVO.setSalesCount(0);
        saveReqVO.setBrowseCount(0);
        saveReqVO.setSkus(Collections.singletonList(buildSkuSaveReqVO(createReqDTO)));
        return saveReqVO;
    }

    private ProductSkuSaveReqVO buildSkuSaveReqVO(ProductSpuCreateReqDTO createReqDTO) {
        ProductSkuSaveReqVO skuSaveReqVO = new ProductSkuSaveReqVO();
        skuSaveReqVO.setName(createReqDTO.getName());
        skuSaveReqVO.setPrice(createReqDTO.getPrice());
        skuSaveReqVO.setMarketPrice(createReqDTO.getMarketPrice());
        skuSaveReqVO.setCostPrice(createReqDTO.getCostPrice());
        skuSaveReqVO.setPicUrl(createReqDTO.getPicUrl());
        skuSaveReqVO.setStock(createReqDTO.getStock());
        return skuSaveReqVO;
    }

}
