package cn.iocoder.yudao.module.gamification.dal.mapper;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailPageReqVO;
import cn.iocoder.yudao.module.gamification.dal.dataobject.InvitationRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请记录 Mapper
 */
@Mapper
public interface InvitationRecordMapper extends BaseMapperX<InvitationRecordDO> {

    default PageResult<InvitationRecordDO> selectPage(InvitationDetailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InvitationRecordDO>()
                .eqIfPresent(InvitationRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(InvitationRecordDO::getInviteTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .orderByDesc(InvitationRecordDO::getId));
    }

}
