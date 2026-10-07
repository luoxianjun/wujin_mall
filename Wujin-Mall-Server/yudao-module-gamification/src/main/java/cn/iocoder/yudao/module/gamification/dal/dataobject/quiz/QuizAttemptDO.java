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

@TableName("gamification_quiz_attempt")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDO extends BaseDO {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_TIMEOUT_SUBMITTED = "TIMEOUT_SUBMITTED";
    public static final String STATUS_INVALIDATED = "INVALIDATED";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizActivityId;

    private Long activityId;

    private Long userId;

    private Integer attemptNo;

    private String status;

    private Integer score;

    private Long elapsedMillis;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private String invalidatedReason;
}
