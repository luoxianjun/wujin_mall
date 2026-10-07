package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigRespVO;
import cn.iocoder.yudao.module.gamification.controller.admin.invitation.vo.InvitationConfigUpdateReqVO;

/**
 * 邀请配置 Service 接口
 */
public interface InvitationConfigService {

    /**
     * 获取邀请配置
     *
     * @return 邀请配置
     */
    InvitationConfigRespVO getConfig();

    /**
     * 更新邀请配置
     *
     * @param updateReqVO 更新信息
     */
    void updateConfig(InvitationConfigUpdateReqVO updateReqVO);

}
