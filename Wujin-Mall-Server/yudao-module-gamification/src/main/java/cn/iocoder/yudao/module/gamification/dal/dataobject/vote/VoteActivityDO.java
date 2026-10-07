package cn.iocoder.yudao.module.gamification.dal.dataobject.vote;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("gamification_vote_activity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteActivityDO extends BaseDO {

    /** 投票类型：单选 */
    public static final Integer TYPE_SINGLE = 0;
    /** 投票类型：多选 */
    public static final Integer TYPE_MULTIPLE = 1;
    /** 投票类型：排序 */
    public static final Integer TYPE_RANKING = 2;

    /** 状态：启用 */
    public static final Integer STATUS_ENABLED = 0;
    /** 状态：禁用 */
    public static final Integer STATUS_DISABLED = 1;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的论坛活动ID */
    private Long activityId;

    /** 投票类型：0=单选, 1=多选, 2=排序 */
    private Integer voteType;

    /** 最多可选数量（多选时使用） */
    private Integer maxChoices;

    /** 是否匿名投票 */
    private Boolean anonymous;

    /** 是否显示实时结果 */
    private Boolean showRealtimeResult;

    /** 是否允许用户添加选项 */
    private Boolean allowUserAddOption;

    /** 是否要求实名用户参与 */
    private Boolean requireRealName;

    /** 选项数量上限 */
    private Integer maxOptions;

    /** 投票截止时间 */
    private LocalDateTime endTime;

    /** 最低参与人数 */
    private Integer minParticipants;

    /** 是否允许评论 */
    private Boolean allowComment;

    /** 每人票数 */
    private Integer votesPerUser;

    /** 状态：0=启用, 1=禁用 */
    private Integer status;
}
