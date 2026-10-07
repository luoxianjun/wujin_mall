package cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("wujin_sourcing_lead")
@KeySequence("wujin_sourcing_lead_seq")
public class WujinSourcingLeadDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String keyword;
    private String lane;
    private String sourceKeyword;
    private String industry;
    private Long supplierId;
    private String supplierName;
    private Long merchantId;
    private String contactName;
    private String contactPhone;
    private String requirement;
    private String leadStatus;
    private String dispatchStatus;
    private String dispatchRemark;
    private String handleRemark;
    private LocalDateTime firstContactTime;
    private LocalDateTime quotedTime;
    private LocalDateTime convertedTime;
    private LocalDateTime lostTime;
    private Long processDurationMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public String getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public String getDispatchRemark() {
        return dispatchRemark;
    }

    public void setDispatchRemark(String dispatchRemark) {
        this.dispatchRemark = dispatchRemark;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public LocalDateTime getFirstContactTime() {
        return firstContactTime;
    }

    public void setFirstContactTime(LocalDateTime firstContactTime) {
        this.firstContactTime = firstContactTime;
    }

    public LocalDateTime getQuotedTime() {
        return quotedTime;
    }

    public void setQuotedTime(LocalDateTime quotedTime) {
        this.quotedTime = quotedTime;
    }

    public LocalDateTime getConvertedTime() {
        return convertedTime;
    }

    public void setConvertedTime(LocalDateTime convertedTime) {
        this.convertedTime = convertedTime;
    }

    public LocalDateTime getLostTime() {
        return lostTime;
    }

    public void setLostTime(LocalDateTime lostTime) {
        this.lostTime = lostTime;
    }

    public Long getProcessDurationMinutes() {
        return processDurationMinutes;
    }

    public void setProcessDurationMinutes(Long processDurationMinutes) {
        this.processDurationMinutes = processDurationMinutes;
    }
}
