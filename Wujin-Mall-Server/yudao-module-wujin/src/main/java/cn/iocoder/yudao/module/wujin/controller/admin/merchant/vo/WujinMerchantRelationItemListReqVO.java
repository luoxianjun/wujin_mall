package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金商家关系申报项列表查询 Request VO")
public class WujinMerchantRelationItemListReqVO {

    private Long submissionId;
    private Long entityId;
    private String relationType;
    private Boolean fromTemplate;
    private Boolean requiredFlag;

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
}
