package cn.iocoder.yudao.module.gamification.dal.dataobject.invitation;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 邀请码 DO
 *
 * @author gamification
 */
@TableName("gamification_invitation_code")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCodeDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邀请码（8位字符）
     */
    private String code;

    /**
     * 状态：1-有效，0-失效
     */
    private Integer status;

    /**
     * 累计邀请人数
     */
    private Integer totalInvitations;

    /**
     * 有效邀请人数（完成任务的）
     */
    private Integer validInvitations;

    /**
     * 状态常量
     */
    public static final Integer STATUS_VALID = 1;
    public static final Integer STATUS_INVALID = 0;
}
