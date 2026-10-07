package cn.iocoder.yudao.module.gamification.dal.mysql.invitation;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationRelationDO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请关系 Mapper
 *
 * @author gamification
 */
@Mapper
public interface InvitationRelationMapper extends BaseMapperX<InvitationRelationDO> {

    /**
     * 根据被邀请人ID查询邀请关系
     *
     * @param inviteeId 被邀请人ID
     * @return 邀请关系DO
     */
    default InvitationRelationDO selectByInviteeId(Long inviteeId) {
        return selectOne(InvitationRelationDO::getInviteeId, inviteeId);
    }

    /**
     * 统计邀请人的邀请数量
     *
     * @param inviterId 邀请人ID
     * @return 邀请数量
     */
    default Long countByInviterId(Long inviterId) {
        return selectCount(InvitationRelationDO::getInviterId, inviterId);
    }

    /**
     * 统计邀请人的有效邀请数量
     *
     * @param inviterId 邀请人ID
     * @param status 状态
     * @return 有效邀请数量
     */
    default Long countByInviterIdAndStatus(Long inviterId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<InvitationRelationDO>()
                .eq(InvitationRelationDO::getInviterId, inviterId)
                .eq(InvitationRelationDO::getStatus, status));
    }

    /**
     * 根据邀请人分页查询邀请关系
     *
     * @param pageParam 分页参数
     * @param inviterId 邀请人 ID
     * @return 邀请关系分页
     */
    default PageResult<InvitationRelationDO> selectPageByInviterId(PageParam pageParam, Long inviterId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<InvitationRelationDO>()
                .eq(InvitationRelationDO::getInviterId, inviterId)
                .orderByDesc(InvitationRelationDO::getRegisterTime, InvitationRelationDO::getId));
    }
}
