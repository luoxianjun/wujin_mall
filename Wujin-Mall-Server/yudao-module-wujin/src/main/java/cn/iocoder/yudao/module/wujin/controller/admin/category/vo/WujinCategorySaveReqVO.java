package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金三泳道分类新增/更新 Request VO")
public class WujinCategorySaveReqVO {

    @Schema(description = "分类编号", example = "1")
    private Long id;

    @Schema(description = "父分类编号，根节点为 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "父分类编号不能为空")
    private Long parentId;

    @Schema(description = "泳道", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRODUCT")
    @NotBlank(message = "泳道不能为空")
    private String lane;

    @Schema(description = "分类编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "P-TIRE-CAR")
    @NotBlank(message = "分类编码不能为空")
    private String code;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "乘用车轮胎")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "分类层级不能为空")
    private Integer level;

    @Schema(description = "排序", example = "10")
    private Integer sort;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "默认展示深度", example = "2")
    private Integer displayDepth;

    @Schema(description = "健康状态", example = "HEALTHY")
    private String healthStatus;

    @Schema(description = "分类说明")
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDisplayDepth() {
        return displayDepth;
    }

    public void setDisplayDepth(Integer displayDepth) {
        this.displayDepth = displayDepth;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
