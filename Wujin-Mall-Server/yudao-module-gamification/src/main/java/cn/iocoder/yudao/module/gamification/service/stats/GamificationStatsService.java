package cn.iocoder.yudao.module.gamification.service.stats;

import cn.iocoder.yudao.module.gamification.controller.admin.stats.vo.GamificationStatsRespVO;

/**
 * 游戏化综合统计 Service
 */
public interface GamificationStatsService {

    /**
     * 获取游戏化综合统计数据
     *
     * @return 综合统计数据
     */
    GamificationStatsRespVO getOverviewStats();
}
