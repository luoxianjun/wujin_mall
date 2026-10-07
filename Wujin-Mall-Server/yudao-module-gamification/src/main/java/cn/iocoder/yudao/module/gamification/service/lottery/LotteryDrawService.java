package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryRecordDO;

import java.util.List;

/**
 * 抽奖核心服务 - 负责抽奖逻辑、概率计算、库存管理
 */
public interface LotteryDrawService {

    /**
     * 执行一次抽奖
     *
     * @param userId 用户ID
     * @param lotteryActivityId 抽奖活动ID
     * @return 抽奖记录
     */
    LotteryRecordDO draw(Long userId, Long lotteryActivityId);

    /**
     * 检查用户是否可以抽奖
     *
     * @param userId 用户ID
     * @param lotteryActivityId 抽奖活动ID
     * @return true=可以抽奖
     */
    boolean canDraw(Long userId, Long lotteryActivityId);

    /**
     * 获取用户的抽奖记录
     *
     * @param userId 用户ID
     * @param lotteryActivityId 抽奖活动ID
     * @return 抽奖记录列表
     */
    List<LotteryRecordDO> getUserRecords(Long userId, Long lotteryActivityId);

    /**
     * 执行定时开奖（为所有参与者开奖）
     *
     * @param lotteryActivityId 抽奖活动ID
     */
    void executeScheduledDraw(Long lotteryActivityId);
}
