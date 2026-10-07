package cn.iocoder.yudao.module.wujin.dal.mysql.supply;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinMerchantSupplyCapabilityMapper extends BaseMapperX<WujinMerchantSupplyCapabilityDO> {

    default List<WujinMerchantSupplyCapabilityDO> selectList(WujinMerchantSupplyCapabilityListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantSupplyCapabilityDO>()
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getMerchantId, reqVO.getMerchantId())
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getProductId, reqVO.getProductId())
                .likeIfPresent(WujinMerchantSupplyCapabilityDO::getProductName, reqVO.getProductName())
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getEntityId, reqVO.getEntityId())
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getIndustry, reqVO.getIndustry())
                .eqIfPresent(WujinMerchantSupplyCapabilityDO::getSupplyStatus, reqVO.getSupplyStatus())
                .orderByDesc(WujinMerchantSupplyCapabilityDO::getId));
    }

    default WujinMerchantSupplyCapabilityDO selectByMerchantProductAndEntity(Long merchantId, Long productId,
                                                                             Long entityId) {
        List<WujinMerchantSupplyCapabilityDO> list = selectList(new LambdaQueryWrapperX<WujinMerchantSupplyCapabilityDO>()
                .eq(WujinMerchantSupplyCapabilityDO::getMerchantId, merchantId)
                .eq(WujinMerchantSupplyCapabilityDO::getProductId, productId)
                .eq(WujinMerchantSupplyCapabilityDO::getEntityId, entityId)
                .orderByDesc(WujinMerchantSupplyCapabilityDO::getId));
        return list.isEmpty() ? null : list.get(0);
    }

    default List<WujinMerchantSupplyCapabilityDO> selectListByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<WujinMerchantSupplyCapabilityDO>()
                .eq(WujinMerchantSupplyCapabilityDO::getProductId, productId));
    }

    default int updateSupplyStatusByProductId(Long productId, Integer supplyStatus) {
        return update(null, new LambdaUpdateWrapper<WujinMerchantSupplyCapabilityDO>()
                .eq(WujinMerchantSupplyCapabilityDO::getProductId, productId)
                .set(WujinMerchantSupplyCapabilityDO::getSupplyStatus, supplyStatus));
    }

    default int updateStockCountByProductId(Long productId, Integer stockCount) {
        return update(null, new LambdaUpdateWrapper<WujinMerchantSupplyCapabilityDO>()
                .eq(WujinMerchantSupplyCapabilityDO::getProductId, productId)
                .set(WujinMerchantSupplyCapabilityDO::getStockCount, stockCount));
    }
}
