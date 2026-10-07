package cn.iocoder.yudao.module.forum.service.statistics;

import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsRespVO;
import cn.iocoder.yudao.module.forum.controller.admin.statistics.vo.AdminForumStatisticsTrendRespVO;

/**
 * 论坛统计 Service
 */
public interface ForumStatisticsService {

    /**
     * 获取管理后台统计概览
     */
    AdminForumStatisticsRespVO getStatisticsSummary();

    /**
     * 获取近 30 天的会员、帖子增量
     */
    AdminForumStatisticsTrendRespVO getLast30DaysTrend();

}
