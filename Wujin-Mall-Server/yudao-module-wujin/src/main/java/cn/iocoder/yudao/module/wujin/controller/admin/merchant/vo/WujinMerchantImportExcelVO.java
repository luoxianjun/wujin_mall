package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import cn.idev.excel.annotation.ExcelProperty;

/**
 * 五金商家关系批量导入 Excel 行
 */
public class WujinMerchantImportExcelVO {

    @ExcelProperty("商品编号")
    private Long productId;
    @ExcelProperty("商品名称")
    private String productName;
    @ExcelProperty("成品分类编号")
    private Long productCategoryId;
    @ExcelProperty("产业链实体编号")
    private Long entityId;
    @ExcelProperty("产业链实体名称")
    private String entityName;
    @ExcelProperty("关系类型")
    private String relationType;
    @ExcelProperty("库存")
    private Integer stockCount;
    @ExcelProperty("最小起订量")
    private Integer minOrderQuantity;
    @ExcelProperty("交付天数")
    private Integer deliveryDays;
    @ExcelProperty("服务区域")
    private String serviceArea;
    @ExcelProperty("备注")
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
