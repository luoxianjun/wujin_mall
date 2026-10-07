package cn.iocoder.yudao.module.wujin.dal.dataobject.chain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("wujin_chain_entity")
@KeySequence("wujin_chain_entity_seq")
public class WujinChainEntityDO extends BaseDO {

    @TableId
    private Long id;
    private String entityCode;
    private String name;
    private String lane;
    private String industries;
    private Boolean junctionFlag;
    private String riskNote;
    private Integer status;

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getIndustries() {
        return industries;
    }

    public void setIndustries(String industries) {
        this.industries = industries;
    }

    public Boolean getJunctionFlag() {
        return junctionFlag;
    }

    public void setJunctionFlag(Boolean junctionFlag) {
        this.junctionFlag = junctionFlag;
    }

    public String getRiskNote() {
        return riskNote;
    }

    public void setRiskNote(String riskNote) {
        this.riskNote = riskNote;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public static class Builder {
        private final WujinChainEntityDO entity = new WujinChainEntityDO();

        public Builder id(Long id) {
            entity.setId(id);
            return this;
        }

        public Builder entityCode(String entityCode) {
            entity.setEntityCode(entityCode);
            return this;
        }

        public Builder name(String name) {
            entity.setName(name);
            return this;
        }

        public Builder lane(String lane) {
            entity.setLane(lane);
            return this;
        }

        public Builder industries(String industries) {
            entity.setIndustries(industries);
            return this;
        }

        public Builder junctionFlag(Boolean junctionFlag) {
            entity.setJunctionFlag(junctionFlag);
            return this;
        }

        public Builder riskNote(String riskNote) {
            entity.setRiskNote(riskNote);
            return this;
        }

        public Builder status(Integer status) {
            entity.setStatus(status);
            return this;
        }

        public WujinChainEntityDO build() {
            return entity;
        }
    }
}
