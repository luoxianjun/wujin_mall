package cn.iocoder.yudao.module.forum.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsTrendRespVO;
import cn.iocoder.yudao.module.forum.service.statistics.ForumStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 论坛统计")
@RestController
@RequestMapping("/forum/statistics")
@Validated
public class AdminForumStatisticsController {

    @Resource
    private ForumStatisticsService statisticsService;

    @GetMapping("/summary")
    @Operation(summary = "统计今日新增会员、帖子、活动和交互")
    public CommonResult<AdminForumStatisticsRespVO> getStatisticsSummary() {
        return success(statisticsService.getStatisticsSummary());
    }

    @GetMapping("/trend")
    @Operation(summary = "统计近30天会员增量、帖子增量")
    public CommonResult<AdminForumStatisticsTrendRespVO> getLast30DaysTrend() {
        return success(statisticsService.getLast30DaysTrend());
    }

}
