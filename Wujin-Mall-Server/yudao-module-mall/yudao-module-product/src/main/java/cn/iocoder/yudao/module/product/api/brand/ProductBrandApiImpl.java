package cn.iocoder.yudao.module.product.api.brand;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 商品品牌 API 实现类
 */
@Service
@Validated
public class ProductBrandApiImpl implements ProductBrandApi {

    @Resource
    private ProductBrandService brandService;

    @Override
    public List<ProductBrandRespDTO> getBrandList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return BeanUtils.toBean(brandService.getBrandList(ids), ProductBrandRespDTO.class);
    }

}
