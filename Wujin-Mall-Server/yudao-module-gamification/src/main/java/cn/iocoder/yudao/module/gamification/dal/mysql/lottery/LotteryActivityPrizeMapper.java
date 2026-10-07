package cn.iocoder.yudao.module.gamification.dal.mysql.lottery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LotteryActivityPrizeMapper extends BaseMapperX<LotteryActivityPrizeDO> {

    default List<LotteryActivityPrizeDO> selectByLotteryActivityId(Long lotteryActivityId) {
        return selectList(new LambdaQueryWrapperX<LotteryActivityPrizeDO>()
                .eq(LotteryActivityPrizeDO::getLotteryActivityId, lotteryActivityId)
                .orderByAsc(LotteryActivityPrizeDO::getSortOrder));
    }

    default void deleteByLotteryActivityId(Long lotteryActivityId) {
        delete(new LambdaQueryWrapperX<LotteryActivityPrizeDO>()
                .eq(LotteryActivityPrizeDO::getLotteryActivityId, lotteryActivityId));
    }
}
