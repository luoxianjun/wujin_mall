package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface QuizQuestionOptionMapper extends BaseMapperX<QuizQuestionOptionDO> {

    default List<QuizQuestionOptionDO> selectByQuestionIds(Collection<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<QuizQuestionOptionDO>()
                .in(QuizQuestionOptionDO::getQuestionId, questionIds)
                .orderByAsc(QuizQuestionOptionDO::getSort, QuizQuestionOptionDO::getId));
    }
}
