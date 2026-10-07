package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金供应商供应能力 Response VO")
public class WujinSupplierCapabilityRespVO {

    private Long supplierId;
    private String supplierType;
    private String supplierName;
    private Integer matchScore;
    private String serviceNote;
    private List<String> mainCapabilities;
    private List<LaneCapability> lanes;
    private List<CapabilityItem> capabilities;
    /**
     * 平台审核通过的商品自定义标签
     */
    private List<String> approvedTags;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getServiceNote() {
        return serviceNote;
    }

    public void setServiceNote(String serviceNote) {
        this.serviceNote = serviceNote;
    }

    public List<String> getMainCapabilities() {
        return mainCapabilities;
    }

    public void setMainCapabilities(List<String> mainCapabilities) {
        this.mainCapabilities = mainCapabilities;
    }

    public List<LaneCapability> getLanes() {
        return lanes;
    }

    public void setLanes(List<LaneCapability> lanes) {
        this.lanes = lanes;
    }

    public List<CapabilityItem> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<CapabilityItem> capabilities) {
        this.capabilities = capabilities;
    }

    public List<String> getApprovedTags() {
        return approvedTags;
    }

    public void setApprovedTags(List<String> approvedTags) {
        this.approvedTags = approvedTags;
    }

    public static class LaneCapability {

        private String label;
        private String value;
        private String note;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class CapabilityItem {

        private Long productId;
        private String productName;
        private Long entityId;
        private String entityName;
        private String lane;
        private String industry;
        private Integer stockCount;
        private Integer minOrderQuantity;
        private Integer deliveryDays;
        private String serviceArea;
        private String remark;

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

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
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
}
