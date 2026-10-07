package cn.iocoder.yudao.module.wujin.controller.admin.chain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金产业链实体新增/更新 Request VO")
public class WujinChainEntitySaveReqVO {

    private Long id;
    @NotBlank(message = "实体编码不能为空")
    private String entityCode;
    @NotBlank(message = "实体名称不能为空")
    private String name;
    @NotBlank(message = "泳道不能为空")
    private String lane;
    private String industries;
    private Boolean junctionFlag;
    private String riskNote;
    @NotNull(message = "开启状态不能为空")
    private Integer status;

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
}
