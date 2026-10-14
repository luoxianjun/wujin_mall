package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class WujinAppDetailServiceImpl implements WujinAppDetailService {

    private static final int ENABLED_STATUS = 0;
    /**
     * 产业链关系生效状态：1 为关系表生效值，20/30 为历史数据沿用的审核通过值
     */
    private static final List<Integer> EFFECTIVE_RELATION_STATUSES = Arrays.asList(1, 20, 30);

    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @Override
    public WujinEntityDetailRespVO getEntityDetail(WujinEntityDetailReqVO reqVO) {
        WujinLane lane = resolveLane(reqVO);
        WujinChainEntityDO entity = resolveEntity(reqVO, lane);
        if (entity == null || !Integer.valueOf(ENABLED_STATUS).equals(entity.getStatus())) {
            return buildNotFoundDetail(reqVO, lane, entity);
        }
        WujinEntityDetailRespVO respVO = buildEntityDetail(entity, reqVO, lane);
        respVO.setExists(true);
        return respVO;
    }

    private WujinChainEntityDO resolveEntity(WujinEntityDetailReqVO reqVO, WujinLane lane) {
        if (reqVO.getId() != null) {
            WujinChainEntityDO entity = chainEntityService.getEntity(reqVO.getId());
            if (entity != null) {
                return entity;
            }
        }

        String keyword = firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword());
        if (isBlank(keyword)) {
            return null;
        }

        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setName(keyword);
        listReqVO.setLane(lane.name());
        listReqVO.setIndustry(reqVO.getIndustry());
        listReqVO.setStatus(ENABLED_STATUS);
        List<WujinChainEntityDO> entities = chainEntityService.getEntityList(listReqVO);
        if (!entities.isEmpty()) {
            return entities.get(0);
        }

        WujinChainEntityListReqVO fallbackReqVO = new WujinChainEntityListReqVO();
        fallbackReqVO.setName(keyword);
        fallbackReqVO.setStatus(ENABLED_STATUS);
        entities = chainEntityService.getEntityList(fallbackReqVO);
        return entities.isEmpty() ? null : entities.get(0);
    }

    private WujinEntityDetailRespVO buildEntityDetail(WujinChainEntityDO entity, WujinEntityDetailReqVO reqVO, WujinLane requestedLane) {
        WujinLane lane = parseLane(entity.getLane(), requestedLane);
        String name = firstNotBlank(entity.getName(), reqVO.getKeyword(), reqVO.getSourceKeyword(), "五金商品");
        String industry = resolveIndustry(entity, reqVO.getIndustry());
        String entityLabel = laneEntityLabel(lane);
        String riskNote = firstNotBlank(entity.getRiskNote(), buildDefaultRiskNote(name, lane, industry));

        WujinEntityDetailRespVO respVO = new WujinEntityDetailRespVO();
        respVO.setId(entity.getId());
        respVO.setName(name);
        respVO.setEntityLabel(entityLabel);
        respVO.setSectionTitle(entityLabel);
        // 产业链实体没有统一的价格、起订量与规格，只返回维护过的真实信息，不再拼装模板文案
        respVO.setSummary(entity.getRiskNote());
        respVO.setIndustry(industry);
        respVO.setSections(buildSections(entity, lane));
        respVO.setCrossIndustryTips(buildCrossIndustryTips(riskNote, industry, lane));
        respVO.setIndustryComparison(buildIndustryComparison(industry, lane));
        return respVO;
    }

    /**
     * 实体不存在或已停用时不再拼装模板内容，只返回“已下架或不存在”的状态
     */
    private WujinEntityDetailRespVO buildNotFoundDetail(WujinEntityDetailReqVO reqVO, WujinLane lane,
                                                       WujinChainEntityDO entity) {
        WujinEntityDetailRespVO respVO = new WujinEntityDetailRespVO();
        respVO.setExists(false);
        respVO.setId(reqVO.getId());
        respVO.setName(entity != null ? entity.getName()
                : firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword(), laneEntityLabel(lane)));
        respVO.setEntityLabel(laneEntityLabel(lane));
        respVO.setSectionTitle(laneEntityLabel(lane));
        respVO.setSummary(lane == WujinLane.PRODUCT ? "该商品已下架或不存在"
                : lane == WujinLane.PROCESS ? "该加工工艺已下架或不存在" : "该原材料已下架或不存在");
        respVO.setSections(Collections.<WujinEntityDetailRespVO.DetailSection>emptyList());
        respVO.setCrossIndustryTips(Collections.<String>emptyList());
        respVO.setIndustryComparison(Collections.<WujinEntityDetailRespVO.IndustryComparison>emptyList());
        return respVO;
    }

    /**
     * 详情区块只展示真实数据：泳道、行业与生效的上下游关系；上下游为空时给出明确提示
     */
    private List<WujinEntityDetailRespVO.DetailSection> buildSections(WujinChainEntityDO entity, WujinLane lane) {
        List<WujinEntityDetailRespVO.DetailSection> sections = new ArrayList<>();
        sections.add(new WujinEntityDetailRespVO.DetailSection("所属泳道", laneDisplayName(lane)));
        List<String> industries = splitIndustries(entity.getIndustries());
        if (!industries.isEmpty()) {
            sections.add(new WujinEntityDetailRespVO.DetailSection("适用行业", String.join("、", industries)));
        }
        if (lane == WujinLane.PRODUCT) {
            sections.add(new WujinEntityDetailRespVO.DetailSection("上游原材料", buildListText(
                    relatedEntityNames(entity.getId(), true, WujinRelationType.REQUIRES_MATERIAL), "暂无上游关联的原材料")));
            sections.add(new WujinEntityDetailRespVO.DetailSection("上游加工工艺", buildListText(
                    relatedEntityNames(entity.getId(), true, WujinRelationType.REQUIRES_PROCESS), "暂无上游关联的加工工艺")));
            return sections;
        }
        sections.add(new WujinEntityDetailRespVO.DetailSection("下游应用",
                buildListText(relatedEntityNames(entity.getId(), false, null), "暂无下游应用")));
        sections.add(new WujinEntityDetailRespVO.DetailSection("上游关联",
                buildListText(relatedEntityNames(entity.getId(), true, null), "暂无上游关联")));
        return sections;
    }

    private List<String> buildCrossIndustryTips(String riskNote, String industry, WujinLane lane) {
        List<String> tips = new ArrayList<>();
        tips.add(riskNote);
        tips.add(industry + "场景下请确认认证、检测口径和交付批次是否满足要求。");
        if (lane == WujinLane.MATERIAL) {
            tips.add("同牌号材料在承压、食品接触、医疗等场景下不默认可互换。");
        } else if (lane == WujinLane.PROCESS) {
            tips.add("同名工艺在精度、表面处理和检测能力上可能存在显著差异。");
        } else {
            tips.add("同名商品在不同行业的规格、公差和认证要求可能不同。");
        }
        return tips;
    }

    private List<WujinEntityDetailRespVO.IndustryComparison> buildIndustryComparison(String industry, WujinLane lane) {
        List<WujinEntityDetailRespVO.IndustryComparison> comparisons = new ArrayList<>();
        comparisons.add(new WujinEntityDetailRespVO.IndustryComparison(
                "通用五金",
                buildGeneralStandard(lane),
                "适合快速比价、确认规格与常规交期。"));
        comparisons.add(new WujinEntityDetailRespVO.IndustryComparison(
                firstNotBlank(industry, "高要求行业"),
                buildStrictStandard(lane),
                "通常还要补充资质、样品验证和批次追溯。"));
        return comparisons;
    }

    private Set<String> relatedEntityNames(Long entityId, boolean outgoing, WujinRelationType relationType) {
        WujinChainEntityRelationListReqVO listReqVO = new WujinChainEntityRelationListReqVO();
        if (relationType != null) {
            listReqVO.setRelationType(relationType.name());
        }
        if (outgoing) {
            listReqVO.setSourceEntityId(entityId);
        } else {
            listReqVO.setTargetEntityId(entityId);
        }
        List<WujinChainEntityRelationDO> relations = relationService.getRelationList(listReqVO);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> relatedIds = relations.stream()
                .filter(relation -> EFFECTIVE_RELATION_STATUSES.contains(relation.getAuditStatus()))
                .map(relation -> outgoing ? relation.getTargetEntityId() : relation.getSourceEntityId())
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> names = new LinkedHashSet<>();
        for (Long relatedId : relatedIds) {
            WujinChainEntityDO relatedEntity = chainEntityService.getEntity(relatedId);
            if (relatedEntity != null && !isBlank(relatedEntity.getName())) {
                names.add(relatedEntity.getName());
            }
        }
        return names;
    }

    private String buildListText(Set<String> names, String fallback) {
        if (names == null || names.isEmpty()) {
            return fallback;
        }
        return String.join("、", names);
    }

    private String resolveIndustry(WujinChainEntityDO entity, String requestedIndustry) {
        if (!isBlank(requestedIndustry)) {
            return requestedIndustry;
        }
        List<String> industries = splitIndustries(entity == null ? null : entity.getIndustries());
        return industries.isEmpty() ? "五金工业" : industries.get(0);
    }

    private List<String> splitIndustries(String industries) {
        if (isBlank(industries)) {
            return Collections.emptyList();
        }
        return Arrays.stream(industries.split("[,，]"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private WujinLane resolveLane(WujinEntityDetailReqVO reqVO) {
        WujinLane lane = parseLane(reqVO.getLane(), null);
        if (lane != null) {
            return lane;
        }
        lane = parseLane(reqVO.getEntityType(), null);
        return lane == null ? WujinLane.PRODUCT : lane;
    }

    private WujinLane parseLane(String value, WujinLane defaultValue) {
        if (isBlank(value)) {
            return defaultValue;
        }
        try {
            return WujinLane.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    private String laneDisplayName(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "加工工艺";
        }
        if (lane == WujinLane.MATERIAL) {
            return "原材料";
        }
        return "成品";
    }

    private String laneEntityLabel(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "工艺详情";
        }
        if (lane == WujinLane.MATERIAL) {
            return "原材料详情";
        }
        return "商品详情";
    }

    private String buildGeneralStandard(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "优先看工序能力、交付节奏、良率与常规质检。";
        }
        if (lane == WujinLane.MATERIAL) {
            return "优先看牌号、库存、交期与基础检测报告。";
        }
        return "优先看规格、交期、批量价格与常规质检。";
    }

    private String buildStrictStandard(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "优先看设备能力、公差控制、过程验证与追溯记录。";
        }
        if (lane == WujinLane.MATERIAL) {
            return "优先看批次一致性、认证、替代风险与稳定供应。";
        }
        return "优先看认证、批次追溯、检测报告与稳定供应。";
    }

    private String buildDefaultRiskNote(String name, WujinLane lane, String industry) {
        if (lane == WujinLane.PROCESS) {
            return name + "在" + industry + "场景下需要确认工艺精度、表面处理和检测能力。";
        }
        if (lane == WujinLane.MATERIAL) {
            return name + "在" + industry + "场景下需要确认牌号、批次与替代边界。";
        }
        return name + "在" + industry + "场景下需要确认规格、公差与认证口径。";
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
