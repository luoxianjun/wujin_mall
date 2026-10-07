package cn.iocoder.yudao.module.wujin.dal.mysql.merchant;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinMerchantRelationSubmissionMapper extends BaseMapperX<WujinMerchantRelationSubmissionDO> {

    default List<WujinMerchantRelationSubmissionDO> selectList(WujinMerchantRelationSubmissionListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantRelationSubmissionDO>()
                .eqIfPresent(WujinMerchantRelationSubmissionDO::getMerchantId, reqVO.getMerchantId())
                .eqIfPresent(WujinMerchantRelationSubmissionDO::getProductId, reqVO.getProductId())
                .likeIfPresent(WujinMerchantRelationSubmissionDO::getProductName, reqVO.getProductName())
                .eqIfPresent(WujinMerchantRelationSubmissionDO::getProductLane, reqVO.getProductLane())
                .eqIfPresent(WujinMerchantRelationSubmissionDO::getTemplateId, reqVO.getTemplateId())
                .eqIfPresent(WujinMerchantRelationSubmissionDO::getAuditStatus, reqVO.getAuditStatus())
                .orderByDesc(WujinMerchantRelationSubmissionDO::getId));
    }

    default List<WujinMerchantRelationSubmissionDO> selectApprovedListByProductId(Long productId,
                                                                                  Integer autoApprovedStatus,
                                                                                  Integer manualApprovedStatus) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantRelationSubmissionDO>()
                .eq(WujinMerchantRelationSubmissionDO::getProductId, productId)
                .in(WujinMerchantRelationSubmissionDO::getAuditStatus, autoApprovedStatus, manualApprovedStatus)
                .orderByDesc(WujinMerchantRelationSubmissionDO::getId));
    }
}
