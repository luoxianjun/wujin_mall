package cn.iocoder.yudao.module.wujin.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金关系审核记录新增/更新 Request VO")
public class WujinRelationAuditRecordSaveReqVO {

    private Long id;
    @NotNull(message = "申报单编号不能为空")
    private Long submissionId;
    @NotNull(message = "审核人编号不能为空")
    private Long auditorId;
    @NotBlank(message = "审核动作不能为空")
    private String action;
    @NotBlank(message = "审核原因不能为空")
    private String reason;
    private String comment;
    @NotNull(message = "是否生效不能为空")
    private Boolean effectiveFlag;

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
}
