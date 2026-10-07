package cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 五金搜索监控分日趋势 Response VO")
public class WujinMonitorTrendRespVO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer totalSearchCount;
    private List<DailyPoint> points;
    private List<KeywordStat> topKeywords;
    private List<LaneStat> laneStats;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getTotalSearchCount() {
        return totalSearchCount;
    }

    public void setTotalSearchCount(Integer totalSearchCount) {
        this.totalSearchCount = totalSearchCount;
    }

    public List<DailyPoint> getPoints() {
        return points;
    }

    public void setPoints(List<DailyPoint> points) {
        this.points = points;
    }

    public List<KeywordStat> getTopKeywords() {
        return topKeywords;
    }

    public void setTopKeywords(List<KeywordStat> topKeywords) {
        this.topKeywords = topKeywords;
    }

    public List<LaneStat> getLaneStats() {
        return laneStats;
    }

    public void setLaneStats(List<LaneStat> laneStats) {
        this.laneStats = laneStats;
    }

    public static class DailyPoint {

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Integer searchCount;
        private Double searchSatisfaction;
        private Double chainViewRate;
        private Double classificationAccuracy;
        private Double averageResponseTimeMillis;
        private Integer highRiskWarningCount;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Integer getSearchCount() {
            return searchCount;
        }

        public void setSearchCount(Integer searchCount) {
            this.searchCount = searchCount;
        }

        public Double getSearchSatisfaction() {
            return searchSatisfaction;
        }

        public void setSearchSatisfaction(Double searchSatisfaction) {
            this.searchSatisfaction = searchSatisfaction;
        }

        public Double getChainViewRate() {
            return chainViewRate;
        }

        public void setChainViewRate(Double chainViewRate) {
            this.chainViewRate = chainViewRate;
        }

        public Double getClassificationAccuracy() {
            return classificationAccuracy;
        }

        public void setClassificationAccuracy(Double classificationAccuracy) {
            this.classificationAccuracy = classificationAccuracy;
        }

        public Double getAverageResponseTimeMillis() {
            return averageResponseTimeMillis;
        }

        public void setAverageResponseTimeMillis(Double averageResponseTimeMillis) {
            this.averageResponseTimeMillis = averageResponseTimeMillis;
        }

        public Integer getHighRiskWarningCount() {
            return highRiskWarningCount;
        }

        public void setHighRiskWarningCount(Integer highRiskWarningCount) {
            this.highRiskWarningCount = highRiskWarningCount;
        }
    }

    public static class KeywordStat {

        private String keyword;
        private Integer searchCount;
        private Double chainViewRate;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Integer getSearchCount() {
            return searchCount;
        }

        public void setSearchCount(Integer searchCount) {
            this.searchCount = searchCount;
        }

        public Double getChainViewRate() {
            return chainViewRate;
        }

        public void setChainViewRate(Double chainViewRate) {
            this.chainViewRate = chainViewRate;
        }
    }

    public static class LaneStat {

        private String lane;
        private Integer searchCount;

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public Integer getSearchCount() {
            return searchCount;
        }

        public void setSearchCount(Integer searchCount) {
            this.searchCount = searchCount;
        }
    }
}
