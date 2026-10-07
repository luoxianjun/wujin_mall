package cn.iocoder.yudao.module.wujin.dal.mysql.merchant;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinMerchantRelationItemMapper extends BaseMapperX<WujinMerchantRelationItemDO> {

    default List<WujinMerchantRelationItemDO> selectList(WujinMerchantRelationItemListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantRelationItemDO>()
                .eqIfPresent(WujinMerchantRelationItemDO::getSubmissionId, reqVO.getSubmissionId())
                .eqIfPresent(WujinMerchantRelationItemDO::getEntityId, reqVO.getEntityId())
                .eqIfPresent(WujinMerchantRelationItemDO::getRelationType, reqVO.getRelationType())
                .eqIfPresent(WujinMerchantRelationItemDO::getFromTemplate, reqVO.getFromTemplate())
                .eqIfPresent(WujinMerchantRelationItemDO::getRequiredFlag, reqVO.getRequiredFlag())
                .orderByDesc(WujinMerchantRelationItemDO::getId));
    }

    default List<WujinMerchantRelationItemDO> selectListBySubmissionId(Long submissionId) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantRelationItemDO>()
                .eq(WujinMerchantRelationItemDO::getSubmissionId, submissionId)
                .orderByDesc(WujinMerchantRelationItemDO::getId));
    }
}
