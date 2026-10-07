package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAnswerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuizAnswerMapper extends BaseMapperX<QuizAnswerDO> {

    default List<QuizAnswerDO> selectByAttemptId(Long attemptId) {
        return selectList(new LambdaQueryWrapperX<QuizAnswerDO>()
                .eq(QuizAnswerDO::getAttemptId, attemptId)
                .orderByAsc(QuizAnswerDO::getId));
    }
}
