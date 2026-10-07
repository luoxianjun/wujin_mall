package cn.iocoder.yudao.module.wujin.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金行业模板列表查询 Request VO")
public class WujinIndustryTemplateListReqVO {

    @Schema(description = "模板编码", example = "TPL_TIRE")
    private String templateCode;

    @Schema(description = "模板名称", example = "轮胎橡胶模板")
    private String name;

    @Schema(description = "行业编码", example = "轮胎橡胶")
    private String industryCode;

    @Schema(description = "成品泳道", example = "PRODUCT")
    private String productLane;

    @Schema(description = "开启状态", example = "0")
    private Integer status;

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
}
