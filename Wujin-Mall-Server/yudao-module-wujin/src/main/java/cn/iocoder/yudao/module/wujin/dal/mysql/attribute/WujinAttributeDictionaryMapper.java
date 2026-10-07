package cn.iocoder.yudao.module.wujin.dal.mysql.attribute;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinAttributeDictionaryMapper extends BaseMapperX<WujinAttributeDictionaryDO> {

    default List<WujinAttributeDictionaryDO> selectList(WujinAttributeDictionaryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinAttributeDictionaryDO>()
                .likeIfPresent(WujinAttributeDictionaryDO::getCode, reqVO.getCode())
                .likeIfPresent(WujinAttributeDictionaryDO::getName, reqVO.getName())
                .eqIfPresent(WujinAttributeDictionaryDO::getGroupName, reqVO.getGroupName())
                .eqIfPresent(WujinAttributeDictionaryDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinAttributeDictionaryDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(WujinAttributeDictionaryDO::getValueType, reqVO.getValueType())
                .eqIfPresent(WujinAttributeDictionaryDO::getStatus, reqVO.getStatus())
                .orderByAsc(WujinAttributeDictionaryDO::getSort)
                .orderByAsc(WujinAttributeDictionaryDO::getId));
    }

    default WujinAttributeDictionaryDO selectByCode(String code) {
        return selectOne(WujinAttributeDictionaryDO::getCode, code);
    }

    /**
     * 获得指定泳道可用的启用属性，包含三泳道通用属性
     */
    default List<WujinAttributeDictionaryDO> selectEnabledListByLane(String lane, Integer enabledStatus) {
        LambdaQueryWrapperX<WujinAttributeDictionaryDO> query = new LambdaQueryWrapperX<>();
        query.eq(WujinAttributeDictionaryDO::getStatus, enabledStatus);
        if (lane != null && !lane.trim().isEmpty()) {
            query.and(wrapper -> wrapper.eq(WujinAttributeDictionaryDO::getLane, lane)
                    .or().isNull(WujinAttributeDictionaryDO::getLane)
                    .or().eq(WujinAttributeDictionaryDO::getLane, ""));
        }
        query.orderByAsc(WujinAttributeDictionaryDO::getSort).orderByAsc(WujinAttributeDictionaryDO::getId);
        return selectList(query);
    }
}
