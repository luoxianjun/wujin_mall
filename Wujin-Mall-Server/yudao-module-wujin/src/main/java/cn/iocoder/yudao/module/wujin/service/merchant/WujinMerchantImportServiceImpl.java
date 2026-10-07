package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportPreviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportResultRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Validated
public class WujinMerchantImportServiceImpl implements WujinMerchantImportService {

    private static final int AUDIT_STATUS_IMPORTED_APPROVED = 30;

    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinChainEntityAdminService entityService;
    @Resource
    private WujinMerchantRelationSubmissionAdminService submissionService;
    @Resource
    private WujinMerchantRelationItemAdminService itemService;
    @Resource
    private WujinMerchantSupplyCapabilityAdminService capabilityService;

    @Override
    public WujinMerchantImportPreviewRespVO previewImport(WujinMerchantImportReqVO reqVO) {
        WujinIndustryTemplateDO template = templateService.getTemplate(reqVO.getTemplateId());
        List<WujinMerchantImportPreviewRespVO.RowPreview> validRows = new ArrayList<>();
        List<WujinMerchantImportPreviewRespVO.RowPreview> invalidRows = new ArrayList<>();
        List<WujinMerchantImportReqVO.ImportRow> rows = reqVO.getRows() == null
                ? Collections.<WujinMerchantImportReqVO.ImportRow>emptyList() : reqVO.getRows();
        for (int i = 0; i < rows.size(); i++) {
            WujinMerchantImportPreviewRespVO.RowPreview preview = previewRow(reqVO, template, rows.get(i), i + 1);
            if (preview.getErrors().isEmpty()) {
                validRows.add(preview);
            } else {
                invalidRows.add(preview);
            }
        }

        WujinMerchantImportPreviewRespVO respVO = new WujinMerchantImportPreviewRespVO();
        respVO.setTotalCount(rows.size());
        respVO.setValidCount(validRows.size());
        respVO.setInvalidCount(invalidRows.size());
        respVO.setValidRows(validRows);
        respVO.setInvalidRows(invalidRows);
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WujinMerchantImportResultRespVO importRows(WujinMerchantImportReqVO reqVO) {
        WujinMerchantImportPreviewRespVO preview = previewImport(reqVO);
        List<Long> submissionIds = new ArrayList<>();
        for (WujinMerchantImportPreviewRespVO.RowPreview row : preview.getValidRows()) {
            Long submissionId = submissionService.createSubmission(submissionReq(reqVO, row));
            itemService.createItem(itemReq(submissionId, row));
            capabilityService.saveOrUpdateCapability(capabilityReq(reqVO, row));
            submissionIds.add(submissionId);
        }

        WujinMerchantImportResultRespVO respVO = new WujinMerchantImportResultRespVO();
        respVO.setImportedCount(submissionIds.size());
        respVO.setSkippedCount(preview.getInvalidCount());
        respVO.setSubmissionIds(submissionIds);
        respVO.setInvalidRows(preview.getInvalidRows());
        return respVO;
    }

    private WujinMerchantImportPreviewRespVO.RowPreview previewRow(WujinMerchantImportReqVO reqVO,
                                                                  WujinIndustryTemplateDO template,
                                                                  WujinMerchantImportReqVO.ImportRow row,
                                                                  int rowNo) {
        WujinMerchantImportPreviewRespVO.RowPreview preview = new WujinMerchantImportPreviewRespVO.RowPreview();
        List<String> errors = new ArrayList<>();
        preview.setRowNo(rowNo);
        preview.setMerchantId(row.getMerchantId());
        preview.setProductId(row.getProductId());
        preview.setProductName(row.getProductName());
        preview.setProductCategoryId(row.getProductCategoryId() == null
                ? reqVO.getDefaultProductCategoryId() : row.getProductCategoryId());
        preview.setRelationType(row.getRelationType());
        preview.setStockCount(row.getStockCount());
        preview.setMinOrderQuantity(row.getMinOrderQuantity());
        preview.setDeliveryDays(row.getDeliveryDays());
        preview.setServiceArea(row.getServiceArea());
        preview.setRemark(row.getRemark());

        if (template == null) {
            errors.add("行业模板不存在");
        }
        if (row.getMerchantId() == null) {
            errors.add("商家编号不能为空");
        }
        if (row.getProductId() == null) {
            errors.add("商品编号不能为空");
        }
        if (isBlank(row.getProductName())) {
            errors.add("商品名称不能为空");
        }
        if (isBlank(row.getRelationType()) || !validRelationType(row.getRelationType())) {
            errors.add("关系类型无效");
        }

        WujinChainEntityDO entity = resolveEntity(row, errors);
        if (entity != null) {
            preview.setEntityId(entity.getId());
            preview.setEntityName(entity.getName());
            preview.setEntityLane(entity.getLane());
        }
        preview.setErrors(errors);
        return preview;
    }

    private WujinChainEntityDO resolveEntity(WujinMerchantImportReqVO.ImportRow row, List<String> errors) {
        if (row.getEntityId() != null) {
            WujinChainEntityDO entity = entityService.getEntity(row.getEntityId());
            if (entity == null) {
                errors.add("产业链实体不存在");
            }
            return entity;
        }
        if (isBlank(row.getEntityName())) {
            errors.add("产业链实体不能为空");
            return null;
        }
        WujinChainEntityListReqVO listReqVO = new WujinChainEntityListReqVO();
        listReqVO.setName(row.getEntityName());
        listReqVO.setStatus(0);
        List<WujinChainEntityDO> entities = entityService.getEntityList(listReqVO);
        for (WujinChainEntityDO entity : entities) {
            if (row.getEntityName().equals(entity.getName())) {
                return entity;
            }
        }
        errors.add("产业链实体不存在：" + row.getEntityName());
        return null;
    }

    private WujinMerchantRelationSubmissionSaveReqVO submissionReq(WujinMerchantImportReqVO reqVO,
                                                                   WujinMerchantImportPreviewRespVO.RowPreview row) {
        WujinMerchantRelationSubmissionSaveReqVO saveReqVO = new WujinMerchantRelationSubmissionSaveReqVO();
        saveReqVO.setMerchantId(row.getMerchantId());
        saveReqVO.setProductId(row.getProductId());
        saveReqVO.setProductName(row.getProductName());
        saveReqVO.setProductLane(isBlank(reqVO.getProductLane()) ? WujinLane.PRODUCT.name() : reqVO.getProductLane());
        saveReqVO.setProductCategoryId(row.getProductCategoryId());
        saveReqVO.setTemplateId(reqVO.getTemplateId());
        saveReqVO.setAuditStatus(AUDIT_STATUS_IMPORTED_APPROVED);
        saveReqVO.setAuditRoute("IMPORT_APPROVED");
        saveReqVO.setCompletenessScore(100);
        saveReqVO.setRemark("IMPORT：" + nullToEmpty(row.getRemark()));
        return saveReqVO;
    }

    private WujinMerchantRelationItemSaveReqVO itemReq(Long submissionId,
                                                       WujinMerchantImportPreviewRespVO.RowPreview row) {
        WujinMerchantRelationItemSaveReqVO saveReqVO = new WujinMerchantRelationItemSaveReqVO();
        saveReqVO.setSubmissionId(submissionId);
        saveReqVO.setEntityId(row.getEntityId());
        saveReqVO.setRelationType(row.getRelationType());
        saveReqVO.setFromTemplate(false);
        saveReqVO.setRequiredFlag(true);
        saveReqVO.setRemark(row.getRemark());
        return saveReqVO;
    }

    private WujinMerchantSupplyCapabilitySaveReqVO capabilityReq(WujinMerchantImportReqVO reqVO,
                                                                 WujinMerchantImportPreviewRespVO.RowPreview row) {
        WujinMerchantSupplyCapabilitySaveReqVO saveReqVO = new WujinMerchantSupplyCapabilitySaveReqVO();
        saveReqVO.setMerchantId(row.getMerchantId());
        saveReqVO.setProductId(row.getProductId());
        saveReqVO.setProductName(row.getProductName());
        saveReqVO.setEntityId(row.getEntityId());
        saveReqVO.setLane(row.getEntityLane());
        saveReqVO.setIndustry(reqVO.getIndustryCode());
        saveReqVO.setSupplyStatus(0);
        saveReqVO.setStockCount(row.getStockCount());
        saveReqVO.setMinOrderQuantity(row.getMinOrderQuantity());
        saveReqVO.setDeliveryDays(row.getDeliveryDays());
        saveReqVO.setServiceArea(row.getServiceArea());
        saveReqVO.setRemark("商家导入同步：" + nullToEmpty(row.getRemark()));
        return saveReqVO;
    }

    private boolean validRelationType(String relationType) {
        try {
            WujinRelationType.valueOf(relationType);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
