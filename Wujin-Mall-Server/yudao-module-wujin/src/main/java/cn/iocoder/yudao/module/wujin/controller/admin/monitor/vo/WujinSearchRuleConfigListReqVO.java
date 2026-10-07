package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金搜索规则配置列表查询 Request VO")
public class WujinSearchRuleConfigListReqVO {

    private String ruleType;
    private String lane;
    private String industryCode;
    private Integer status;

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
