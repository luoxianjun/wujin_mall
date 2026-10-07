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

@TableName("gamification_vote_option")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteOptionDO extends BaseDO {

    /** 审核状态：待审核 */
    public static final Integer AUDIT_PENDING = 0;
    /** 审核状态：通过 */
    public static final Integer AUDIT_APPROVED = 1;
    /** 审核状态：拒绝 */
    public static final Integer AUDIT_REJECTED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 投票活动ID */
    private Long voteActivityId;

    /** 选项标题 */
    private String title;

    /** 选项配图URL */
    private String imageUrl;

    /** 选项描述 */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    /** 是否用户添加 */
    private Boolean addedByUser;

    /** 添加用户ID（用户添加时） */
    private Long userId;

    /** 审核状态：0=待审核, 1=通过, 2=拒绝 */
    private Integer auditStatus;
}
