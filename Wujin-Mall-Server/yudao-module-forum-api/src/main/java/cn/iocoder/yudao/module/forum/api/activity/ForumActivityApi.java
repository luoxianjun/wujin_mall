package cn.iocoder.yudao.module.forum.api.activity;

import cn.iocoder.yudao.module.forum.api.activity.dto.ForumActivityParticipationConfigDTO;

import java.util.List;

/**
 * 论坛活动 API 接口
 *
 * 用于跨模块调用，例如 gamification 模块查询活动报名用户
 */
public interface ForumActivityApi {

    /**
     * 获取活动已通过审核的报名用户ID列表
     */
    List<Long> getApprovedUserIds(Long activityId);

    /**
     * 获取活动报名参与配置
     */
    ForumActivityParticipationConfigDTO getParticipationConfig(Long activityId);

    /**
     * 判断用户是否已报名活动（审核状态不限）
     */
    boolean isUserParticipated(Long activityId, Long userId);

    /**
     * 判断用户是否已通过审核并进入活动参与池
     */
    boolean isUserApprovedParticipated(Long activityId, Long userId);

    /**
     * 获取活动当前占用名额人数（待审核 + 已通过）
     */
    long getParticipantCount(Long activityId);

    /**
     * 获取活动已审核通过的人数
     */
    long getApprovedParticipantCount(Long activityId);
}
