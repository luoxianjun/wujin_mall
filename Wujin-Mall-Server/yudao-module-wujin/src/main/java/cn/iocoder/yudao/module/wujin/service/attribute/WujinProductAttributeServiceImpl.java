package cn.iocoder.yudao.module.wujin.service.attribute;

import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductCustomTagDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.attribute.WujinProductAttributeValueMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.attribute.WujinProductCustomTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl.VALUE_TYPE_BOOLEAN;
import static cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl.VALUE_TYPE_ENUM;
import static cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl.VALUE_TYPE_MULTI_ENUM;
import static cn.iocoder.yudao.module.wujin.service.attribute.WujinAttributeDictionaryAdminServiceImpl.VALUE_TYPE_NUMBER;

@Service
@Validated
public class WujinProductAttributeServiceImpl implements WujinProductAttributeService {

    static final int MAX_CUSTOM_TAG_COUNT = 10;
    static final int MAX_CUSTOM_TAG_LENGTH = 20;
    private static final int MAX_ATTRIBUTE_NAME_LENGTH = 64;
    private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 255;
    private static final List<String> BOOLEAN_VALUES = Arrays.asList("是", "否", "true", "false");
    private static final String ACTION_APPROVE = "APPROVE";
    private static final String ACTION_REJECT = "REJECT";

    @Resource
    private WujinAttributeDictionaryAdminService attributeDictionaryService;
    @Resource
    private WujinProductAttributeValueMapper attributeValueMapper;
    @Resource
    private WujinProductCustomTagMapper customTagMapper;

    @Override
    public void validateStandardAttributes(String lane,
                                           List<WujinMerchantRelationSubmitReqVO.StandardAttribute> attributes) {
        if (attributes == null) {
            return;
        }
        List<WujinAttributeDictionaryDO> dictionary = attributeDictionaryService.getEnabledAttributeList(lane);
        Set<Long> filledAttributeIds = new HashSet<>();
        Set<String> filledNames = new HashSet<>();
        for (WujinMerchantRelationSubmitReqVO.StandardAttribute attribute : attributes) {
            if (attribute == null || isBlank(attribute.getValue())) {
                continue;
            }
            WujinAttributeDictionaryDO definition = resolveDefinition(dictionary, attribute);
            String name = definition != null ? definition.getName() : trim(attribute.getName());
            if (isBlank(name)) {
                throw new IllegalArgumentException("标准属性名称不能为空");
            }
            if (!filledNames.add(name)) {
                throw new IllegalArgumentException("标准属性「" + name + "」重复填写");
            }
            if (name.length() > MAX_ATTRIBUTE_NAME_LENGTH) {
                throw new IllegalArgumentException("标准属性名称「" + name + "」过长");
            }
            if (attribute.getValue().trim().length() > MAX_ATTRIBUTE_VALUE_LENGTH) {
                throw new IllegalArgumentException("标准属性「" + name + "」的值过长");
            }
            if (definition != null) {
                validateValue(definition, attribute.getValue().trim());
                filledAttributeIds.add(definition.getId());
            }
        }
        for (WujinAttributeDictionaryDO definition : dictionary) {
            if (Boolean.TRUE.equals(definition.getRequiredFlag()) && !filledAttributeIds.contains(definition.getId())) {
                throw new IllegalArgumentException("标准属性「" + definition.getName() + "」为必填项");
            }
        }
    }

    @Override
    public int saveStandardAttributes(Long submissionId, Long merchantId, Long productId, String lane,
                                      List<WujinMerchantRelationSubmitReqVO.StandardAttribute> attributes) {
        if (attributes == null) {
            return 0;
        }
        validateStandardAttributes(lane, attributes);
        List<WujinAttributeDictionaryDO> dictionary = attributeDictionaryService.getEnabledAttributeList(lane);
        attributeValueMapper.deleteBySubmissionId(submissionId);
        int sort = 0;
        for (WujinMerchantRelationSubmitReqVO.StandardAttribute attribute : attributes) {
            if (attribute == null || isBlank(attribute.getValue())) {
                continue;
            }
            WujinAttributeDictionaryDO definition = resolveDefinition(dictionary, attribute);
            WujinProductAttributeValueDO value = new WujinProductAttributeValueDO();
            value.setSubmissionId(submissionId);
            value.setMerchantId(merchantId);
            value.setProductId(productId);
            value.setAttributeValue(attribute.getValue().trim());
            value.setSort(sort++);
            if (definition != null) {
                value.setAttributeId(definition.getId());
                value.setAttributeCode(definition.getCode());
                value.setAttributeName(definition.getName());
                value.setStandardFlag(true);
            } else {
                value.setAttributeCode(trim(attribute.getCode()));
                value.setAttributeName(trim(attribute.getName()));
                value.setStandardFlag(false);
            }
            attributeValueMapper.insert(value);
        }
        return sort;
    }

    @Override
    public int submitCustomTags(Long submissionId, Long merchantId, Long productId, String productName,
                                List<String> tags, String reviewNote) {
        List<String> tagNames = normalizeTags(tags);
        int pendingCount = 0;
        for (String tagName : tagNames) {
            if (hasActiveTag(merchantId, productId, tagName)) {
                continue;
            }
            WujinProductCustomTagDO tag = new WujinProductCustomTagDO();
            tag.setSubmissionId(submissionId);
            tag.setMerchantId(merchantId);
            tag.setProductId(productId);
            tag.setProductName(productName);
            tag.setTagName(tagName);
            tag.setReviewNote(trim(reviewNote));
            tag.setAuditStatus(TAG_AUDIT_STATUS_PENDING);
            customTagMapper.insert(tag);
            pendingCount++;
        }
        return pendingCount;
    }

    @Override
    public List<WujinProductAttributeValueDO> getAttributeValueList(Long submissionId, Long productId) {
        if (submissionId == null && productId == null) {
            return Collections.emptyList();
        }
        return attributeValueMapper.selectList(submissionId, productId);
    }

    @Override
    public List<WujinProductCustomTagDO> getCustomTagList(WujinProductCustomTagListReqVO reqVO) {
        return customTagMapper.selectList(reqVO);
    }

    @Override
    public List<String> getApprovedTagNames(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        Set<String> names = new LinkedHashSet<>();
        for (WujinProductCustomTagDO tag : customTagMapper.selectListByProductIdAndStatus(productId,
                TAG_AUDIT_STATUS_APPROVED)) {
            names.add(tag.getTagName());
        }
        return new ArrayList<>(names);
    }

    @Override
    public void reviewCustomTag(WujinProductCustomTagReviewReqVO reqVO, Long auditorId) {
        WujinProductCustomTagDO tag = customTagMapper.selectById(reqVO.getId());
        if (tag == null) {
            throw new IllegalArgumentException("自定义标签不存在");
        }
        if (!Integer.valueOf(TAG_AUDIT_STATUS_PENDING).equals(tag.getAuditStatus())) {
            throw new IllegalArgumentException("自定义标签已审核，不能重复审核");
        }
        String action = reqVO.getAction().trim().toUpperCase();
        if (ACTION_APPROVE.equals(action)) {
            tag.setAuditStatus(TAG_AUDIT_STATUS_APPROVED);
        } else if (ACTION_REJECT.equals(action)) {
            if (isBlank(reqVO.getComment())) {
                throw new IllegalArgumentException("驳回自定义标签需要填写原因");
            }
            tag.setAuditStatus(TAG_AUDIT_STATUS_REJECTED);
        } else {
            throw new IllegalArgumentException("审核动作无效：" + reqVO.getAction());
        }
        tag.setAuditComment(trim(reqVO.getComment()));
        tag.setAuditorId(auditorId);
        tag.setAuditTime(LocalDateTime.now());
        customTagMapper.updateById(tag);
    }

    private WujinAttributeDictionaryDO resolveDefinition(List<WujinAttributeDictionaryDO> dictionary,
                                                         WujinMerchantRelationSubmitReqVO.StandardAttribute attribute) {
        for (WujinAttributeDictionaryDO definition : dictionary) {
            if (attribute.getAttributeId() != null && attribute.getAttributeId().equals(definition.getId())) {
                return definition;
            }
        }
        for (WujinAttributeDictionaryDO definition : dictionary) {
            if (!isBlank(attribute.getCode()) && attribute.getCode().trim().equalsIgnoreCase(definition.getCode())) {
                return definition;
            }
        }
        for (WujinAttributeDictionaryDO definition : dictionary) {
            if (!isBlank(attribute.getName()) && attribute.getName().trim().equals(definition.getName())) {
                return definition;
            }
        }
        return null;
    }

    private void validateValue(WujinAttributeDictionaryDO definition, String value) {
        String valueType = definition.getValueType();
        if (VALUE_TYPE_NUMBER.equals(valueType)) {
            try {
                new BigDecimal(value);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("标准属性「" + definition.getName() + "」需要填写数字");
            }
        } else if (VALUE_TYPE_BOOLEAN.equals(valueType)) {
            if (!BOOLEAN_VALUES.contains(value)) {
                throw new IllegalArgumentException("标准属性「" + definition.getName() + "」只能填写是或否");
            }
        } else if (VALUE_TYPE_ENUM.equals(valueType)) {
            validateOption(definition, value);
        } else if (VALUE_TYPE_MULTI_ENUM.equals(valueType)) {
            for (String item : value.split("[,，、]")) {
                if (!isBlank(item)) {
                    validateOption(definition, item.trim());
                }
            }
        }
    }

    private void validateOption(WujinAttributeDictionaryDO definition, String value) {
        List<String> options = definition.getValueOptions() == null
                ? Collections.<String>emptyList() : definition.getValueOptions();
        if (!options.contains(value)) {
            throw new IllegalArgumentException("标准属性「" + definition.getName() + "」的值「" + value
                    + "」不在可选范围内");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        Set<String> names = new LinkedHashSet<>();
        for (String tag : tags) {
            if (isBlank(tag)) {
                continue;
            }
            String name = tag.trim();
            if (name.length() > MAX_CUSTOM_TAG_LENGTH) {
                throw new IllegalArgumentException("自定义标签「" + name + "」不能超过 " + MAX_CUSTOM_TAG_LENGTH + " 个字");
            }
            names.add(name);
        }
        if (names.size() > MAX_CUSTOM_TAG_COUNT) {
            throw new IllegalArgumentException("自定义标签最多 " + MAX_CUSTOM_TAG_COUNT + " 个");
        }
        return new ArrayList<>(names);
    }

    private boolean hasActiveTag(Long merchantId, Long productId, String tagName) {
        for (WujinProductCustomTagDO tag : customTagMapper.selectListByProductAndTag(merchantId, productId, tagName)) {
            if (!Integer.valueOf(TAG_AUDIT_STATUS_REJECTED).equals(tag.getAuditStatus())) {
                return true;
            }
        }
        return false;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
