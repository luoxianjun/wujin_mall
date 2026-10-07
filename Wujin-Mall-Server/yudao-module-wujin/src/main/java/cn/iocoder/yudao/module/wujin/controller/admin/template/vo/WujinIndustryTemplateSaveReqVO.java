package cn.iocoder.yudao.module.wujin.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金行业模板新增/更新 Request VO")
public class WujinIndustryTemplateSaveReqVO {

    @Schema(description = "模板编号", example = "1")
    private Long id;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "TPL_TIRE")
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "轮胎橡胶模板")
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @Schema(description = "行业编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "轮胎橡胶")
    @NotBlank(message = "行业编码不能为空")
    private String industryCode;

    @Schema(description = "成品泳道", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRODUCT")
    @NotBlank(message = "成品泳道不能为空")
    private String productLane;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public String getProductLane() {
        return productLane;
    }

    public void setProductLane(String productLane) {
        this.productLane = productLane;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
