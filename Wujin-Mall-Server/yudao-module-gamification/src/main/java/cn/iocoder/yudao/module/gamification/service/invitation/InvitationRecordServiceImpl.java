package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.api.user.ForumUserProfileApi;
import cn.iocoder.yudao.module.forum.api.user.dto.ForumUserProfileDTO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailRespVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRewardDO;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRelationMapper;
import cn.iocoder.yudao.module.gamification.dal.mysql.invitation.InvitationRewardMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvitationRecordServiceImpl implements InvitationRecordService {

    @Resource
    private InvitationRelationMapper invitationRelationMapper;

    @Resource
    private InvitationRewardMapper invitationRewardMapper;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private ForumUserProfileApi forumUserProfileApi;

    @Override
    public PageResult<InvitationDetailRespVO> getInvitationDetailPage(InvitationDetailPageReqVO pageReqVO) {
        Set<Long> inviterIds = resolveUserIds(pageReqVO.getInviterNickname());
        if (StringUtils.hasText(pageReqVO.getInviterNickname()) && inviterIds.isEmpty()) {
            return PageResult.empty(0L);
        }

        Set<Long> inviteeIds = resolveUserIds(pageReqVO.getInviteeNickname());
        if (StringUtils.hasText(pageReqVO.getInviteeNickname()) && inviteeIds.isEmpty()) {
            return PageResult.empty(0L);
        }

        PageResult<InvitationRelationDO> pageResult = invitationRelationMapper.selectPage(
                pageReqVO,
                new LambdaQueryWrapperX<InvitationRelationDO>()
                        .inIfPresent(InvitationRelationDO::getInviterId, inviterIds)
                        .inIfPresent(InvitationRelationDO::getInviteeId, inviteeIds)
                        .eqIfPresent(InvitationRelationDO::getStatus, pageReqVO.getStatus())
                        .betweenIfPresent(
                                InvitationRelationDO::getRegisterTime,
                                pageReqVO.getBeginTime(),
                                pageReqVO.getEndTime())
                        .orderByDesc(InvitationRelationDO::getRegisterTime));
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }

        Set<Long> userIds = pageResult.getList().stream()
                .flatMap(relation -> java.util.stream.Stream.of(relation.getInviterId(), relation.getInviteeId()))
                .collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        Set<Long> relationIds = pageResult.getList().stream()
                .map(InvitationRelationDO::getId)
                .collect(Collectors.toSet());
        Map<Long, List<InvitationRewardDO>> rewardMap = invitationRewardMapper
                .selectList(InvitationRewardDO::getRelationId, relationIds)
                .stream()
                .collect(Collectors.groupingBy(InvitationRewardDO::getRelationId));

        List<InvitationDetailRespVO> voList = pageResult.getList().stream()
                .map(relation -> buildDetailVO(relation, userMap, rewardMap.getOrDefault(relation.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public PageResult<AppInvitationRecordRespVO> getMyInvitationRecordPage(Long inviterId, AppInvitationRecordPageReqVO pageReqVO) {
        PageResult<InvitationRelationDO> pageResult = invitationRelationMapper.selectPageByInviterId(pageReqVO, inviterId);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult.getTotal());
        }

        Set<Long> inviteeIds = pageResult.getList().stream()
                .map(InvitationRelationDO::getInviteeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> memberUserMap = inviteeIds.isEmpty()
                ? Collections.emptyMap()
                : memberUserApi.getUserMap(inviteeIds);
        Map<Long, ForumUserProfileDTO> forumProfileMap = inviteeIds.isEmpty()
                ? Collections.emptyMap()
                : forumUserProfileApi.getUserProfileMapByUserIds(inviteeIds);
        final Map<Long, MemberUserRespDTO> finalMemberUserMap = memberUserMap != null ? memberUserMap : Collections.emptyMap();
        final Map<Long, ForumUserProfileDTO> finalForumProfileMap = forumProfileMap != null ? forumProfileMap : Collections.emptyMap();

        List<AppInvitationRecordRespVO> voList = pageResult.getList().stream()
                .map(relation -> {
                    Long inviteeId = relation.getInviteeId();
                    MemberUserRespDTO memberUser = inviteeId != null ? finalMemberUserMap.get(inviteeId) : null;
                    ForumUserProfileDTO forumProfile = inviteeId != null ? finalForumProfileMap.get(inviteeId) : null;
                    return buildAppRecordVO(relation, memberUser, forumProfile);
                })
                .collect(Collectors.toList());
        return new PageResult<>(voList, pageResult.getTotal());
    }

    private InvitationDetailRespVO buildDetailVO(
            InvitationRelationDO relation,
            Map<Long, MemberUserRespDTO> userMap,
            List<InvitationRewardDO> rewards) {
        InvitationDetailRespVO vo = new InvitationDetailRespVO();
        vo.setId(relation.getId());
        vo.setInviterId(relation.getInviterId());
        vo.setInviteeId(relation.getInviteeId());
        vo.setInvitationCode(relation.getInvitationCode());
        vo.setStatus(relation.getStatus());
        vo.setRegisterTime(relation.getRegisterTime());
        vo.setVerifiedTime(relation.getCompleteTime());

        MemberUserRespDTO inviter = userMap.get(relation.getInviterId());
        vo.setInviterNickname(inviter != null ? inviter.getNickname() : null);

        MemberUserRespDTO invitee = userMap.get(relation.getInviteeId());
        vo.setInviteeNickname(invitee != null ? invitee.getNickname() : null);

        vo.setInviterRewardPoints(sumRewardPoints(rewards, InvitationRewardDO.REWARD_TYPE_INVITER));
        vo.setInviteeRewardPoints(sumRewardPoints(rewards, InvitationRewardDO.REWARD_TYPE_INVITEE));
        return vo;
    }

    private Integer sumRewardPoints(List<InvitationRewardDO> rewards, String rewardType) {
        return rewards.stream()
                .filter(reward -> reward.matchesRewardType(rewardType))
                .map(InvitationRewardDO::getPoints)
                .filter(points -> points != null)
                .reduce(0, Integer::sum);
    }

    private AppInvitationRecordRespVO buildAppRecordVO(
            InvitationRelationDO relation,
            MemberUserRespDTO memberUser,
            ForumUserProfileDTO forumProfile) {
        AppInvitationRecordRespVO vo = new AppInvitationRecordRespVO();
        vo.setId(relation.getId());
        vo.setInviteeId(relation.getInviteeId());
        vo.setInviteeNickname(resolveInviteeNickname(memberUser, forumProfile));
        vo.setRegisterTime(relation.getRegisterTime());
        vo.setVerified(forumProfile != null && Boolean.TRUE.equals(forumProfile.getSchoolEmailVerified()));
        vo.setCompleted(InvitationRelationDO.STATUS_COMPLETED.equals(relation.getStatus()));
        vo.setStatus(relation.getStatus());
        return vo;
    }

    private String resolveInviteeNickname(MemberUserRespDTO memberUser, ForumUserProfileDTO forumProfile) {
        String forumNickname = forumProfile != null ? forumProfile.getNickname() : null;
        String memberNickname = memberUser != null ? memberUser.getNickname() : null;
        return StrUtil.blankToDefault(StrUtil.trim(forumNickname),
                StrUtil.blankToDefault(StrUtil.trim(memberNickname), "好友用户"));
    }

    private Set<Long> resolveUserIds(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return Collections.emptySet();
        }
        return memberUserApi.getUserListByNickname(nickname).stream()
                .map(MemberUserRespDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }
}
