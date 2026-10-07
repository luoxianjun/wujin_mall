package cn.iocoder.yudao.module.wujin.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collection;

@Schema(description = "管理后台 - 五金三泳道分类列表查询 Request VO")
public class WujinCategoryListReqVO {

    @Schema(description = "分类名称", example = "轮胎")
    private String name;

    @Schema(description = "分类编码", example = "P-TIRE")
    private String code;

    @Schema(description = "泳道", example = "PRODUCT")
    private String lane;

    @Schema(description = "父分类编号", example = "1")
    private Long parentId;

    @Schema(description = "父分类编号数组")
    private Collection<Long> parentIds;

    @Schema(description = "开启状态", example = "0")
    private Integer status;

    @Schema(description = "健康状态", example = "HEALTHY")
    private String healthStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Collection<Long> getParentIds() {
        return parentIds;
    }

    public void setParentIds(Collection<Long> parentIds) {
        this.parentIds = parentIds;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
}
