package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.sourcing.WujinSourcingLeadMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Validated
public class WujinAppSourcingServiceImpl implements WujinAppSourcingService {

    private static final int ENABLED_STATUS = 0;
    private static final int AUDIT_STATUS_APPROVED = 30;
    private static final int SUPPLY_STATUS_ACTIVE = 0;
    private static final String LEAD_STATUS_SUBMITTED = "SUBMITTED";
    private static final String DISPATCH_STATUS_PENDING = "PENDING";

    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;
    @Resource
    private WujinSourcingLeadMapper sourcingLeadMapper;

    @Override
    public List<WujinSupplierCandidateRespVO> getSupplierCandidates(WujinSupplierCandidateReqVO reqVO) {
        WujinLane lane = parseLane(reqVO.getLane(), WujinLane.MATERIAL);
        List<WujinChainEntityDO> entities = searchEnabledEntities(reqVO, lane);
        List<WujinSupplierCandidateRespVO> candidates = new ArrayList<>();
        for (WujinChainEntityDO entity : entities) {
            candidates.addAll(buildSupplyCapabilityCandidates(reqVO, lane, entity));
            candidates.addAll(buildMerchantCandidates(reqVO, lane, entity));
            candidates.add(buildCandidate(reqVO, lane, entity));
        }
        Collections.sort(candidates, (left, right) -> right.getMatchScore().compareTo(left.getMatchScore()));
        return candidates;
    }

    @Override
    public WujinSourcingLeadSubmitRespVO submitLead(WujinSourcingLeadSubmitReqVO reqVO) {
        validateLead(reqVO);

        WujinSourcingLeadDO lead = new WujinSourcingLeadDO();
        lead.setUserId(reqVO.getUserId());
        lead.setKeyword(reqVO.getKeyword());
        lead.setLane(parseLane(reqVO.getLane(), WujinLane.MATERIAL).name());
        lead.setSourceKeyword(reqVO.getSourceKeyword());
        lead.setIndustry(reqVO.getIndustry());
        lead.setSupplierId(reqVO.getSupplierId());
        lead.setSupplierName(reqVO.getSupplierName());
        lead.setContactName(reqVO.getContactName());
        lead.setContactPhone(reqVO.getContactPhone());
        lead.setRequirement(reqVO.getRequirement());
        lead.setLeadStatus(LEAD_STATUS_SUBMITTED);
        lead.setDispatchStatus(DISPATCH_STATUS_PENDING);
        sourcingLeadMapper.insert(lead);

        WujinSourcingLeadSubmitRespVO respVO = new WujinSourcingLeadSubmitRespVO();
        respVO.setLeadId(lead.getId());
        respVO.setLeadStatus(LEAD_STATUS_SUBMITTED);
        respVO.setMessage("寻源线索已提交，平台将尽快分发给匹配商家");
        return respVO;
    }

    private List<WujinChainEntityDO> searchEnabledEntities(WujinSupplierCandidateReqVO reqVO, WujinLane lane) {
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setLane(lane.name());
        listReqVO.setIndustry(reqVO.getIndustry());
        listReqVO.setStatus(ENABLED_STATUS);
        List<WujinChainEntityDO> entities = chainEntityService.getEntityList(listReqVO);
        if (!entities.isEmpty()) {
            return entities;
        }
        WujinChainEntityListReqVO keywordReqVO = new WujinChainEntityListReqVO();
        keywordReqVO.setLane(lane.name());
        keywordReqVO.setName(reqVO.getKeyword());
        keywordReqVO.setStatus(ENABLED_STATUS);
        return chainEntityService.getEntityList(keywordReqVO);
    }

    private List<WujinSupplierCandidateRespVO> buildSupplyCapabilityCandidates(WujinSupplierCandidateReqVO reqVO,
                                                                               WujinLane lane,
                                                                               WujinChainEntityDO entity) {
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setEntityId(entity.getId());
        listReqVO.setLane(lane.name());
        listReqVO.setSupplyStatus(SUPPLY_STATUS_ACTIVE);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(listReqVO);
        if (capabilities.isEmpty()) {
            return Collections.emptyList();
        }

        List<WujinSupplierCandidateRespVO> candidates = new ArrayList<>();
        for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
            candidates.add(buildSupplyCapabilityCandidate(reqVO, lane, entity, capability));
        }
        return candidates;
    }

    private WujinSupplierCandidateRespVO buildSupplyCapabilityCandidate(WujinSupplierCandidateReqVO reqVO,
                                                                       WujinLane lane,
                                                                       WujinChainEntityDO entity,
                                                                       WujinMerchantSupplyCapabilityDO capability) {
        WujinSupplierCandidateRespVO candidate = new WujinSupplierCandidateRespVO();
        candidate.setId(capability.getMerchantId());
        candidate.setEntityId(entity.getId());
        candidate.setEntityName(entity.getName());
        candidate.setLane(lane.name());
        candidate.setSupplierName("商家" + capability.getMerchantId() + " · " + capability.getProductName());
        candidate.setMatchScore(calculateSupplyCapabilityMatchScore(reqVO, entity, capability));
        candidate.setMainProducts(capability.getProductName() + "、" + entity.getName() + "、" + laneName(lane) + "现货供应");
        candidate.setServiceNote(buildSupplyCapabilityServiceNote(reqVO, lane, entity, capability));
        return candidate;
    }

    private int calculateSupplyCapabilityMatchScore(WujinSupplierCandidateReqVO reqVO, WujinChainEntityDO entity,
                                                    WujinMerchantSupplyCapabilityDO capability) {
        int score = 92;
        if (contains(entity.getName(), reqVO.getKeyword()) || contains(capability.getProductName(), reqVO.getKeyword())) {
            score += 4;
        }
        if (contains(capability.getIndustry(), reqVO.getIndustry())) {
            score += 2;
        }
        Integer stockCount = capability.getStockCount();
        if (stockCount != null && stockCount > 0) {
            score += 1;
        }
        return Math.min(99, score);
    }

    private String buildSupplyCapabilityServiceNote(WujinSupplierCandidateReqVO reqVO, WujinLane lane,
                                                    WujinChainEntityDO entity,
                                                    WujinMerchantSupplyCapabilityDO capability) {
        String industry = isBlank(reqVO.getIndustry()) ? "通用五金" : reqVO.getIndustry();
        String stock = capability.getStockCount() == null ? "库存待确认" : "库存" + capability.getStockCount();
        String minOrder = capability.getMinOrderQuantity() == null ? "起订量可议" : "起订量" + capability.getMinOrderQuantity();
        String delivery = capability.getDeliveryDays() == null ? "交付周期待确认" : capability.getDeliveryDays() + "天交付";
        String serviceArea = isBlank(capability.getServiceArea()) ? "服务区域待确认" : "服务" + capability.getServiceArea();
        return "来自供应能力索引，商家商品“" + capability.getProductName() + "”绑定“" + entity.getName()
                + "”，" + stock + "，" + minOrder + "，" + delivery + "，" + serviceArea
                + "，可围绕" + industry + "行业提供" + laneName(lane) + "寻源、样品确认和批量报价。";
    }

    private List<WujinSupplierCandidateRespVO> buildMerchantCandidates(WujinSupplierCandidateReqVO reqVO, WujinLane lane,
                                                                       WujinChainEntityDO entity) {
        WujinMerchantRelationItemListReqVO itemListReqVO = new WujinMerchantRelationItemListReqVO();
        itemListReqVO.setEntityId(entity.getId());
        List<WujinMerchantRelationItemDO> relationItems = itemService.getItemList(itemListReqVO);
        if (relationItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<WujinSupplierCandidateRespVO> candidates = new ArrayList<>();
        for (WujinMerchantRelationItemDO item : relationItems) {
            WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(item.getSubmissionId());
            if (!isApprovedSubmission(submission)) {
                continue;
            }
            candidates.add(buildMerchantCandidate(reqVO, lane, entity, item, submission));
        }
        return candidates;
    }

    private boolean isApprovedSubmission(WujinMerchantRelationSubmissionDO submission) {
        return submission != null && Integer.valueOf(AUDIT_STATUS_APPROVED).equals(submission.getAuditStatus());
    }

    private WujinSupplierCandidateRespVO buildMerchantCandidate(WujinSupplierCandidateReqVO reqVO, WujinLane lane,
                                                               WujinChainEntityDO entity,
                                                               WujinMerchantRelationItemDO item,
                                                               WujinMerchantRelationSubmissionDO submission) {
        WujinSupplierCandidateRespVO candidate = new WujinSupplierCandidateRespVO();
        candidate.setId(submission.getMerchantId());
        candidate.setEntityId(entity.getId());
        candidate.setEntityName(entity.getName());
        candidate.setLane(lane.name());
        candidate.setSupplierName("商家" + submission.getMerchantId() + " · " + submission.getProductName());
        candidate.setMatchScore(calculateMerchantMatchScore(reqVO, entity, item, submission));
        candidate.setMainProducts(submission.getProductName() + "、" + entity.getName() + "、" + laneName(lane) + "供应");
        candidate.setServiceNote(buildMerchantServiceNote(reqVO, lane, entity, item, submission));
        return candidate;
    }

    private int calculateMerchantMatchScore(WujinSupplierCandidateReqVO reqVO, WujinChainEntityDO entity,
                                            WujinMerchantRelationItemDO item,
                                            WujinMerchantRelationSubmissionDO submission) {
        int score = 86;
        if (contains(entity.getName(), reqVO.getKeyword())) {
            score += 6;
        }
        if (Boolean.TRUE.equals(item.getRequiredFlag())) {
            score += 4;
        }
        Integer completenessScore = submission.getCompletenessScore();
        if (completenessScore != null) {
            score += Math.min(7, Math.max(0, completenessScore - 80) / 3);
        }
        return Math.min(99, score);
    }

    private String buildMerchantServiceNote(WujinSupplierCandidateReqVO reqVO, WujinLane lane,
                                            WujinChainEntityDO entity,
                                            WujinMerchantRelationItemDO item,
                                            WujinMerchantRelationSubmissionDO submission) {
        String industry = isBlank(reqVO.getIndustry()) ? "通用五金" : reqVO.getIndustry();
        String requiredNote = Boolean.TRUE.equals(item.getRequiredFlag()) ? "核心必需关系" : "可选关系";
        return "已通过关系申报，商家商品“" + submission.getProductName() + "”绑定“" + entity.getName()
                + "”" + requiredNote + "，可围绕" + industry + "行业提供" + laneName(lane)
                + "寻源、样品确认和批量报价。";
    }

    private WujinSupplierCandidateRespVO buildCandidate(WujinSupplierCandidateReqVO reqVO, WujinLane lane,
                                                        WujinChainEntityDO entity) {
        WujinSupplierCandidateRespVO candidate = new WujinSupplierCandidateRespVO();
        candidate.setId(entity.getId());
        candidate.setEntityId(entity.getId());
        candidate.setEntityName(entity.getName());
        candidate.setLane(lane.name());
        candidate.setSupplierName(entity.getName() + "供应协作商");
        candidate.setMatchScore(calculateMatchScore(reqVO, entity));
        candidate.setMainProducts(entity.getName() + "、" + laneName(lane) + "寻源、规格报价");
        candidate.setServiceNote(buildServiceNote(reqVO, lane, entity));
        return candidate;
    }

    private int calculateMatchScore(WujinSupplierCandidateReqVO reqVO, WujinChainEntityDO entity) {
        int score = 70;
        if (contains(entity.getName(), reqVO.getKeyword())) {
            score += 20;
        }
        if (contains(entity.getIndustries(), reqVO.getIndustry())) {
            score += 8;
        }
        if (Boolean.TRUE.equals(entity.getJunctionFlag())) {
            score += 2;
        }
        return Math.min(99, score);
    }

    private String buildServiceNote(WujinSupplierCandidateReqVO reqVO, WujinLane lane, WujinChainEntityDO entity) {
        String industry = isBlank(reqVO.getIndustry()) ? "通用五金" : reqVO.getIndustry();
        String source = isBlank(reqVO.getSourceKeyword()) ? reqVO.getKeyword() : reqVO.getSourceKeyword();
        return "适配" + industry + "行业，可围绕“" + source + "”提供" + laneName(lane)
                + "寻源、样品确认和批量报价。";
    }

    private void validateLead(WujinSourcingLeadSubmitReqVO reqVO) {
        if (isBlank(reqVO.getKeyword())) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        if (isBlank(reqVO.getContactPhone())) {
            throw new IllegalArgumentException("联系方式不能为空");
        }
        if (isBlank(reqVO.getRequirement())) {
            throw new IllegalArgumentException("需求说明不能为空");
        }
    }

    private WujinLane parseLane(String value, WujinLane defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return WujinLane.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    private String laneName(WujinLane lane) {
        if (lane == WujinLane.MATERIAL) {
            return "原材料";
        }
        if (lane == WujinLane.PROCESS) {
            return "加工";
        }
        return "成品";
    }

    private boolean contains(String text, String keyword) {
        return !isBlank(text) && !isBlank(keyword) && text.contains(keyword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
