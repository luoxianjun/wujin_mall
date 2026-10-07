package cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商家后台 - 五金关系申报提交 Response VO")
public class WujinMerchantRelationSubmitRespVO {

    @Schema(description = "申报单编号", example = "1")
    private Long submissionId;

    @Schema(description = "商品编号", example = "2001")
    private Long productId;

    @Schema(description = "审核状态", example = "20")
    private Integer auditStatus;

    @Schema(description = "审核路线", example = "AUTO_APPROVE")
    private String auditRoute;

    @Schema(description = "审核路线说明")
    private String auditReason;

    @Schema(description = "完善度分数", example = "100")
    private Integer completenessScore;

    @Schema(description = "完善度建议")
    private String completenessSuggestion;

    @Schema(description = "已复制模板项数量", example = "2")
    private Integer copiedTemplateItemCount;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public Integer getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(Integer completenessScore) {
        this.completenessScore = completenessScore;
    }

    public String getCompletenessSuggestion() {
        return completenessSuggestion;
    }

    public void setCompletenessSuggestion(String completenessSuggestion) {
        this.completenessSuggestion = completenessSuggestion;
    }

    public Integer getCopiedTemplateItemCount() {
        return copiedTemplateItemCount;
    }

    public void setCopiedTemplateItemCount(Integer copiedTemplateItemCount) {
        this.copiedTemplateItemCount = copiedTemplateItemCount;
    }
}
