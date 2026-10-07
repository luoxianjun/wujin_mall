package cn.iocoder.yudao.module.gamification.dal.mysql.quiz;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.quiz.vo.QuizAttemptPageReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.quiz.QuizAttemptDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Mapper
public interface QuizAttemptMapper extends BaseMapperX<QuizAttemptDO> {

    default Integer selectMaxAttemptNo(Long quizActivityId, Long userId) {
        QuizAttemptDO attempt = selectOne(new LambdaQueryWrapperX<QuizAttemptDO>()
                .eq(QuizAttemptDO::getQuizActivityId, quizActivityId)
                .eq(QuizAttemptDO::getUserId, userId)
                .orderByDesc(QuizAttemptDO::getAttemptNo)
                .last("LIMIT 1"));
        return attempt == null || attempt.getAttemptNo() == null ? 0 : attempt.getAttemptNo();
    }

    default QuizAttemptDO selectLatestByQuizActivityIdAndUserId(Long quizActivityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<QuizAttemptDO>()
                .eq(QuizAttemptDO::getQuizActivityId, quizActivityId)
                .eq(QuizAttemptDO::getUserId, userId)
                .orderByDesc(QuizAttemptDO::getAttemptNo)
                .last("LIMIT 1"));
    }

    default QuizAttemptDO selectByQuizActivityIdAndUserIdAndAttemptNo(Long quizActivityId, Long userId, Integer attemptNo) {
        return selectOne(new LambdaQueryWrapperX<QuizAttemptDO>()
                .eq(QuizAttemptDO::getQuizActivityId, quizActivityId)
                .eq(QuizAttemptDO::getUserId, userId)
                .eq(QuizAttemptDO::getAttemptNo, attemptNo));
    }

    default QuizAttemptDO selectBestSubmittedAttempt(Long quizActivityId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<QuizAttemptDO>()
                .eq(QuizAttemptDO::getQuizActivityId, quizActivityId)
                .eq(QuizAttemptDO::getUserId, userId)
                .in(QuizAttemptDO::getStatus, Arrays.asList(
                        QuizAttemptDO.STATUS_SUBMITTED,
                        QuizAttemptDO.STATUS_TIMEOUT_SUBMITTED))
                .orderByDesc(QuizAttemptDO::getScore)
                .orderByAsc(QuizAttemptDO::getElapsedMillis)
                .orderByAsc(QuizAttemptDO::getSubmittedAt)
                .last("LIMIT 1"));
    }

    default List<QuizAttemptDO> selectSubmittedByQuizActivityId(Long quizActivityId) {
        return selectList(new LambdaQueryWrapperX<QuizAttemptDO>()
                .eq(QuizAttemptDO::getQuizActivityId, quizActivityId)
                .in(QuizAttemptDO::getStatus, Arrays.asList(
                        QuizAttemptDO.STATUS_SUBMITTED,
                        QuizAttemptDO.STATUS_TIMEOUT_SUBMITTED)));
    }

    default List<QuizAttemptDO> selectInProgressList() {
        return selectList(QuizAttemptDO::getStatus, QuizAttemptDO.STATUS_IN_PROGRESS);
    }

    default PageResult<QuizAttemptDO> selectPage(QuizAttemptPageReqVO reqVO, Collection<Long> userIds) {
        return selectPage(reqVO, buildQueryWrapper(reqVO, userIds)
                .orderByDesc(QuizAttemptDO::getId));
    }

    default List<QuizAttemptDO> selectListForExport(QuizAttemptPageReqVO reqVO, Collection<Long> userIds) {
        return selectList(buildQueryWrapper(reqVO, userIds)
                .orderByDesc(QuizAttemptDO::getId));
    }

    default LambdaQueryWrapperX<QuizAttemptDO> buildQueryWrapper(QuizAttemptPageReqVO reqVO, Collection<Long> userIds) {
        return new LambdaQueryWrapperX<QuizAttemptDO>()
                .eqIfPresent(QuizAttemptDO::getQuizActivityId, reqVO.getQuizActivityId())
                .eqIfPresent(QuizAttemptDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(QuizAttemptDO::getUserId, reqVO.getUserId())
                .inIfPresent(QuizAttemptDO::getUserId, userIds)
                .eqIfPresent(QuizAttemptDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(QuizAttemptDO::getSubmittedAt, reqVO.getSubmittedAt());
    }
}
