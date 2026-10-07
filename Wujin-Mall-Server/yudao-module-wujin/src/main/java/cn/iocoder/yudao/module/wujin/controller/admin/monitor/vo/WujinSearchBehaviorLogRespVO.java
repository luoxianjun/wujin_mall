package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import java.time.LocalDateTime;

public class WujinSearchBehaviorLogRespVO {

    private Long id;
    private Long userId;
    private String keyword;
    private String intent;
    private String resultLane;
    private String industryCode;
    private Boolean chainViewed;
    private Boolean classificationCorrect;
    private Boolean highRiskWarningTriggered;
    private Integer satisfactionScore;
    private Long responseTimeMillis;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
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

    public Integer getSatisfactionScore() {
        return satisfactionScore;
    }

    public void setSatisfactionScore(Integer satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    public Long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public void setResponseTimeMillis(Long responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
