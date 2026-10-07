package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金商家关系申报新增/更新 Request VO")
public class WujinMerchantRelationSubmissionSaveReqVO {

    private Long id;
    @NotNull(message = "商家编号不能为空")
    private Long merchantId;
    @NotNull(message = "商品编号不能为空")
    private Long productId;
    @NotBlank(message = "商品名称不能为空")
    private String productName;
    @NotBlank(message = "商品泳道不能为空")
    private String productLane;
    private Long productCategoryId;
    @NotNull(message = "行业模板编号不能为空")
    private Long templateId;
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
    private String auditRoute;
    private Integer completenessScore;
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

    public String getProductLane() {
        return productLane;
    }

    public void setProductLane(String productLane) {
        this.productLane = productLane;
    }

    public Long getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(Long productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditRoute() {
        return auditRoute;
    }

    public void setAuditRoute(String auditRoute) {
        this.auditRoute = auditRoute;
    }

    public Integer getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(Integer completenessScore) {
        this.completenessScore = completenessScore;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
