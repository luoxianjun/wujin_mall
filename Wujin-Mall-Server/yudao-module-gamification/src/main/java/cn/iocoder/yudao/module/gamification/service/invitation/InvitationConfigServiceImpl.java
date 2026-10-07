package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigUpdateReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationConfigDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class InvitationConfigServiceImpl implements InvitationConfigService {

    private static final String KEY_INVITER_REWARD_POINTS = "invitation.inviter_reward_points";
    private static final String KEY_INVITEE_REWARD_POINTS = "invitation.invitee_reward_points";
    private static final String KEY_REWARD_MODE = "invitation.reward_mode";
    private static final String KEY_MAX_INVITATIONS = "invitation.max_invitations";
    private static final String KEY_ENABLED = "invitation.enabled";
    private static final String KEY_REWARD_RETRY_LIMIT = "invitation.reward_retry_limit";
    private static final String KEY_IP_LIMIT = "invitation.antifraud.ip_limit";
    private static final String KEY_DEVICE_LIMIT = "invitation.antifraud.device_limit";

    private static final Integer DEFAULT_INVITER_REWARD_POINTS = 20;
    private static final Integer DEFAULT_INVITEE_REWARD_POINTS = 10;
    private static final String DEFAULT_REWARD_MODE = "both";
    private static final Integer DEFAULT_MAX_INVITATIONS = 100;
    private static final Integer DEFAULT_REWARD_RETRY_LIMIT = 3;
    private static final Integer DEFAULT_IP_LIMIT = 5;
    private static final Integer DEFAULT_DEVICE_LIMIT = 3;

    @Resource
    private InvitationConfigMapper invitationConfigMapper;

    @Override
    public InvitationConfigRespVO getConfig() {
        InvitationConfigRespVO respVO = new InvitationConfigRespVO();
        respVO.setId(1L);
        respVO.setInviterRewardPoints(
                getIntConfig(KEY_INVITER_REWARD_POINTS, DEFAULT_INVITER_REWARD_POINTS));
        respVO.setInviteeRewardPoints(
                getIntConfig(KEY_INVITEE_REWARD_POINTS, DEFAULT_INVITEE_REWARD_POINTS));
        respVO.setRewardMode(getStringConfig(KEY_REWARD_MODE, DEFAULT_REWARD_MODE));
        respVO.setMaxInvitations(getIntConfig(KEY_MAX_INVITATIONS, DEFAULT_MAX_INVITATIONS));
        respVO.setEnabled(getBooleanConfig(KEY_ENABLED, true));
        respVO.setRewardRetryLimit(
                getIntConfig(KEY_REWARD_RETRY_LIMIT, DEFAULT_REWARD_RETRY_LIMIT));
        respVO.setIpLimit(getIntConfig(KEY_IP_LIMIT, DEFAULT_IP_LIMIT));
        respVO.setDeviceLimit(getIntConfig(KEY_DEVICE_LIMIT, DEFAULT_DEVICE_LIMIT));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(InvitationConfigUpdateReqVO updateReqVO) {
        upsertConfig(
                KEY_INVITER_REWARD_POINTS,
                String.valueOf(updateReqVO.getInviterRewardPoints()),
                "Inviter reward points");
        upsertConfig(
                KEY_INVITEE_REWARD_POINTS,
                String.valueOf(updateReqVO.getInviteeRewardPoints()),
                "Invitee reward points");
        upsertConfig(KEY_REWARD_MODE, updateReqVO.getRewardMode(), "Reward mode");
        upsertConfig(
                KEY_MAX_INVITATIONS,
                String.valueOf(updateReqVO.getMaxInvitations()),
                "Maximum invitations per inviter");
        upsertConfig(KEY_ENABLED, String.valueOf(updateReqVO.getEnabled()), "Invitation enabled");
        upsertConfig(
                KEY_REWARD_RETRY_LIMIT,
                String.valueOf(updateReqVO.getRewardRetryLimit()),
                "Reward retry limit");
        upsertConfig(
                KEY_IP_LIMIT,
                String.valueOf(updateReqVO.getIpLimit()),
                "IP registration limit");
        upsertConfig(
                KEY_DEVICE_LIMIT,
                String.valueOf(updateReqVO.getDeviceLimit()),
                "Device registration limit");
        log.info("[updateConfig] updated invitation config id={}", updateReqVO.getId());
    }

    private Integer getIntConfig(String key, Integer defaultValue) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(config.getConfigValue());
        } catch (NumberFormatException ex) {
            log.warn("[getIntConfig] invalid value key={}, value={}", key, config.getConfigValue(), ex);
            return defaultValue;
        }
    }

    private String getStringConfig(String key, String defaultValue) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(key);
        return config == null || config.getConfigValue() == null ? defaultValue : config.getConfigValue();
    }

    private Boolean getBooleanConfig(String key, Boolean defaultValue) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(key);
        return config == null || config.getConfigValue() == null
                ? defaultValue
                : Boolean.valueOf(config.getConfigValue());
    }

    private void upsertConfig(String key, String value, String description) {
        InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(key);
        if (config == null) {
            invitationConfigMapper.insert(InvitationConfigDO.builder()
                    .configKey(key)
                    .configValue(value)
                    .description(description)
                    .build());
            return;
        }
        config.setConfigValue(value);
        config.setDescription(description);
        invitationConfigMapper.updateById(config);
    }
}
