package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationStatisticsRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRewardDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRewardMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(InvitationStatisticsServiceImpl.class)
class InvitationStatisticsServiceTest extends BaseDbUnitTest {

    @Resource
    private InvitationStatisticsService invitationStatisticsService;

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @Resource
    private InvitationRewardMapper invitationRewardMapper;

    @Test
    void getStatistics_derivesTotalsFromRelationAndRewardRows() {
        InvitationRelationDO completedRelation = InvitationRelationDO.builder()
                .inviterId(1L)
                .inviteeId(11L)
                .invitationCode("A1")
                .status(InvitationRelationDO.STATUS_COMPLETED)
                .registerTime(LocalDateTime.now().minusHours(1))
                .completeTime(LocalDateTime.now().minusMinutes(30))
                .build();
        invitationRelationMapper.insert(completedRelation);
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(1L)
                .inviteeId(12L)
                .invitationCode("A2")
                .status(InvitationRelationDO.STATUS_PENDING)
                .registerTime(LocalDateTime.now().minusDays(2))
                .build());
        invitationRelationMapper.insert(InvitationRelationDO.builder()
                .inviterId(2L)
                .inviteeId(13L)
                .invitationCode("A3")
                .status(InvitationRelationDO.STATUS_INVALID)
                .registerTime(LocalDateTime.now().minusDays(3))
                .build());

        invitationRewardMapper.insert(InvitationRewardDO.builder()
                .relationId(completedRelation.getId())
                .inviterId(1L)
                .inviteeId(11L)
                .rewardType(InvitationRewardDO.REWARD_TYPE_INVITER)
                .points(20)
                .status(InvitationRewardDO.STATUS_SUCCESS)
                .grantTime(LocalDateTime.now().minusMinutes(20))
                .build());
        invitationRewardMapper.insert(InvitationRewardDO.builder()
                .relationId(completedRelation.getId())
                .inviterId(1L)
                .inviteeId(11L)
                .rewardType(InvitationRewardDO.REWARD_TYPE_INVITEE)
                .points(10)
                .status(InvitationRewardDO.STATUS_SUCCESS)
                .grantTime(LocalDateTime.now().minusMinutes(10))
                .build());

        InvitationStatisticsRespVO statistics = invitationStatisticsService.getStatistics();

        assertEquals(3L, statistics.getTotalInvitations());
        assertEquals(1L, statistics.getSuccessfulInvitations());
        assertEquals(1L, statistics.getPendingInvitations());
        assertEquals(1L, statistics.getFailedInvitations());
        assertEquals(1L, statistics.getTodayInvitations());
        assertEquals(30L, statistics.getTodayRewardPoints());
        assertEquals(30L, statistics.getTotalRewardPoints());
        assertEquals(2L, statistics.getActiveInviters());
        assertEquals(33.33D, statistics.getSuccessRate());
    }
}
