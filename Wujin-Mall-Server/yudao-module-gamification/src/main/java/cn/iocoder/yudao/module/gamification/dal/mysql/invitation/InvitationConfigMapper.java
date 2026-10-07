package cn.iocoder.yudao.module.gamification.dal.mysql.invitation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.gamification.dal.dataobject.invitation.InvitationConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邀请配置 Mapper
 *
 * @author gamification
 */
@Mapper
public interface InvitationConfigMapper extends BaseMapperX<InvitationConfigDO> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置DO
     */
    default InvitationConfigDO selectByConfigKey(String configKey) {
        return selectOne(InvitationConfigDO::getConfigKey, configKey);
    }
}
