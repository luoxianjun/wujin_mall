package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationConfigDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRewardDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationConfigMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRewardMapper;
import cn.iocoder.yudao.module.gamification.service.notification.GamificationNotificationService;
import cn.iocoder.yudao.module.gamification.service.points.GamificationPointsService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.enums.point.MemberPointBizTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvitationRewardServiceImpl implements InvitationRewardService {

    private static final Logger log = LoggerFactory.getLogger(InvitationRewardServiceImpl.class);
    private static final String CONFIG_INVITER_POINTS = "invitation.inviter_reward_points";
    private static final String CONFIG_INVITEE_POINTS = "invitation.invitee_reward_points";
    private static final String CONFIG_REWARD_MODE = "invitation.reward_mode";
    private static final Integer DEFAULT_INVITER_POINTS = 20;
    private static final Integer DEFAULT_INVITEE_POINTS = 10;
    private static final String DEFAULT_REWARD_MODE = "both";

    @Resource
    private InvitationRelationMapper invitationRelationMapper;
    @Resource
    private InvitationRewardMapper invitationRewardMapper;
    @Resource
    private InvitationConfigMapper invitationConfigMapper;
    @Resource
    private GamificationPointsService gamificationPointsService;
    @Resource
    private GamificationNotificationService gamificationNotificationService;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processVerificationReward(Long userId) {
        if (userId == null) {
            return false;
        }
        InvitationRelationDO relation = invitationRelationMapper.selectByInviteeId(userId);
        if (relation == null || InvitationRelationDO.STATUS_INVALID.equals(relation.getStatus())) {
            return false;
        }
        if (!invitationRewardMapper.selectByRelationId(relation.getId()).isEmpty()) {
            log.warn("[processVerificationReward] rewards already exist relationId={}", relation.getId());
            return false;
        }

        if (!InvitationRelationDO.STATUS_COMPLETED.equals(relation.getStatus())) {
            relation.setStatus(InvitationRelationDO.STATUS_COMPLETED);
            relation.setCompleteTime(LocalDateTime.now());
            invitationRelationMapper.updateById(relation);
        }

        Integer inviterPoints = getIntConfig(CONFIG_INVITER_POINTS, DEFAULT_INVITER_POINTS);
        Integer inviteePoints = getIntConfig(CONFIG_INVITEE_POINTS, DEFAULT_INVITEE_POINTS);
        String rewardMode = getStringConfig(CONFIG_REWARD_MODE, DEFAULT_REWARD_MODE);

        List<InvitationRewardDO> rewards = new ArrayList<>();
        if (shouldRewardInviter(rewardMode)) {
            rewards.add(InvitationRewardDO.builder()
                    .relationId(relation.getId())
                    .inviterId(relation.getInviterId())
                    .inviteeId(relation.getInviteeId())
                    .rewardType(InvitationRewardDO.REWARD_TYPE_INVITER)
                    .points(inviterPoints)
                    .status(InvitationRewardDO.STATUS_PENDING)
                    .build());
        }
        if (shouldRewardInvitee(rewardMode)) {
            rewards.add(InvitationRewardDO.builder()
                    .relationId(relation.getId())
                    .inviterId(relation.getInviterId())
                    .inviteeId(relation.getInviteeId())
                    .rewardType(InvitationRewardDO.REWARD_TYPE_INVITEE)
                    .points(inviteePoints)
                    .status(InvitationRewardDO.STATUS_PENDING)
                    .build());
        }

        rewards.forEach(invitationRewardMapper::insert);
        for (InvitationRewardDO reward : rewards) {
            grantReward(reward.getId());
        }
        return !rewards.isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grantReward(Long rewardId) {
        if (rewardId == null) {
            return false;
        }
        InvitationRewardDO reward = invitationRewardMapper.selectById(rewardId);
        if (reward == null) {
            return false;
        }
        if (!InvitationRewardDO.STATUS_PENDING.equals(reward.getStatus())
                && !InvitationRewardDO.STATUS_FAILED.equals(reward.getStatus())) {
            return false;
        }

        try {
            Long rewardUserId = reward.resolveRewardUserId();
            if (rewardUserId == null) {
                throw new IllegalStateException("reward target user is missing");
            }

            reward.setStatus(InvitationRewardDO.STATUS_PROCESSING);
            invitationRewardMapper.updateById(reward);

            Integer bizType = reward.isInviterReward()
                    ? MemberPointBizTypeEnum.INVITATION_COMPLETE.getType()
                    : MemberPointBizTypeEnum.INVITATION_INVITEE.getType();
            boolean pointsGranted = gamificationPointsService.grantPoints(
                    rewardUserId,
                    reward.getPoints(),
                    bizType,
                    reward.getId(),
                    "invitation reward");
            if (!pointsGranted) {
                throw new IllegalStateException("grant points failed");
            }

            sendRewardNotification(reward, rewardUserId);

            reward.setStatus(InvitationRewardDO.STATUS_SUCCESS);
            reward.setGrantTime(LocalDateTime.now());
            invitationRewardMapper.updateById(reward);
            return true;
        } catch (Exception ex) {
            log.error("[grantReward] failed rewardId={}", rewardId, ex);
            reward.setStatus(InvitationRewardDO.STATUS_FAILED);
            invitationRewardMapper.updateById(reward);
            return false;
        }
    }

    @Override
    public int retryFailedRewards(Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? 100 : limit;
        List<InvitationRewardDO> failedRewards =
                invitationRewardMapper.selectFailedRewardsForRetry(null, actualLimit);
        int successCount = 0;
        for (InvitationRewardDO reward : failedRewards) {
            if (grantReward(reward.getId())) {
                successCount++;
            }
        }
        return successCount;
    }

    private void sendRewardNotification(InvitationRewardDO reward, Long rewardUserId) {
        InvitationRelationDO relation = invitationRelationMapper.selectById(reward.getRelationId());
        if (relation == null) {
            return;
        }
        if (reward.isInviterReward()) {
            MemberUserRespDTO invitee = memberUserApi.getUser(relation.getInviteeId());
            String inviteeName =
                    invitee != null && invitee.getNickname() != null ? invitee.getNickname() : "new user";
            gamificationNotificationService.sendInvitationRewardNotification(
                    rewardUserId, inviteeName, reward.getPoints());
            return;
        }
        gamificationNotificationService.sendWelcomeNotification(rewardUserId, reward.getPoints());
    }

    private boolean shouldRewardInviter(String rewardMode) {
        return rewardMode == null
                || DEFAULT_REWARD_MODE.equalsIgnoreCase(rewardMode)
                || "inviter".equalsIgnoreCase(rewardMode)
                || "inviter_only".equalsIgnoreCase(rewardMode);
    }

    private boolean shouldRewardInvitee(String rewardMode) {
        return rewardMode == null
                || DEFAULT_REWARD_MODE.equalsIgnoreCase(rewardMode)
                || "invitee".equalsIgnoreCase(rewardMode);
    }

    private Integer getIntConfig(String configKey, Integer defaultValue) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(configKey);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(config.getConfigValue());
        } catch (NumberFormatException ex) {
            log.warn("[getIntConfig] invalid config key={}, value={}", configKey, config.getConfigValue(), ex);
            return defaultValue;
        }
    }

    private String getStringConfig(String configKey, String defaultValue) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(configKey);
        return config == null || config.getConfigValue() == null ? defaultValue : config.getConfigValue();
    }
}
