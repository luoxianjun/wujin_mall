package cn.iocoder.yudao.module.gamification.dal.dataobject.quiz;

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

@TableName("gamification_quiz_reward_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRewardRecordDO extends BaseDO {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_PENDING_FULFILLMENT = "PENDING_FULFILLMENT";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizActivityId;

    private Long attemptId;

    private Long userId;

    private String rewardType;

    private Integer pointAmount;

    private String rewardName;

    private String status;

    private Integer retryCount;

    private LocalDateTime distributedAt;
}
