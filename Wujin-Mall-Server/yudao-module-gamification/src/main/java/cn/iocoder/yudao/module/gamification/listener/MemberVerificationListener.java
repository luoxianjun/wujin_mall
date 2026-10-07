package cn.iocoder.yudao.module.gamification.listener;

import cn.iocoder.yudao.module.gamification.service.invitation.InvitationRewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 会员实名认证监听器
 * 监听实名认证完成事件，触发邀请奖励发放
 *
 * @author gamification
 */
@Component
public class MemberVerificationListener {

    private static final Logger log = LoggerFactory.getLogger(MemberVerificationListener.class);

    @Resource
    private InvitationRewardService invitationRewardService;

    /**
     * 处理会员实名认证完成事件
     *
     * @param event 实名认证事件
     */
    @Async
    @EventListener
    public void onMemberVerified(MemberVerifiedEvent event) {
        if (event == null || event.getUserId() == null) {
            log.warn("[onMemberVerified] Invalid event: {}", event);
            return;
        }

        Long userId = event.getUserId();
        log.info("[onMemberVerified] Received verification event for userId={}", userId);

        try {
            // 处理邀请奖励发放
            boolean success = invitationRewardService.processVerificationReward(userId);

            if (success) {
                log.info("[onMemberVerified] Successfully processed rewards for userId={}", userId);
            } else {
                log.info("[onMemberVerified] No rewards to process for userId={} (not invited or already processed)", userId);
            }
        } catch (Exception e) {
            log.error("[onMemberVerified] Failed to process rewards for userId={}", userId, e);
            // 不抛出异常，避免影响其他监听器
        }
    }
}
