package cn.iocoder.yudao.module.gamification.dal.dataobject.invitation;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 邀请奖励记录 DO
 *
 * @author gamification
 */
@TableName("gamification_invitation_reward")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRewardDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请关系ID
     */
    private Long relationId;

    /**
     * 奖励接收者ID
     */
    private Long inviterId;

    private Long inviteeId;

    /**
     * 奖励类型：1-邀请人奖励，2-被邀请人奖励
     */
    private String rewardType;

    /**
     * 奖励积分数量
     */
    @TableField("reward_points")
    private Integer points;

    /**
     * 发放状态：1-待发放，2-发放中，3-发放成功，4-发放失败
     */
    private Integer status;

    /**
     * 重试次数
     */

    /**
     * 发放时间
     */
    private LocalDateTime grantTime;

    /**
     * 失败原因
     */

    /**
     * 奖励类型常量
     */
    public static final String REWARD_TYPE_INVITER = "INVITER";
    public static final String REWARD_TYPE_INVITEE = "INVITEE";
    private static final String LEGACY_REWARD_TYPE_INVITER = "1";
    private static final String LEGACY_REWARD_TYPE_INVITEE = "2";

    /**
     * 状态常量
     */
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_PROCESSING = 2;
    public static final Integer STATUS_SUCCESS = 3;
    public static final Integer STATUS_FAILED = 4;

    public boolean matchesRewardType(String expectedRewardType) {
        return normalizeRewardType(rewardType).equals(normalizeRewardType(expectedRewardType));
    }

    public boolean isInviterReward() {
        return matchesRewardType(REWARD_TYPE_INVITER);
    }

    public boolean isInviteeReward() {
        return matchesRewardType(REWARD_TYPE_INVITEE);
    }

    public Long resolveRewardUserId() {
        if (isInviterReward()) {
            return inviterId;
        }
        if (isInviteeReward()) {
            return inviteeId;
        }
        return null;
    }

    private static String normalizeRewardType(String rewardType) {
        if (rewardType == null) {
            return "";
        }
        String normalized = rewardType.trim().toUpperCase(Locale.ROOT);
        if (LEGACY_REWARD_TYPE_INVITER.equals(normalized)) {
            return REWARD_TYPE_INVITER;
        }
        if (LEGACY_REWARD_TYPE_INVITEE.equals(normalized)) {
            return REWARD_TYPE_INVITEE;
        }
        return normalized;
    }
}
