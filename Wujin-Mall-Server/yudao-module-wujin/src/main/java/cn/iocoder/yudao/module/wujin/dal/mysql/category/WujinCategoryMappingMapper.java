package cn.iocoder.yudao.module.wujin.dal.mysql.category;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinCategoryMappingMapper extends BaseMapperX<WujinCategoryMappingDO> {

    default List<WujinCategoryMappingDO> selectList(WujinCategoryMappingListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinCategoryMappingDO>()
                .eqIfPresent(WujinCategoryMappingDO::getSourceCategoryId, reqVO.getSourceCategoryId())
                .eqIfPresent(WujinCategoryMappingDO::getSourceLane, reqVO.getSourceLane())
                .eqIfPresent(WujinCategoryMappingDO::getTargetCategoryId, reqVO.getTargetCategoryId())
                .eqIfPresent(WujinCategoryMappingDO::getTargetLane, reqVO.getTargetLane())
                .eqIfPresent(WujinCategoryMappingDO::getMappingType, reqVO.getMappingType())
                .eqIfPresent(WujinCategoryMappingDO::getStatus, reqVO.getStatus())
                .orderByDesc(WujinCategoryMappingDO::getId));
    }
}
