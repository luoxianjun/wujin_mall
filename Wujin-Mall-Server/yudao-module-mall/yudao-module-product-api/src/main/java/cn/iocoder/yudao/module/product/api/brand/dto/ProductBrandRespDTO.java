package cn.iocoder.yudao.module.product.api.brand.dto;

import lombok.Data;

/**
 * 商品品牌 Response DTO
 */
@Data
public class ProductBrandRespDTO {

    private Long id;

    private String name;

    private String picUrl;

    private Integer status;

}
