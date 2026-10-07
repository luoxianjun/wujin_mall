package cn.iocoder.yudao.module.wujin.category;

import cn.iocoder.yudao.module.wujin.search.WujinLane;

public class WujinCategoryNode {

    private final String id;
    private final String name;
    private final WujinLane lane;
    private final WujinCategoryNode parent;
    private final int childCount;

    public WujinCategoryNode(String id, String name, WujinLane lane, WujinCategoryNode parent, int childCount) {
        this.id = id;
        this.name = name;
        this.lane = lane;
        this.parent = parent;
        this.childCount = childCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WujinLane getLane() {
        return lane;
    }

    public WujinCategoryNode getParent() {
        return parent;
    }

    public int getChildCount() {
        return childCount;
    }
}
