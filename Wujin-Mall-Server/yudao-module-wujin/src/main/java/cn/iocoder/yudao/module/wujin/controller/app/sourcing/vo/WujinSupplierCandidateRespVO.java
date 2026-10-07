package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户 App - 五金供应商候选 Response VO")
public class WujinSupplierCandidateRespVO {

    private Long id;
    /**
     * 候选来源：MERCHANT 商家供应能力/关系申报，id 为商家编号；PLATFORM 平台产业链实体兜底，id 为实体编号
     */
    private String supplierType;
    private Long entityId;
    private String entityName;
    private String lane;
    private String supplierName;
    private Integer matchScore;
    private String mainProducts;
    private String serviceNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getMainProducts() {
        return mainProducts;
    }

    public void setMainProducts(String mainProducts) {
        this.mainProducts = mainProducts;
    }

    public String getServiceNote() {
        return serviceNote;
    }

    public void setServiceNote(String serviceNote) {
        this.serviceNote = serviceNote;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }
}
