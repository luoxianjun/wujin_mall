package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditReason;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditReviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityRelationListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityRelationDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.supply.WujinMerchantSupplyCapabilityDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.supply.WujinMerchantSupplyCapabilityMapper;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityRelationAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationItemAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinChainEntityRelationAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinMerchantRelationItemAdminServiceImpl.class,
        WujinRelationAuditRecordAdminServiceImpl.class,
        WujinRelationAuditReviewServiceImpl.class})
class WujinRelationAuditReviewServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinChainEntityRelationAdminService relationService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;
    @Resource
    private WujinRelationAuditReviewService reviewService;
    @Resource
    private WujinMerchantSupplyCapabilityMapper capabilityMapper;

    @Test
    void approveSubmissionCreatesEffectiveChainRelationsAndAuditRecord() {
        Long materialEntityId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name()));
        Long processEntityId = entityService.createEntity(entityReq("E_VULCANIZE", "硫化工艺", WujinLane.PROCESS.name()));
        Long productEntityId = entityService.createEntity(entityReq("E_TIRE", "高耐磨乘用车轮胎", WujinLane.PRODUCT.name()));
        Long submissionId = createSubmission();
        itemService.createItem(itemReq(submissionId, materialEntityId, WujinRelationType.REQUIRES_MATERIAL.name(), true));
        itemService.createItem(itemReq(submissionId, processEntityId, WujinRelationType.REQUIRES_PROCESS.name(), true));
        WujinMerchantSupplyCapabilityDO pendingCapability = pendingCapability(productEntityId);
        capabilityMapper.insert(pendingCapability);

        WujinRelationAuditReviewRespVO result = reviewService.review(reviewReq(submissionId, WujinAuditAction.APPROVE.name()));

        assertEquals(submissionId, result.getSubmissionId());
        assertEquals(WujinAuditAction.APPROVE.name(), result.getAction());
        assertTrue(result.getEffectiveFlag());
        assertEquals(2, result.getEffectiveRelationCount());
        assertEquals(productEntityId, result.getProductEntityId());

        WujinMerchantRelationSubmissionDO submission = submissionService.getSubmission(submissionId);
        assertEquals(30, submission.getAuditStatus());
        assertEquals("MANUAL_REVIEW", submission.getAuditRoute());
        WujinMerchantSupplyCapabilityDO capability = capabilityMapper.selectById(pendingCapability.getId());
        assertEquals(0, capability.getSupplyStatus());
        assertEquals(12, capability.getMinOrderQuantity());
        assertEquals(5, capability.getDeliveryDays());
        assertEquals("华东", capability.getServiceArea());

        WujinRelationAuditRecordDO record = auditRecordService.getAuditRecord(result.getAuditRecordId());
        assertEquals(WujinAuditReason.REVIEW_APPROVED.name(), record.getReason());
        assertTrue(record.getEffectiveFlag());

        WujinChainEntityRelationListReqVO relationListReqVO = new WujinChainEntityRelationListReqVO();
        relationListReqVO.setSourceEntityId(productEntityId);
        List<WujinChainEntityRelationDO> relations = relationService.getRelationList(relationListReqVO);
        assertEquals(2, relations.size());
        assertTrue(relations.stream().anyMatch(relation -> materialEntityId.equals(relation.getTargetEntityId())));
        assertTrue(relations.stream().anyMatch(relation -> processEntityId.equals(relation.getTargetEntityId())));
    }

    private WujinRelationAuditReviewReqVO reviewReq(Long submissionId, String action) {
        WujinRelationAuditReviewReqVO reqVO = new WujinRelationAuditReviewReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setAuditorId(9001L);
        reqVO.setAction(action);
        reqVO.setComment("平台审核通过，关系生效入网");
        return reqVO;
    }

    private Long createSubmission() {
        Long templateId = templateService.createTemplate(templateReq());
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(1001L);
        reqVO.setProductId(2001L);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(10);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(80);
        return submissionService.createSubmission(reqVO);
    }

    private WujinMerchantRelationItemSaveReqVO itemReq(Long submissionId, Long entityId, String relationType,
                                                       Boolean requiredFlag) {
        WujinMerchantRelationItemSaveReqVO reqVO = new WujinMerchantRelationItemSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(relationType);
        reqVO.setFromTemplate(true);
        reqVO.setRequiredFlag(requiredFlag);
        reqVO.setRemark("平台模板关系");
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq() {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode("TPL_TIRE");
        reqVO.setName("轮胎橡胶模板");
        reqVO.setIndustryCode("TIRE_RUBBER");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinChainEntitySaveReqVO entityReq(String entityCode, String name, String lane) {
        WujinChainEntitySaveReqVO reqVO = new WujinChainEntitySaveReqVO();
        reqVO.setEntityCode(entityCode);
        reqVO.setName(name);
        reqVO.setLane(lane);
        reqVO.setIndustries("TIRE_RUBBER");
        reqVO.setStatus(0);
        reqVO.setJunctionFlag(false);
        return reqVO;
    }

    private WujinMerchantSupplyCapabilityDO pendingCapability(Long entityId) {
        WujinMerchantSupplyCapabilityDO capability = new WujinMerchantSupplyCapabilityDO();
        capability.setMerchantId(1001L);
        capability.setProductId(2001L);
        capability.setProductName("高耐磨乘用车轮胎");
        capability.setEntityId(entityId);
        capability.setLane(WujinLane.PRODUCT.name());
        capability.setIndustry("TIRE_RUBBER");
        capability.setSupplyStatus(1);
        capability.setStockCount(80);
        capability.setMinOrderQuantity(12);
        capability.setDeliveryDays(5);
        capability.setServiceArea("华东");
        capability.setRemark("商品发布时填写");
        return capability;
    }
}
