package cn.iocoder.yudao.module.gamification.service.lottery;

import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryActivityPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.lottery.LotteryPrizeDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryActivityPrizeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.lottery.LotteryPrizeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 抽奖活动管理服务实现
 */
@Service
public class LotteryActivityServiceImpl implements LotteryActivityService {

    private static final Logger log = LoggerFactory.getLogger(LotteryActivityServiceImpl.class);

    @Resource
    private LotteryActivityMapper lotteryActivityMapper;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Resource
    private LotteryActivityPrizeMapper lotteryActivityPrizeMapper;

    @Override
    public Long createLotteryActivity(LotteryActivityDO activity) {
        lotteryActivityMapper.insert(activity);
        log.info("[createLotteryActivity] Created: id={}, activityId={}, type={}",
                activity.getId(), activity.getActivityId(), activity.getType());
        return activity.getId();
    }

    @Override
    public void updateLotteryActivity(LotteryActivityDO activity) {
        lotteryActivityMapper.updateById(activity);
        log.info("[updateLotteryActivity] Updated: id={}", activity.getId());
    }

    @Override
    public LotteryActivityDO getLotteryActivity(Long id) {
        return lotteryActivityMapper.selectById(id);
    }

    @Override
    public LotteryActivityDO getByActivityId(Long activityId) {
        return lotteryActivityMapper.selectByActivityId(activityId);
    }

    @Override
    public Long createPrize(LotteryPrizeDO prize) {
        lotteryPrizeMapper.insert(prize);
        log.info("[createPrize] Created: id={}, name={}", prize.getId(), prize.getName());
        return prize.getId();
    }

    @Override
    public void updatePrize(LotteryPrizeDO prize) {
        lotteryPrizeMapper.updateById(prize);
        log.info("[updatePrize] Updated: id={}", prize.getId());
    }

    @Override
    public void deletePrize(Long prizeId) {
        lotteryPrizeMapper.deleteById(prizeId);
        log.info("[deletePrize] Deleted: id={}", prizeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLotteryActivityPrizes(Long lotteryActivityId, List<LotteryActivityPrizeDO> activityPrizes) {
        // 先删除旧关联
        lotteryActivityPrizeMapper.deleteByLotteryActivityId(lotteryActivityId);
        // 再批量插入新关联
        if (activityPrizes != null && !activityPrizes.isEmpty()) {
            for (LotteryActivityPrizeDO ap : activityPrizes) {
                ap.setLotteryActivityId(lotteryActivityId);
                lotteryActivityPrizeMapper.insert(ap);
            }
        }
        log.info("[saveLotteryActivityPrizes] Saved {} prizes for activity {}",
                activityPrizes != null ? activityPrizes.size() : 0, lotteryActivityId);
    }

    @Override
    public List<LotteryActivityPrizeDO> getActivityPrizes(Long lotteryActivityId) {
        return lotteryActivityPrizeMapper.selectByLotteryActivityId(lotteryActivityId);
    }
}
