package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivitySignUpDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface QuizForumActivitySignUpMapper extends BaseMapperX<QuizForumActivitySignUpDO> {

    default List<QuizForumActivitySignUpDO> selectByActivityIdsAndUserIds(Collection<Long> activityIds,
                                                                          Collection<Long> userIds) {
        if (activityIds == null || activityIds.isEmpty() || userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<QuizForumActivitySignUpDO>()
                .in(QuizForumActivitySignUpDO::getActivityId, activityIds)
                .in(QuizForumActivitySignUpDO::getUserId, userIds)
                .orderByDesc(QuizForumActivitySignUpDO::getCreateTime));
    }

}
