package cn.iocoder.yudao.module.forum.dal.dataobject.activity;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 论坛活动报名 DO
 *
 * @author forum
 */
@TableName("forum_activity_sign_up")
@KeySequence("forum_activity_sign_up_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumActivitySignUpDO extends BaseDO {

    /**
     * 报名ID
     */
    @TableId
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 报名用户ID
     */
    private Long userId;

    /**
     * 报名备注
     */
    private String remark;

    /**
     * 点评/回顾/反馈
     */
    private String feedback;

    /**
     * 审核状态：0-待审核，1-已通过，2-已拒绝，3-已取消
     */
    private Integer approvalStatus;

    /**
     * 审核备注
     */
    private String approvalRemark;

    /**
     * 是否已签到
     */
    private Boolean checkedIn;

    /**
     * 签到时间
     */
    private java.time.LocalDateTime checkInTime;

    /**
     * 签到经度
     */
    private Double checkInLongitude;

    /**
     * 签到纬度
     */
    private Double checkInLatitude;

}
