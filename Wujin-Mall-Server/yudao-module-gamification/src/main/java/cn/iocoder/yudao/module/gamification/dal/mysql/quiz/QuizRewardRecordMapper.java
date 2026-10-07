package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizRewardRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuizRewardRecordMapper extends BaseMapperX<QuizRewardRecordDO> {

    default QuizRewardRecordDO selectByQuizActivityIdAndUserIdAndRewardName(Long quizActivityId, Long userId, String rewardName) {
        return selectOne(new LambdaQueryWrapperX<QuizRewardRecordDO>()
                .eq(QuizRewardRecordDO::getQuizActivityId, quizActivityId)
                .eq(QuizRewardRecordDO::getUserId, userId)
                .eq(QuizRewardRecordDO::getRewardName, rewardName));
    }

    default List<QuizRewardRecordDO> selectByQuizActivityId(Long quizActivityId) {
        return selectList(QuizRewardRecordDO::getQuizActivityId, quizActivityId);
    }

    default List<QuizRewardRecordDO> selectByQuizActivityIdAndUserId(Long quizActivityId, Long userId) {
        return selectList(new LambdaQueryWrapperX<QuizRewardRecordDO>()
                .eq(QuizRewardRecordDO::getQuizActivityId, quizActivityId)
                .eq(QuizRewardRecordDO::getUserId, userId)
                .orderByDesc(QuizRewardRecordDO::getDistributedAt, QuizRewardRecordDO::getId));
    }
}
