package cn.iocoder.yudao.module.wujin.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金关系审核操作 Response VO")
public class WujinRelationAuditReviewRespVO {

    @Schema(description = "申报单编号", example = "1")
    private Long submissionId;

    @Schema(description = "审核记录编号", example = "10")
    private Long auditRecordId;

    @Schema(description = "审核动作", example = "APPROVE")
    private String action;

    @Schema(description = "审核原因", example = "REVIEW_APPROVED")
    private String reason;

    @Schema(description = "是否生效入网", example = "true")
    private Boolean effectiveFlag;

    @Schema(description = "成品实体编号", example = "100")
    private Long productEntityId;

    @Schema(description = "生效关系数量", example = "2")
    private Integer effectiveRelationCount;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getAuditRecordId() {
        return auditRecordId;
    }

    public void setAuditRecordId(Long auditRecordId) {
        this.auditRecordId = auditRecordId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getEffectiveFlag() {
        return effectiveFlag;
    }

    public void setEffectiveFlag(Boolean effectiveFlag) {
        this.effectiveFlag = effectiveFlag;
    }

    public Long getProductEntityId() {
        return productEntityId;
    }

    public void setProductEntityId(Long productEntityId) {
        this.productEntityId = productEntityId;
    }

    public Integer getEffectiveRelationCount() {
        return effectiveRelationCount;
    }

    public void setEffectiveRelationCount(Integer effectiveRelationCount) {
        this.effectiveRelationCount = effectiveRelationCount;
    }
}
