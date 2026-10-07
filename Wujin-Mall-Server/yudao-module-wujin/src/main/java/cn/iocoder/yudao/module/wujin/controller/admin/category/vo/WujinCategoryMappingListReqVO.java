package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金分类映射列表查询 Request VO")
public class WujinCategoryMappingListReqVO {

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

    @Schema(description = "开启状态", example = "0")
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
