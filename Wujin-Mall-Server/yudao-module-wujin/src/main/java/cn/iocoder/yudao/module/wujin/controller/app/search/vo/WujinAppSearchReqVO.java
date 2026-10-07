package cn.iocoder.yudao.module.wujin.controller.app.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "用户 App - 五金三泳道搜索 Request VO")
public class WujinAppSearchReqVO {

    @Schema(description = "用户编号", example = "101")
    private Long userId;
    @Schema(description = "搜索关键词", requiredMode = Schema.RequiredMode.REQUIRED, example = "天然橡胶")
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    @Schema(description = "用户指定泳道", example = "MATERIAL")
    private String requestedLane;
    @Schema(description = "入口路径", example = "DIRECT_SEARCH")
    private String entryPath;
    @Schema(description = "来源泳道", example = "PRODUCT")
    private String sourceLane;
    @Schema(description = "来源关键词", example = "轮胎")
    private String sourceKeyword;
    @Schema(description = "来源商品编号，用于精确查询该商品的上游", example = "9001")
    private Long sourceProductId;
    @Schema(description = "来源产业链实体编号，用于精确查询该实体的上游", example = "100")
    private Long sourceEntityId;
    @Schema(description = "行业上下文", example = "医疗器械")
    private String industry;
    @Schema(description = "分类联想点击的分类编号", example = "3001")
    private Long categoryId;
    @Schema(description = "分类联想点击的分类路径", example = "衣服 > 男装 > 西服")
    private String categoryPath;
    @Schema(description = "是否查看制造链", example = "true")
    private Boolean chainViewed;
    @Schema(description = "满意度评分", example = "5")
    private Integer satisfactionScore;
    @Schema(description = "响应时间毫秒", example = "640")
    private Long responseTimeMillis;

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

    public String getRequestedLane() {
        return requestedLane;
    }

    public void setRequestedLane(String requestedLane) {
        this.requestedLane = requestedLane;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String entryPath) {
        this.entryPath = entryPath;
    }

    public String getSourceLane() {
        return sourceLane;
    }

    public void setSourceLane(String sourceLane) {
        this.sourceLane = sourceLane;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    public void setSourceKeyword(String sourceKeyword) {
        this.sourceKeyword = sourceKeyword;
    }

    public Long getSourceProductId() {
        return sourceProductId;
    }

    public void setSourceProductId(Long sourceProductId) {
        this.sourceProductId = sourceProductId;
    }

    public Long getSourceEntityId() {
        return sourceEntityId;
    }

    public void setSourceEntityId(Long sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public Boolean getChainViewed() {
        return chainViewed;
    }

    public void setChainViewed(Boolean chainViewed) {
        this.chainViewed = chainViewed;
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
