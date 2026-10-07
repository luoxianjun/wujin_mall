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

@TableName("gamification_quiz_reward_rule")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRewardRuleDO extends BaseDO {

    public static final String REWARD_TYPE_POINTS = "POINTS";
    public static final String REWARD_TYPE_PHYSICAL = "PHYSICAL";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizActivityId;

    private Integer rankStart;

    private Integer rankEnd;

    private String rewardType;

    private Integer pointAmount;

    private String rewardName;
}
