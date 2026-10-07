package cn.iocoder.yudao.module.wujin.search;

import java.util.Collections;
import java.util.List;

public class WujinSearchDecisionRequest {

    private final String keyword;
    private final WujinSearchIntent intent;
    private final WujinEntryPath entryPath;
    private final WujinLane sourceLane;
    private final WujinLane requestedLane;
    private final String sourceKeyword;
    private final String industry;
    private final List<String> riskTags;
    private final int childCount;

    private WujinSearchDecisionRequest(Builder builder) {
        this.keyword = builder.keyword;
        this.intent = builder.intent == null ? WujinSearchIntent.UNKNOWN : builder.intent;
        this.entryPath = builder.entryPath == null ? WujinEntryPath.DIRECT_SEARCH : builder.entryPath;
        this.sourceLane = builder.sourceLane;
        this.requestedLane = builder.requestedLane;
        this.sourceKeyword = builder.sourceKeyword;
        this.industry = builder.industry;
        this.riskTags = builder.riskTags == null ? Collections.emptyList() : builder.riskTags;
        this.childCount = builder.childCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getKeyword() {
        return keyword;
    }

    public WujinSearchIntent getIntent() {
        return intent;
    }

    public WujinEntryPath getEntryPath() {
        return entryPath;
    }

    public WujinLane getSourceLane() {
        return sourceLane;
    }

    public WujinLane getRequestedLane() {
        return requestedLane;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    public String getIndustry() {
        return industry;
    }

    public List<String> getRiskTags() {
        return riskTags;
    }

    public int getChildCount() {
        return childCount;
    }

    public static class Builder {
        private String keyword;
        private WujinSearchIntent intent;
        private WujinEntryPath entryPath;
        private WujinLane sourceLane;
        private WujinLane requestedLane;
        private String sourceKeyword;
        private String industry;
        private List<String> riskTags;
        private int childCount;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder intent(WujinSearchIntent intent) {
            this.intent = intent;
            return this;
        }

        public Builder entryPath(WujinEntryPath entryPath) {
            this.entryPath = entryPath;
            return this;
        }

        public Builder sourceLane(WujinLane sourceLane) {
            this.sourceLane = sourceLane;
            return this;
        }

        public Builder requestedLane(WujinLane requestedLane) {
            this.requestedLane = requestedLane;
            return this;
        }

        public Builder sourceKeyword(String sourceKeyword) {
            this.sourceKeyword = sourceKeyword;
            return this;
        }

        public Builder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder riskTags(List<String> riskTags) {
            this.riskTags = riskTags;
            return this;
        }

        public Builder childCount(int childCount) {
            this.childCount = childCount;
            return this;
        }

        public WujinSearchDecisionRequest build() {
            return new WujinSearchDecisionRequest(this);
        }
    }
}
