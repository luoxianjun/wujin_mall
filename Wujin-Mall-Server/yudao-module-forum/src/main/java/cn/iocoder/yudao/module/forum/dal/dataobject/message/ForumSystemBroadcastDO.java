package cn.iocoder.yudao.module.forum.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 系统广播消息 DO
 * 
 * 用于存储管理员发送的广播消息历史
 *
 * @author forum
 */
@TableName("forum_system_broadcast")
@KeySequence("forum_system_broadcast_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumSystemBroadcastDO extends BaseDO {

    /**
     * 消息ID
     */
    @TableId
    private Long id;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容（富文本HTML）
     */
    private String content;

    /**
     * 发送者用户ID（管理员）
     */
    private Long senderId;

    /**
     * 发送成功数量
     */
    private Integer successCount;

    /**
     * 发送失败数量
     */
    private Integer failCount;

    /**
     * 发送状态：0-发送中，1-发送完成，2-发送失败
     */
    private Integer status;

}
