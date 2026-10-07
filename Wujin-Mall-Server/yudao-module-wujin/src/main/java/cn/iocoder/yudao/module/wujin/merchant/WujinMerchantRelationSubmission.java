package cn.iocoder.yudao.module.wujin.merchant;

import cn.iocoder.yudao.module.wujin.search.WujinLane;

import java.util.Collections;
import java.util.List;

public class WujinMerchantRelationSubmission {

    private final String productId;
    private final String productName;
    private final WujinLane productLane;
    private final String productCategoryId;
    private final List<WujinMerchantRelationDraft> relations;
    private int certificationCount;
    private boolean hasApplicationDescription;

    private WujinMerchantRelationSubmission(Builder builder) {
        this.productId = builder.productId;
        this.productName = builder.productName;
        this.productLane = builder.productLane;
        this.productCategoryId = builder.productCategoryId;
        this.relations = builder.relations == null ? Collections.emptyList() : builder.relations;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public WujinLane getProductLane() {
        return productLane;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public List<WujinMerchantRelationDraft> getRelations() {
        return relations;
    }

    public int getCertificationCount() {
        return certificationCount;
    }

    public void setCertificationCount(int certificationCount) {
        this.certificationCount = certificationCount;
    }

    public boolean isHasApplicationDescription() {
        return hasApplicationDescription;
    }

    public void setHasApplicationDescription(boolean hasApplicationDescription) {
        this.hasApplicationDescription = hasApplicationDescription;
    }

    public static class Builder {
        private String productId;
        private String productName;
        private WujinLane productLane;
        private String productCategoryId;
        private List<WujinMerchantRelationDraft> relations;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder productLane(WujinLane productLane) {
            this.productLane = productLane;
            return this;
        }

        public Builder productCategoryId(String productCategoryId) {
            this.productCategoryId = productCategoryId;
            return this;
        }

        public Builder relations(List<WujinMerchantRelationDraft> relations) {
            this.relations = relations;
            return this;
        }

        public WujinMerchantRelationSubmission build() {
            return new WujinMerchantRelationSubmission(this);
        }
    }
}
