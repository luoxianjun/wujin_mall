package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationStatisticsRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRewardDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRewardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class InvitationStatisticsServiceImpl implements InvitationStatisticsService {

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @Resource
    private InvitationRewardMapper invitationRewardMapper;

    @Override
    public InvitationStatisticsRespVO getStatistics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long totalInvitations = invitationRelationMapper.selectCount();
        long successfulInvitations = invitationRelationMapper.selectCount(
                new LambdaQueryWrapperX<InvitationRelationDO>()
                        .eq(InvitationRelationDO::getStatus, InvitationRelationDO.STATUS_COMPLETED));
        long pendingInvitations = invitationRelationMapper.selectCount(
                new LambdaQueryWrapperX<InvitationRelationDO>()
                        .eq(InvitationRelationDO::getStatus, InvitationRelationDO.STATUS_PENDING));
        long failedInvitations = invitationRelationMapper.selectCount(
                new LambdaQueryWrapperX<InvitationRelationDO>()
                        .eq(InvitationRelationDO::getStatus, InvitationRelationDO.STATUS_INVALID));
        long todayInvitations = invitationRelationMapper.selectCount(
                new LambdaQueryWrapperX<InvitationRelationDO>()
                        .betweenIfPresent(InvitationRelationDO::getRegisterTime, todayStart, todayEnd));

        List<InvitationRewardDO> successfulRewards = invitationRewardMapper.selectList(
                new LambdaQueryWrapperX<InvitationRewardDO>()
                        .eq(InvitationRewardDO::getStatus, InvitationRewardDO.STATUS_SUCCESS));
        long totalRewardPoints = successfulRewards.stream()
                .mapToLong(reward -> reward.getPoints() == null ? 0L : reward.getPoints())
                .sum();
        long todayRewardPoints = successfulRewards.stream()
                .filter(reward -> reward.getGrantTime() != null)
                .filter(reward -> !reward.getGrantTime().isBefore(todayStart)
                        && !reward.getGrantTime().isAfter(todayEnd))
                .mapToLong(reward -> reward.getPoints() == null ? 0L : reward.getPoints())
                .sum();

        List<InvitationRelationDO> allRelations = invitationRelationMapper.selectList();
        long activeInviters = allRelations.stream()
                .map(InvitationRelationDO::getInviterId)
                .filter(inviterId -> inviterId != null)
                .distinct()
                .count();

        InvitationStatisticsRespVO respVO = new InvitationStatisticsRespVO();
        respVO.setTotalInvitations(totalInvitations);
        respVO.setSuccessfulInvitations(successfulInvitations);
        respVO.setPendingInvitations(pendingInvitations);
        respVO.setFailedInvitations(failedInvitations);
        respVO.setTodayInvitations(todayInvitations);
        respVO.setTodayRewardPoints(todayRewardPoints);
        respVO.setTotalRewardPoints(totalRewardPoints);
        respVO.setActiveInviters(activeInviters);
        respVO.setSuccessRate(calculateSuccessRate(successfulInvitations, totalInvitations));
        return respVO;
    }

    private Double calculateSuccessRate(long successfulInvitations, long totalInvitations) {
        if (totalInvitations <= 0) {
            return 0D;
        }
        double ratio = successfulInvitations * 100D / totalInvitations;
        return Math.round(ratio * 100D) / 100D;
    }
}
