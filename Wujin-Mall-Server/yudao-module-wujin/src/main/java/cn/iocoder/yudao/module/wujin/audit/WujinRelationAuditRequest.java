package cn.iocoder.yudao.module.wujin.audit;

import cn.iocoder.yudao.module.wujin.chain.WujinRiskWarning;
import cn.iocoder.yudao.module.wujin.merchant.WujinMerchantRelationSubmission;
import cn.iocoder.yudao.module.wujin.search.WujinLane;

import java.util.Collections;
import java.util.List;

public class WujinRelationAuditRequest {

    private final String auditId;
    private final WujinMerchantRelationSubmission submission;
    private final WujinLane declaredProductLane;
    private final WujinRiskWarning riskWarning;
    private final List<WujinSimilarEntity> similarEntities;

    private WujinRelationAuditRequest(Builder builder) {
        this.auditId = builder.auditId;
        this.submission = builder.submission;
        this.declaredProductLane = builder.declaredProductLane;
        this.riskWarning = builder.riskWarning == null ? WujinRiskWarning.none() : builder.riskWarning;
        this.similarEntities = builder.similarEntities == null ? Collections.emptyList() : builder.similarEntities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAuditId() {
        return auditId;
    }

    public WujinMerchantRelationSubmission getSubmission() {
        return submission;
    }

    public WujinLane getDeclaredProductLane() {
        return declaredProductLane;
    }

    public WujinRiskWarning getRiskWarning() {
        return riskWarning;
    }

    public List<WujinSimilarEntity> getSimilarEntities() {
        return similarEntities;
    }

    public static class Builder {
        private String auditId;
        private WujinMerchantRelationSubmission submission;
        private WujinLane declaredProductLane;
        private WujinRiskWarning riskWarning;
        private List<WujinSimilarEntity> similarEntities;

        public Builder auditId(String auditId) {
            this.auditId = auditId;
            return this;
        }

        public Builder submission(WujinMerchantRelationSubmission submission) {
            this.submission = submission;
            return this;
        }

        public Builder declaredProductLane(WujinLane declaredProductLane) {
            this.declaredProductLane = declaredProductLane;
            return this;
        }

        public Builder riskWarning(WujinRiskWarning riskWarning) {
            this.riskWarning = riskWarning;
            return this;
        }

        public Builder similarEntities(List<WujinSimilarEntity> similarEntities) {
            this.similarEntities = similarEntities;
            return this;
        }

        public WujinRelationAuditRequest build() {
            return new WujinRelationAuditRequest(this);
        }
    }
}
