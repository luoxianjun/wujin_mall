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

@TableName("gamification_quiz_activity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizActivityDO extends BaseDO {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    public static final String ANSWER_REVEAL_MODE_PER_QUESTION = "PER_QUESTION";
    public static final String ANSWER_REVEAL_MODE_AFTER_SUBMIT = "AFTER_SUBMIT";
    public static final String ANSWER_REVEAL_MODE_AFTER_ACTIVITY_END = "AFTER_ACTIVITY_END";
    public static final String ANSWER_REVEAL_MODE_HIDDEN = "HIDDEN";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long questionBankId;

    private Integer questionCount;

    private Integer maxAttempts;

    private Integer durationSeconds;

    private Boolean randomQuestionOrder;

    private Boolean randomOptionOrder;

    private Integer leaderboardSize;

    private String answerRevealMode;

    private String status;
}
