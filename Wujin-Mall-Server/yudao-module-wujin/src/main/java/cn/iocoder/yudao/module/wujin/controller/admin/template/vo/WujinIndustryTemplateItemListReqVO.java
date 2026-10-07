package cn.iocoder.yudao.module.wujin.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金行业模板项列表查询 Request VO")
public class WujinIndustryTemplateItemListReqVO {

    @Schema(description = "模板编号", example = "1")
    private Long templateId;

    @Schema(description = "关联实体编号", example = "10")
    private Long entityId;

    @Schema(description = "关系类型", example = "REQUIRES_MATERIAL")
    private String relationType;

    @Schema(description = "是否必填", example = "true")
    private Boolean requiredFlag;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    public Boolean getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Boolean requiredFlag) {
        this.requiredFlag = requiredFlag;
    }
}
