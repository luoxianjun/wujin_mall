package cn.iocoder.yudao.module.forum.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛消息 DO
 * 
 * 用于存储用户之间的私信消息
 *
 * @author forum
 */
@TableName("forum_message")
@KeySequence("forum_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumMessageDO extends BaseDO {

    /**
     * 消息ID
     */
    @TableId
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 消息类型：1-文本，2-图片，3-语音，4-视频
     */
    private Integer messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean readStatus;

}

