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

@TableName("gamification_quiz_question")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDO extends BaseDO {

    public static final String QUESTION_TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
    public static final String QUESTION_TYPE_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String QUESTION_TYPE_TRUE_FALSE = "TRUE_FALSE";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bankId;

    private String questionType;

    private String content;

    private String imageUrl;

    private Integer score;

    private String explanation;

    private Integer sort;
}
