package cn.iocoder.yudao.module.wujin.controller.admin.monitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorDashboardRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorTrendRespVO;
import cn.iocoder.yudao.module.wujin.service.monitor.WujinMonitorDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 五金运营看板")
@RestController
@RequestMapping("/wujin/monitor-dashboard")
@Validated
public class WujinMonitorDashboardController {

    @Resource
    private WujinMonitorDashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "获得运营看板汇总")
    @PreAuthorize("@ss.hasPermission('wujin:monitor-dashboard:query')")
    public CommonResult<WujinMonitorDashboardRespVO> getSummary() {
        return success(dashboardService.getSummary());
    }

    @GetMapping("/trend")
    @Operation(summary = "获得搜索监控分日趋势")
    @Parameter(name = "days", description = "统计天数，1-90，默认 7")
    @PreAuthorize("@ss.hasAnyPermissions('wujin:monitor-dashboard:query', 'wujin:search-behavior-log:query')")
    public CommonResult<WujinMonitorTrendRespVO> getTrend(
            @RequestParam(value = "days", required = false, defaultValue = "7") Integer days) {
        return success(dashboardService.getTrend(LocalDate.now(), days));
    }
}
