package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizQuestionBankPageReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizQuestionBankDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizQuestionBankMapper extends BaseMapperX<QuizQuestionBankDO> {

    default QuizQuestionBankDO selectByName(String name) {
        return selectOne(QuizQuestionBankDO::getName, name);
    }

    default PageResult<QuizQuestionBankDO> selectPage(QuizQuestionBankPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuizQuestionBankDO>()
                .likeIfPresent(QuizQuestionBankDO::getName, reqVO.getName())
                .eqIfPresent(QuizQuestionBankDO::getEnabled, reqVO.getEnabled())
                .orderByDesc(QuizQuestionBankDO::getId));
    }

    default PageResult<QuizQuestionBankDO> selectEnabledPage(QuizQuestionBankPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuizQuestionBankDO>()
                .likeIfPresent(QuizQuestionBankDO::getName, reqVO.getName())
                .eq(QuizQuestionBankDO::getEnabled, Boolean.TRUE)
                .orderByDesc(QuizQuestionBankDO::getId));
    }
}
