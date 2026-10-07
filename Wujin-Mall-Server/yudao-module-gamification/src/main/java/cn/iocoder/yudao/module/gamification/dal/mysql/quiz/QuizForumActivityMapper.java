package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizForumActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QuizForumActivityMapper extends BaseMapperX<QuizForumActivityDO> {

    default List<QuizForumActivityDO> selectEndedBetween(LocalDateTime begin, LocalDateTime end) {
        return selectList(new LambdaQueryWrapperX<QuizForumActivityDO>()
                .between(QuizForumActivityDO::getEndTime, begin, end));
    }

    /**
     * 查询即将在指定时间窗口内开始的已启用活动
     */
    default List<QuizForumActivityDO> selectStartingBetween(LocalDateTime begin, LocalDateTime end) {
        return selectList(new LambdaQueryWrapperX<QuizForumActivityDO>()
                .between(QuizForumActivityDO::getStartTime, begin, end)
                .eq(QuizForumActivityDO::getStatus, 0)); // 0 = ENABLED
    }
}
