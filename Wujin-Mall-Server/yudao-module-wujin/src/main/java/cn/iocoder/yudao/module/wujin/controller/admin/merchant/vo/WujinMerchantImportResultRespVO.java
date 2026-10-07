package cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo;

import java.util.List;

public class WujinMerchantImportResultRespVO {

    private Integer importedCount;
    private Integer skippedCount;
    private Integer effectiveCount;
    private Integer pendingReviewCount;
    private List<Long> submissionIds;
    private List<WujinMerchantImportPreviewRespVO.RowPreview> invalidRows;

    public Integer getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(Integer importedCount) {
        this.importedCount = importedCount;
    }

    public Integer getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(Integer skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<Long> getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(List<Long> submissionIds) {
        this.submissionIds = submissionIds;
    }

    public List<WujinMerchantImportPreviewRespVO.RowPreview> getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(List<WujinMerchantImportPreviewRespVO.RowPreview> invalidRows) {
        this.invalidRows = invalidRows;
    }

    public Integer getEffectiveCount() {
        return effectiveCount;
    }

    public void setEffectiveCount(Integer effectiveCount) {
        this.effectiveCount = effectiveCount;
    }

    public Integer getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Integer pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }
}
