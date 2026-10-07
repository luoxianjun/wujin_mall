package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuizRewardRuleMapper extends BaseMapperX<QuizRewardRuleDO> {

    default List<QuizRewardRuleDO> selectByQuizActivityId(Long quizActivityId) {
        return selectList(new LambdaQueryWrapperX<QuizRewardRuleDO>()
                .eq(QuizRewardRuleDO::getQuizActivityId, quizActivityId)
                .orderByAsc(QuizRewardRuleDO::getRankStart, QuizRewardRuleDO::getId));
    }
}
