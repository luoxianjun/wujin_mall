package cn.iocoder.yudao.module.wujin.dal.dataobject.chain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("wujin_chain_entity_relation")
@KeySequence("wujin_chain_entity_relation_seq")
public class WujinChainEntityRelationDO extends BaseDO {

    @TableId
    private Long id;
    private Long sourceEntityId;
    private Long targetEntityId;
    private String relationType;
    private Integer weight;
    private Integer costRatio;
    private String industryContext;
    private Integer auditStatus;
    private String auditRemark;

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceEntityId() {
        return sourceEntityId;
    }

    public void setSourceEntityId(Long sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    public Long getTargetEntityId() {
        return targetEntityId;
    }

    public void setTargetEntityId(Long targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getCostRatio() {
        return costRatio;
    }

    public void setCostRatio(Integer costRatio) {
        this.costRatio = costRatio;
    }

    public String getIndustryContext() {
        return industryContext;
    }

    public void setIndustryContext(String industryContext) {
        this.industryContext = industryContext;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public static class Builder {
        private final WujinChainEntityRelationDO relation = new WujinChainEntityRelationDO();

        public Builder id(Long id) {
            relation.setId(id);
            return this;
        }

        public Builder sourceEntityId(Long sourceEntityId) {
            relation.setSourceEntityId(sourceEntityId);
            return this;
        }

        public Builder targetEntityId(Long targetEntityId) {
            relation.setTargetEntityId(targetEntityId);
            return this;
        }

        public Builder relationType(String relationType) {
            relation.setRelationType(relationType);
            return this;
        }

        public Builder weight(Integer weight) {
            relation.setWeight(weight);
            return this;
        }

        public Builder costRatio(Integer costRatio) {
            relation.setCostRatio(costRatio);
            return this;
        }

        public Builder industryContext(String industryContext) {
            relation.setIndustryContext(industryContext);
            return this;
        }

        public Builder auditStatus(Integer auditStatus) {
            relation.setAuditStatus(auditStatus);
            return this;
        }

        public Builder auditRemark(String auditRemark) {
            relation.setAuditRemark(auditRemark);
            return this;
        }

        public WujinChainEntityRelationDO build() {
            return relation;
        }
    }
}
