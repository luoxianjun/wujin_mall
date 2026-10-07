package cn.iocoder.yudao.module.gamification.service.invitation;

import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;

/**
 * 邀请码服务接口
 *
 * @author gamification
 */
public interface InvitationCodeService {

    /**
     * 获取用户的邀请码（如果不存在则生成）
     *
     * @param userId 用户ID
     * @return 邀请码
     */
    String getOrCreateInvitationCode(Long userId);

    /**
     * 使用邀请码注册
     *
     * @param inviteeId 被邀请人ID
     * @param invitationCode 邀请码
     * @param deviceInfo 设备信息
     * @param ip IP地址
     * @return 是否成功
     */
    boolean registerWithInvitationCode(Long inviteeId, String invitationCode, String deviceInfo, String ip);

    /**
     * 根据邀请码查询邀请码信息
     *
     * @param code 邀请码
     * @return 邀请码DO
     */
    InvitationCodeDO getInvitationCodeByCode(String code);

    /**
     * 完成邀请任务（被邀请人完成新手任务）
     *
     * @param inviteeId 被邀请人ID
     * @return 是否成功
     */
    boolean completeInvitationTask(Long inviteeId);
}
