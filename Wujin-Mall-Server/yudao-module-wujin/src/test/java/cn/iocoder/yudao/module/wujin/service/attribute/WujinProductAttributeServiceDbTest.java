package cn.iocoder.yudao.module.wujin.service.attribute;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionaryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinAttributeDictionarySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinAttributeDictionaryDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductCustomTagDO;
import cn.iocoder.yudao.module.wujin.search.WujinLane;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({WujinAttributeDictionaryAdminServiceImpl.class, WujinProductAttributeServiceImpl.class})
class WujinProductAttributeServiceDbTest extends BaseDbUnitTest {

    @Resource
    private WujinAttributeDictionaryAdminService attributeDictionaryService;
    @Resource
    private WujinProductAttributeService productAttributeService;

    @Test
    void createAttributeNormalizesCodeOptionsAndRejectsDuplicates() {
        WujinAttributeDictionarySaveReqVO reqVO = attributeReq(" tire_width ", "胎宽", WujinLane.PRODUCT.name(),
                "ENUM", Arrays.asList("205", " 205 ", "", "215"), true);
        Long id = attributeDictionaryService.createAttribute(reqVO);

        WujinAttributeDictionaryDO attribute = attributeDictionaryService.getAttribute(id);
        assertEquals("TIRE_WIDTH", attribute.getCode());
        assertEquals(Arrays.asList("205", "215"), attribute.getValueOptions());
        assertEquals(Boolean.FALSE, attribute.getSearchableFlag());

        assertThrows(IllegalArgumentException.class, () -> attributeDictionaryService.createAttribute(
                attributeReq("TIRE_WIDTH", "胎宽重复", WujinLane.PRODUCT.name(), "TEXT", null, false)));
        assertThrows(IllegalArgumentException.class, () -> attributeDictionaryService.createAttribute(
                attributeReq("EMPTY_ENUM", "空枚举", WujinLane.PRODUCT.name(), "ENUM", Collections.<String>emptyList(), false)));
        assertThrows(IllegalArgumentException.class, () -> attributeDictionaryService.createAttribute(
                attributeReq("BAD_LANE", "错误泳道", "FINISHED", "TEXT", null, false)));
    }

    @Test
    void enabledAttributeListIncludesLaneAndCommonAttributesOnly() {
        attributeDictionaryService.createAttribute(attributeReq("SPEC", "规格型号", WujinLane.PRODUCT.name(), "TEXT", null, true));
        attributeDictionaryService.createAttribute(attributeReq("ORIGIN", "产地", null, "TEXT", null, false));
        attributeDictionaryService.createAttribute(attributeReq("GRADE", "材质牌号", WujinLane.MATERIAL.name(), "TEXT", null, true));
        WujinAttributeDictionarySaveReqVO disabled = attributeReq("OLD", "停用属性", WujinLane.PRODUCT.name(), "TEXT", null, false);
        disabled.setStatus(1);
        attributeDictionaryService.createAttribute(disabled);

        List<String> productCodes = attributeDictionaryService.getEnabledAttributeList(WujinLane.PRODUCT.name()).stream()
                .map(WujinAttributeDictionaryDO::getCode).collect(Collectors.toList());
        assertEquals(Arrays.asList("SPEC", "ORIGIN"), productCodes);

        WujinAttributeDictionaryListReqVO listReqVO = new WujinAttributeDictionaryListReqVO();
        listReqVO.setLane(WujinLane.MATERIAL.name());
        assertEquals(1, attributeDictionaryService.getAttributeList(listReqVO).size());
    }

    @Test
    void validateStandardAttributesChecksRequiredTypesAndOptions() {
        attributeDictionaryService.createAttribute(attributeReq("SPEC", "规格型号", WujinLane.PRODUCT.name(), "TEXT", null, true));
        attributeDictionaryService.createAttribute(attributeReq("LOAD_INDEX", "载重指数", WujinLane.PRODUCT.name(), "NUMBER", null, false));
        attributeDictionaryService.createAttribute(attributeReq("SEASON", "适用季节", WujinLane.PRODUCT.name(), "ENUM",
                Arrays.asList("夏季", "冬季", "四季"), false));

        IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, () ->
                productAttributeService.validateStandardAttributes(WujinLane.PRODUCT.name(),
                        Collections.singletonList(attribute(null, "产地", "山东"))));
        assertTrue(missing.getMessage().contains("规格型号"));

        assertThrows(IllegalArgumentException.class, () -> productAttributeService.validateStandardAttributes(
                WujinLane.PRODUCT.name(), Arrays.asList(attribute("SPEC", null, "205/55R16"),
                        attribute("LOAD_INDEX", null, "九十一"))));
        assertThrows(IllegalArgumentException.class, () -> productAttributeService.validateStandardAttributes(
                WujinLane.PRODUCT.name(), Arrays.asList(attribute("SPEC", null, "205/55R16"),
                        attribute(null, "适用季节", "雨季"))));
        assertThrows(IllegalArgumentException.class, () -> productAttributeService.validateStandardAttributes(
                WujinLane.PRODUCT.name(), Arrays.asList(attribute("SPEC", null, "205/55R16"),
                        attribute(null, "规格型号", "215/60R16"))));

        productAttributeService.validateStandardAttributes(WujinLane.PRODUCT.name(), Arrays.asList(
                attribute("spec", null, "205/55R16"), attribute(null, "载重指数", "91"),
                attribute(null, "适用季节", "四季"), attribute(null, "产地", "山东")));
        productAttributeService.validateStandardAttributes(WujinLane.PRODUCT.name(), null);
    }

    @Test
    void saveStandardAttributesReplacesSubmissionValuesAndKeepsCustomNames() {
        Long specId = attributeDictionaryService.createAttribute(
                attributeReq("SPEC", "规格型号", WujinLane.PRODUCT.name(), "TEXT", null, true));

        productAttributeService.saveStandardAttributes(501L, 1001L, 2001L, WujinLane.PRODUCT.name(),
                Arrays.asList(attribute("SPEC", null, "195/65R15"), attribute(null, "产地", "山东")));
        int saved = productAttributeService.saveStandardAttributes(501L, 1001L, 2001L, WujinLane.PRODUCT.name(),
                Arrays.asList(attribute(null, "规格型号", " 205/55R16 "), attribute(null, "产地", "江苏"),
                        attribute(null, "空值属性", " ")));

        assertEquals(2, saved);
        List<WujinProductAttributeValueDO> values = productAttributeService.getAttributeValueList(501L, null);
        assertEquals(2, values.size());
        assertEquals(specId, values.get(0).getAttributeId());
        assertEquals("SPEC", values.get(0).getAttributeCode());
        assertEquals("205/55R16", values.get(0).getAttributeValue());
        assertEquals(Boolean.TRUE, values.get(0).getStandardFlag());
        assertEquals("产地", values.get(1).getAttributeName());
        assertEquals(Boolean.FALSE, values.get(1).getStandardFlag());
        assertEquals(0, productAttributeService.saveStandardAttributes(502L, 1001L, 2001L,
                WujinLane.PRODUCT.name(), null));
    }

    @Test
    void customTagsAreSubmittedOnceAndReviewedByPlatform() {
        int pending = productAttributeService.submitCustomTags(601L, 1001L, 2001L, "高耐磨轮胎",
                Arrays.asList("耐高温", " 耐高温 ", "静音", ""), "矿山工况实测");
        assertEquals(2, pending);
        assertEquals(0, productAttributeService.submitCustomTags(602L, 1001L, 2001L, "高耐磨轮胎",
                Collections.singletonList("静音"), null));

        WujinProductCustomTagListReqVO listReqVO = new WujinProductCustomTagListReqVO();
        listReqVO.setAuditStatus(WujinProductAttributeService.TAG_AUDIT_STATUS_PENDING);
        List<WujinProductCustomTagDO> tags = productAttributeService.getCustomTagList(listReqVO);
        assertEquals(2, tags.size());
        WujinProductCustomTagDO quiet = tags.stream().filter(tag -> "静音".equals(tag.getTagName())).findFirst().get();
        WujinProductCustomTagDO heat = tags.stream().filter(tag -> "耐高温".equals(tag.getTagName())).findFirst().get();
        assertEquals("矿山工况实测", heat.getReviewNote());

        productAttributeService.reviewCustomTag(review(heat.getId(), "approve", null), 1L);
        assertThrows(IllegalArgumentException.class, () ->
                productAttributeService.reviewCustomTag(review(quiet.getId(), "REJECT", " "), 1L));
        productAttributeService.reviewCustomTag(review(quiet.getId(), "REJECT", "缺少检测报告"), 1L);
        assertThrows(IllegalArgumentException.class, () ->
                productAttributeService.reviewCustomTag(review(heat.getId(), "REJECT", "重复审核"), 1L));

        assertEquals(Collections.singletonList("耐高温"), productAttributeService.getApprovedTagNames(2001L));
        listReqVO.setAuditStatus(WujinProductAttributeService.TAG_AUDIT_STATUS_REJECTED);
        WujinProductCustomTagDO rejected = productAttributeService.getCustomTagList(listReqVO).get(0);
        assertEquals("缺少检测报告", rejected.getAuditComment());
        assertEquals(1L, rejected.getAuditorId());
        assertNotNull(rejected.getAuditTime());

        // 驳回后的标签允许商家修改说明后再次提交
        assertEquals(1, productAttributeService.submitCustomTags(603L, 1001L, 2001L, "高耐磨轮胎",
                Collections.singletonList("静音"), "补充检测报告"));
    }

    @Test
    void customTagsRejectTooLongOrTooManyTags() {
        assertThrows(IllegalArgumentException.class, () -> productAttributeService.submitCustomTags(701L, 1001L,
                2001L, "商品", Collections.singletonList("这是一个超过二十个字符长度限制的自定义标签名称"), null));
        assertThrows(IllegalArgumentException.class, () -> productAttributeService.submitCustomTags(701L, 1001L,
                2001L, "商品", Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"), null));
        assertFalse(productAttributeService.getCustomTagList(new WujinProductCustomTagListReqVO()).iterator().hasNext());
    }

    private WujinAttributeDictionarySaveReqVO attributeReq(String code, String name, String lane, String valueType,
                                                           List<String> options, boolean required) {
        WujinAttributeDictionarySaveReqVO reqVO = new WujinAttributeDictionarySaveReqVO();
        reqVO.setCode(code);
        reqVO.setName(name);
        reqVO.setGroupName("成品属性");
        reqVO.setLane(lane);
        reqVO.setValueType(valueType);
        reqVO.setValueOptions(options);
        reqVO.setRequiredFlag(required);
        reqVO.setStatus(0);
        return reqVO;
    }

    private WujinMerchantRelationSubmitReqVO.StandardAttribute attribute(String code, String name, String value) {
        WujinMerchantRelationSubmitReqVO.StandardAttribute attribute = new WujinMerchantRelationSubmitReqVO.StandardAttribute();
        attribute.setCode(code);
        attribute.setName(name);
        attribute.setValue(value);
        return attribute;
    }

    private WujinProductCustomTagReviewReqVO review(Long id, String action, String comment) {
        WujinProductCustomTagReviewReqVO reqVO = new WujinProductCustomTagReviewReqVO();
        reqVO.setId(id);
        reqVO.setAction(action);
        reqVO.setComment(comment);
        return reqVO;
    }
}
