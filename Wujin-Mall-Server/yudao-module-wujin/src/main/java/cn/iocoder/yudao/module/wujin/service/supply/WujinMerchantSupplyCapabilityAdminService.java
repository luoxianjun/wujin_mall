package cn.iocoder.yudao.module.wujin.service.supply;

import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;

import java.util.List;

public interface WujinMerchantSupplyCapabilityAdminService {

    Long createCapability(WujinMerchantSupplyCapabilitySaveReqVO createReqVO);

    void updateCapability(WujinMerchantSupplyCapabilitySaveReqVO updateReqVO);

    Long saveOrUpdateCapability(WujinMerchantSupplyCapabilitySaveReqVO saveReqVO);

    List<WujinMerchantSupplyCapabilityDO> getCapabilityList(WujinMerchantSupplyCapabilityListReqVO listReqVO);
}
