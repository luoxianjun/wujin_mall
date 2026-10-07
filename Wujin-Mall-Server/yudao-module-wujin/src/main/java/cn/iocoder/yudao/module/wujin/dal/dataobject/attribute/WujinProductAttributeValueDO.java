package cn.iocoder.yudao.module.wujin.dal.dataobject.attribute;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 五金商品标准属性值：商家发布时按平台属性字典填写
 */
@TableName("wujin_product_attribute_value")
@KeySequence("wujin_product_attribute_value_seq")
public class WujinProductAttributeValueDO extends BaseDO {

    @TableId
    private Long id;
    private Long submissionId;
    private Long merchantId;
    private Long productId;
    /**
     * 平台属性字典编号，非字典属性为空
     */
    private Long attributeId;
    private String attributeCode;
    private String attributeName;
    private String attributeValue;
    private Boolean standardFlag;
    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public String getAttributeCode() {
        return attributeCode;
    }

    public void setAttributeCode(String attributeCode) {
        this.attributeCode = attributeCode;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public Boolean getStandardFlag() {
        return standardFlag;
    }

    public void setStandardFlag(Boolean standardFlag) {
        this.standardFlag = standardFlag;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
