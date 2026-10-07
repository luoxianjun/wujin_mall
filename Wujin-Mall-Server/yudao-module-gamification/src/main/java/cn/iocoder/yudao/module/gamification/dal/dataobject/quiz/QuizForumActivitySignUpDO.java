package cn.iocoder.yudao.module.gamification.dal.dataobject.quiz;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName("forum_activity_sign_up")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuizForumActivitySignUpDO extends BaseDO {

    @TableId
    private Long id;

    private Long activityId;

    private Long userId;

    private String remark;

    private Integer approvalStatus;

    private String approvalRemark;

    private Boolean checkedIn;

    private LocalDateTime checkInTime;

}
