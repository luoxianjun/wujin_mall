package cn.iocoder.yudao.module.wujin.service.attribute;

import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.attribute.vo.WujinProductCustomTagReviewReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductAttributeValueDO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.attribute.WujinProductCustomTagDO;

import java.util.List;

/**
 * 五金商品标准属性与自定义标签 Service：发布时持久化，自定义标签走平台审核
 */
public interface WujinProductAttributeService {

    int TAG_AUDIT_STATUS_PENDING = 10;
    int TAG_AUDIT_STATUS_APPROVED = 30;
    int TAG_AUDIT_STATUS_REJECTED = 40;

    /**
     * 按平台属性字典校验标准属性：字典属性的值类型与可选值、必填属性是否填写
     *
     * @param lane       商品所在泳道
     * @param attributes 商家填写的标准属性
     */
    void validateStandardAttributes(String lane, List<WujinMerchantRelationSubmitReqVO.StandardAttribute> attributes);

    /**
     * 保存申报单的标准属性值，覆盖该申报单已有属性值
     *
     * @return 保存的属性数量
     */
    int saveStandardAttributes(Long submissionId, Long merchantId, Long productId, String lane,
                               List<WujinMerchantRelationSubmitReqVO.StandardAttribute> attributes);

    /**
     * 提交自定义标签进入平台审核，同一商品已待审或已通过的同名标签不重复提交
     *
     * @return 新提交的待审核标签数量
     */
    int submitCustomTags(Long submissionId, Long merchantId, Long productId, String productName,
                         List<String> tags, String reviewNote);

    List<WujinProductAttributeValueDO> getAttributeValueList(Long submissionId, Long productId);

    List<WujinProductCustomTagDO> getCustomTagList(WujinProductCustomTagListReqVO reqVO);

    /**
     * 获得商品已审核通过的自定义标签名称
     */
    List<String> getApprovedTagNames(Long productId);

    void reviewCustomTag(WujinProductCustomTagReviewReqVO reqVO, Long auditorId);
}
