package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 五金分类映射 Response VO")
public class WujinCategoryMappingRespVO {

    @Schema(description = "映射编号", example = "1")
    private Long id;
    @Schema(description = "来源分类编号", example = "1")
    private Long sourceCategoryId;
    @Schema(description = "来源泳道", example = "PRODUCT")
    private String sourceLane;
    @Schema(description = "目标分类编号", example = "2")
    private Long targetCategoryId;
    @Schema(description = "目标泳道", example = "MATERIAL")
    private String targetLane;
    @Schema(description = "映射类型", example = "REQUIRES_MATERIAL")
    private String mappingType;
    @Schema(description = "置信度", example = "95")
    private Integer confidence;
    @Schema(description = "开启状态", example = "0")
    private Integer status;
    @Schema(description = "风险说明")
    private String riskNote;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
