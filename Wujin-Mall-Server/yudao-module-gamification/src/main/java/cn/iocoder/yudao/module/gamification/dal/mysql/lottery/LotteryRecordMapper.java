package cn.iocoder.yudao.module.gamification.dal.mysql.lottery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface LotteryRecordMapper extends BaseMapperX<LotteryRecordDO> {

    default List<LotteryRecordDO> selectByUserAndActivity(Long userId, Long lotteryActivityId) {
        return selectList(new LambdaQueryWrapperX<LotteryRecordDO>()
                .eq(LotteryRecordDO::getUserId, userId)
                .eq(LotteryRecordDO::getLotteryActivityId, lotteryActivityId)
                .orderByDesc(LotteryRecordDO::getDrawTime));
    }

    default long countByUserAndActivity(Long userId, Long lotteryActivityId) {
        return selectCount(new LambdaQueryWrapperX<LotteryRecordDO>()
                .eq(LotteryRecordDO::getUserId, userId)
                .eq(LotteryRecordDO::getLotteryActivityId, lotteryActivityId));
    }

    default long countByUserAndActivityToday(Long userId, Long lotteryActivityId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return selectCount(new LambdaQueryWrapperX<LotteryRecordDO>()
                .eq(LotteryRecordDO::getUserId, userId)
                .eq(LotteryRecordDO::getLotteryActivityId, lotteryActivityId)
                .between(LotteryRecordDO::getDrawTime, startOfDay, endOfDay));
    }

    default long countConsecutiveLosses(Long userId, Long lotteryActivityId) {
        // Count consecutive non-winning draws from the user's most recent draws
        List<LotteryRecordDO> records = selectList(new LambdaQueryWrapperX<LotteryRecordDO>()
                .eq(LotteryRecordDO::getUserId, userId)
                .eq(LotteryRecordDO::getLotteryActivityId, lotteryActivityId)
                .orderByDesc(LotteryRecordDO::getDrawTime));
        long count = 0;
        for (LotteryRecordDO record : records) {
            if (Boolean.TRUE.equals(record.getWon())) {
                break;
            }
            count++;
        }
        return count;
    }

    default List<LotteryRecordDO> selectRecentWinners(Long lotteryActivityId, int limit) {
        return selectList(new LambdaQueryWrapperX<LotteryRecordDO>()
                .eq(LotteryRecordDO::getLotteryActivityId, lotteryActivityId)
                .eq(LotteryRecordDO::getWon, true)
                .orderByDesc(LotteryRecordDO::getDrawTime)
                .last("LIMIT " + limit));
    }
}
