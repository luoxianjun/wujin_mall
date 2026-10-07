package cn.iocoder.yudao.module.forum.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 论坛会话 DO
 * 
 * 用于管理用户之间的私信会话
 *
 * @author forum
 */
@TableName("forum_conversation")
@KeySequence("forum_conversation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumConversationDO extends BaseDO {

    /**
     * 会话ID
     */
    @TableId
    private Long id;

    /**
     * 用户1 ID
     */
    private Long user1Id;

    /**
     * 用户2 ID
     */
    private Long user2Id;

    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 最后一条消息发送人ID
     */
    private Long lastMessageSenderId;

    /**
     * 用户1未读消息数
     */
    private Integer user1UnreadCount;

    /**
     * 用户2未读消息数
     */
    private Integer user2UnreadCount;

}

