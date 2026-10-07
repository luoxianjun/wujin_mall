package cn.iocoder.yudao.module.wujin.dal.mysql.chain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinChainEntityRelationMapper extends BaseMapperX<WujinChainEntityRelationDO> {

    default List<WujinChainEntityRelationDO> selectList(WujinChainEntityRelationListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinChainEntityRelationDO>()
                .eqIfPresent(WujinChainEntityRelationDO::getSourceEntityId, reqVO.getSourceEntityId())
                .eqIfPresent(WujinChainEntityRelationDO::getTargetEntityId, reqVO.getTargetEntityId())
                .eqIfPresent(WujinChainEntityRelationDO::getRelationType, reqVO.getRelationType())
                .eqIfPresent(WujinChainEntityRelationDO::getIndustryContext, reqVO.getIndustryContext())
                .eqIfPresent(WujinChainEntityRelationDO::getAuditStatus, reqVO.getAuditStatus())
                .orderByDesc(WujinChainEntityRelationDO::getId));
    }
}
