package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.app.invitation.vo.AppInvitationRecordRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailPageReqVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationDetailRespVO;

/**
 * 邀请记录 Service 接口
 */
public interface InvitationRecordService {

    /**
     * 获取邀请明细分页
     *
     * @param pageReqVO 分页查询
     * @return 邀请明细分页
     */
    PageResult<InvitationDetailRespVO> getInvitationDetailPage(InvitationDetailPageReqVO pageReqVO);

    /**
     * 获取当前邀请人的邀请记录分页
     *
     * @param inviterId 当前邀请人用户 ID
     * @param pageReqVO 分页查询
     * @return 邀请记录分页
     */
    PageResult<AppInvitationRecordRespVO> getMyInvitationRecordPage(Long inviterId, AppInvitationRecordPageReqVO pageReqVO);

}
