package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.chain.WujinRelationType;
import cn.iocoder.yudao.module.wujin.controller.admin.chain.vo.WujinChainEntityListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportPreviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportResultRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationSubmissionSaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.supply.vo.WujinMerchantSupplyCapabilitySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.chain.WujinChainEntityDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import cn.iocoder.yudao.module.wujin.service.chain.WujinChainEntityAdminService;
import cn.iocoder.yudao.module.wujin.service.supply.WujinMerchantSupplyCapabilityAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateAdminService;
import cn.iocoder.yudao.module.wujin.service.template.WujinIndustryTemplateItemAdminService;
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
    private static final int AUDIT_STATUS_WAIT_REVIEW = 10;
    private static final int SUPPLY_STATUS_ENABLED = 0;
    private static final int SUPPLY_STATUS_DISABLED = 1;

    @Resource
    private WujinIndustryTemplateAdminService templateService;
    @Resource
    private WujinIndustryTemplateItemAdminService templateItemService;
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
        List<WujinIndustryTemplateItemDO> templateItems = getTemplateItems(reqVO.getTemplateId());
        List<WujinMerchantImportPreviewRespVO.RowPreview> validRows = new ArrayList<>();
        List<WujinMerchantImportPreviewRespVO.RowPreview> invalidRows = new ArrayList<>();
        List<WujinMerchantImportReqVO.ImportRow> rows = reqVO.getRows() == null
                ? Collections.<WujinMerchantImportReqVO.ImportRow>emptyList() : reqVO.getRows();
        for (int i = 0; i < rows.size(); i++) {
            WujinMerchantImportPreviewRespVO.RowPreview preview = previewRow(reqVO, template, rows.get(i), i + 1);
            preview.setTemplateMatched(matchesTemplate(templateItems, preview));
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
        int effectiveCount = 0;
        for (WujinMerchantImportPreviewRespVO.RowPreview row : preview.getValidRows()) {
            Long submissionId = submissionService.createSubmission(submissionReq(reqVO, row));
            itemService.createItem(itemReq(submissionId, row));
            capabilityService.saveOrUpdateCapability(capabilityReq(reqVO, row));
            submissionIds.add(submissionId);
            if (Boolean.TRUE.equals(row.getTemplateMatched())) {
                effectiveCount++;
            }
        }

        WujinMerchantImportResultRespVO respVO = new WujinMerchantImportResultRespVO();
        respVO.setImportedCount(submissionIds.size());
        respVO.setEffectiveCount(effectiveCount);
        respVO.setPendingReviewCount(submissionIds.size() - effectiveCount);
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
        preview.setEntityId(row.getEntityId());
        preview.setEntityName(row.getEntityName());
        preview.setRelationType(normalizeRelationType(row.getRelationType()));
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
        if (isBlank(preview.getRelationType()) || !validRelationType(preview.getRelationType())) {
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
        if (Boolean.TRUE.equals(row.getTemplateMatched())) {
            saveReqVO.setAuditStatus(AUDIT_STATUS_IMPORTED_APPROVED);
            saveReqVO.setAuditRoute("IMPORT_APPROVED");
            saveReqVO.setRemark("IMPORT：" + nullToEmpty(row.getRemark()));
        } else {
            saveReqVO.setAuditStatus(AUDIT_STATUS_WAIT_REVIEW);
            saveReqVO.setAuditRoute("MANUAL_REVIEW");
            saveReqVO.setRemark("IMPORT（关系不在行业模板中，待平台审核）：" + nullToEmpty(row.getRemark()));
        }
        saveReqVO.setCompletenessScore(100);
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
        saveReqVO.setSupplyStatus(Boolean.TRUE.equals(row.getTemplateMatched())
                ? SUPPLY_STATUS_ENABLED : SUPPLY_STATUS_DISABLED);
        saveReqVO.setStockCount(row.getStockCount());
        saveReqVO.setMinOrderQuantity(row.getMinOrderQuantity());
        saveReqVO.setDeliveryDays(row.getDeliveryDays());
        saveReqVO.setServiceArea(row.getServiceArea());
        saveReqVO.setRemark("商家导入同步：" + nullToEmpty(row.getRemark()));
        return saveReqVO;
    }

    private List<WujinIndustryTemplateItemDO> getTemplateItems(Long templateId) {
        if (templateId == null) {
            return Collections.emptyList();
        }
        WujinIndustryTemplateItemListReqVO listReqVO = new WujinIndustryTemplateItemListReqVO();
        listReqVO.setTemplateId(templateId);
        return templateItemService.getTemplateItemList(listReqVO);
    }

    private boolean matchesTemplate(List<WujinIndustryTemplateItemDO> templateItems,
                                    WujinMerchantImportPreviewRespVO.RowPreview row) {
        if (row.getEntityId() == null || row.getRelationType() == null) {
            return false;
        }
        for (WujinIndustryTemplateItemDO item : templateItems) {
            if (row.getEntityId().equals(item.getEntityId()) && row.getRelationType().equals(item.getRelationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 兼容 Excel 中填写的中文关系类型
     */
    private String normalizeRelationType(String relationType) {
        if (isBlank(relationType)) {
            return relationType;
        }
        String value = relationType.trim();
        if ("原材料".equals(value) || "材料".equals(value)) {
            return WujinRelationType.REQUIRES_MATERIAL.name();
        }
        if ("加工工艺".equals(value) || "工艺".equals(value) || "加工".equals(value)) {
            return WujinRelationType.REQUIRES_PROCESS.name();
        }
        if ("设备".equals(value)) {
            return WujinRelationType.REQUIRES_EQUIPMENT.name();
        }
        return value.toUpperCase();
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
