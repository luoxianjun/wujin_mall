package cn.iocoder.yudao.module.forum.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppMessagePageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumMessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛消息 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumMessageMapper extends BaseMapperX<ForumMessageDO> {

    /**
     * 分页查询会话的消息列表
     */
    default PageResult<ForumMessageDO> selectPage(AppMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumMessageDO>()
                .eq(ForumMessageDO::getConversationId, reqVO.getConversationId())
                .orderByDesc(ForumMessageDO::getCreateTime));
    }

    /**
     * 统计会话中未读消息数（接收人维度）
     */
    default Long countUnreadByConversationIdAndReceiverId(Long conversationId, Long receiverId) {
        return selectCount(new LambdaQueryWrapperX<ForumMessageDO>()
                .eq(ForumMessageDO::getConversationId, conversationId)
                .eq(ForumMessageDO::getReceiverId, receiverId)
                .eq(ForumMessageDO::getReadStatus, false));
    }

    /**
     * 统计发送人在会话中连续未回复的消息数
     * 用于实现"未回复私信限制，最多发送3条消息"的功能
     */
    default Long countUnrepliedMessagesBySender(Long conversationId, Long senderId, Long receiverId) {
        // 查询最后一条接收人发送的消息
        ForumMessageDO lastReceiverMessage = selectOne(new LambdaQueryWrapperX<ForumMessageDO>()
                .eq(ForumMessageDO::getConversationId, conversationId)
                .eq(ForumMessageDO::getSenderId, receiverId)
                .orderByDesc(ForumMessageDO::getCreateTime)
                .last("LIMIT 1"));

        // 如果接收人从未发送过消息，统计发送人的所有消息
        if (lastReceiverMessage == null) {
            return selectCount(new LambdaQueryWrapperX<ForumMessageDO>()
                    .eq(ForumMessageDO::getConversationId, conversationId)
                    .eq(ForumMessageDO::getSenderId, senderId));
        }

        // 统计在接收人最后一条消息之后，发送人发送的消息数
        return selectCount(new LambdaQueryWrapperX<ForumMessageDO>()
                .eq(ForumMessageDO::getConversationId, conversationId)
                .eq(ForumMessageDO::getSenderId, senderId)
                .gt(ForumMessageDO::getCreateTime, lastReceiverMessage.getCreateTime()));
    }

}

