package cn.iocoder.yudao.module.gamification.dal.mysql.vote;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoteRecordMapper extends BaseMapperX<VoteRecordDO> {

    default List<VoteRecordDO> selectByVoteActivityIdAndUserId(Long voteActivityId, Long userId) {
        return selectList(new LambdaQueryWrapperX<VoteRecordDO>()
                .eq(VoteRecordDO::getVoteActivityId, voteActivityId)
                .eq(VoteRecordDO::getUserId, userId));
    }

    default Long selectCountByOptionId(Long optionId) {
        return selectCount(new LambdaQueryWrapperX<VoteRecordDO>()
                .eq(VoteRecordDO::getOptionId, optionId));
    }

    @Select("SELECT COUNT(DISTINCT user_id) FROM gamification_vote_record WHERE vote_activity_id = #{voteActivityId} AND deleted = 0")
    Long selectDistinctUserCountByVoteActivityId(@Param("voteActivityId") Long voteActivityId);

    default List<VoteRecordDO> selectByVoteActivityId(Long voteActivityId) {
        return selectList(new LambdaQueryWrapperX<VoteRecordDO>()
                .eq(VoteRecordDO::getVoteActivityId, voteActivityId));
    }
}
