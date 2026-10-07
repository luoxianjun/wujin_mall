package cn.iocoder.yudao.module.wujin.controller.admin.audit.vo;

import java.time.LocalDateTime;

public class WujinRelationAuditRecordRespVO {

    private Long id;
    private Long submissionId;
    private Long auditorId;
    private String action;
    private String reason;
    private String comment;
    private Boolean effectiveFlag;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getEffectiveFlag() {
        return effectiveFlag;
    }

    public void setEffectiveFlag(Boolean effectiveFlag) {
        this.effectiveFlag = effectiveFlag;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
