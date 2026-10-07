package cn.iocoder.yudao.module.gamification.dal.mysql.invitation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRewardDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邀请奖励记录 Mapper
 *
 * @author gamification
 */
@Mapper
public interface InvitationRewardMapper extends BaseMapperX<InvitationRewardDO> {

    /**
     * 查询待发放的奖励记录
     *
     * @param status 状态
     * @param limit 限制数量
     * @return 奖励记录列表
     */
    default List<InvitationRewardDO> selectPendingRewards(@Param("status") Integer status, @Param("limit") Integer limit) {
        return selectList("status", status)
            .stream()
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询需要重试的失败记录
     *
     * @param maxRetryCount 最大重试次数
     * @param limit 限制数量
     * @return 奖励记录列表
     */
    default List<InvitationRewardDO> selectFailedRewardsForRetry(@Param("maxRetryCount") Integer maxRetryCount, @Param("limit") Integer limit) {
        return selectList(new LambdaQueryWrapper<InvitationRewardDO>()
                .eq(InvitationRewardDO::getStatus, InvitationRewardDO.STATUS_FAILED)
                .orderByAsc(InvitationRewardDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 根据邀请关系ID查询奖励记录
     *
     * @param relationId 邀请关系ID
     * @return 奖励记录列表
     */
    default List<InvitationRewardDO> selectByRelationId(@Param("relationId") Long relationId) {
        return selectList("relation_id", relationId);
    }

    /**
     * 根据用户ID查询奖励记录
     *
     * @param userId 用户ID
     * @return 奖励记录列表
     */
    default List<InvitationRewardDO> selectByUserId(@Param("userId") Long userId) {
        return selectList(new LambdaQueryWrapper<InvitationRewardDO>()
                .nested(wrapper -> wrapper
                        .nested(inviterWrapper -> inviterWrapper
                                .eq(InvitationRewardDO::getInviterId, userId)
                                .in(InvitationRewardDO::getRewardType,
                                        InvitationRewardDO.REWARD_TYPE_INVITER,
                                        "1"))
                        .or()
                        .nested(inviteeWrapper -> inviteeWrapper
                                .eq(InvitationRewardDO::getInviteeId, userId)
                                .in(InvitationRewardDO::getRewardType,
                                        InvitationRewardDO.REWARD_TYPE_INVITEE,
                                        "2"))));
    }
}
