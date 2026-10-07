package cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "商家后台 - 五金寻源线索处理 Request VO")
public class WujinMerchantSourcingLeadHandleReqVO {

    @NotNull(message = "线索编号不能为空")
    private Long leadId;
    @NotNull(message = "商家编号不能为空")
    private Long merchantId;
    @NotBlank(message = "处理动作不能为空")
    private String handleAction;
    private String handleRemark;

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
}
