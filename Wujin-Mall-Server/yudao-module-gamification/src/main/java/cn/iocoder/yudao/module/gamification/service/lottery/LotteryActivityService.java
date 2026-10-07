package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;

import java.util.List;

/**
 * 抽奖活动管理服务 - 负责活动和奖品的CRUD
 */
public interface LotteryActivityService {

    /**
     * 创建抽奖活动
     */
    Long createLotteryActivity(LotteryActivityDO activity);

    /**
     * 更新抽奖活动
     */
    void updateLotteryActivity(LotteryActivityDO activity);

    /**
     * 获取抽奖活动详情
     */
    LotteryActivityDO getLotteryActivity(Long id);

    /**
     * 通过论坛活动ID获取抽奖活动
     */
    LotteryActivityDO getByActivityId(Long activityId);

    /**
     * 创建奖品（独立，不关联活动）
     */
    Long createPrize(LotteryPrizeDO prize);

    /**
     * 更新奖品
     */
    void updatePrize(LotteryPrizeDO prize);

    /**
     * 删除奖品
     */
    void deletePrize(Long prizeId);

    /**
     * 保存活动-奖品关联（先删后插）
     * @param lotteryActivityId 抽奖活动ID
     * @param activityPrizes 关联奖品列表（含概率和排序）
     */
    void saveLotteryActivityPrizes(Long lotteryActivityId, List<LotteryActivityPrizeDO> activityPrizes);

    /**
     * 获取活动关联的奖品列表
     */
    List<LotteryActivityPrizeDO> getActivityPrizes(Long lotteryActivityId);
}
