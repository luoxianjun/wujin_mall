package cn.iocoder.yudao.module.wujin.merchant;

public class WujinMerchantAuditDecision {

    private final WujinMerchantAuditRoute route;
    private final String reason;

    public WujinMerchantAuditDecision(WujinMerchantAuditRoute route, String reason) {
        this.route = route;
        this.reason = reason;
    }

    public WujinMerchantAuditRoute getRoute() {
        return route;
    }

    public String getReason() {
        return reason;
    }
}
