package cn.iocoder.yudao.module.forum.dal.mysql.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.config.ForumConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 论坛配置 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumConfigMapper extends BaseMapperX<ForumConfigDO> {

    /**
     * 根据配置键查询配置
     */
    default ForumConfigDO selectByKey(String configKey) {
        return selectOne(new LambdaQueryWrapperX<ForumConfigDO>()
                .eq(ForumConfigDO::getConfigKey, configKey));
    }

    /**
     * 查询所有配置
     */
    default List<ForumConfigDO> selectAll() {
        return selectList(new LambdaQueryWrapperX<ForumConfigDO>()
                .orderByAsc(ForumConfigDO::getId));
    }

}
