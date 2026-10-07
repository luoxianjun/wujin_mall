package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 五金商家批量导入 Request VO")
public class WujinMerchantImportReqVO {

    @NotNull(message = "行业模板编号不能为空")
    private Long templateId;
    private String industryCode;
    private String productLane;
    private Long defaultProductCategoryId;
    @Valid
    private List<ImportRow> rows;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getProductLane() {
        return productLane;
    }

    public void setProductLane(String productLane) {
        this.productLane = productLane;
    }

    public Long getDefaultProductCategoryId() {
        return defaultProductCategoryId;
    }

    public void setDefaultProductCategoryId(Long defaultProductCategoryId) {
        this.defaultProductCategoryId = defaultProductCategoryId;
    }

    public List<ImportRow> getRows() {
        return rows;
    }

    public void setRows(List<ImportRow> rows) {
        this.rows = rows;
    }

    public static class ImportRow {

        private Long merchantId;
        private Long productId;
        private String productName;
        private Long productCategoryId;
        private Long entityId;
        private String entityName;
        private String relationType;
        private Integer stockCount;
        private Integer minOrderQuantity;
        private Integer deliveryDays;
        private String serviceArea;
        private String remark;

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
    }
}
