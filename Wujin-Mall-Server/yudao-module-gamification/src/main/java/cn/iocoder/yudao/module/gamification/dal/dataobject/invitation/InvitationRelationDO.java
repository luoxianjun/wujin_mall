package cn.iocoder.yudao.module.gamification.dal.dataobject.invitation;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 邀请关系 DO
 *
 * @author gamification
 */
@TableName("gamification_invitation_relation")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRelationDO extends BaseDO {

    /**
     * 主键ID
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
     * 使用的邀请码
     */
    private String invitationCode;

    /**
     * 状态：1-待完成，2-已完成，3-已失效
     */
    private Integer status;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 完成时间（完成新手任务）
     */
    private LocalDateTime completeTime;

    /**
     * 设备指纹
     */
    private String deviceFingerprint;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 状态常量
     */
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_COMPLETED = 2;
    public static final Integer STATUS_INVALID = 3;
}
