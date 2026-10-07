package cn.iocoder.yudao.module.wujin.service.detail;

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
    private static final int AUDIT_STATUS_APPROVED = 30;

    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @Override
    public WujinEntityDetailRespVO getEntityDetail(WujinEntityDetailReqVO reqVO) {
        WujinLane lane = resolveLane(reqVO);
        WujinChainEntityDO entity = resolveEntity(reqVO, lane);
        if (entity == null) {
            return buildFallbackDetail(reqVO, lane);
        }
        return buildEntityDetail(entity, reqVO, lane);
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
        String summary = buildSummary(entity, lane, industry);
        String riskNote = firstNotBlank(entity.getRiskNote(), buildDefaultRiskNote(name, lane, industry));

        Set<String> outgoingNames = relatedEntityNames(entity.getId(), true);
        Set<String> incomingNames = relatedEntityNames(entity.getId(), false);

        WujinEntityDetailRespVO respVO = new WujinEntityDetailRespVO();
        respVO.setId(entity.getId());
        respVO.setName(name);
        respVO.setEntityLabel(entityLabel);
        respVO.setSectionTitle(entityLabel);
        respVO.setSummary(summary);
        respVO.setIndustry(industry);
        respVO.setPrice(resolvePrice(lane));
        respVO.setMoq(resolveMoq(lane));
        respVO.setSpec(resolveSpec(entity, lane));
        respVO.setCertification(resolveCertification(industry, lane));
        respVO.setSections(buildSections(lane, name, riskNote, outgoingNames, incomingNames));
        respVO.setCrossIndustryTips(buildCrossIndustryTips(riskNote, industry, lane));
        respVO.setIndustryComparison(buildIndustryComparison(industry, lane));
        return respVO;
    }

    private WujinEntityDetailRespVO buildFallbackDetail(WujinEntityDetailReqVO reqVO, WujinLane lane) {
        String name = firstNotBlank(reqVO.getKeyword(), reqVO.getSourceKeyword(), "五金商品");
        String industry = firstNotBlank(reqVO.getIndustry(), "五金工业");
        String entityLabel = laneEntityLabel(lane);
        String riskNote = buildDefaultRiskNote(name, lane, industry);

        WujinEntityDetailRespVO respVO = new WujinEntityDetailRespVO();
        respVO.setId(reqVO.getId());
        respVO.setName(name);
        respVO.setEntityLabel(entityLabel);
        respVO.setSectionTitle(entityLabel);
        respVO.setSummary(name + " 的" + entityLabel + "已整理关键规格、供应能力与跨行业差异。");
        respVO.setIndustry(industry);
        respVO.setPrice(resolvePrice(lane));
        respVO.setMoq(resolveMoq(lane));
        respVO.setSpec(resolveSpec(null, lane));
        respVO.setCertification(resolveCertification(industry, lane));
        respVO.setSections(buildSections(lane, name, riskNote, Collections.emptySet(), Collections.emptySet()));
        respVO.setCrossIndustryTips(buildCrossIndustryTips(riskNote, industry, lane));
        respVO.setIndustryComparison(buildIndustryComparison(industry, lane));
        return respVO;
    }

    private List<WujinEntityDetailRespVO.DetailSection> buildSections(WujinLane lane, String name, String riskNote,
                                                                      Set<String> outgoingNames, Set<String> incomingNames) {
        List<WujinEntityDetailRespVO.DetailSection> sections = new ArrayList<>();
        if (lane == WujinLane.PROCESS) {
            sections.add(new WujinEntityDetailRespVO.DetailSection("工艺能力",
                    buildListText(outgoingNames, "支持打样、小批量与批量加工，并可继续联查上下游实体。")));
            sections.add(new WujinEntityDetailRespVO.DetailSection("设备要求", riskNote));
            sections.add(new WujinEntityDetailRespVO.DetailSection("适配商品",
                    buildListText(incomingNames, "可结合当前搜索结果确认适配商品与部件。")));
            return sections;
        }
        if (lane == WujinLane.MATERIAL) {
            sections.add(new WujinEntityDetailRespVO.DetailSection("材料牌号", resolveMaterialSpec(name)));
            sections.add(new WujinEntityDetailRespVO.DetailSection("适用成品",
                    buildListText(incomingNames, "可从当前原材料继续反查下游成品和工艺。")));
            sections.add(new WujinEntityDetailRespVO.DetailSection("供应方式",
                    "支持现货、分切、批次追溯与替代料建议。"));
            return sections;
        }
        sections.add(new WujinEntityDetailRespVO.DetailSection("规格口径", resolveProductSpec(name)));
        sections.add(new WujinEntityDetailRespVO.DetailSection("关联工艺",
                buildListText(outgoingNames, "可联查冲压、热处理、电镀等关键工艺能力。")));
        sections.add(new WujinEntityDetailRespVO.DetailSection("上游材料",
                buildListText(resolveMaterialCandidates(outgoingNames, incomingNames),
                        "可继续联查钢材、合金、橡胶等关键来源。")));
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

    private Set<String> relatedEntityNames(Long entityId, boolean outgoing) {
        WujinChainEntityRelationListReqVO listReqVO = new WujinChainEntityRelationListReqVO();
        if (outgoing) {
            listReqVO.setSourceEntityId(entityId);
        } else {
            listReqVO.setTargetEntityId(entityId);
        }
        listReqVO.setAuditStatus(AUDIT_STATUS_APPROVED);
        List<WujinChainEntityRelationDO> relations = relationService.getRelationList(listReqVO);
        if (relations == null || relations.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> relatedIds = relations.stream()
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

    private Set<String> resolveMaterialCandidates(Set<String> outgoingNames, Set<String> incomingNames) {
        if (!outgoingNames.isEmpty()) {
            return outgoingNames;
        }
        return incomingNames;
    }

    private String buildListText(Set<String> names, String fallback) {
        if (names == null || names.isEmpty()) {
            return fallback;
        }
        return String.join("、", names);
    }

    private String buildSummary(WujinChainEntityDO entity, WujinLane lane, String industry) {
        String riskNote = entity.getRiskNote();
        if (!isBlank(riskNote)) {
            return riskNote;
        }
        return entity.getName() + "在" + industry + "场景下的" + laneEntityLabel(lane)
                + "已整理，覆盖规格、能力与跨行业差异。";
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

    private String laneEntityLabel(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "工艺详情";
        }
        if (lane == WujinLane.MATERIAL) {
            return "原材料详情";
        }
        return "商品详情";
    }

    private String resolvePrice(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "按工序报价";
        }
        if (lane == WujinLane.MATERIAL) {
            return "按吨/批次报价";
        }
        return "按规格询价";
    }

    private String resolveMoq(WujinLane lane) {
        if (lane == WujinLane.PROCESS) {
            return "支持打样";
        }
        if (lane == WujinLane.MATERIAL) {
            return "现货/期货可议";
        }
        return "小批量起订";
    }

    private String resolveSpec(WujinChainEntityDO entity, WujinLane lane) {
        String name = entity == null ? "" : entity.getName();
        if (lane == WujinLane.PROCESS) {
            return "按图纸、公差与表面处理要求确认";
        }
        if (lane == WujinLane.MATERIAL) {
            return resolveMaterialSpec(name);
        }
        return resolveProductSpec(name);
    }

    private String resolveCertification(String industry, WujinLane lane) {
        if (containsAny(industry, "医疗", "器械")) {
            return "资质与批次追溯需重点核验";
        }
        if (containsAny(industry, "汽车", "新能源")) {
            return "建议补充质量体系与过程能力证明";
        }
        if (lane == WujinLane.MATERIAL) {
            return "材质证明与检测报告按需提供";
        }
        return "常规认证按需提供";
    }

    private String resolveProductSpec(String name) {
        if (isBlank(name)) {
            return "常见型号、材质、公差与认证口径一并查看";
        }
        return name + "的常见型号、材质、公差与认证口径一并查看";
    }

    private String resolveMaterialSpec(String name) {
        if (isBlank(name)) {
            return "牌号、产地、批次与库存状态一并展示";
        }
        return name + "的牌号、产地、批次与库存状态一并展示";
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

    private boolean containsAny(String value, String... fragments) {
        if (isBlank(value)) {
            return false;
        }
        return Arrays.stream(fragments).anyMatch(value::contains);
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
