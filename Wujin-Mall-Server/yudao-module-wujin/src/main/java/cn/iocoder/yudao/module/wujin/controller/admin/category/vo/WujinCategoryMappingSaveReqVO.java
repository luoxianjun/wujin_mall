package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金分类映射新增/更新 Request VO")
public class WujinCategoryMappingSaveReqVO {

    @Schema(description = "映射编号", example = "1")
    private Long id;

    @Schema(description = "来源分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源分类编号不能为空")
    private Long sourceCategoryId;

    @Schema(description = "来源泳道", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRODUCT")
    @NotBlank(message = "来源泳道不能为空")
    private String sourceLane;

    @Schema(description = "目标分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "目标分类编号不能为空")
    private Long targetCategoryId;

    @Schema(description = "目标泳道", requiredMode = Schema.RequiredMode.REQUIRED, example = "MATERIAL")
    @NotBlank(message = "目标泳道不能为空")
    private String targetLane;

    @Schema(description = "映射类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REQUIRES_MATERIAL")
    @NotBlank(message = "映射类型不能为空")
    private String mappingType;

    @Schema(description = "置信度", example = "95")
    private Integer confidence;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "风险说明")
    private String riskNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceCategoryId() {
        return sourceCategoryId;
    }

    public void setSourceCategoryId(Long sourceCategoryId) {
        this.sourceCategoryId = sourceCategoryId;
    }

    public String getSourceLane() {
        return sourceLane;
    }

    public void setSourceLane(String sourceLane) {
        this.sourceLane = sourceLane;
    }

    public Long getTargetCategoryId() {
        return targetCategoryId;
    }

    public void setTargetCategoryId(Long targetCategoryId) {
        this.targetCategoryId = targetCategoryId;
    }

    public String getTargetLane() {
        return targetLane;
    }

    public void setTargetLane(String targetLane) {
        this.targetLane = targetLane;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRiskNote() {
        return riskNote;
    }

    public void setRiskNote(String riskNote) {
        this.riskNote = riskNote;
    }
}
