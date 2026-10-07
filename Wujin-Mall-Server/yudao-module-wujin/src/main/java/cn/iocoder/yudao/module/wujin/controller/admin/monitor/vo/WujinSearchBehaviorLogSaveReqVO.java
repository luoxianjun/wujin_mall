package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金搜索行为日志新增/更新 Request VO")
public class WujinSearchBehaviorLogSaveReqVO {

    private Long id;
    private Long userId;
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    private String intent;
    @NotBlank(message = "结果泳道不能为空")
    private String resultLane;
    private String industryCode;
    @NotNull(message = "是否查看制造链不能为空")
    private Boolean chainViewed;
    private Boolean classificationCorrect;
    @NotNull(message = "是否触发高风险提示不能为空")
    private Boolean highRiskWarningTriggered;
    private Integer satisfactionScore;
    @NotNull(message = "响应时间不能为空")
    private Long responseTimeMillis;

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
}
