package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 五金寻源线索进度 Response VO")
public class WujinSourcingLeadProgressRespVO {

    private Long leadId;
    private String title;
    private String keyword;
    private String lane;
    private String sourceKeyword;
    private String industry;
    private String supplierName;
    private String requirement;
    private String leadStatus;
    private String leadStatusName;
    private String dispatchStatus;
    private String summary;
    private Integer quotedAmount;
    private LocalDateTime createTime;
    /**
     * 进度节点；列表接口不返回
     */
    private List<Step> steps;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getSourceKeyword() {
        return sourceKeyword;
    }

    public void setSourceKeyword(String sourceKeyword) {
        this.sourceKeyword = sourceKeyword;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    public String getLeadStatusName() {
        return leadStatusName;
    }

    public void setLeadStatusName(String leadStatusName) {
        this.leadStatusName = leadStatusName;
    }

    public String getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getQuotedAmount() {
        return quotedAmount;
    }

    public void setQuotedAmount(Integer quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public static class Step {

        private String key;
        private String title;
        private String time;
        private String description;
        private Boolean done;
        private Boolean active;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Boolean getDone() {
            return done;
        }

        public void setDone(Boolean done) {
            this.done = done;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}
