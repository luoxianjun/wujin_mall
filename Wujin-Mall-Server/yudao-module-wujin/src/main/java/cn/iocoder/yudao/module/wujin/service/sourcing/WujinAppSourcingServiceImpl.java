package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadProgressRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.sourcing.WujinSourcingLeadMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.attribute.WujinProductAttributeService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Validated
public class WujinAppSourcingServiceImpl implements WujinAppSourcingService {

    private static final int ENABLED_STATUS = 0;
    private static final int AUDIT_STATUS_APPROVED = 30;
    private static final int SUPPLY_STATUS_ACTIVE = 0;
    private static final String LEAD_STATUS_SUBMITTED = "SUBMITTED";
    private static final String DISPATCH_STATUS_PENDING = "PENDING";
    private static final String DISPATCH_STATUS_DISPATCHED = "DISPATCHED";
    private static final int MY_LEAD_LIMIT = 50;
    private static final DateTimeFormatter STEP_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    @Resource
    private WujinProductAttributeService productAttributeService;

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
        return distinctCandidates(candidates);
    }

    /**
     * 同一商家同时有供应能力和审核通过的关系申报时只保留匹配分最高的一条，避免小程序出现重复供应商卡片
     */
    private List<WujinSupplierCandidateRespVO> distinctCandidates(List<WujinSupplierCandidateRespVO> candidates) {
        Set<String> keys = new LinkedHashSet<>();
        List<WujinSupplierCandidateRespVO> distinct = new ArrayList<>();
        for (WujinSupplierCandidateRespVO candidate : candidates) {
            if (keys.add(candidate.getSupplierType() + ":" + candidate.getId() + ":" + candidate.getEntityId())) {
                distinct.add(candidate);
            }
        }
        return distinct;
    }

    @Override
    public WujinSourcingLeadSubmitRespVO submitLead(WujinSourcingLeadSubmitReqVO reqVO) {
        validateLead(reqVO);

        WujinSourcingLeadDO lead = new WujinSourcingLeadDO();
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        lead.setUserId(loginUserId != null ? loginUserId : reqVO.getUserId());
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

    @Override
    public WujinSourcingLeadProgressRespVO getLeadProgress(Long leadId, Long userId) {
        WujinSourcingLeadDO lead = leadId == null ? null : sourcingLeadMapper.selectById(leadId);
        if (lead == null) {
            throw new IllegalArgumentException("寻源线索不存在");
        }
        if (userId == null || !userId.equals(lead.getUserId())) {
            throw new IllegalArgumentException("无权查看该寻源线索");
        }
        WujinSourcingLeadProgressRespVO respVO = buildLeadSummary(lead);
        respVO.setSteps(buildLeadSteps(lead));
        return respVO;
    }

    @Override
    public List<WujinSourcingLeadProgressRespVO> getMyLeadList(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<WujinSourcingLeadProgressRespVO> list = new ArrayList<>();
        for (WujinSourcingLeadDO lead : sourcingLeadMapper.selectListByUserId(userId, MY_LEAD_LIMIT)) {
            list.add(buildLeadSummary(lead));
        }
        return list;
    }

    @Override
    public WujinSupplierCapabilityRespVO getSupplierCapability(WujinSupplierCapabilityReqVO reqVO) {
        if (SUPPLIER_TYPE_PLATFORM.equals(reqVO.getSupplierType())) {
            return buildPlatformSupplierCapability(reqVO);
        }
        WujinMerchantSupplyCapabilityListReqVO listReqVO = new WujinMerchantSupplyCapabilityListReqVO();
        listReqVO.setMerchantId(reqVO.getSupplierId());
        listReqVO.setSupplyStatus(SUPPLY_STATUS_ACTIVE);
        List<WujinMerchantSupplyCapabilityDO> capabilities = capabilityService.getCapabilityList(listReqVO);

        WujinSupplierCapabilityRespVO respVO = new WujinSupplierCapabilityRespVO();
        respVO.setSupplierId(reqVO.getSupplierId());
        respVO.setSupplierType(SUPPLIER_TYPE_MERCHANT);
        respVO.setSupplierName("商家" + reqVO.getSupplierId());
        List<WujinSupplierCapabilityRespVO.CapabilityItem> items = new ArrayList<>();
        Set<String> mainCapabilities = new LinkedHashSet<>();
        Set<String> approvedTags = new LinkedHashSet<>();
        Set<Long> productIds = new LinkedHashSet<>();
        boolean keywordMatched = false;
        for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
            WujinChainEntityDO entity = capability.getEntityId() == null
                    ? null : chainEntityService.getEntity(capability.getEntityId());
            WujinSupplierCapabilityRespVO.CapabilityItem item = new WujinSupplierCapabilityRespVO.CapabilityItem();
            item.setProductId(capability.getProductId());
            item.setProductName(capability.getProductName());
            item.setEntityId(capability.getEntityId());
            item.setEntityName(entity == null ? null : entity.getName());
            item.setLane(capability.getLane());
            item.setIndustry(capability.getIndustry());
            item.setStockCount(capability.getStockCount());
            item.setMinOrderQuantity(capability.getMinOrderQuantity());
            item.setDeliveryDays(capability.getDeliveryDays());
            item.setServiceArea(capability.getServiceArea());
            item.setRemark(capability.getRemark());
            items.add(item);
            mainCapabilities.add(capability.getProductName());
            if (entity != null) {
                mainCapabilities.add(entity.getName());
            }
            keywordMatched = keywordMatched || contains(capability.getProductName(), reqVO.getKeyword())
                    || (entity != null && contains(entity.getName(), reqVO.getKeyword()));
            if (capability.getProductId() != null && productIds.add(capability.getProductId())) {
                approvedTags.addAll(productAttributeService.getApprovedTagNames(capability.getProductId()));
            }
        }
        if (!items.isEmpty()) {
            respVO.setSupplierName("商家" + reqVO.getSupplierId() + " · " + items.get(0).getProductName());
        }
        respVO.setCapabilities(items);
        respVO.setMainCapabilities(new ArrayList<>(mainCapabilities));
        respVO.setApprovedTags(new ArrayList<>(approvedTags));
        respVO.setLanes(buildLaneCapabilities(capabilities));
        respVO.setMatchScore(items.isEmpty() ? 0 : keywordMatched ? 96 : 88);
        respVO.setServiceNote(buildCapabilitySummary(capabilities));
        return respVO;
    }

    private WujinSupplierCapabilityRespVO buildPlatformSupplierCapability(WujinSupplierCapabilityReqVO reqVO) {
        WujinChainEntityDO entity = chainEntityService.getEntity(reqVO.getSupplierId());
        WujinSupplierCapabilityRespVO respVO = new WujinSupplierCapabilityRespVO();
        respVO.setSupplierId(reqVO.getSupplierId());
        respVO.setSupplierType(SUPPLIER_TYPE_PLATFORM);
        respVO.setCapabilities(Collections.<WujinSupplierCapabilityRespVO.CapabilityItem>emptyList());
        respVO.setApprovedTags(Collections.<String>emptyList());
        if (entity == null) {
            respVO.setMainCapabilities(Collections.<String>emptyList());
            respVO.setLanes(Collections.<WujinSupplierCapabilityRespVO.LaneCapability>emptyList());
            respVO.setMatchScore(0);
            return respVO;
        }
        WujinLane lane = parseLane(entity.getLane(), WujinLane.MATERIAL);
        respVO.setSupplierName(entity.getName() + "供应协作商");
        respVO.setMatchScore(calculateMatchScore(toCandidateReq(reqVO), entity));
        respVO.setServiceNote("平台产业链实体兜底候选，提交寻源线索后由平台匹配" + entity.getName()
                + "的认证商家并跟进报价。");
        respVO.setMainCapabilities(new ArrayList<>(Arrays.asList(entity.getName(),
                laneName(lane) + "寻源", "规格报价")));
        WujinSupplierCapabilityRespVO.LaneCapability laneCapability = new WujinSupplierCapabilityRespVO.LaneCapability();
        laneCapability.setLabel(laneName(lane));
        laneCapability.setValue(lane.name());
        laneCapability.setNote("由平台撮合" + entity.getName() + "供应商");
        respVO.setLanes(Collections.singletonList(laneCapability));
        return respVO;
    }

    private WujinSupplierCandidateReqVO toCandidateReq(WujinSupplierCapabilityReqVO reqVO) {
        WujinSupplierCandidateReqVO candidateReqVO = new WujinSupplierCandidateReqVO();
        candidateReqVO.setKeyword(reqVO.getKeyword());
        candidateReqVO.setLane(reqVO.getLane());
        candidateReqVO.setSourceKeyword(reqVO.getSourceKeyword());
        candidateReqVO.setIndustry(reqVO.getIndustry());
        return candidateReqVO;
    }

    private List<WujinSupplierCapabilityRespVO.LaneCapability> buildLaneCapabilities(
            List<WujinMerchantSupplyCapabilityDO> capabilities) {
        Map<String, List<WujinMerchantSupplyCapabilityDO>> byLane = new LinkedHashMap<>();
        for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
            String lane = parseLane(capability.getLane(), WujinLane.PRODUCT).name();
            if (!byLane.containsKey(lane)) {
                byLane.put(lane, new ArrayList<WujinMerchantSupplyCapabilityDO>());
            }
            byLane.get(lane).add(capability);
        }
        List<WujinSupplierCapabilityRespVO.LaneCapability> lanes = new ArrayList<>();
        for (Map.Entry<String, List<WujinMerchantSupplyCapabilityDO>> entry : byLane.entrySet()) {
            WujinSupplierCapabilityRespVO.LaneCapability laneCapability = new WujinSupplierCapabilityRespVO.LaneCapability();
            laneCapability.setValue(entry.getKey());
            laneCapability.setLabel(laneName(WujinLane.valueOf(entry.getKey())));
            Integer fastestDays = null;
            for (WujinMerchantSupplyCapabilityDO capability : entry.getValue()) {
                if (capability.getDeliveryDays() != null
                        && (fastestDays == null || capability.getDeliveryDays() < fastestDays)) {
                    fastestDays = capability.getDeliveryDays();
                }
            }
            laneCapability.setNote(entry.getValue().size() + " 项供应能力"
                    + (fastestDays == null ? "，交期待确认" : "，最快 " + fastestDays + " 天交付"));
            lanes.add(laneCapability);
        }
        return lanes;
    }

    private String buildCapabilitySummary(List<WujinMerchantSupplyCapabilityDO> capabilities) {
        if (capabilities.isEmpty()) {
            return "该供应商暂未登记启用中的供应能力，可提交寻源线索由平台协助确认。";
        }
        Set<String> serviceAreas = new LinkedHashSet<>();
        Integer minOrder = null;
        for (WujinMerchantSupplyCapabilityDO capability : capabilities) {
            if (!isBlank(capability.getServiceArea())) {
                serviceAreas.add(capability.getServiceArea());
            }
            if (capability.getMinOrderQuantity() != null
                    && (minOrder == null || capability.getMinOrderQuantity() < minOrder)) {
                minOrder = capability.getMinOrderQuantity();
            }
        }
        return "登记 " + capabilities.size() + " 项启用中的供应能力"
                + (serviceAreas.isEmpty() ? "" : "，服务" + String.join("、", serviceAreas))
                + (minOrder == null ? "，起订量可议" : "，最低起订量 " + minOrder) + "。";
    }

    private WujinSourcingLeadProgressRespVO buildLeadSummary(WujinSourcingLeadDO lead) {
        WujinSourcingLeadProgressRespVO respVO = new WujinSourcingLeadProgressRespVO();
        respVO.setLeadId(lead.getId());
        respVO.setTitle(lead.getKeyword() + " 寻源线索");
        respVO.setKeyword(lead.getKeyword());
        respVO.setLane(lead.getLane());
        respVO.setSourceKeyword(lead.getSourceKeyword());
        respVO.setIndustry(lead.getIndustry());
        respVO.setSupplierName(lead.getSupplierName());
        respVO.setRequirement(lead.getRequirement());
        respVO.setLeadStatus(lead.getLeadStatus());
        respVO.setLeadStatusName(leadStatusName(lead.getLeadStatus()));
        respVO.setDispatchStatus(lead.getDispatchStatus());
        respVO.setQuotedAmount(lead.getQuotedAmount());
        respVO.setCreateTime(lead.getCreateTime());
        respVO.setSummary(leadSummary(lead));
        return respVO;
    }

    private List<WujinSourcingLeadProgressRespVO.Step> buildLeadSteps(WujinSourcingLeadDO lead) {
        List<WujinSourcingLeadProgressRespVO.Step> steps = new ArrayList<>();
        boolean dispatched = DISPATCH_STATUS_DISPATCHED.equals(lead.getDispatchStatus());
        boolean contacted = lead.getFirstContactTime() != null || statusAtLeast(lead.getLeadStatus(), "CONTACTED");
        boolean quoted = lead.getQuotedTime() != null || statusAtLeast(lead.getLeadStatus(), "QUOTED");
        boolean lost = "LOST".equals(lead.getLeadStatus());
        boolean converted = "CONVERTED".equals(lead.getLeadStatus());

        steps.add(step("SUBMITTED", "已提交", lead.getCreateTime(), "需求已记录：" + lead.getRequirement(), true));
        steps.add(step("DISPATCHED", "平台分发", lead.getDispatchTime(), dispatched
                ? "已分发给" + (isBlank(lead.getSupplierName()) ? "匹配商家" : lead.getSupplierName())
                : "平台正在按" + laneName(parseLane(lead.getLane(), WujinLane.MATERIAL)) + "泳道匹配供应商", dispatched));
        steps.add(step("CONTACTED", "供应商联系", lead.getFirstContactTime(),
                contacted ? "供应商已与您取得联系" : "等待供应商确认能力、交期并联系您", contacted));
        String quoteNote = lead.getQuotedAmount() == null ? "供应商已给出报价"
                : "供应商报价 ¥" + new BigDecimal(lead.getQuotedAmount())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toPlainString();
        steps.add(step("QUOTED", "供应商报价", lead.getQuotedTime(),
                quoted ? quoteNote : "确认规格后供应商将提供报价", quoted));
        if (lost) {
            steps.add(step("LOST", "未达成合作", lead.getLostTime(),
                    isBlank(lead.getHandleRemark()) ? "本次寻源未达成合作，可重新发起寻源" : lead.getHandleRemark(), true));
        } else {
            steps.add(step("CONVERTED", "达成合作", lead.getConvertedTime(),
                    converted ? "已与供应商达成合作" : "进入样品、合同或替代方案确认", converted));
        }
        if (!lost && !converted) {
            for (WujinSourcingLeadProgressRespVO.Step step : steps) {
                if (!Boolean.TRUE.equals(step.getDone())) {
                    step.setActive(true);
                    break;
                }
            }
        }
        return steps;
    }

    private WujinSourcingLeadProgressRespVO.Step step(String key, String title, LocalDateTime time,
                                                      String description, boolean done) {
        WujinSourcingLeadProgressRespVO.Step step = new WujinSourcingLeadProgressRespVO.Step();
        step.setKey(key);
        step.setTitle(title);
        step.setTime(time == null ? (done ? "" : "待处理") : STEP_TIME_FORMATTER.format(time));
        step.setDescription(description);
        step.setDone(done);
        step.setActive(false);
        return step;
    }

    private boolean statusAtLeast(String status, String expected) {
        List<String> order = Arrays.asList("SUBMITTED", "ASSIGNED", "CONTACTED", "QUOTED", "CONVERTED");
        int index = order.indexOf(status);
        return index >= 0 && index >= order.indexOf(expected);
    }

    private String leadSummary(WujinSourcingLeadDO lead) {
        String status = lead.getLeadStatus();
        if ("CONVERTED".equals(status)) {
            return "已与供应商达成合作。";
        }
        if ("LOST".equals(status)) {
            return "本次寻源未达成合作，可调整需求后重新发起。";
        }
        if ("QUOTED".equals(status)) {
            return "供应商已报价，请留意供应商联系并确认合作。";
        }
        if ("CONTACTED".equals(status)) {
            return "供应商已联系，正在确认规格、交期与报价。";
        }
        if (DISPATCH_STATUS_DISPATCHED.equals(lead.getDispatchStatus())) {
            return "已分发给匹配商家，等待供应商联系。";
        }
        return "平台正在为您匹配供应商。";
    }

    private String leadStatusName(String status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case "SUBMITTED":
                return "已提交";
            case "ASSIGNED":
                return "已分发";
            case "CONTACTED":
                return "已联系";
            case "QUOTED":
                return "已报价";
            case "CONVERTED":
                return "已成交";
            case "LOST":
                return "未成交";
            default:
                return status;
        }
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
        candidate.setSupplierType(SUPPLIER_TYPE_MERCHANT);
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
        candidate.setSupplierType(SUPPLIER_TYPE_MERCHANT);
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
        candidate.setSupplierType(SUPPLIER_TYPE_PLATFORM);
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
