package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金商家关系申报项新增/更新 Request VO")
public class WujinMerchantRelationItemSaveReqVO {

    private Long id;
    @NotNull(message = "申报单编号不能为空")
    private Long submissionId;
    @NotNull(message = "关联实体编号不能为空")
    private Long entityId;
    @NotBlank(message = "关系类型不能为空")
    private String relationType;
    @NotNull(message = "模板来源不能为空")
    private Boolean fromTemplate;
    @NotNull(message = "是否必填不能为空")
    private Boolean requiredFlag;
    private String remark;

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

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Boolean getFromTemplate() {
        return fromTemplate;
    }

    public void setFromTemplate(Boolean fromTemplate) {
        this.fromTemplate = fromTemplate;
    }

    public Boolean getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Boolean requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
