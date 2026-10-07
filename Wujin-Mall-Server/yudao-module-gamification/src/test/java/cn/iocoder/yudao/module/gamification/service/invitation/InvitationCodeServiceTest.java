package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationConfigDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationCodeMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationConfigMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRewardMapper;
import cn.iocoder.yudao.module.gamification.util.AntifraudUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Import(InvitationCodeServiceImpl.class)
class InvitationCodeServiceTest extends BaseDbUnitTest {

    @Resource
    private InvitationCodeService invitationCodeService;

    @Resource
    private InvitationCodeMapper invitationCodeMapper;

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @Resource
    private InvitationConfigMapper invitationConfigMapper;

    @Resource
    private InvitationRewardMapper invitationRewardMapper;

    @MockBean
    private AntifraudUtils antifraudUtils;

    @Test
    void getOrCreateInvitationCode_reusesExistingCode() {
        InvitationCodeDO code = InvitationCodeDO.builder()
                .userId(11L)
                .code("KEEP1234")
                .status(InvitationCodeDO.STATUS_VALID)
                .totalInvitations(0)
                .validInvitations(0)
                .build();
        invitationCodeMapper.insert(code);

        String actual = invitationCodeService.getOrCreateInvitationCode(11L);

        assertEquals("KEEP1234", actual);
        assertEquals("KEEP1234", invitationCodeMapper.selectByUserId(11L).getCode());
    }

    @Test
    void registerWithInvitationCode_rejectsSelfInvite() {
        insertBooleanConfig("invitation.enabled", true);
        InvitationCodeDO code = InvitationCodeDO.builder()
                .userId(22L)
                .code("SELF1234")
                .status(InvitationCodeDO.STATUS_VALID)
                .totalInvitations(0)
                .validInvitations(0)
                .build();
        invitationCodeMapper.insert(code);

        assertThrows(ServiceException.class,
                () -> invitationCodeService.registerWithInvitationCode(22L, "SELF1234", "device", "127.0.0.1"));
    }

    @Test
    void registerWithInvitationCode_rejectsWhenMaxInvitationsReached() {
        insertBooleanConfig("invitation.enabled", true);
        insertIntConfig("invitation.max_invitations", 1);
        InvitationCodeDO code = InvitationCodeDO.builder()
                .userId(33L)
                .code("LIMIT123")
                .status(InvitationCodeDO.STATUS_VALID)
                .totalInvitations(1)
                .validInvitations(1)
                .build();
        invitationCodeMapper.insert(code);
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(33L)
                .inviteeId(34L)
                .invitationCode("LIMIT123")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.now().minusDays(1))
                .build());
        when(antifraudUtils.checkInvitationLimit(33L, 1, 1)).thenReturn(false);

        assertThrows(ServiceException.class,
                () -> invitationCodeService.registerWithInvitationCode(35L, "LIMIT123", "device", "127.0.0.2"));
    }

    @Test
    void registerWithInvitationCode_doesNotGrantPointsDuringRegistration() {
        insertBooleanConfig("invitation.enabled", true);
        insertIntConfig("invitation.max_invitations", 10);
        insertIntConfig("invitation.antifraud.ip_limit", 5);
        insertIntConfig("invitation.antifraud.device_limit", 3);
        InvitationCodeDO code = InvitationCodeDO.builder()
                .userId(44L)
                .code("GOOD1234")
                .status(InvitationCodeDO.STATUS_VALID)
                .totalInvitations(0)
                .validInvitations(0)
                .build();
        invitationCodeMapper.insert(code);
        when(antifraudUtils.checkInvitationLimit(anyLong(), anyInt(), anyInt())).thenReturn(true);
        when(antifraudUtils.generateDeviceFingerprint("device")).thenReturn("fingerprint-1");
        when(antifraudUtils.checkIpLimit("127.0.0.3", 5)).thenReturn(true);
        when(antifraudUtils.checkDeviceLimit("fingerprint-1", 3)).thenReturn(true);

        boolean result =
                invitationCodeService.registerWithInvitationCode(45L, "GOOD1234", "device", "127.0.0.3");

        assertTrue(result);
        InvitationRelationDO relation = invitationRelationMapper.selectByInviteeId(45L);
        assertEquals(InvitationRelationDO.STATUS_PENDING, relation.getStatus());
        assertEquals(0L, invitationRewardMapper.selectCount());
        assertEquals(1, invitationCodeMapper.selectByUserId(44L).getTotalInvitations());
    }

    private void insertIntConfig(String key, Integer value) {
        invitationConfigMapper.insert(InvitationConfigDO.builder()
                .configKey(key)
                .configValue(String.valueOf(value))
                .description(key)
                .build());
    }

    private void insertBooleanConfig(String key, boolean value) {
        invitationConfigMapper.insert(InvitationConfigDO.builder()
                .configKey(key)
                .configValue(String.valueOf(value))
                .description(key)
                .build());
    }
}
