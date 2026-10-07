package cn.iocoder.yudao.module.forum.service.message;

import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppInteractionUnreadRespVO;

/**
 * 论坛互动未读统计 Service
 *
 * 负责维护点赞、评论、帖子回复等未读计数。
 */
public interface ForumInteractionUnreadService {

    /**
     * 增加未读点赞数
     *
     * @param userId 用户 ID
     * @param delta 增量（必须为正）
     */
    void incrementLikeUnread(Long userId, long delta);

    /**
     * 增加未读评论数
     *
     * @param userId 用户 ID
     * @param delta 增量（必须为正）
     */
    void incrementCommentUnread(Long userId, long delta);

    /**
     * 增加关注/点赞/评论过帖子后的新回复未读数
     *
     * @param userId 用户 ID
     * @param delta 增量（必须为正）
     */
    void incrementPostReplyUnread(Long userId, long delta);

    /**
     * 清除未读点赞数
     *
     * @param userId 用户 ID
     */
    void clearLikeUnread(Long userId);

    /**
     * 清除未读评论数
     *
     * @param userId 用户 ID
     */
    void clearCommentUnread(Long userId);

    /**
     * 清除帖子新回复未读数
     *
     * @param userId 用户 ID
     */
    void clearPostReplyUnread(Long userId);

    /**
     * 查询互动未读统计
     *
     * @param userId 用户 ID
     * @return 未读统计
     */
    AppInteractionUnreadRespVO getUnreadCount(Long userId);

}
