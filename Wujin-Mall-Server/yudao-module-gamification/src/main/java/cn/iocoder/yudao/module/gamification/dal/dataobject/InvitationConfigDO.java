package cn.iocoder.yudao.module.gamification.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.ibatis.type.Alias;

/**
 * 邀请配置 DO
 */
@Deprecated
@Alias("LegacyInvitationConfigDO")
@TableName("gamification_invitation_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请人奖励积分
     */
    private Integer inviterRewardPoints;

    /**
     * 被邀请人奖励积分
     */
    private Integer inviteeRewardPoints;

    /**
     * 每日邀请上限
     */
    private Integer dailyInviteLimit;

    /**
     * 邀请码有效期（天）
     */
    private Integer inviteCodeValidDays;

    /**
     * 是否启用邀请功能
     */
    private Boolean enabled;

}
