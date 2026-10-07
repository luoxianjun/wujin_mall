package cn.iocoder.yudao.module.wujin.chain;

import cn.iocoder.yudao.module.wujin.search.WujinLane;

public class WujinTracePath {

    private final String displayText;
    private final WujinLane sourceLane;
    private final WujinLane targetLane;

    public WujinTracePath(String displayText, WujinLane sourceLane, WujinLane targetLane) {
        this.displayText = displayText;
        this.sourceLane = sourceLane;
        this.targetLane = targetLane;
    }

    public String getDisplayText() {
        return displayText;
    }

    public WujinLane getSourceLane() {
        return sourceLane;
    }

    public WujinLane getTargetLane() {
        return targetLane;
    }
}
