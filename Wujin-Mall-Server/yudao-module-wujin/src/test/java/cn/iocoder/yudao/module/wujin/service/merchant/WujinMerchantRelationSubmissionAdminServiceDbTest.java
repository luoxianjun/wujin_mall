package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationSubmissionDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({WujinChainEntityAdminServiceImpl.class,
        WujinIndustryTemplateAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinMerchantRelationItemAdminServiceImpl.class})
class WujinMerchantRelationSubmissionAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;

    @Test
    void createSubmissionPersistsAndFiltersByMerchantAndStatus() {
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶"));
        Long submissionId = submissionService.createSubmission(submissionReq(1001L, 2001L, templateId,
                WujinLane.PRODUCT.name(), 0));
        submissionService.createSubmission(submissionReq(1002L, 2002L, templateId, WujinLane.PRODUCT.name(), 1));

        WujinMerchantRelationSubmissionListReqVO listReqVO = new WujinMerchantRelationSubmissionListReqVO();
        listReqVO.setMerchantId(1001L);
        listReqVO.setAuditStatus(0);
        List<WujinMerchantRelationSubmissionDO> submissions = submissionService.getSubmissionList(listReqVO);

        assertEquals(1, submissions.size());
        assertEquals(submissionId, submissions.get(0).getId());
        assertEquals(2001L, submissions.get(0).getProductId());
        assertEquals(WujinLane.PRODUCT.name(), submissions.get(0).getProductLane());
    }

    @Test
    void createSubmissionItemPersistsAndFiltersByTemplateSource() {
        Long entityId = entityService.createEntity(entityReq("E_RUBBER", "天然橡胶", WujinLane.MATERIAL.name(), "轮胎橡胶"));
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶"));
        Long submissionId = submissionService.createSubmission(submissionReq(1001L, 2001L, templateId,
                WujinLane.PRODUCT.name(), 0));

        Long itemId = itemService.createItem(itemReq(submissionId, entityId, WujinRelationType.REQUIRES_MATERIAL.name(), true));
        itemService.createItem(itemReq(submissionId, entityId, WujinRelationType.SPECIFICATION.name(), false));

        WujinMerchantRelationItemListReqVO listReqVO = new WujinMerchantRelationItemListReqVO();
        listReqVO.setSubmissionId(submissionId);
        listReqVO.setFromTemplate(true);
        List<WujinMerchantRelationItemDO> items = itemService.getItemList(listReqVO);

        assertEquals(1, items.size());
        assertEquals(itemId, items.get(0).getId());
        assertEquals(entityId, items.get(0).getEntityId());
        assertEquals(WujinRelationType.REQUIRES_MATERIAL.name(), items.get(0).getRelationType());
    }

    @Test
    void createSubmissionRejectsNonProductLane() {
        Long templateId = templateService.createTemplate(templateReq("TPL_TIRE", "轮胎橡胶模板", "轮胎橡胶"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.createSubmission(submissionReq(1001L, 2001L, templateId,
                        WujinLane.MATERIAL.name(), 0)));

        assertEquals("商家发布商品必须归属成品泳道", exception.getMessage());
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(Long merchantId, Long productId, Long templateId,
                                                                   String productLane, Integer auditStatus) {
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(merchantId);
        reqVO.setProductId(productId);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setProductLane(productLane);
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(auditStatus);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(80);
        reqVO.setRemark("商家提交");
        return reqVO;
    }

    private WujinMerchantRelationItemSaveReqVO itemReq(Long submissionId, Long entityId, String relationType, Boolean fromTemplate) {
        WujinMerchantRelationItemSaveReqVO reqVO = new WujinMerchantRelationItemSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setEntityId(entityId);
        reqVO.setRelationType(relationType);
        reqVO.setFromTemplate(fromTemplate);
        reqVO.setRequiredFlag(true);
        reqVO.setRemark("关系说明");
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq(String templateCode, String name, String industryCode) {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode(templateCode);
        reqVO.setName(name);
        reqVO.setIndustryCode(industryCode);
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
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
}
