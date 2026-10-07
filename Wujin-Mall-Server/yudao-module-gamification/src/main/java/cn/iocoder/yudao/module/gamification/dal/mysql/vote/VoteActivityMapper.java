package cn.iocoder.yudao.module.gamification.dal.mysql.vote;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.vote.VoteActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoteActivityMapper extends BaseMapperX<VoteActivityDO> {

    default VoteActivityDO selectByActivityId(Long activityId) {
        return selectOne(new LambdaQueryWrapperX<VoteActivityDO>()
                .eq(VoteActivityDO::getActivityId, activityId));
    }
}
