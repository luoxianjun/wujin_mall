package cn.iocoder.yudao.module.wujin.search;

public class WujinSearchDecision {

    private final WujinLane defaultLane;
    private final int granularity;
    private final String explanation;
    private final String contextHint;
    private final boolean riskWarningRequired;
    private final String riskWarningText;

    public WujinSearchDecision(WujinLane defaultLane, int granularity, String explanation,
                               String contextHint, boolean riskWarningRequired) {
        this(defaultLane, granularity, explanation, contextHint, riskWarningRequired, null);
    }

    public WujinSearchDecision(WujinLane defaultLane, int granularity, String explanation,
                               String contextHint, boolean riskWarningRequired, String riskWarningText) {
        this.defaultLane = defaultLane;
        this.granularity = granularity;
        this.explanation = explanation;
        this.contextHint = contextHint;
        this.riskWarningRequired = riskWarningRequired;
        this.riskWarningText = riskWarningText;
    }

    public WujinLane getDefaultLane() {
        return defaultLane;
    }

    public int getGranularity() {
        return granularity;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getContextHint() {
        return contextHint;
    }

    public boolean isRiskWarningRequired() {
        return riskWarningRequired;
    }

    public String getRiskWarningText() {
        return riskWarningText;
    }
}
