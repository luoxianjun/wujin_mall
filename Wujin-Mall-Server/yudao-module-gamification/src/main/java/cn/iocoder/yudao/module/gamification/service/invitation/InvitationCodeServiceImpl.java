package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationConfigDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationCodeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationConfigMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.util.AntifraudUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

@Service
public class InvitationCodeServiceImpl implements InvitationCodeService {

    private static final Logger log = LoggerFactory.getLogger(InvitationCodeServiceImpl.class);
    private static final String KEY_MAX_INVITATIONS = "invitation.max_invitations";
    private static final String KEY_IP_LIMIT = "invitation.antifraud.ip_limit";
    private static final String KEY_DEVICE_LIMIT = "invitation.antifraud.device_limit";
    private static final String KEY_ENABLED = "invitation.enabled";
    private static final int MAX_RETRY_GENERATE_CODE = 10;
    private static final int CODE_LENGTH = 8;

    @Resource
    private InvitationCodeMapper invitationCodeMapper;
    @Resource
    private InvitationRelationMapper invitationRelationMapper;
    @Resource
    private InvitationConfigMapper invitationConfigMapper;
    @Resource
    private AntifraudUtils antifraudUtils;

    @Override
    public String getOrCreateInvitationCode(Long userId) {
        if (userId == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "userId cannot be null");
        }
        InvitationCodeDO existingCode = invitationCodeMapper.selectByUserId(userId);
        if (existingCode != null) {
            return existingCode.getCode();
        }

        String code = generateUniqueCode();
        InvitationCodeDO invitationCode = InvitationCodeDO.builder()
                .userId(userId)
                .code(code)
                .status(InvitationCodeDO.STATUS_VALID)
                .totalInvitations(0)
                .validInvitations(0)
                .build();
        try {
            invitationCodeMapper.insert(invitationCode);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发场景：另一个请求已经创建了邀请码，直接查询返回
            log.info("[getOrCreateInvitationCode] duplicate key, re-reading for userId={}", userId);
            existingCode = invitationCodeMapper.selectByUserId(userId);
            if (existingCode != null) {
                return existingCode.getCode();
            }
            throw new ServiceException(BAD_REQUEST.getCode(), "failed to create invitation code");
        }
        log.info("[getOrCreateInvitationCode] created invitation code userId={}, code={}", userId, code);
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerWithInvitationCode(
            Long inviteeId, String invitationCode, String deviceInfo, String ip) {
        if (inviteeId == null || invitationCode == null || invitationCode.trim().isEmpty()) {
            throw new ServiceException(
                    BAD_REQUEST.getCode(), "inviteeId and invitationCode are required");
        }
        if (!getBooleanConfig(KEY_ENABLED, true)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "invitation feature is disabled");
        }

        InvitationRelationDO existingRelation = invitationRelationMapper.selectByInviteeId(inviteeId);
        if (existingRelation != null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "你已经绑定过邀请关系了，无需重复绑定");
        }

        InvitationCodeDO inviterCode = invitationCodeMapper.selectByCode(invitationCode);
        if (inviterCode == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "invitation code does not exist");
        }
        if (!InvitationCodeDO.STATUS_VALID.equals(inviterCode.getStatus())) {
            throw new ServiceException(BAD_REQUEST.getCode(), "invitation code is invalid");
        }
        if (inviterCode.getUserId().equals(inviteeId)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "cannot use your own invitation code");
        }

        int maxInvitations = getIntConfig(KEY_MAX_INVITATIONS, 100);
        long currentCount = invitationRelationMapper.countByInviterId(inviterCode.getUserId());
        if (!antifraudUtils.checkInvitationLimit(
                inviterCode.getUserId(), (int) currentCount, maxInvitations)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "invitation limit reached");
        }

        String deviceFingerprint = antifraudUtils.generateDeviceFingerprint(deviceInfo);
        if (!antifraudUtils.checkIpLimit(ip, getIntConfig(KEY_IP_LIMIT, 5))) {
            throw new ServiceException(BAD_REQUEST.getCode(), "ip registration limit exceeded");
        }
        if (!antifraudUtils.checkDeviceLimit(
                deviceFingerprint, getIntConfig(KEY_DEVICE_LIMIT, 3))) {
            throw new ServiceException(BAD_REQUEST.getCode(), "device registration limit exceeded");
        }

        InvitationRelationDO relation = InvitationRelationDO.builder()
                .inviterId(inviterCode.getUserId())
                .inviteeId(inviteeId)
                .invitationCode(invitationCode)
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.now())
                .deviceFingerprint(deviceFingerprint)
                .registerIp(ip)
                .build();
        invitationRelationMapper.insert(relation);

        inviterCode.setTotalInvitations(inviterCode.getTotalInvitations() + 1);
        invitationCodeMapper.updateById(inviterCode);
        log.info(
                "[registerWithInvitationCode] created pending relation inviterId={}, inviteeId={}, relationId={}",
                inviterCode.getUserId(),
                inviteeId,
                relation.getId());
        return true;
    }

    @Override
    public InvitationCodeDO getInvitationCodeByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return invitationCodeMapper.selectByCode(code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeInvitationTask(Long inviteeId) {
        if (inviteeId == null) {
            throw new ServiceException(BAD_REQUEST.getCode(), "userId cannot be null");
        }
        InvitationRelationDO relation = invitationRelationMapper.selectByInviteeId(inviteeId);
        if (relation == null || !InvitationRelationDO.STATUS_PENDING.equals(relation.getStatus())) {
            return false;
        }

        relation.setStatus(InvitationRelationDO.STATUS_COMPLETED);
        relation.setCompleteTime(LocalDateTime.now());
        invitationRelationMapper.updateById(relation);

        InvitationCodeDO inviterCode = invitationCodeMapper.selectByUserId(relation.getInviterId());
        if (inviterCode != null) {
            inviterCode.setValidInvitations(inviterCode.getValidInvitations() + 1);
            invitationCodeMapper.updateById(inviterCode);
        }
        return true;
    }

    private String generateUniqueCode() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(Character::isLetterOrDigit)
                .build();
        for (int i = 0; i < MAX_RETRY_GENERATE_CODE; i++) {
            String code = generator.generate(CODE_LENGTH).toUpperCase();
            if (invitationCodeMapper.selectByCode(code) == null) {
                return code;
            }
        }
        throw new ServiceException(BAD_REQUEST.getCode(), "failed to generate invitation code");
    }

    private Integer getIntConfig(String configKey, Integer defaultValue) {
        try {
            InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(configKey);
            if (config != null && config.getConfigValue() != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            log.error("[getIntConfig] Failed to get config: key={}", configKey, e);
        }
        return defaultValue;
    }

    private boolean getBooleanConfig(String configKey, boolean defaultValue) {
        try {
            InvitationConfigDO config = invitationConfigMapper.selectByConfigKey(configKey);
            if (config != null && config.getConfigValue() != null) {
                return Boolean.parseBoolean(config.getConfigValue());
            }
        } catch (Exception e) {
            log.error("[getBooleanConfig] Failed to get config: key={}", configKey, e);
        }
        return defaultValue;
    }
}
