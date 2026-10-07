package cn.iocoder.yudao.module.wujin.service.product;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationItemMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.merchant.WujinMerchantRelationSubmissionMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.supply.WujinMerchantSupplyCapabilityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.template.WujinIndustryTemplateMapper;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinProductSpuStatusSyncServiceImpl implements WujinProductSpuStatusSyncService {

    private static final Integer PRODUCT_STATUS_ENABLE = 1;
    private static final Integer SUPPLY_STATUS_ENABLED = 0;
    private static final Integer SUPPLY_STATUS_DISABLED = 1;
    private static final Integer AUDIT_STATUS_AUTO_APPROVED = 20;
    private static final Integer AUDIT_STATUS_MANUAL_APPROVED = 30;

    @Resource
    private WujinMerchantSupplyCapabilityMapper capabilityMapper;
    @Resource
    private WujinMerchantRelationSubmissionMapper submissionMapper;
    @Resource
    private WujinMerchantRelationItemMapper itemMapper;
    @Resource
    private WujinChainEntityMapper entityMapper;
    @Resource
    private WujinIndustryTemplateMapper templateMapper;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncProductStatus(Long productId, String productName, Integer productStatus) {
        if (productId == null || productStatus == null) {
            return 0;
        }
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityMapper.selectListByProductId(productId);
        if (capabilities.isEmpty() && PRODUCT_STATUS_ENABLE.equals(productStatus)) {
            return rebuildCapabilitiesFromApprovedSubmissions(productId);
        }
        if (capabilities.isEmpty()) {
            return 0;
        }
        Integer supplyStatus = PRODUCT_STATUS_ENABLE.equals(productStatus)
                ? SUPPLY_STATUS_ENABLED : SUPPLY_STATUS_DISABLED;
        return capabilityMapper.updateSupplyStatusByProductId(productId, supplyStatus);
    }

    private int rebuildCapabilitiesFromApprovedSubmissions(Long productId) {
        List<WujinMerchantRelationSubmissionDO> submissions = submissionMapper.selectApprovedListByProductId(productId,
                AUDIT_STATUS_AUTO_APPROVED, AUDIT_STATUS_MANUAL_APPROVED);
        int count = 0;
        for (WujinMerchantRelationSubmissionDO submission : submissions) {
            WujinIndustryTemplateDO template = templateMapper.selectById(submission.getTemplateId());
            List<WujinMerchantRelationItemDO> items = itemMapper.selectListBySubmissionId(submission.getId());
            for (WujinMerchantRelationItemDO item : items) {
                if (!canCreateSupplyCapability(item.getRelationType())) {
                    continue;
                }
                WujinChainEntityDO entity = entityMapper.selectById(item.getEntityId());
                if (entity == null) {
                    continue;
                }
                capabilityService.saveOrUpdateCapability(capabilityReq(submission, template, entity));
                count++;
            }
        }
        return count;
    }

    private boolean canCreateSupplyCapability(String relationType) {
        return WujinRelationType.REQUIRES_MATERIAL.name().equals(relationType)
                || WujinRelationType.REQUIRES_PROCESS.name().equals(relationType)
                || WujinRelationType.REQUIRES_EQUIPMENT.name().equals(relationType);
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(WujinMerchantRelationSubmissionDO submission,
                                                                 WujinIndustryTemplateDO template,
                                                                 WujinChainEntityDO entity) {
        WujinMerchantSupplyCapabilitySaveReqVO saveReqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        saveReqVO.setMerchantId(submission.getMerchantId());
        saveReqVO.setProductId(submission.getProductId());
        saveReqVO.setProductName(submission.getProductName());
        saveReqVO.setEntityId(entity.getId());
        saveReqVO.setLane(entity.getLane());
        saveReqVO.setIndustry(template == null ? null : template.getIndustryCode());
        saveReqVO.setSupplyStatus(SUPPLY_STATUS_ENABLED);
        saveReqVO.setRemark("SPU上架自动恢复供应能力：" + entity.getName());
        return saveReqVO;
    }
}
