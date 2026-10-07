package cn.iocoder.yudao.module.gamification.dal.mysql.lottery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LotteryPrizeMapper extends BaseMapperX<LotteryPrizeDO> {

    /**
     * 原子扣减库存，仅当 remaining_stock > 0 时成功
     * @return 影响行数（1=成功，0=库存不足）
     */
    @Update("UPDATE gamification_lottery_prize SET remaining_stock = remaining_stock - 1 WHERE id = #{prizeId} AND remaining_stock > 0 AND deleted = 0")
    int decrementStock(Long prizeId);
}
