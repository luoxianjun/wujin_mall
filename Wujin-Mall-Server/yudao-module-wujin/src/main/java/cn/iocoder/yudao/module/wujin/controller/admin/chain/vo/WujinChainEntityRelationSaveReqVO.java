package cn.iocoder.yudao.module.wujin.controller.admin.chain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金产业链实体关系新增/更新 Request VO")
public class WujinChainEntityRelationSaveReqVO {

    private Long id;
    @NotNull(message = "下游实体编号不能为空")
    private Long sourceEntityId;
    @NotNull(message = "上游实体编号不能为空")
    private Long targetEntityId;
    @NotBlank(message = "关系类型不能为空")
    private String relationType;
    private Integer weight;
    private Integer costRatio;
    private String industryContext;
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;
    private String auditRemark;

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
}
