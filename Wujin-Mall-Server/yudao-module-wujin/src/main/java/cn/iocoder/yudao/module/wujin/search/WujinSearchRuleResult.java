package cn.iocoder.yudao.module.wujin.search;

public class WujinSearchRuleResult {

    private final int granularity;
    private final WujinLane lane;
    private final boolean riskWarningRequired;
    private final String riskWarningText;
    private final String explanation;

    public WujinSearchRuleResult(int granularity, WujinLane lane, boolean riskWarningRequired,
                                 String riskWarningText, String explanation) {
        this.granularity = granularity;
        this.lane = lane;
        this.riskWarningRequired = riskWarningRequired;
        this.riskWarningText = riskWarningText;
        this.explanation = explanation;
    }

    public int getGranularity() {
        return granularity;
    }

    public WujinLane getLane() {
        return lane;
    }

    public boolean isRiskWarningRequired() {
        return riskWarningRequired;
    }

    public String getRiskWarningText() {
        return riskWarningText;
    }

    public String getExplanation() {
        return explanation;
    }
}
