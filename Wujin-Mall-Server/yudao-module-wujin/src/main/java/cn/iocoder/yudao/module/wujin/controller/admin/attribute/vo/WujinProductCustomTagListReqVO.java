package cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金商品自定义标签列表 Request VO")
public class WujinProductCustomTagListReqVO {

    private Long submissionId;
    private Long merchantId;
    private Long productId;
    private String productName;
    private String tagName;
    private Integer auditStatus;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }
}
