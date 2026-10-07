package cn.iocoder.yudao.module.wujin.dal.mysql.chain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinChainEntityMapper extends BaseMapperX<WujinChainEntityDO> {

    default List<WujinChainEntityDO> selectList(WujinChainEntityListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinChainEntityDO>()
                .eqIfPresent(WujinChainEntityDO::getEntityCode, reqVO.getEntityCode())
                .likeIfPresent(WujinChainEntityDO::getName, reqVO.getName())
                .eqIfPresent(WujinChainEntityDO::getLane, reqVO.getLane())
                .likeIfPresent(WujinChainEntityDO::getIndustries, reqVO.getIndustry())
                .eqIfPresent(WujinChainEntityDO::getJunctionFlag, reqVO.getJunctionFlag())
                .eqIfPresent(WujinChainEntityDO::getStatus, reqVO.getStatus())
                .orderByDesc(WujinChainEntityDO::getId));
    }
}
