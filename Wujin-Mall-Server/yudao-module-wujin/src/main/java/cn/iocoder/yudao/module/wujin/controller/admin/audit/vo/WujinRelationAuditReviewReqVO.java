package cn.iocoder.yudao.module.wujin.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金关系审核操作 Request VO")
public class WujinRelationAuditReviewReqVO {

    @Schema(description = "申报单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申报单编号不能为空")
    private Long submissionId;

    @Schema(description = "审核人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9001")
    @NotNull(message = "审核人编号不能为空")
    private Long auditorId;

    @Schema(description = "审核动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "APPROVE")
    @NotBlank(message = "审核动作不能为空")
    private String action;

    @Schema(description = "审核意见")
    private String comment;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
