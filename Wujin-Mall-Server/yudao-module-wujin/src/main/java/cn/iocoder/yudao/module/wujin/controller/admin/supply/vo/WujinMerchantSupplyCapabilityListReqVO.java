package cn.iocoder.yudao.module.wujin.controller.admin.supply.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金商家供应能力列表 Request VO")
public class WujinMerchantSupplyCapabilityListReqVO {

    private Long merchantId;
    private Long productId;
    private String productName;
    private Long entityId;
    private String lane;
    private String industry;
    private Integer supplyStatus;

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
}
