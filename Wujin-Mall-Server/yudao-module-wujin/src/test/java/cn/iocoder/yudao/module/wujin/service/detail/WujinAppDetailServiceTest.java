package cn.iocoder.yudao.module.wujin.service.detail;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.detail.vo.WujinEntityDetailRespVO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinAppDetailServiceImpl.class, WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class})
class WujinAppDetailServiceTest extends BaseDbUnitTest {

    @Resource
    private WujinAppDetailService detailService;
    @Resource
    private WujinChainEntityAdminService chainEntityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @Test
    void getEntityDetailUsesPersistedEntityAndApprovedRelations() {
        Long productId = chainEntityService.createEntity(entityReq("P_TIRE_CAR", "乘用车轮胎",
                WujinLane.PRODUCT.name(), "汽车,橡胶", "汽车场景下需核验负载等级和认证批次"));
        Long processId = chainEntityService.createEntity(entityReq("C_TIRE_VULCANIZE", "硫化成型",
                WujinLane.PROCESS.name(), "汽车,橡胶", "确认硫化温控和工艺稳定性"));
        relationService.createRelation(relationReq(productId, processId, WujinRelationType.REQUIRES_PROCESS.name(), 35));

        WujinEntityDetailReqVO reqVO = new WujinEntityDetailReqVO();
        reqVO.setId(productId);
        reqVO.setLane(WujinLane.PRODUCT.name());
        reqVO.setIndustry("汽车");

        WujinEntityDetailRespVO detail = detailService.getEntityDetail(reqVO);

        assertEquals(Boolean.TRUE, detail.getExists());
        assertEquals(productId, detail.getId());
        assertEquals("乘用车轮胎", detail.getName());
        assertEquals("商品详情", detail.getEntityLabel());
        assertEquals("汽车", detail.getIndustry());
        assertTrue(detail.getSummary().contains("认证批次"));
        assertTrue(detail.getSections().stream().anyMatch(section -> section.getLabel().equals("上游加工工艺")
                && section.getValue().equals("硫化成型")));
        assertTrue(detail.getSections().stream().anyMatch(section -> section.getLabel().equals("上游原材料")
                && section.getValue().equals("暂无上游关联的原材料")));
        assertTrue(detail.getSections().stream().anyMatch(section -> section.getLabel().equals("适用行业")
                && section.getValue().equals("汽车、橡胶")));
        assertNull(detail.getPrice());
        assertNull(detail.getMoq());
        assertTrue(detail.getCrossIndustryTips().stream().anyMatch(tip -> tip.contains("认证批次")));
    }

    @Test
    void getEntityDetailReportsNotExistingInsteadOfTemplateContent() {
        WujinEntityDetailReqVO reqVO = new WujinEntityDetailReqVO();
        reqVO.setKeyword("未知商品");
        reqVO.setLane(WujinLane.MATERIAL.name());
        reqVO.setIndustry("医疗器械");

        WujinEntityDetailRespVO detail = detailService.getEntityDetail(reqVO);

        assertEquals(Boolean.FALSE, detail.getExists());
        assertEquals("未知商品", detail.getName());
        assertEquals("原材料详情", detail.getEntityLabel());
        assertEquals("该原材料已下架或不存在", detail.getSummary());
        assertTrue(detail.getSections().isEmpty());
        assertTrue(detail.getCrossIndustryTips().isEmpty());
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane,
                                                String industries, String riskNote) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries(industries);
        reqVO.setRiskNote(riskNote);
        reqVO.setJunctionFlag(false);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntityRelationSaveReqVO relationReq(Long sourceEntityId, Long targetEntityId,
                                                          String relationType, Integer costRatio) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(sourceEntityId);
        reqVO.setTargetEntityId(targetEntityId);
        reqVO.setRelationType(relationType);
        reqVO.setWeight(100);
        reqVO.setCostRatio(costRatio);
        reqVO.setIndustryContext("汽车");
        reqVO.setAuditStatus(30);
        reqVO.setAuditRemark("测试通过");
        return reqVO;
    }
}
