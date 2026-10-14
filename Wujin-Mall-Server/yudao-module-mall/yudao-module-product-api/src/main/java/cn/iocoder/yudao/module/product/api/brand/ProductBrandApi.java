package cn.iocoder.yudao.module.product.api.brand;

import cn.iocoder.yudao.module.product.api.brand.dto.ProductBrandRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 商品品牌 API 接口
 */
public interface ProductBrandApi {

    /**
     * 批量获得品牌
     *
     * @param ids 品牌编号
     * @return 品牌列表
     */
    List<ProductBrandRespDTO> getBrandList(Collection<Long> ids);

}
