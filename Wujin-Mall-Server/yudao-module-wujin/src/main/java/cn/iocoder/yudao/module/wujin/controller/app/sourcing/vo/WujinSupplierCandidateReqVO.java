package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "用户 App - 五金供应商候选 Request VO")
public class WujinSupplierCandidateReqVO {

    @Schema(description = "搜索关键词", requiredMode = Schema.RequiredMode.REQUIRED, example = "天然橡胶")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    @Schema(description = "当前泳道", example = "MATERIAL")
    private String lane;
    @Schema(description = "来源关键词", example = "轮胎")
    private String sourceKeyword;
    @Schema(description = "行业上下文", example = "橡胶")
    private String industry;

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
