package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorTrendRespVO;

import java.time.LocalDate;

public interface WujinMonitorDashboardService {

    WujinMonitorDashboardRespVO getSummary();

    /**
     * 按天聚合搜索行为日志，生成截止 endDate 的近 days 天趋势、热搜词和泳道分布
     */
    WujinMonitorTrendRespVO getTrend(LocalDate endDate, int days);
}
