package cn.iocoder.yudao.module.wujin.dal.dataobject.attribute;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import java.util.List;

/**
 * 五金平台属性字典：商家发布商品时填写的标准属性定义
 */
@TableName(value = "wujin_attribute_dictionary", autoResultMap = true)
@KeySequence("wujin_attribute_dictionary_seq")
public class WujinAttributeDictionaryDO extends BaseDO {

    @TableId
    private Long id;
    private String code;
    private String name;
    private String groupName;
    /**
     * 适用泳道：PRODUCT/PROCESS/MATERIAL，为空表示三泳道通用
     */
    private String lane;
    private Long categoryId;
    /**
     * 值类型：TEXT/NUMBER/ENUM/MULTI_ENUM/BOOLEAN
     */
    private String valueType;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> valueOptions;
    private String unit;
    private Boolean requiredFlag;
    private Boolean searchableFlag;
    private Integer sort;
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
