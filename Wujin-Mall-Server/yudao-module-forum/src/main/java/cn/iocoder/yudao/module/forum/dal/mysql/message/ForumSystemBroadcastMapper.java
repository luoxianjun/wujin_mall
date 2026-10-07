package cn.iocoder.yudao.module.forum.dal.mysql.message;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.forum.dal.dataobject.message.ForumSystemBroadcastDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统广播消息 Mapper
 *
 * @author forum
 */
@Mapper
public interface ForumSystemBroadcastMapper extends BaseMapperX<ForumSystemBroadcastDO> {

    /**
     * 分页查询广播消息历史
     */
    default PageResult<ForumSystemBroadcastDO> selectPage(PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ForumSystemBroadcastDO>()
                .orderByDesc(ForumSystemBroadcastDO::getCreateTime));
    }

}
