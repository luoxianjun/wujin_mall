package cn.iocoder.yudao.module.gamification.dal.mysql.invitation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationCodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请码 Mapper
 *
 * @author gamification
 */
@Mapper
public interface InvitationCodeMapper extends BaseMapperX<InvitationCodeDO> {

    /**
     * 根据用户ID查询邀请码
     *
     * @param userId 用户ID
     * @return 邀请码DO
     */
    default InvitationCodeDO selectByUserId(Long userId) {
        return selectOne(InvitationCodeDO::getUserId, userId);
    }

    /**
     * 根据邀请码查询
     *
     * @param code 邀请码
     * @return 邀请码DO
     */
    default InvitationCodeDO selectByCode(String code) {
        return selectOne(InvitationCodeDO::getCode, code);
    }
}
