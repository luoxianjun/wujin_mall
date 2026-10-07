package cn.iocoder.yudao.module.wujin.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金行业模板项新增/更新 Request VO")
public class WujinIndustryTemplateItemSaveReqVO {

    @Schema(description = "模板项编号", example = "1")
    private Long id;

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "模板编号不能为空")
    private Long templateId;

    @Schema(description = "关联实体编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "关联实体编号不能为空")
    private Long entityId;

    @Schema(description = "关系类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REQUIRES_MATERIAL")
    @NotBlank(message = "关系类型不能为空")
    private String relationType;

    @Schema(description = "是否必填", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否必填不能为空")
    private Boolean requiredFlag;

    @Schema(description = "排序", example = "10")
    private Integer sort;

    @Schema(description = "权重", example = "80")
    private Integer weight;

    @Schema(description = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
