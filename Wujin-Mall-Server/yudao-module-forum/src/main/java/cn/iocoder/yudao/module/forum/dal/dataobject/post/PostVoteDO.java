package cn.iocoder.yudao.module.forum.dal.dataobject.post;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 帖子内嵌投票 DO
 */
@TableName("forum_post_vote")
@KeySequence("forum_post_vote_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVoteDO extends BaseDO {

    @TableId
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 投票类型：0=单选, 1=多选 */
    private Integer voteType;

    /** 最多选择数(多选时使用) */
    private Integer maxChoices;

    /** 截止时间 */
    private LocalDateTime endTime;

    /** 是否匿名投票 */
    private Boolean anonymous;

    /** 是否显示实时投票结果 */
    private Boolean showRealtimeResult;
}
