package cn.iocoder.yudao.module.gamification.service.invitation;

/**
 * 邀请奖励服务接口
 *
 * @author gamification
 */
public interface InvitationRewardService {

    /**
     * 处理实名认证完成后的奖励发放
     *
     * @param userId 完成实名认证的用户ID
     * @return 是否成功创建奖励记录
     */
    boolean processVerificationReward(Long userId);

    /**
     * 发放单个奖励
     *
     * @param rewardId 奖励记录ID
     * @return 是否成功
     */
    boolean grantReward(Long rewardId);

    /**
     * 重试失败的奖励发放
     *
     * @param limit 处理数量限制
     * @return 成功重试的数量
     */
    int retryFailedRewards(Integer limit);
}
