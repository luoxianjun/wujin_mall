package cn.iocoder.yudao.module.wujin.monitor;

public class WujinMonitorAlert {

    private final WujinMonitorMetric metric;
    private final String message;

    public WujinMonitorAlert(WujinMonitorMetric metric, String message) {
        this.metric = metric;
        this.message = message;
    }

    public WujinMonitorMetric getMetric() {
        return metric;
    }

    public String getMessage() {
        return message;
    }
}
