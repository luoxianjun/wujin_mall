package cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo;

import java.util.List;

public class WujinSourcingLeadConversionReportRespVO {

    private Integer totalLeadCount;
    private Integer contactedCount;
    private Integer quotedCount;
    private Integer convertedCount;
    private Integer lostCount;
    private String conversionRate;
    private Long averageProcessDurationMinutes;
    private List<MerchantStat> merchantStats;
    private List<LaneStat> laneStats;

    public Integer getTotalLeadCount() {
        return totalLeadCount;
    }

    public void setTotalLeadCount(Integer totalLeadCount) {
        this.totalLeadCount = totalLeadCount;
    }

    public Integer getContactedCount() {
        return contactedCount;
    }

    public void setContactedCount(Integer contactedCount) {
        this.contactedCount = contactedCount;
    }

    public Integer getQuotedCount() {
        return quotedCount;
    }

    public void setQuotedCount(Integer quotedCount) {
        this.quotedCount = quotedCount;
    }

    public Integer getConvertedCount() {
        return convertedCount;
    }

    public void setConvertedCount(Integer convertedCount) {
        this.convertedCount = convertedCount;
    }

    public Integer getLostCount() {
        return lostCount;
    }

    public void setLostCount(Integer lostCount) {
        this.lostCount = lostCount;
    }

    public String getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(String conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Long getAverageProcessDurationMinutes() {
        return averageProcessDurationMinutes;
    }

    public void setAverageProcessDurationMinutes(Long averageProcessDurationMinutes) {
        this.averageProcessDurationMinutes = averageProcessDurationMinutes;
    }

    public List<MerchantStat> getMerchantStats() {
        return merchantStats;
    }

    public void setMerchantStats(List<MerchantStat> merchantStats) {
        this.merchantStats = merchantStats;
    }

    public List<LaneStat> getLaneStats() {
        return laneStats;
    }

    public void setLaneStats(List<LaneStat> laneStats) {
        this.laneStats = laneStats;
    }

    public static class MerchantStat {
        private Long merchantId;
        private Integer totalLeadCount;
        private Integer convertedCount;
        private String conversionRate;

        public Long getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(Long merchantId) {
            this.merchantId = merchantId;
        }

        public Integer getTotalLeadCount() {
            return totalLeadCount;
        }

        public void setTotalLeadCount(Integer totalLeadCount) {
            this.totalLeadCount = totalLeadCount;
        }

        public Integer getConvertedCount() {
            return convertedCount;
        }

        public void setConvertedCount(Integer convertedCount) {
            this.convertedCount = convertedCount;
        }

        public String getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(String conversionRate) {
            this.conversionRate = conversionRate;
        }
    }

    public static class LaneStat {
        private String lane;
        private Integer totalLeadCount;
        private Integer convertedCount;
        private String conversionRate;

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }

        public Integer getTotalLeadCount() {
            return totalLeadCount;
        }

        public void setTotalLeadCount(Integer totalLeadCount) {
            this.totalLeadCount = totalLeadCount;
        }

        public Integer getConvertedCount() {
            return convertedCount;
        }

        public void setConvertedCount(Integer convertedCount) {
            this.convertedCount = convertedCount;
        }

        public String getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(String conversionRate) {
            this.conversionRate = conversionRate;
        }
    }
}
