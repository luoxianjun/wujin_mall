package cn.iocoder.yudao.module.forum.dal.dataobject.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 论坛帖子 DO
 *
 * 对应表 forum_post
 */
@TableName("forum_post")
@KeySequence("forum_post_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;

    /**
     * 发帖人，关联 member_user.id
     */
    private Long userId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子分类枚举值
     */
    private Integer category;

    /**
     * 帖子分类列表，JSON 数组
     */
    private String categories;

    /**
     * 图片 URL 列表，JSON 格式存储
     */
    private String imageUrls;

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
     * 是否仅本校可见
     */
    private Boolean schoolOnly;

    /**
     * 审核状态：0 待审核，1 已通过，2 已驳回
     */
    private Integer status;

    /**
     * 审核结论（审核不通过时的拒绝原因）
     */
    private String reviewResult;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 蹲后续人数
     */
    private Integer followCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 最后评论时间
     */
    private LocalDateTime latestCommentTime;

    /**
     * 发帖选择的学校
     */
    private String school;

}
