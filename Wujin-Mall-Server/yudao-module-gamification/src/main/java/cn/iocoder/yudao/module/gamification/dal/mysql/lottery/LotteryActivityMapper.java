package cn.iocoder.yudao.module.gamification.dal.mysql.lottery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LotteryActivityMapper extends BaseMapperX<LotteryActivityDO> {

    default LotteryActivityDO selectByActivityId(Long activityId) {
        return selectOne(new LambdaQueryWrapperX<LotteryActivityDO>()
                .eq(LotteryActivityDO::getActivityId, activityId));
    }
}
