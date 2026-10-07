package cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户 App - 五金寻源线索提交 Response VO")
public class WujinSourcingLeadSubmitRespVO {

    private Long leadId;
    private String leadStatus;
    private String message;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
