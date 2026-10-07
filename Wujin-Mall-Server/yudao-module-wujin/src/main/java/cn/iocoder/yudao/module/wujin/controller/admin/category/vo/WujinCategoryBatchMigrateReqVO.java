package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 五金三泳道分类批量迁移 Request VO")
public class WujinCategoryBatchMigrateReqVO {

    @Schema(description = "分类编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分类编号列表不能为空")
    private List<Long> ids;

    @Schema(description = "目标父分类编号，根节点为 0", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "目标父分类编号不能为空")
    private Long targetParentId;

    @Schema(description = "目标泳道", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROCESS")
    @NotBlank(message = "目标泳道不能为空")
    private String targetLane;

    @Schema(description = "目标分类层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "目标分类层级不能为空")
    private Integer targetLevel;

    @Schema(description = "目标展示深度", example = "3")
    private Integer displayDepth;

    @Schema(description = "目标健康状态", example = "NEEDS_SPLIT")
    private String healthStatus;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Long getTargetParentId() {
        return targetParentId;
    }

    public void setTargetParentId(Long targetParentId) {
        this.targetParentId = targetParentId;
    }

    public String getTargetLane() {
        return targetLane;
    }

    public void setTargetLane(String targetLane) {
        this.targetLane = targetLane;
    }

    public Integer getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(Integer targetLevel) {
        this.targetLevel = targetLevel;
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
}
