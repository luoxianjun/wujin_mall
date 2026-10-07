package cn.iocoder.yudao.module.forum.dal.dataobject.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 帖子投票选项 DO
 */
@TableName("forum_post_vote_option")
@KeySequence("forum_post_vote_option_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVoteOptionDO extends BaseDO {

    @TableId
    private Long id;

    /** 投票ID */
    private Long voteId;

    /** 选项内容 */
    private String title;

    /** 排序 */
    private Integer sortOrder;
}
