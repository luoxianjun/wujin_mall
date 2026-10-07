package cn.iocoder.yudao.module.forum.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.controller.app.message.vo.AppConversationPageReqVO;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumConversationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛会话 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumConversationMapper extends BaseMapperX<ForumConversationDO> {

    /**
     * 查询两个用户之间的会话
     */
    default ForumConversationDO selectByUserIds(Long user1Id, Long user2Id) {
        return selectOne(new LambdaQueryWrapperX<ForumConversationDO>()
                .and(wrapper -> wrapper
                        .eq(ForumConversationDO::getUser1Id, user1Id)
                        .eq(ForumConversationDO::getUser2Id, user2Id)
                        .or()
                        .eq(ForumConversationDO::getUser1Id, user2Id)
                        .eq(ForumConversationDO::getUser2Id, user1Id)
                ));
    }

    /**
     * 分页查询用户的会话列表
     */
    default PageResult<ForumConversationDO> selectPage(AppConversationPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumConversationDO>()
                .and(wrapper -> wrapper
                        .eq(ForumConversationDO::getUser1Id, userId)
                        .or()
                        .eq(ForumConversationDO::getUser2Id, userId)
                )
                .orderByDesc(ForumConversationDO::getLastMessageTime));
    }

    /**
     * 统计用户的未读消息总数
     */
    default Integer countUnreadByUserId(Long userId) {
        // 查询所有会话，计算未读消息总数
        return selectList(new LambdaQueryWrapperX<ForumConversationDO>()
                .and(wrapper -> wrapper
                        .eq(ForumConversationDO::getUser1Id, userId)
                        .or()
                        .eq(ForumConversationDO::getUser2Id, userId)
                ))
                .stream()
                .mapToInt(conversation -> {
                    if (conversation.getUser1Id().equals(userId)) {
                        return conversation.getUser1UnreadCount();
                    } else {
                        return conversation.getUser2UnreadCount();
                    }
                })
                .sum();
    }

}

