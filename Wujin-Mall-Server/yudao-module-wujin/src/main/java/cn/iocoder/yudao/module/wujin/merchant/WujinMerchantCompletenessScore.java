package cn.iocoder.yudao.module.wujin.merchant;

public class WujinMerchantCompletenessScore {

    private final int score;
    private final boolean benchmarkCandidate;
    private final String suggestion;

    public WujinMerchantCompletenessScore(int score, boolean benchmarkCandidate, String suggestion) {
        this.score = score;
        this.benchmarkCandidate = benchmarkCandidate;
        this.suggestion = suggestion;
    }

    public int getScore() {
        return score;
    }

    public boolean isBenchmarkCandidate() {
        return benchmarkCandidate;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
