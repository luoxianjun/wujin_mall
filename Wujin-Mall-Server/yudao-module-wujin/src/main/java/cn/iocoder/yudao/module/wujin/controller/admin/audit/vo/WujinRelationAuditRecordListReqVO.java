package cn.iocoder.yudao.module.wujin.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金关系审核记录列表查询 Request VO")
public class WujinRelationAuditRecordListReqVO {

    private Long submissionId;
    private Long auditorId;
    private String action;
    private String reason;
    private Boolean effectiveFlag;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
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
}
