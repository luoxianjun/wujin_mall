package cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 五金寻源线索分发 Request VO")
public class WujinSourcingLeadDispatchReqVO {

    @NotNull(message = "线索编号不能为空")
    private Long leadId;
    @NotNull(message = "商家编号不能为空")
    private Long merchantId;
    private String dispatchRemark;

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

    public String getDispatchRemark() {
        return dispatchRemark;
    }

    public void setDispatchRemark(String dispatchRemark) {
        this.dispatchRemark = dispatchRemark;
    }
}
