package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizActivityPageReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuizActivityMapper extends BaseMapperX<QuizActivityDO> {

    default QuizActivityDO selectByActivityId(Long activityId) {
        return selectOne(QuizActivityDO::getActivityId, activityId);
    }

    default PageResult<QuizActivityDO> selectPage(QuizActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuizActivityDO>()
                .eqIfPresent(QuizActivityDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(QuizActivityDO::getQuestionBankId, reqVO.getQuestionBankId())
                .eqIfPresent(QuizActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(QuizActivityDO::getId));
    }
}
