package cn.iocoder.yudao.module.forum.dal.dataobject.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛评论 DO
 */
@TableName("forum_comment")
@KeySequence("forum_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumCommentDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 关联帖子 ID
     */
    private Long postId;

    /**
     * 评论人，关联 member_user.id
     */
    private Long userId;

    /**
     * 父评论 ID
     */
    private Long parentId;

    /**
     * 根评论 ID
     */
    private Long rootId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 是否匿名
     */
    private Boolean anonymous;

    /**
     * 匿名昵称（匿名时使用）
     */
    private String anonymousNickname;

    /**
     * 匿名头像（匿名时使用）
     */
    private String anonymousAvatar;

    /**
     * 状态：0 正常，1 已删除，2 待审核
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Integer likeCount;

}

