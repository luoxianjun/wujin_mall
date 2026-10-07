package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuizQuestionMapper extends BaseMapperX<QuizQuestionDO> {

    default List<QuizQuestionDO> selectByBankId(Long bankId) {
        return selectList(new LambdaQueryWrapperX<QuizQuestionDO>()
                .eq(QuizQuestionDO::getBankId, bankId)
                .orderByAsc(QuizQuestionDO::getSort, QuizQuestionDO::getId));
    }
}
