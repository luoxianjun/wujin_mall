package cn.iocoder.yudao.module.wujin.search;

public class WujinSearchRuleContext {

    private final String keyword;
    private final WujinLane lane;
    private final String industry;
    private final WujinEntryPath entryPath;
    private final WujinLane sourceLane;
    private final String sourceKeyword;

    public WujinSearchRuleContext(String keyword, WujinLane lane, String industry, WujinEntryPath entryPath,
                                  WujinLane sourceLane, String sourceKeyword) {
        this.keyword = keyword;
        this.lane = lane;
        this.industry = industry;
        this.entryPath = entryPath;
        this.sourceLane = sourceLane;
        this.sourceKeyword = sourceKeyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public WujinLane getLane() {
        return lane;
    }

    public String getIndustry() {
        return industry;
    }

    public WujinEntryPath getEntryPath() {
        return entryPath;
    }

    public WujinLane getSourceLane() {
        return sourceLane;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    String valueOf(String field) {
        if ("keyword".equals(field)) {
            return keyword;
        }
        if ("lane".equals(field)) {
            return lane == null ? null : lane.name();
        }
        if ("industry".equals(field)) {
            return industry;
        }
        if ("entryPath".equals(field)) {
            return entryPath == null ? null : entryPath.name();
        }
        if ("sourceLane".equals(field)) {
            return sourceLane == null ? null : sourceLane.name();
        }
        if ("sourceKeyword".equals(field)) {
            return sourceKeyword;
        }
        return null;
    }
}
