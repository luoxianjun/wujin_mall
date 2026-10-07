package cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "商家后台 - 五金寻源线索处理 Request VO")
public class WujinMerchantSourcingLeadHandleReqVO {

    @NotNull(message = "线索编号不能为空")
    private Long leadId;
    /**
     * 商家编号；为空时使用当前登录用户
     */
    private Long merchantId;
    @NotBlank(message = "处理动作不能为空")
    private String handleAction;
    private String handleRemark;
    private String followStage;
    private LocalDateTime nextFollowTime;
    @Min(value = 0, message = "报价金额不能为负数")
    private Integer quotedAmount;
    @Min(value = 0, message = "预计转化率不能小于 0")
    @Max(value = 100, message = "预计转化率不能大于 100")
    private Integer winProbability;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getHandleAction() {
        return handleAction;
    }

    public void setHandleAction(String handleAction) {
        this.handleAction = handleAction;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public String getFollowStage() {
        return followStage;
    }

    public void setFollowStage(String followStage) {
        this.followStage = followStage;
    }

    public LocalDateTime getNextFollowTime() {
        return nextFollowTime;
    }

    public void setNextFollowTime(LocalDateTime nextFollowTime) {
        this.nextFollowTime = nextFollowTime;
    }

    public Integer getQuotedAmount() {
        return quotedAmount;
    }

    public void setQuotedAmount(Integer quotedAmount) {
        this.quotedAmount = quotedAmount;
    }

    public Integer getWinProbability() {
        return winProbability;
    }

    public void setWinProbability(Integer winProbability) {
        this.winProbability = winProbability;
    }
}
