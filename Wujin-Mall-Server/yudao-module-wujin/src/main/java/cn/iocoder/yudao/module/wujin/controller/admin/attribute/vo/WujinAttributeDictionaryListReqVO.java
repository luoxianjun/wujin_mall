package cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金平台属性字典列表 Request VO")
public class WujinAttributeDictionaryListReqVO {

    private String code;
    private String name;
    private String groupName;
    private String lane;
    private Long categoryId;
    private String valueType;
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
