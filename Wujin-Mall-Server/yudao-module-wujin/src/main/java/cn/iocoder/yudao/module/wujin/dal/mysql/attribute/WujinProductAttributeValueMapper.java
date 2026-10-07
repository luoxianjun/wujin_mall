package cn.iocoder.yudao.module.wujin.dal.mysql.attribute;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinProductAttributeValueMapper extends BaseMapperX<WujinProductAttributeValueDO> {

    default List<WujinProductAttributeValueDO> selectList(Long submissionId, Long productId) {
        return selectList(new LambdaQueryWrapperX<WujinProductAttributeValueDO>()
                .eqIfPresent(WujinProductAttributeValueDO::getSubmissionId, submissionId)
                .eqIfPresent(WujinProductAttributeValueDO::getProductId, productId)
                .orderByAsc(WujinProductAttributeValueDO::getSort)
                .orderByAsc(WujinProductAttributeValueDO::getId));
    }

    default int deleteBySubmissionId(Long submissionId) {
        return delete(WujinProductAttributeValueDO::getSubmissionId, submissionId);
    }
}
