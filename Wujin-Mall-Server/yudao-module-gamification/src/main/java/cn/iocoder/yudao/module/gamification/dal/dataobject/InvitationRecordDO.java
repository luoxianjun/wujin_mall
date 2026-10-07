package cn.iocoder.yudao.module.gamification.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 邀请记录 DO
 */
@TableName("gamification_invitation_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRecordDO extends BaseDO {

    /**
     * 邀请记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 邀请状态：0-待确认，1-已成功，2-已失效
     */
    private Integer status;

    /**
     * 邀请人获得积分
     */
    private Integer inviterRewardPoints;

    /**
     * 被邀请人获得积分
     */
    private Integer inviteeRewardPoints;

    /**
     * 邀请时间
     */
    private LocalDateTime inviteTime;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

}
