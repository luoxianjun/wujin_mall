package cn.iocoder.yudao.module.wujin.dal.dataobject.category;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("wujin_category_mapping")
@KeySequence("wujin_category_mapping_seq")
public class WujinCategoryMappingDO extends BaseDO {

    @TableId
    private Long id;
    private Long sourceCategoryId;
    private String sourceLane;
    private Long targetCategoryId;
    private String targetLane;
    private String mappingType;
    private Integer confidence;
    private Integer status;
    private String riskNote;

    public static Builder builder() {
        return new Builder();
    }

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

    public static class Builder {
        private final WujinCategoryMappingDO mapping = new WujinCategoryMappingDO();

        public Builder id(Long id) {
            mapping.setId(id);
            return this;
        }

        public Builder sourceCategoryId(Long sourceCategoryId) {
            mapping.setSourceCategoryId(sourceCategoryId);
            return this;
        }

        public Builder sourceLane(String sourceLane) {
            mapping.setSourceLane(sourceLane);
            return this;
        }

        public Builder targetCategoryId(Long targetCategoryId) {
            mapping.setTargetCategoryId(targetCategoryId);
            return this;
        }

        public Builder targetLane(String targetLane) {
            mapping.setTargetLane(targetLane);
            return this;
        }

        public Builder mappingType(String mappingType) {
            mapping.setMappingType(mappingType);
            return this;
        }

        public Builder confidence(Integer confidence) {
            mapping.setConfidence(confidence);
            return this;
        }

        public Builder status(Integer status) {
            mapping.setStatus(status);
            return this;
        }

        public Builder riskNote(String riskNote) {
            mapping.setRiskNote(riskNote);
            return this;
        }

        public WujinCategoryMappingDO build() {
            return mapping;
        }
    }
}
