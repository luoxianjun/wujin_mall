package cn.iocoder.yudao.module.product.api.spu.dto;

import lombok.Data;

/**
 * 商品 SPU 创建 Request DTO。
 */
@Data
public class ProductSpuCreateReqDTO {

    private String name;

    private String keyword;

    private String introduction;

    private String description;

    private Long categoryId;

    private Long brandId;

    private String picUrl;

    /**
     * 销售价格，单位：分。
     */
    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private Integer stock;

}
