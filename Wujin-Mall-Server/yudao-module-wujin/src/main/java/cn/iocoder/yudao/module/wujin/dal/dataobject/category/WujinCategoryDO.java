package cn.iocoder.yudao.module.wujin.dal.dataobject.category;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("wujin_category")
@KeySequence("wujin_category_seq")
public class WujinCategoryDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    @TableId
    private Long id;
    private Long parentId;
    private String lane;
    private String code;
    private String name;
    private Integer level;
    private Integer sort;
    private Integer status;
    private Integer displayDepth;
    private String healthStatus;
    private String description;

    public static Builder builder() {
        return new Builder();
    }

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

    public static class Builder {
        private final WujinCategoryDO category = new WujinCategoryDO();

        public Builder id(Long id) {
            category.setId(id);
            return this;
        }

        public Builder parentId(Long parentId) {
            category.setParentId(parentId);
            return this;
        }

        public Builder lane(String lane) {
            category.setLane(lane);
            return this;
        }

        public Builder code(String code) {
            category.setCode(code);
            return this;
        }

        public Builder name(String name) {
            category.setName(name);
            return this;
        }

        public Builder level(Integer level) {
            category.setLevel(level);
            return this;
        }

        public Builder sort(Integer sort) {
            category.setSort(sort);
            return this;
        }

        public Builder status(Integer status) {
            category.setStatus(status);
            return this;
        }

        public Builder displayDepth(Integer displayDepth) {
            category.setDisplayDepth(displayDepth);
            return this;
        }

        public Builder healthStatus(String healthStatus) {
            category.setHealthStatus(healthStatus);
            return this;
        }

        public Builder description(String description) {
            category.setDescription(description);
            return this;
        }

        public WujinCategoryDO build() {
            return category;
        }
    }
}
