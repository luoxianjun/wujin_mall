package cn.iocoder.yudao.module.wujin.controller.app.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金三泳道搜索 Response VO")
public class WujinAppSearchRespVO {

    private String keyword;
    private String defaultLane;
    private Integer granularity;
    private String explanation;
    private String contextHint;
    private Boolean riskWarningRequired;
    private String riskWarningText;
    private String traceHint;
    private List<LaneSummary> laneSummaries;
    private List<CategorySuggestion> categorySuggestions;
    private List<CategoryGroup> categoryGroups;
    private List<RelatedProduct> relatedProducts;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDefaultLane() {
        return defaultLane;
    }

    public void setDefaultLane(String defaultLane) {
        this.defaultLane = defaultLane;
    }

    public Integer getGranularity() {
        return granularity;
    }

    public void setGranularity(Integer granularity) {
        this.granularity = granularity;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getContextHint() {
        return contextHint;
    }

    public void setContextHint(String contextHint) {
        this.contextHint = contextHint;
    }

    public Boolean getRiskWarningRequired() {
        return riskWarningRequired;
    }

    public void setRiskWarningRequired(Boolean riskWarningRequired) {
        this.riskWarningRequired = riskWarningRequired;
    }

    public String getRiskWarningText() {
        return riskWarningText;
    }

    public void setRiskWarningText(String riskWarningText) {
        this.riskWarningText = riskWarningText;
    }

    public String getTraceHint() {
        return traceHint;
    }

    public void setTraceHint(String traceHint) {
        this.traceHint = traceHint;
    }

    public List<LaneSummary> getLaneSummaries() {
        return laneSummaries;
    }

    public void setLaneSummaries(List<LaneSummary> laneSummaries) {
        this.laneSummaries = laneSummaries;
    }

    public List<CategorySuggestion> getCategorySuggestions() {
        return categorySuggestions;
    }

    public void setCategorySuggestions(List<CategorySuggestion> categorySuggestions) {
        this.categorySuggestions = categorySuggestions;
    }

    public List<CategoryGroup> getCategoryGroups() {
        return categoryGroups;
    }

    public void setCategoryGroups(List<CategoryGroup> categoryGroups) {
        this.categoryGroups = categoryGroups;
    }

    public List<RelatedProduct> getRelatedProducts() {
        return relatedProducts;
    }

    public void setRelatedProducts(List<RelatedProduct> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    public static class CategoryGroup {
        private Integer level;
        private String levelName;
        private List<CategorySuggestion> categories;

        public CategoryGroup() {
        }

        public CategoryGroup(Integer level, String levelName, List<CategorySuggestion> categories) {
            this.level = level;
            this.levelName = levelName;
            this.categories = categories;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getLevelName() {
            return levelName;
        }

        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }

        public List<CategorySuggestion> getCategories() {
            return categories;
        }

        public void setCategories(List<CategorySuggestion> categories) {
            this.categories = categories;
        }
    }

    public static class RelatedProduct {
        private Long id;
        private String name;
        private String picUrl;
        private Integer price;
        private Integer marketPrice;
        private Integer stock;
        private Long merchantId;
        private Long entityId;
        private String entityName;
        private String lane;
        private Integer minOrderQuantity;
        private Integer deliveryDays;
        private String serviceArea;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

        public Integer getMarketPrice() {
            return marketPrice;
        }

        public void setMarketPrice(Integer marketPrice) {
            this.marketPrice = marketPrice;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public Long getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(Long merchantId) {
            this.merchantId = merchantId;
        }

        public Long getEntityId() {
            return entityId;
        }

        public void setEntityId(Long entityId) {
            this.entityId = entityId;
        }

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public Integer getMinOrderQuantity() {
            return minOrderQuantity;
        }

        public void setMinOrderQuantity(Integer minOrderQuantity) {
            this.minOrderQuantity = minOrderQuantity;
        }

        public Integer getDeliveryDays() {
            return deliveryDays;
        }

        public void setDeliveryDays(Integer deliveryDays) {
            this.deliveryDays = deliveryDays;
        }

        public String getServiceArea() {
            return serviceArea;
        }

        public void setServiceArea(String serviceArea) {
            this.serviceArea = serviceArea;
        }
    }

    public static class LaneSummary {
        private String lane;
        private String laneName;
        private Integer resultCount;
        private String summary;
        private String nextAction;

        public LaneSummary() {
        }

        public LaneSummary(String lane, String laneName, Integer resultCount, String summary, String nextAction) {
            this.lane = lane;
            this.laneName = laneName;
            this.resultCount = resultCount;
            this.summary = summary;
            this.nextAction = nextAction;
        }

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public String getLaneName() {
            return laneName;
        }

        public void setLaneName(String laneName) {
            this.laneName = laneName;
        }

        public Integer getResultCount() {
            return resultCount;
        }

        public void setResultCount(Integer resultCount) {
            this.resultCount = resultCount;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getNextAction() {
            return nextAction;
        }

        public void setNextAction(String nextAction) {
            this.nextAction = nextAction;
        }
    }

    public static class CategorySuggestion {
        private Long categoryId;
        private String categoryName;
        private String categoryPath;
        private String lane;
        private String laneName;
        private Integer level;

        public CategorySuggestion() {
        }

        public CategorySuggestion(Long categoryId, String categoryName, String categoryPath, String lane,
                                  String laneName, Integer level) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryPath = categoryPath;
            this.lane = lane;
            this.laneName = laneName;
            this.level = level;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryPath() {
            return categoryPath;
        }

        public void setCategoryPath(String categoryPath) {
            this.categoryPath = categoryPath;
        }

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public String getLaneName() {
            return laneName;
        }

        public void setLaneName(String laneName) {
            this.laneName = laneName;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }
}
