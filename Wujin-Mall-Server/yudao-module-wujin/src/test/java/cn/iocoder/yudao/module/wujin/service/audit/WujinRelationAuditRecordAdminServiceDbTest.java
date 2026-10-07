package cn.iocoder.yudao.module.wujin.service.audit;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditAction;
import cn.iocoder.yudao.module.wujin.audit.WujinAuditReason;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.audit.vo.WujinRelationAuditRecordSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.audit.WujinRelationAuditRecordDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminService;
import cn.iocoder.yudao.module.wujin.service.merchant.WujinMerchantRelationSubmissionAdminServiceImpl;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinIndustryTemplateAdminServiceImpl.class,
        WujinMerchantRelationSubmissionAdminServiceImpl.class,
        WujinRelationAuditRecordAdminServiceImpl.class})
class WujinRelationAuditRecordAdminServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinRelationAuditRecordAdminService auditRecordService;

    @Test
    void createAuditRecordPersistsAndFiltersByAction() {
        Long submissionId = createSubmission();
        Long auditId = auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.MANUAL_REVIEW.name(),
                WujinAuditReason.PRECHECK_PASSED.name(), false));
        auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.REJECT.name(),
                WujinAuditReason.REVIEW_REJECTED.name(), false));

        WujinRelationAuditRecordListReqVO listReqVO = new WujinRelationAuditRecordListReqVO();
        listReqVO.setSubmissionId(submissionId);
        listReqVO.setAction(WujinAuditAction.MANUAL_REVIEW.name());
        List<WujinRelationAuditRecordDO> records = auditRecordService.getAuditRecordList(listReqVO);

        assertEquals(1, records.size());
        assertEquals(auditId, records.get(0).getId());
        assertEquals(WujinAuditReason.PRECHECK_PASSED.name(), records.get(0).getReason());
    }

    @Test
    void createAuditRecordRequiresExistingSubmission() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> auditRecordService.createAuditRecord(recordReq(9999L, WujinAuditAction.MANUAL_REVIEW.name(),
                        WujinAuditReason.PRECHECK_PASSED.name(), false)));

        assertEquals("审核记录关联申报不存在", exception.getMessage());
    }

    @Test
    void approveRecordCanMarkRelationEffective() {
        Long submissionId = createSubmission();

        Long auditId = auditRecordService.createAuditRecord(recordReq(submissionId, WujinAuditAction.APPROVE.name(),
                WujinAuditReason.REVIEW_APPROVED.name(), true));

        WujinRelationAuditRecordDO record = auditRecordService.getAuditRecord(auditId);
        assertTrue(record.getEffectiveFlag());
        assertEquals("关系与模板一致", record.getComment());
    }

    private Long createSubmission() {
        Long templateId = templateService.createTemplate(templateReq());
        return submissionService.createSubmission(submissionReq(templateId));
    }

    private WujinRelationAuditRecordSaveReqVO recordReq(Long submissionId, String action, String reason, Boolean effectiveFlag) {
        WujinRelationAuditRecordSaveReqVO reqVO = new WujinRelationAuditRecordSaveReqVO();
        reqVO.setSubmissionId(submissionId);
        reqVO.setAuditorId(9001L);
        reqVO.setAction(action);
        reqVO.setReason(reason);
        reqVO.setComment("关系与模板一致");
        reqVO.setEffectiveFlag(effectiveFlag);
        return reqVO;
    }

    private WujinIndustryTemplateSaveReqVO templateReq() {
        WujinIndustryTemplateSaveReqVO reqVO = new WujinIndustryTemplateSaveReqVO();
        reqVO.setTemplateCode("TPL_TIRE");
        reqVO.setName("轮胎橡胶模板");
        reqVO.setIndustryCode("轮胎橡胶");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(Long templateId) {
        WujinMerchantRelationSubmissionSaveReqVO reqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        reqVO.setMerchantId(1001L);
        reqVO.setProductId(2001L);
        reqVO.setProductName("高耐磨乘用车轮胎");
        reqVO.setProductLane(WujinLane.PRODUCT.name());
        reqVO.setProductCategoryId(3001L);
        reqVO.setTemplateId(templateId);
        reqVO.setAuditStatus(0);
        reqVO.setAuditRoute("MANUAL_REVIEW");
        reqVO.setCompletenessScore(80);
        return reqVO;
    }
}
