package cn.iocoder.yudao.module.forum.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.*;

import javax.validation.Valid;

/**
 * 论坛消息 Service 接口
 *
 * @author forum
 */
public interface ForumMessageService {

    /**
     * 发送私信
     *
     * @param userId 发送人ID
     * @param reqVO 发送请求
     * @return 消息ID
     */
    Long sendMessage(Long userId, @Valid AppSendMessageReqVO reqVO);

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @param reqVO 分页请求
     * @return 会话列表
     */
    PageResult<AppConversationRespVO> getConversationPage(Long userId, AppConversationPageReqVO reqVO);

    /**
     * 获取会话消息列表
     *
     * @param userId 用户ID
     * @param reqVO 分页请求
     * @return 消息列表
     */
    PageResult<AppMessageRespVO> getMessagePage(Long userId, AppMessagePageReqVO reqVO);

    /**
     * 标记会话消息为已读
     *
     * @param userId 用户ID
     * @param conversationId 会话ID
     */
    void markConversationAsRead(Long userId, Long conversationId);

    /**
     * 获取未读消息统计
     *
     * @param userId 用户ID
     * @return 未读消息统计
     */
    AppUnreadCountRespVO getUnreadCount(Long userId);

    /**
     * 创建系统通知
     *
     * @param userId 接收人ID
     * @param noticeType 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param relatedId 关联业务ID
     * @param relatedType 关联业务类型
     */
    void createSystemNotice(Long userId, Integer noticeType, String title, String content, 
                           Long relatedId, Integer relatedType);

    /**
     * 获取系统通知列表
     *
     * @param userId 用户ID
     * @param reqVO 分页请求
     * @return 系统通知列表
     */
    PageResult<AppSystemNoticeRespVO> getSystemNoticePage(Long userId, AppSystemNoticePageReqVO reqVO);

    /**
     * 标记系统通知为已读
     *
     * @param userId 用户ID
     * @param noticeId 通知ID
     */
    void markNoticeAsRead(Long userId, Long noticeId);

    /**
     * 标记所有系统通知为已读
     *
     * @param userId 用户ID
     */
    void markAllNoticesAsRead(Long userId);

}

