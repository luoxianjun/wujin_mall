package cn.iocoder.yudao.module.wujin.category;

public class WujinCategoryHealthSummary {

    private final int healthyCount;
    private final int needsSplitCount;
    private final int unboundCount;
    private final int totalCount;

    public WujinCategoryHealthSummary(int healthyCount, int needsSplitCount, int unboundCount, int totalCount) {
        this.healthyCount = healthyCount;
        this.needsSplitCount = needsSplitCount;
        this.unboundCount = unboundCount;
        this.totalCount = totalCount;
    }

    public int getHealthyCount() {
        return healthyCount;
    }

    public int getNeedsSplitCount() {
        return needsSplitCount;
    }

    public int getUnboundCount() {
        return unboundCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
