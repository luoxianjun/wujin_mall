package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(InvitationRewardServiceImpl.class)
class InvitationRewardServiceTest extends BaseDbUnitTest {

    @Resource
    private InvitationRewardService invitationRewardService;

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @Resource
    private InvitationRewardMapper invitationRewardMapper;

    @Resource
    private InvitationConfigMapper invitationConfigMapper;

    @MockBean
    private GamificationPointsService gamificationPointsService;

    @MockBean
    private GamificationNotificationService gamificationNotificationService;

    @MockBean
    private MemberUserApi memberUserApi;

    @Test
    void processVerificationReward_createsInviterRewardRows() {
        InvitationRelationDO relation = insertRelation(101L, 201L, InvitationRelationDO.STATUS_PENDING);
        insertConfig("invitation.reward_mode", "inviter_only");
        insertConfig("invitation.inviter_reward_points", "25");
        insertConfig("invitation.invitee_reward_points", "10");
        when(gamificationPointsService.grantPoints(eq(101L), eq(25),
                eq(MemberPointBizTypeEnum.INVITATION_COMPLETE.getType()), anyLong(), eq("invitation reward")))
                .thenReturn(true);
        when(gamificationNotificationService.sendInvitationRewardNotification(101L, "Invitee", 25))
                .thenReturn(true);
        MemberUserRespDTO invitee = new MemberUserRespDTO();
        invitee.setId(201L);
        invitee.setNickname("Invitee");
        when(memberUserApi.getUser(201L)).thenReturn(invitee);

        boolean processed = invitationRewardService.processVerificationReward(201L);

        assertTrue(processed);
        List<InvitationRewardDO> rewards = invitationRewardMapper.selectByRelationId(relation.getId());
        assertEquals(1, rewards.size());
        assertEquals(InvitationRewardDO.REWARD_TYPE_INVITER, rewards.get(0).getRewardType());
        assertEquals(InvitationRewardDO.STATUS_SUCCESS, rewards.get(0).getStatus());
    }

    @Test
    void processVerificationReward_createsInviteeRewardRowsWhenModeBoth() {
        InvitationRelationDO relation = insertRelation(102L, 202L, InvitationRelationDO.STATUS_PENDING);
        insertConfig("invitation.reward_mode", "both");
        insertConfig("invitation.inviter_reward_points", "20");
        insertConfig("invitation.invitee_reward_points", "8");
        when(gamificationPointsService.grantPoints(eq(102L), eq(20),
                eq(MemberPointBizTypeEnum.INVITATION_COMPLETE.getType()), anyLong(), eq("invitation reward")))
                .thenReturn(true);
        when(gamificationPointsService.grantPoints(eq(202L), eq(8),
                eq(MemberPointBizTypeEnum.INVITATION_INVITEE.getType()), anyLong(), eq("invitation reward")))
                .thenReturn(true);
        when(gamificationNotificationService.sendInvitationRewardNotification(102L, "Invitee", 20))
                .thenReturn(true);
        when(gamificationNotificationService.sendWelcomeNotification(202L, 8)).thenReturn(true);
        MemberUserRespDTO invitee = new MemberUserRespDTO();
        invitee.setId(202L);
        invitee.setNickname("Invitee");
        when(memberUserApi.getUser(202L)).thenReturn(invitee);

        boolean processed = invitationRewardService.processVerificationReward(202L);

        assertTrue(processed);
        List<InvitationRewardDO> rewards = invitationRewardMapper.selectByRelationId(relation.getId());
        assertEquals(2, rewards.size());
    }

    @Test
    void processVerificationReward_doesNotCreateDuplicateRewards() {
        InvitationRelationDO relation = insertRelation(103L, 203L, InvitationRelationDO.STATUS_COMPLETED);
        invitationRewardMapper.insert(InvitationRewardDO.builder()
                .relationId(relation.getId())
                .inviterId(103L)
                .inviteeId(203L)
                .rewardType(InvitationRewardDO.REWARD_TYPE_INVITER)
                .points(20)
                .status(InvitationRewardDO.STATUS_SUCCESS)
                .grantTime(LocalDateTime.now())
                .build());

        boolean processed = invitationRewardService.processVerificationReward(203L);

        assertFalse(processed);
        assertEquals(1, invitationRewardMapper.selectByRelationId(relation.getId()).size());
        verify(gamificationPointsService, never()).grantPoints(eq(103L), eq(20), eq(
                MemberPointBizTypeEnum.INVITATION_COMPLETE.getType()), anyLong(), eq("invitation reward"));
    }

    @Test
    void grantReward_marksRewardFailedWhenGrantFails() {
        InvitationRewardDO reward = InvitationRewardDO.builder()
                .relationId(99L)
                .inviterId(300L)
                .inviteeId(400L)
                .rewardType(InvitationRewardDO.REWARD_TYPE_INVITER)
                .points(15)
                .status(InvitationRewardDO.STATUS_PENDING)
                .build();
        invitationRewardMapper.insert(reward);
        when(gamificationPointsService.grantPoints(eq(300L), eq(15),
                eq(MemberPointBizTypeEnum.INVITATION_COMPLETE.getType()), eq(reward.getId()), eq("invitation reward")))
                .thenReturn(false);

        boolean granted = invitationRewardService.grantReward(reward.getId());

        assertFalse(granted);
        InvitationRewardDO updatedReward = invitationRewardMapper.selectById(reward.getId());
        assertEquals(InvitationRewardDO.STATUS_FAILED, updatedReward.getStatus());
    }

    private InvitationRelationDO insertRelation(Long inviterId, Long inviteeId, Integer status) {
        InvitationRelationDO relation = InvitationRelationDO.builder()
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .invitationCode("REL-" + inviteeId)
                .status(status)
                .registerTime(LocalDateTime.now().minusDays(1))
                .build();
        invitationRelationMapper.insert(relation);
        return relation;
    }

    private void insertConfig(String key, String value) {
        invitationConfigMapper.insert(InvitationConfigDO.builder()
                .configKey(key)
                .configValue(value)
                .description(key)
                .build());
    }
}
