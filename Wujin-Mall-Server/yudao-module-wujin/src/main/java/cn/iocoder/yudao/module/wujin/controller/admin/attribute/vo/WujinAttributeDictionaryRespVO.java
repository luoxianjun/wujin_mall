package cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo;

import java.time.LocalDateTime;
import java.util.List;

public class WujinAttributeDictionaryRespVO {

    private Long id;
    private String code;
    private String name;
    private String groupName;
    private String lane;
    private Long categoryId;
    private String valueType;
    private List<String> valueOptions;
    private String unit;
    private Boolean requiredFlag;
    private Boolean searchableFlag;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
