package cn.iocoder.yudao.module.wujin.category;

public class WujinCategoryMappingRule {

    private final String sourceCategoryId;
    private final String targetCategoryId;

    public WujinCategoryMappingRule(String sourceCategoryId, String targetCategoryId) {
        this.sourceCategoryId = sourceCategoryId;
        this.targetCategoryId = targetCategoryId;
    }

    public String getSourceCategoryId() {
        return sourceCategoryId;
    }

    public String getTargetCategoryId() {
        return targetCategoryId;
    }

    public boolean matches(String categoryId) {
        return sourceCategoryId.equals(categoryId) || targetCategoryId.equals(categoryId);
    }
}
