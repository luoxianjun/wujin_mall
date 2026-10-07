package cn.iocoder.yudao.module.forum.service.message;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.*;
import cn.iocoder.yudao.module.forum.convert.message.ForumMessageConvert;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumConversationDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumMessageDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemNoticeDO;
import cn.iocoder.yudao.module.forum.dal.dataobject.user.ForumUserProfileDO;
import cn.iocoder.yudao.module.forum.dal.mysql.message.ForumConversationMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.message.ForumMessageMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.message.ForumSystemNoticeMapper;
import cn.iocoder.yudao.module.forum.dal.mysql.user.ForumUserProfileMapper;
import cn.iocoder.yudao.module.forum.enums.message.MessageTypeEnum;
import cn.iocoder.yudao.module.forum.enums.message.NoticeTypeEnum;
import cn.iocoder.yudao.module.forum.service.message.ForumInteractionUnreadService;
import cn.iocoder.yudao.module.forum.service.text.ForumTextAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.system.api.social.SocialUserApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserRespDTO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.forum.enums.ErrorCodeConstants.*;

/**
 * 论坛消息 Service 实现类
 *
 * @author forum
 */
@Service
@Validated
@Slf4j
public class ForumMessageServiceImpl implements ForumMessageService {

    @Resource
    private ForumConversationMapper conversationMapper;

    @Resource
    private ForumMessageMapper messageMapper;

    @Resource
    private ForumSystemNoticeMapper systemNoticeMapper;

    @Resource
    private ForumUserProfileMapper userProfileMapper;

    @Resource
    private ForumInteractionUnreadService interactionUnreadService;

    @Resource
    private ForumTextAuditService textAuditService;

    @Resource
    private SocialUserApi socialUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(Long userId, AppSendMessageReqVO reqVO) {
        // 0. 文本审核：私聊内容（直接抛异常阻止发送）
        String openid = getUserWxOpenid(userId);
        textAuditService.audit(reqVO.getContent(), openid, "private-message");

        // 1. 校验接收人是否存在
        ForumUserProfileDO receiver = userProfileMapper.selectByUserId(reqVO.getReceiverId());
        if (receiver == null) {
            throw ServiceExceptionUtil.exception(USER_PROFILE_NOT_EXISTS);
        }

        // 2. 查询或创建会话
        ForumConversationDO conversation = conversationMapper.selectByUserIds(userId, reqVO.getReceiverId());
        if (conversation == null) {
            // 创建新会话
            conversation = ForumConversationDO.builder()
                    .user1Id(userId)
                    .user2Id(reqVO.getReceiverId())
                    .lastMessageContent(reqVO.getContent())
                    .lastMessageTime(LocalDateTime.now())
                    .lastMessageSenderId(userId)
                    .user1UnreadCount(0)
                    .user2UnreadCount(1)
                    .build();
            conversationMapper.insert(conversation);
        } else {
            // 3. 检查3条消息限制
            Long unrepliedCount = messageMapper.countUnrepliedMessagesBySender(
                    conversation.getId(), userId, reqVO.getReceiverId());
            if (unrepliedCount >= 3) {
                throw ServiceExceptionUtil.exception(MESSAGE_LIMIT_EXCEEDED);
            }

            // 4. 更新会话信息
            ForumConversationDO updateObj = ForumConversationDO.builder()
                    .id(conversation.getId())
                    .lastMessageContent(reqVO.getContent())
                    .lastMessageTime(LocalDateTime.now())
                    .lastMessageSenderId(userId)
                    .build();

            // 增加接收人的未读消息数
            if (conversation.getUser1Id().equals(reqVO.getReceiverId())) {
                updateObj.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
            } else {
                updateObj.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
            }

            conversationMapper.updateById(updateObj);
        }

        // 5. 创建消息
        ForumMessageDO message = ForumMessageDO.builder()
                .conversationId(conversation.getId())
                .senderId(userId)
                .receiverId(reqVO.getReceiverId())
                .messageType(reqVO.getMessageType())
                .content(reqVO.getContent())
                .readStatus(false)
                .build();
        messageMapper.insert(message);

        log.info("[sendMessage][发送私信成功，messageId={}, senderId={}, receiverId={}]",
                message.getId(), userId, reqVO.getReceiverId());

        return message.getId();
    }

    @Override
    public PageResult<AppConversationRespVO> getConversationPage(Long userId, AppConversationPageReqVO reqVO) {
        // 1. 分页查询会话
        PageResult<ForumConversationDO> pageResult = conversationMapper.selectPage(reqVO, userId);

        // 2. 转换为 VO
        List<AppConversationRespVO> list = pageResult.getList().stream()
                .map(conversation -> buildConversationRespVO(conversation, userId))
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public PageResult<AppMessageRespVO> getMessagePage(Long userId, AppMessagePageReqVO reqVO) {
        // 1. 校验会话是否存在
        ForumConversationDO conversation = conversationMapper.selectById(reqVO.getConversationId());
        if (conversation == null) {
            throw ServiceExceptionUtil.exception(CONVERSATION_NOT_EXISTS);
        }

        // 2. 校验用户是否是会话参与者
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw ServiceExceptionUtil.exception(CONVERSATION_NOT_EXISTS);
        }

        // 3. 分页查询消息
        PageResult<ForumMessageDO> pageResult = messageMapper.selectPage(reqVO);

        // 4. 转换为 VO
        List<AppMessageRespVO> list = pageResult.getList().stream()
                .map(this::buildMessageRespVO)
                .collect(Collectors.toList());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationAsRead(Long userId, Long conversationId) {
        // 1. 查询会话
        ForumConversationDO conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw ServiceExceptionUtil.exception(CONVERSATION_NOT_EXISTS);
        }

        // 2. 校验用户是否是会话参与者
        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw ServiceExceptionUtil.exception(CONVERSATION_NOT_EXISTS);
        }

        // 3. 标记该会话中接收的所有消息为已读
        List<ForumMessageDO> unreadMessages = messageMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ForumMessageDO>()
                        .eq(ForumMessageDO::getConversationId, conversationId)
                        .eq(ForumMessageDO::getReceiverId, userId)
                        .eq(ForumMessageDO::getReadStatus, false));

        for (ForumMessageDO message : unreadMessages) {
            ForumMessageDO updateObj = ForumMessageDO.builder()
                    .id(message.getId())
                    .readStatus(true)
                    .build();
            messageMapper.updateById(updateObj);
        }

        // 4. 清空会话的未读消息数
        ForumConversationDO updateObj = ForumConversationDO.builder()
                .id(conversationId)
                .build();

        if (conversation.getUser1Id().equals(userId)) {
            updateObj.setUser1UnreadCount(0);
        } else {
            updateObj.setUser2UnreadCount(0);
        }

        conversationMapper.updateById(updateObj);

        log.info("[markConversationAsRead][标记会话消息为已读，conversationId={}, userId={}]", conversationId, userId);
    }

    @Override
    public AppUnreadCountRespVO getUnreadCount(Long userId) {
        // 1. 统计未读私信数
        Integer unreadMessageCount = conversationMapper.countUnreadByUserId(userId);

        // 2. 统计未读系统通知数
        Long unreadNoticeCount = systemNoticeMapper.countUnreadByUserId(userId);

        // 3. 返回统计结果
        return AppUnreadCountRespVO.builder()
                .unreadMessageCount(unreadMessageCount)
                .unreadNoticeCount(unreadNoticeCount.intValue())
                .totalUnreadCount(unreadMessageCount + unreadNoticeCount.intValue())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSystemNotice(Long userId, Integer noticeType, String title, String content,
            Long relatedId, Integer relatedType) {
        ForumSystemNoticeDO notice = ForumSystemNoticeDO.builder()
                .userId(userId)
                .noticeType(noticeType)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .readStatus(false)
                .build();
        systemNoticeMapper.insert(notice);

        log.info("[createSystemNotice][创建系统通知成功，noticeId={}, userId={}, noticeType={}]",
                notice.getId(), userId, noticeType);
    }

    @Override
    public PageResult<AppSystemNoticeRespVO> getSystemNoticePage(Long userId, AppSystemNoticePageReqVO reqVO) {
        // 1. 分页查询系统通知
        PageResult<ForumSystemNoticeDO> pageResult = systemNoticeMapper.selectPage(reqVO, userId);

        // 2. 转换为 VO
        List<AppSystemNoticeRespVO> list = pageResult.getList().stream()
                .map(this::buildSystemNoticeRespVO)
                .collect(Collectors.toList());

        // 3. 用户查询对应列表后，清除互动未读计数
        clearInteractionUnreadIfNeeded(userId, reqVO.getNoticeType());

        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNoticeAsRead(Long userId, Long noticeId) {
        // 1. 查询通知
        ForumSystemNoticeDO notice = systemNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }

        // 2. 校验是否是本人的通知
        if (!notice.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }

        // 3. 标记为已读
        if (!notice.getReadStatus()) {
            ForumSystemNoticeDO updateObj = ForumSystemNoticeDO.builder()
                    .id(noticeId)
                    .readStatus(true)
                    .build();
            systemNoticeMapper.updateById(updateObj);
        }

        log.info("[markNoticeAsRead][标记通知为已读，noticeId={}, userId={}]", noticeId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllNoticesAsRead(Long userId) {
        // 查询所有未读通知
        List<ForumSystemNoticeDO> unreadNotices = systemNoticeMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ForumSystemNoticeDO>()
                        .eq(ForumSystemNoticeDO::getUserId, userId)
                        .eq(ForumSystemNoticeDO::getReadStatus, false));

        // 标记为已读
        for (ForumSystemNoticeDO notice : unreadNotices) {
            ForumSystemNoticeDO updateObj = ForumSystemNoticeDO.builder()
                    .id(notice.getId())
                    .readStatus(true)
                    .build();
            systemNoticeMapper.updateById(updateObj);
        }

        log.info("[markAllNoticesAsRead][标记所有通知为已读，userId={}, count={}]", userId, unreadNotices.size());
    }

    private void clearInteractionUnreadIfNeeded(Long userId, Integer noticeType) {
        if (noticeType == null) {
            return;
        }
        NoticeTypeEnum typeEnum = NoticeTypeEnum.getByType(noticeType);
        if (typeEnum == null) {
            return;
        }
        switch (typeEnum) {
            case LIKE:
                interactionUnreadService.clearLikeUnread(userId);
                break;
            case COMMENT:
                interactionUnreadService.clearCommentUnread(userId);
                break;
            case FOLLOW:
                interactionUnreadService.clearPostReplyUnread(userId);
                break;
            default:
                break;
        }
    }

    /**
     * 构建会话响应 VO
     */
    private AppConversationRespVO buildConversationRespVO(ForumConversationDO conversation, Long userId) {
        AppConversationRespVO respVO = new AppConversationRespVO();
        respVO.setId(conversation.getId());
        respVO.setLastMessageContent(conversation.getLastMessageContent());
        respVO.setLastMessageTime(conversation.getLastMessageTime());

        // 确定对方用户ID
        Long otherUserId = conversation.getUser1Id().equals(userId) ? conversation.getUser2Id()
                : conversation.getUser1Id();
        respVO.setOtherUserId(otherUserId);

        // 查询对方用户信息
        ForumUserProfileDO otherUser = userProfileMapper.selectByUserId(otherUserId);
        if (otherUser != null) {
            respVO.setOtherUserUid(otherUser.getUid());
            respVO.setOtherUserNickname(otherUser.getNickname());
            respVO.setOtherUserAvatar(otherUser.getAvatar());
        }

        // 设置未读消息数
        if (conversation.getUser1Id().equals(userId)) {
            respVO.setUnreadCount(conversation.getUser1UnreadCount());
        } else {
            respVO.setUnreadCount(conversation.getUser2UnreadCount());
        }

        return respVO;
    }

    /**
     * 构建消息响应 VO
     */
    private AppMessageRespVO buildMessageRespVO(ForumMessageDO message) {
        AppMessageRespVO respVO = ForumMessageConvert.INSTANCE.convert(message);

        // 设置消息类型名称
        MessageTypeEnum messageType = MessageTypeEnum.getByType(message.getMessageType());
        if (messageType != null) {
            respVO.setMessageTypeName(messageType.getName());
        }

        // 查询发送人信息
        ForumUserProfileDO sender = userProfileMapper.selectByUserId(message.getSenderId());
        if (sender != null) {
            respVO.setSenderUid(sender.getUid());
            respVO.setSenderNickname(sender.getNickname());
            respVO.setSenderAvatar(sender.getAvatar());
        }

        return respVO;
    }

    /**
     * 构建系统通知响应 VO
     */
    private AppSystemNoticeRespVO buildSystemNoticeRespVO(ForumSystemNoticeDO notice) {
        AppSystemNoticeRespVO respVO = ForumMessageConvert.INSTANCE.convertNotice(notice);

        // 设置通知类型名称
        NoticeTypeEnum noticeType = NoticeTypeEnum.getByType(notice.getNoticeType());
        if (noticeType != null) {
            respVO.setNoticeTypeName(noticeType.getName());
        }

        return respVO;
    }

    /**
     * 获取用户的微信小程序 openid
     */
    private String getUserWxOpenid(Long userId) {
        try {
            SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(
                    UserTypeEnum.MEMBER.getValue(), userId, SocialTypeEnum.WECHAT_MINI_PROGRAM.getType());
            return socialUser != null ? socialUser.getOpenid() : null;
        } catch (Exception e) {
            log.error("[getUserWxOpenid][获取用户微信openid失败，userId={}]", userId, e);
            return null;
        }
    }

}
