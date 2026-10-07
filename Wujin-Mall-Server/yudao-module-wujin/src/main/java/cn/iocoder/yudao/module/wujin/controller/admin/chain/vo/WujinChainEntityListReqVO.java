package cn.iocoder.yudao.module.wujin.controller.admin.chain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金产业链实体列表查询 Request VO")
public class WujinChainEntityListReqVO {

    private String entityCode;
    private String name;
    private String lane;
    private String industry;
    private Boolean junctionFlag;
    private Integer status;

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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Boolean getJunctionFlag() {
        return junctionFlag;
    }

    public void setJunctionFlag(Boolean junctionFlag) {
        this.junctionFlag = junctionFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
