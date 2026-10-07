package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 五金供应商供应能力 Request VO")
public class WujinSupplierCapabilityReqVO {

    @Schema(description = "供应商编号：商家候选为商家编号，平台兜底候选为实体编号", example = "1001")
    @NotNull(message = "供应商编号不能为空")
    private Long supplierId;

    @Schema(description = "候选来源：MERCHANT/PLATFORM，为空按商家处理", example = "MERCHANT")
    private String supplierType;

    @Schema(description = "关键词", example = "天然橡胶")
    private String keyword;

    @Schema(description = "当前泳道", example = "MATERIAL")
    private String lane;

    @Schema(description = "来源关键词", example = "轮胎")
    private String sourceKeyword;

    @Schema(description = "行业上下文", example = "汽车")
    private String industry;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    public void setSourceKeyword(String sourceKeyword) {
        this.sourceKeyword = sourceKeyword;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
