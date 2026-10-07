package cn.iocoder.yudao.module.wujin.service.supply;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.supply.WujinMerchantSupplyCapabilityMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinMerchantSupplyCapabilityAdminServiceImpl implements WujinMerchantSupplyCapabilityAdminService {

    @Resource
    private WujinMerchantSupplyCapabilityMapper capabilityMapper;
    @Resource
    private WujinChainEntityMapper entityMapper;

    @Override
    public Long createCapability(WujinMerchantSupplyCapabilitySaveReqVO createReqVO) {
        validateCapability(createReqVO);
        WujinMerchantSupplyCapabilityDO capability = BeanUtils.toBean(createReqVO, WujinMerchantSupplyCapabilityDO.class);
        capabilityMapper.insert(capability);
        return capability.getId();
    }

    @Override
    public void updateCapability(WujinMerchantSupplyCapabilitySaveReqVO updateReqVO) {
        validateCapabilityExists(updateReqVO.getId());
        validateCapability(updateReqVO);
        WujinMerchantSupplyCapabilityDO capability = BeanUtils.toBean(updateReqVO, WujinMerchantSupplyCapabilityDO.class);
        capabilityMapper.updateById(capability);
    }

    @Override
    public Long saveOrUpdateCapability(WujinMerchantSupplyCapabilitySaveReqVO saveReqVO) {
        validateCapability(saveReqVO);
        WujinMerchantSupplyCapabilityDO exists = capabilityMapper.selectByMerchantProductAndEntity(
                saveReqVO.getMerchantId(), saveReqVO.getProductId(), saveReqVO.getEntityId());
        WujinMerchantSupplyCapabilityDO capability = BeanUtils.toBean(saveReqVO, WujinMerchantSupplyCapabilityDO.class);
        if (exists == null) {
            capabilityMapper.insert(capability);
            return capability.getId();
        }
        capability.setId(exists.getId());
        capabilityMapper.updateById(capability);
        return exists.getId();
    }

    @Override
    public List<WujinMerchantSupplyCapabilityDO> getCapabilityList(WujinMerchantSupplyCapabilityListReqVO listReqVO) {
        return capabilityMapper.selectList(listReqVO);
    }

    private void validateCapabilityExists(Long id) {
        if (id == null || capabilityMapper.selectById(id) == null) {
            throw new IllegalArgumentException("供应能力不存在");
        }
    }

    private void validateCapability(WujinMerchantSupplyCapabilitySaveReqVO reqVO) {
        if (entityMapper.selectById(reqVO.getEntityId()) == null) {
            throw new IllegalArgumentException("供应能力关联实体不存在");
        }
    }
}
