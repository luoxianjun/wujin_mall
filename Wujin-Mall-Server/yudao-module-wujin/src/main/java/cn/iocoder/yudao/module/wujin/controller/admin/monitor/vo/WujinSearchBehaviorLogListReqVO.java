package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 五金搜索行为日志列表查询 Request VO")
public class WujinSearchBehaviorLogListReqVO {

    private Long userId;
    private String keyword;
    private String resultLane;
    private String industryCode;
    private Boolean chainViewed;
    private Boolean classificationCorrect;
    private Boolean highRiskWarningTriggered;

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

    public String getResultLane() {
        return resultLane;
    }

    public void setResultLane(String resultLane) {
        this.resultLane = resultLane;
    }

    public String getIndustryCode() {
        return industryCode;
    }

    public void setIndustryCode(String industryCode) {
        this.industryCode = industryCode;
    }

    public Boolean getChainViewed() {
        return chainViewed;
    }

    public void setChainViewed(Boolean chainViewed) {
        this.chainViewed = chainViewed;
    }

    public Boolean getClassificationCorrect() {
        return classificationCorrect;
    }

    public void setClassificationCorrect(Boolean classificationCorrect) {
        this.classificationCorrect = classificationCorrect;
    }

    public Boolean getHighRiskWarningTriggered() {
        return highRiskWarningTriggered;
    }

    public void setHighRiskWarningTriggered(Boolean highRiskWarningTriggered) {
        this.highRiskWarningTriggered = highRiskWarningTriggered;
    }
}
