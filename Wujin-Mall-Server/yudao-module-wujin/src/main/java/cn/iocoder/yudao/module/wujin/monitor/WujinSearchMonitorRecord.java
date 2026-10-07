package cn.iocoder.yudao.module.wujin.monitor;

public class WujinSearchMonitorRecord {

    private final String keyword;
    private final Integer satisfactionScore;
    private final boolean chainViewed;
    private final Boolean classificationCorrect;
    private final long responseTimeMillis;
    private final boolean highRiskWarningTriggered;

    private WujinSearchMonitorRecord(Builder builder) {
        this.keyword = builder.keyword;
        this.satisfactionScore = builder.satisfactionScore;
        this.chainViewed = builder.chainViewed;
        this.classificationCorrect = builder.classificationCorrect;
        this.responseTimeMillis = builder.responseTimeMillis;
        this.highRiskWarningTriggered = builder.highRiskWarningTriggered;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public Integer getSatisfactionScore() {
        return satisfactionScore;
    }

    public boolean isChainViewed() {
        return chainViewed;
    }

    public Boolean getClassificationCorrect() {
        return classificationCorrect;
    }

    public long getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public boolean isHighRiskWarningTriggered() {
        return highRiskWarningTriggered;
    }

    public static class Builder {
        private String keyword;
        private Integer satisfactionScore;
        private boolean chainViewed;
        private Boolean classificationCorrect;
        private long responseTimeMillis;
        private boolean highRiskWarningTriggered;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder satisfactionScore(Integer satisfactionScore) {
            this.satisfactionScore = satisfactionScore;
            return this;
        }

        public Builder chainViewed(boolean chainViewed) {
            this.chainViewed = chainViewed;
            return this;
        }

        public Builder classificationCorrect(Boolean classificationCorrect) {
            this.classificationCorrect = classificationCorrect;
            return this;
        }

        public Builder responseTimeMillis(long responseTimeMillis) {
            this.responseTimeMillis = responseTimeMillis;
            return this;
        }

        public Builder highRiskWarningTriggered(boolean highRiskWarningTriggered) {
            this.highRiskWarningTriggered = highRiskWarningTriggered;
            return this;
        }

        public WujinSearchMonitorRecord build() {
            return new WujinSearchMonitorRecord(this);
        }
    }
}
