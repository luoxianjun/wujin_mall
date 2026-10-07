package cn.iocoder.yudao.module.forum.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛系统通知 DO
 * 
 * 用于存储系统通知消息
 *
 * @author forum
 */
@TableName("forum_system_notice")
@KeySequence("forum_system_notice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumSystemNoticeDO extends BaseDO {

    /**
     * 通知ID
     */
    @TableId
    private Long id;

    /**
     * 接收人ID
     */
    private Long userId;

    /**
     * 通知类型：1-点赞通知，2-评论通知，3-关注通知，4-系统通知，5-活动通知
     */
    private Integer noticeType;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联业务ID（如帖子ID、评论ID、活动ID等）
     */
    private Long relatedId;

    /**
     * 关联业务类型：1-帖子，2-评论，3-活动
     */
    private Integer relatedType;

    /**
     * 是否已读
     */
    private Boolean readStatus;

}

