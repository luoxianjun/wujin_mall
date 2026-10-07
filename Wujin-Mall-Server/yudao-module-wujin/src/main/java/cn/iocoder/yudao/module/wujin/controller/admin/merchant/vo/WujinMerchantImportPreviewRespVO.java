package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import java.util.List;

public class WujinMerchantImportPreviewRespVO {

    private Integer totalCount;
    private Integer validCount;
    private Integer invalidCount;
    private List<RowPreview> validRows;
    private List<RowPreview> invalidRows;

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getValidCount() {
        return validCount;
    }

    public void setValidCount(Integer validCount) {
        this.validCount = validCount;
    }

    public Integer getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(Integer invalidCount) {
        this.invalidCount = invalidCount;
    }

    public List<RowPreview> getValidRows() {
        return validRows;
    }

    public void setValidRows(List<RowPreview> validRows) {
        this.validRows = validRows;
    }

    public List<RowPreview> getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(List<RowPreview> invalidRows) {
        this.invalidRows = invalidRows;
    }

    public static class RowPreview {

        private Integer rowNo;
        private Long merchantId;
        private Long productId;
        private String productName;
        private Long productCategoryId;
        private Long entityId;
        private String entityName;
        private String entityLane;
        private String relationType;
        private Integer stockCount;
        private Integer minOrderQuantity;
        private Integer deliveryDays;
        private String serviceArea;
        private String remark;
        private List<String> errors;

        public Integer getRowNo() {
            return rowNo;
        }

        public void setRowNo(Integer rowNo) {
            this.rowNo = rowNo;
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

        public Long getProductCategoryId() {
            return productCategoryId;
        }

        public void setProductCategoryId(Long productCategoryId) {
            this.productCategoryId = productCategoryId;
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

        public String getEntityLane() {
            return entityLane;
        }

        public void setEntityLane(String entityLane) {
            this.entityLane = entityLane;
        }

        public String getRelationType() {
            return relationType;
        }

        public void setRelationType(String relationType) {
            this.relationType = relationType;
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

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}
