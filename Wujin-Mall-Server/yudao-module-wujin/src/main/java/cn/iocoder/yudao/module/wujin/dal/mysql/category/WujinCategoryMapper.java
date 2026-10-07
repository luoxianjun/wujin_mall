package cn.iocoder.yudao.module.wujin.dal.mysql.category;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinCategoryMapper extends BaseMapperX<WujinCategoryDO> {

    default List<WujinCategoryDO> selectList(WujinCategoryListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinCategoryDO>()
                .likeIfPresent(WujinCategoryDO::getName, reqVO.getName())
                .eqIfPresent(WujinCategoryDO::getCode, reqVO.getCode())
                .eqIfPresent(WujinCategoryDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinCategoryDO::getParentId, reqVO.getParentId())
                .inIfPresent(WujinCategoryDO::getParentId, reqVO.getParentIds())
                .eqIfPresent(WujinCategoryDO::getStatus, reqVO.getStatus())
                .eqIfPresent(WujinCategoryDO::getHealthStatus, reqVO.getHealthStatus())
                .orderByAsc(WujinCategoryDO::getSort)
                .orderByDesc(WujinCategoryDO::getId));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(WujinCategoryDO::getParentId, parentId);
    }
}
