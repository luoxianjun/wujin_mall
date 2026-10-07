package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "用户 App - 五金寻源线索提交 Request VO")
public class WujinSourcingLeadSubmitReqVO {

    @Schema(description = "用户编号", example = "201")
    private Long userId;
    @Schema(description = "搜索关键词", requiredMode = Schema.RequiredMode.REQUIRED, example = "天然橡胶")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    @Schema(description = "当前泳道", example = "MATERIAL")
    private String lane;
    @Schema(description = "来源关键词", example = "轮胎")
    private String sourceKeyword;
    @Schema(description = "行业上下文", example = "橡胶")
    private String industry;
    @Schema(description = "候选供应商编号", example = "11")
    private Long supplierId;
    @Schema(description = "候选供应商名称", example = "天然橡胶供应协作商")
    private String supplierName;
    @Schema(description = "联系人", example = "张三")
    private String contactName;
    @Schema(description = "联系方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800000000")
    @NotBlank(message = "联系方式不能为空")
    private String contactPhone;
    @Schema(description = "需求说明", requiredMode = Schema.RequiredMode.REQUIRED, example = "需要天然橡胶样品")
    @NotBlank(message = "需求说明不能为空")
    private String requirement;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
}
