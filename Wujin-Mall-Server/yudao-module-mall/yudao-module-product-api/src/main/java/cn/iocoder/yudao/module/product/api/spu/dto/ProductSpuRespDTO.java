package cn.iocoder.yudao.module.product.api.spu.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品 SPU 信息 Response DTO。
 */
@Data
public class ProductSpuRespDTO {

    /**
     * 商品 SPU 编号，自增。
     */
    private Long id;

    private String name;

    private Long categoryId;

    private String picUrl;

    /**
     * 商品状态，使用商品模块 SPU 状态整数值。
     */
    private Integer status;

    /**
     * false - 单规格；true - 多规格。
     */
    private Boolean specType;

    /**
     * 商品价格，单位：分。
     */
    private Integer price;

    private Integer marketPrice;

    private Integer costPrice;

    private Integer stock;

    /**
     * 配送方式数组，使用交易模块配送方式整数值。
     */
    private List<Integer> deliveryTypes;

    private Long deliveryTemplateId;

    private Integer giveIntegral;

    /**
     * false - 默认；true - 自行设置。
     */
    private Boolean subCommissionType;

}
