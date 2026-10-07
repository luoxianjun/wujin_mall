package cn.iocoder.yudao.module.forum.dal.dataobject.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛帖子蹲后续 DO
 *
 * @author forum
 */
@TableName("forum_post_follow")
@KeySequence("forum_post_follow_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostFollowDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 帖子 ID
     */
    private Long postId;

    /**
     * 关注用户 ID
     */
    private Long userId;

}

