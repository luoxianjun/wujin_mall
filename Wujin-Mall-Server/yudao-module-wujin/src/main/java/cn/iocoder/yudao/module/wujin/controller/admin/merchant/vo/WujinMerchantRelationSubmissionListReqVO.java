package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金商家关系申报列表查询 Request VO")
public class WujinMerchantRelationSubmissionListReqVO {

    @Schema(description = "商家编号", example = "1001")
    private Long merchantId;

    @Schema(description = "商品编号", example = "2001")
    private Long productId;

    @Schema(description = "商品名称", example = "高耐磨乘用车轮胎")
    private String productName;

    @Schema(description = "商品泳道", example = "PRODUCT")
    private String productLane;

    @Schema(description = "行业模板编号", example = "1")
    private Long templateId;

    @Schema(description = "审核状态", example = "0")
    private Integer auditStatus;

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
}
