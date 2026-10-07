package cn.iocoder.yudao.module.wujin.category;

public class WujinCategoryDisplayPolicy {

    private final int productMaxDepth;
    private final int processMaxDepth;
    private final int materialMaxDepth;

    public WujinCategoryDisplayPolicy(int productMaxDepth, int processMaxDepth, int materialMaxDepth) {
        this.productMaxDepth = productMaxDepth;
        this.processMaxDepth = processMaxDepth;
        this.materialMaxDepth = materialMaxDepth;
    }

    public int getProductMaxDepth() {
        return productMaxDepth;
    }

    public int getProcessMaxDepth() {
        return processMaxDepth;
    }

    public int getMaterialMaxDepth() {
        return materialMaxDepth;
    }
}
