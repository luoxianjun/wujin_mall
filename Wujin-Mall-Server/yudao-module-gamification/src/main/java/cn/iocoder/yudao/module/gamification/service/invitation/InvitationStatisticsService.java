package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationStatisticsRespVO;

/**
 * 邀请统计 Service 接口
 */
public interface InvitationStatisticsService {

    /**
     * 获取邀请统计数据
     *
     * @return 统计数据
     */
    InvitationStatisticsRespVO getStatistics();

}
