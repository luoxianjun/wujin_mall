package cn.iocoder.yudao.module.wujin.dal.mysql.attribute;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductCustomTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinProductCustomTagMapper extends BaseMapperX<WujinProductCustomTagDO> {

    default List<WujinProductCustomTagDO> selectList(WujinProductCustomTagListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinProductCustomTagDO>()
                .eqIfPresent(WujinProductCustomTagDO::getSubmissionId, reqVO.getSubmissionId())
                .eqIfPresent(WujinProductCustomTagDO::getMerchantId, reqVO.getMerchantId())
                .eqIfPresent(WujinProductCustomTagDO::getProductId, reqVO.getProductId())
                .likeIfPresent(WujinProductCustomTagDO::getProductName, reqVO.getProductName())
                .likeIfPresent(WujinProductCustomTagDO::getTagName, reqVO.getTagName())
                .eqIfPresent(WujinProductCustomTagDO::getAuditStatus, reqVO.getAuditStatus())
                .orderByDesc(WujinProductCustomTagDO::getId));
    }

    default List<WujinProductCustomTagDO> selectListByProductAndTag(Long merchantId, Long productId, String tagName) {
        return selectList(new LambdaQueryWrapperX<WujinProductCustomTagDO>()
                .eq(WujinProductCustomTagDO::getMerchantId, merchantId)
                .eq(WujinProductCustomTagDO::getProductId, productId)
                .eq(WujinProductCustomTagDO::getTagName, tagName));
    }

    default List<WujinProductCustomTagDO> selectListByProductIdAndStatus(Long productId, Integer auditStatus) {
        return selectList(new LambdaQueryWrapperX<WujinProductCustomTagDO>()
                .eq(WujinProductCustomTagDO::getProductId, productId)
                .eq(WujinProductCustomTagDO::getAuditStatus, auditStatus)
                .orderByAsc(WujinProductCustomTagDO::getId));
    }
}
