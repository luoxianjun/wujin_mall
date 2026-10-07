package cn.iocoder.yudao.module.gamification.controller.admin.stats;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.gamification.controller.admin.stats.vo.GamificationStatsRespVO;
import cn.iocoder.yudao.module.gamification.service.stats.GamificationStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 游戏化综合统计")
@RestController
@RequestMapping("/gamification/stats")
@Validated
public class GamificationStatsController {

    @Resource
    private GamificationStatsService gamificationStatsService;

    @GetMapping("/overview")
    @Operation(summary = "获取游戏化综合统计")
    @PreAuthorize("@ss.hasPermission('gamification:stats:query')")
    public CommonResult<GamificationStatsRespVO> getOverviewStats() {
        return success(gamificationStatsService.getOverviewStats());
    }
}
