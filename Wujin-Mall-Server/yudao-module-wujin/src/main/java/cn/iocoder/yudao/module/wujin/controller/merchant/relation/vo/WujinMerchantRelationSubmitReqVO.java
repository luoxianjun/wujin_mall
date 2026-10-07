package cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "商家后台 - 五金关系申报提交 Request VO")
public class WujinMerchantRelationSubmitReqVO {

    @Schema(description = "商家编号；为空时使用当前登录用户", example = "1001")
    private Long merchantId;

    @Schema(description = "商品编号；为空时发布流程会创建标准商城 SPU", example = "2001")
    private Long productId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "高耐磨乘用车轮胎")
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @Schema(description = "成品分类编号", example = "3001")
    private Long productCategoryId;

    @Schema(description = "商城商品品牌编号", example = "1")
    private Long productBrandId;

    @Schema(description = "商品主图", example = "https://cdn.company.com/wujin-tyre.png")
    private String productPicUrl;

    @Schema(description = "销售价格，单位：分", example = "19900")
    private Integer productPrice;

    @Schema(description = "市场价格，单位：分", example = "22900")
    private Integer productMarketPrice;

    @Schema(description = "成本价格，单位：分", example = "12800")
    private Integer productCostPrice;

    @Schema(description = "库存", example = "60")
    private Integer productStock;

    @Schema(description = "商品实际供应的产业链实体编号", example = "100")
    private Long supplyEntityId;

    @Schema(description = "供应能力最小起订量", example = "10")
    private Integer supplyMinOrderQuantity;

    @Schema(description = "供应能力交付周期，单位：天", example = "3")
    private Integer supplyDeliveryDays;

    @Schema(description = "供应能力服务区域", example = "全国")
    private String supplyServiceArea;

    @Schema(description = "供应能力说明", example = "支持定制包装，现货当天发出")
    private String supplyRemark;

    @Schema(description = "行业模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "行业模板编号不能为空")
    private Long templateId;

    @Schema(description = "认证数量", example = "2")
    private Integer certificationCount;

    @Schema(description = "是否有应用说明", example = "true")
    private Boolean hasApplicationDescription;

    @Schema(description = "商家自定义关系")
    @Valid
    private List<RelationItem> customRelations = new ArrayList<>();

    @Schema(description = "标准属性；为空时不改动已有属性值，提供时按平台属性字典校验必填项与可选值")
    @Valid
    private List<StandardAttribute> standardAttributes;

    @Schema(description = "自定义标签；提交后进入平台审核", example = "[\"耐高温\"]")
    private List<String> customTags;

    @Schema(description = "自定义标签审核说明", example = "适用于矿山工况")
    private String customTagReviewNote;

    @Schema(description = "自定义标签是否需要审核", example = "true")
    private Boolean customTagReviewRequired;

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

    public Long getProductBrandId() {
        return productBrandId;
    }

    public void setProductBrandId(Long productBrandId) {
        this.productBrandId = productBrandId;
    }

    public String getProductPicUrl() {
        return productPicUrl;
    }

    public void setProductPicUrl(String productPicUrl) {
        this.productPicUrl = productPicUrl;
    }

    public Integer getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Integer productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductMarketPrice() {
        return productMarketPrice;
    }

    public void setProductMarketPrice(Integer productMarketPrice) {
        this.productMarketPrice = productMarketPrice;
    }

    public Integer getProductCostPrice() {
        return productCostPrice;
    }

    public void setProductCostPrice(Integer productCostPrice) {
        this.productCostPrice = productCostPrice;
    }

    public Integer getProductStock() {
        return productStock;
    }

    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }

    public Long getSupplyEntityId() {
        return supplyEntityId;
    }

    public void setSupplyEntityId(Long supplyEntityId) {
        this.supplyEntityId = supplyEntityId;
    }

    public Integer getSupplyMinOrderQuantity() {
        return supplyMinOrderQuantity;
    }

    public void setSupplyMinOrderQuantity(Integer supplyMinOrderQuantity) {
        this.supplyMinOrderQuantity = supplyMinOrderQuantity;
    }

    public Integer getSupplyDeliveryDays() {
        return supplyDeliveryDays;
    }

    public void setSupplyDeliveryDays(Integer supplyDeliveryDays) {
        this.supplyDeliveryDays = supplyDeliveryDays;
    }

    public String getSupplyServiceArea() {
        return supplyServiceArea;
    }

    public void setSupplyServiceArea(String supplyServiceArea) {
        this.supplyServiceArea = supplyServiceArea;
    }

    public String getSupplyRemark() {
        return supplyRemark;
    }

    public void setSupplyRemark(String supplyRemark) {
        this.supplyRemark = supplyRemark;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getCertificationCount() {
        return certificationCount;
    }

    public void setCertificationCount(Integer certificationCount) {
        this.certificationCount = certificationCount;
    }

    public Boolean getHasApplicationDescription() {
        return hasApplicationDescription;
    }

    public void setHasApplicationDescription(Boolean hasApplicationDescription) {
        this.hasApplicationDescription = hasApplicationDescription;
    }

    public List<RelationItem> getCustomRelations() {
        return customRelations;
    }

    public void setCustomRelations(List<RelationItem> customRelations) {
        this.customRelations = customRelations == null ? new ArrayList<>() : customRelations;
    }

    public List<StandardAttribute> getStandardAttributes() {
        return standardAttributes;
    }

    public void setStandardAttributes(List<StandardAttribute> standardAttributes) {
        this.standardAttributes = standardAttributes;
    }

    public List<String> getCustomTags() {
        return customTags;
    }

    public void setCustomTags(List<String> customTags) {
        this.customTags = customTags;
    }

    public String getCustomTagReviewNote() {
        return customTagReviewNote;
    }

    public void setCustomTagReviewNote(String customTagReviewNote) {
        this.customTagReviewNote = customTagReviewNote;
    }

    public Boolean getCustomTagReviewRequired() {
        return customTagReviewRequired;
    }

    public void setCustomTagReviewRequired(Boolean customTagReviewRequired) {
        this.customTagReviewRequired = customTagReviewRequired;
    }

    public static class StandardAttribute {

        @Schema(description = "平台属性字典编号", example = "1")
        private Long attributeId;

        @Schema(description = "平台属性编码", example = "FINISHED_PRODUCT_SPEC")
        private String code;

        @Schema(description = "属性名称", example = "规格型号")
        private String name;

        @Schema(description = "属性值", example = "205/55R16")
        private String value;

        public Long getAttributeId() {
            return attributeId;
        }

        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class RelationItem {

        @Schema(description = "现有原材料或加工工艺实体编号，和自定义名称二选一", example = "10")
        private Long entityId;

        @Schema(description = "自定义原材料或加工工艺名称，现有实体搜不到时填写", example = "高耐磨芳纶帘线")
        private String entityName;

        @Schema(description = "关系类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REQUIRES_MATERIAL")
        @NotBlank(message = "关系类型不能为空")
        private String relationType;

        @Schema(description = "是否必填", example = "false")
        private Boolean requiredFlag;

        @Schema(description = "关系说明")
        private String remark;

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

        public Boolean getRequiredFlag() {
            return requiredFlag;
        }

        public void setRequiredFlag(Boolean requiredFlag) {
            this.requiredFlag = requiredFlag;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
