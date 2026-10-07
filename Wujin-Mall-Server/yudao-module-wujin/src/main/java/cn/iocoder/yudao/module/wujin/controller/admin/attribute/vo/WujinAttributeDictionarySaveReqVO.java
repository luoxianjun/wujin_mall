package cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Schema(description = "管理后台 - 五金平台属性字典新增/更新 Request VO")
public class WujinAttributeDictionarySaveReqVO {

    private Long id;
    @NotBlank(message = "属性编码不能为空")
    @Size(max = 64, message = "属性编码不能超过 64 个字符")
    private String code;
    @NotBlank(message = "属性名称不能为空")
    @Size(max = 64, message = "属性名称不能超过 64 个字符")
    private String name;
    @NotBlank(message = "属性分组不能为空")
    private String groupName;
    private String lane;
    private Long categoryId;
    @NotBlank(message = "值类型不能为空")
    private String valueType;
    private List<String> valueOptions;
    private String unit;
    private Boolean requiredFlag;
    private Boolean searchableFlag;
    private Integer sort;
    @NotNull(message = "开启状态不能为空")
    private Integer status;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<String> getValueOptions() {
        return valueOptions;
    }

    public void setValueOptions(List<String> valueOptions) {
        this.valueOptions = valueOptions;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Boolean requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public Boolean getSearchableFlag() {
        return searchableFlag;
    }

    public void setSearchableFlag(Boolean searchableFlag) {
        this.searchableFlag = searchableFlag;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
