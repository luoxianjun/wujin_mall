package cn.iocoder.yudao.module.wujin.service.chain;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({WujinChainEntityAdminServiceImpl.class, WujinChainEntityRelationAdminServiceImpl.class})
class WujinChainAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;

    @Test
    void createEntityPersistsAndFiltersByLane() {
        Long productId = entityService.createEntity(entityReq("E_TIRE", "轮胎", WujinLane.PRODUCT.name(), "汽车,橡胶"));
        entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "橡胶,医疗器械"));

        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setLane(WujinLane.PRODUCT.name());
        List<WujinChainEntityDO> entities = entityService.getEntityList(listReqVO);

        assertEquals(1, entities.size());
        assertEquals(productId, entities.get(0).getId());
        assertEquals("E_TIRE", entities.get(0).getEntityCode());
        assertEquals(WujinLane.PRODUCT.name(), entities.get(0).getLane());
    }

    @Test
    void createRelationPersistsAndSupportsUpstreamDownstreamQueries() {
        Long tireId = entityService.createEntity(entityReq("E_TIRE", "轮胎", WujinLane.PRODUCT.name(), "汽车,橡胶"));
        Long rubberId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "橡胶,医疗器械"));
        Long relationId = relationService.createRelation(relationReq(tireId, rubberId, WujinRelationType.REQUIRES_MATERIAL.name()));

        WujinChainEntityRelationListReqVO upstreamReqVO = new WujinChainEntityRelationListReqVO();
        upstreamReqVO.setSourceEntityId(tireId);
        List<WujinChainEntityRelationDO> upstream = relationService.getRelationList(upstreamReqVO);
        assertEquals(1, upstream.size());
        assertEquals(relationId, upstream.get(0).getId());
        assertEquals(rubberId, upstream.get(0).getTargetEntityId());

        WujinChainEntityRelationListReqVO downstreamReqVO = new WujinChainEntityRelationListReqVO();
        downstreamReqVO.setTargetEntityId(rubberId);
        List<WujinChainEntityRelationDO> downstream = relationService.getRelationList(downstreamReqVO);
        assertEquals(1, downstream.size());
        assertEquals(tireId, downstream.get(0).getSourceEntityId());
    }

    @Test
    void createRelationRejectsSelfLoop() {
        Long tireId = entityService.createEntity(entityReq("E_TIRE", "轮胎", WujinLane.PRODUCT.name(), "汽车,橡胶"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> relationService.createRelation(relationReq(tireId, tireId, WujinRelationType.REQUIRES_MATERIAL.name())));

        assertEquals("实体关系不能指向自身", exception.getMessage());
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane, String industries) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries(industries);
        reqVO.setStatus(0);
        reqVO.setJunctionFlag(false);
        return reqVO;
    }

    private WujinChainEntityRelationSaveReqVO relationReq(Long sourceEntityId, Long targetEntityId, String relationType) {
        WujinChainEntityRelationSaveReqVO reqVO = new WujinChainEntityRelationSaveReqVO();
        reqVO.setSourceEntityId(sourceEntityId);
        reqVO.setTargetEntityId(targetEntityId);
        reqVO.setRelationType(relationType);
        reqVO.setWeight(50);
        reqVO.setCostRatio(35);
        reqVO.setIndustryContext("汽车");
        reqVO.setAuditStatus(1);
        return reqVO;
    }
}
