package cn.iocoder.yudao.module.gamification.dal.mysql.vote;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VoteOptionMapper extends BaseMapperX<VoteOptionDO> {

    default List<VoteOptionDO> selectByVoteActivityId(Long voteActivityId) {
        return selectList(new LambdaQueryWrapperX<VoteOptionDO>()
                .eq(VoteOptionDO::getVoteActivityId, voteActivityId)
                .eq(VoteOptionDO::getAuditStatus, VoteOptionDO.AUDIT_APPROVED)
                .orderByAsc(VoteOptionDO::getSortOrder));
    }

    default List<VoteOptionDO> selectAllByVoteActivityId(Long voteActivityId) {
        return selectList(new LambdaQueryWrapperX<VoteOptionDO>()
                .eq(VoteOptionDO::getVoteActivityId, voteActivityId)
                .orderByAsc(VoteOptionDO::getSortOrder));
    }

    default Long selectCountByVoteActivityId(Long voteActivityId) {
        return selectCount(new LambdaQueryWrapperX<VoteOptionDO>()
                .eq(VoteOptionDO::getVoteActivityId, voteActivityId));
    }

    default void deleteByVoteActivityId(Long voteActivityId) {
        delete(new LambdaQueryWrapperX<VoteOptionDO>()
                .eq(VoteOptionDO::getVoteActivityId, voteActivityId));
    }
}
