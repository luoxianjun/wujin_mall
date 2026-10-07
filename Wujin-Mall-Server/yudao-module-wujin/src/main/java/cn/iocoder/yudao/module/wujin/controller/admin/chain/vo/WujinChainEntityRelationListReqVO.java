package cn.iocoder.yudao.module.wujin.controller.admin.chain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金产业链实体关系列表查询 Request VO")
public class WujinChainEntityRelationListReqVO {

    private Long sourceEntityId;
    private Long targetEntityId;
    private String relationType;
    private String industryContext;
    private Integer auditStatus;

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
}
