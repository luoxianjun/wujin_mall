package cn.iocoder.yudao.module.wujin.controller.admin.supply.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金商家供应能力保存 Request VO")
public class WujinMerchantSupplyCapabilitySaveReqVO {

    private Long id;
    @NotNull(message = "商家编号不能为空")
    private Long merchantId;
    @NotNull(message = "商品编号不能为空")
    private Long productId;
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    @NotNull(message = "产业链实体编号不能为空")
    private Long entityId;
    @NotBlank(message = "供应泳道不能为空")
    private String lane;
    private String industry;
    @NotNull(message = "供应状态不能为空")
    private Integer supplyStatus;
    private Integer stockCount;
    private Integer minOrderQuantity;
    private Integer deliveryDays;
    private String serviceArea;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Integer getSupplyStatus() {
        return supplyStatus;
    }

    public void setSupplyStatus(Integer supplyStatus) {
        this.supplyStatus = supplyStatus;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Integer getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(Integer minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
