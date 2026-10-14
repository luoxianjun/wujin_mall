package cn.iocoder.yudao.module.wujin.controller.app.detail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金商品详情 Response VO：只返回商品真实数据，缺失项为空")
public class WujinProductDetailRespVO {

    /**
     * 商品是否存在且在售；false 时前端展示“该商品已下架或不存在”
     */
    private Boolean exists;
    private Long id;
    private String name;
    private String picUrl;
    private List<String> sliderPicUrls;
    private Integer price;
    private Integer marketPrice;
    private Integer stock;
    private Integer salesCount;
    private String brandName;
    private String introduction;
    /**
     * 商品详情富文本
     */
    private String description;
    private String lane;
    private Long merchantId;
    private Long entityId;
    private String entityName;
    private Integer minOrderQuantity;
    private Integer deliveryDays;
    private String serviceArea;
    private String riskNote;
    /**
     * 规格参数：商家按平台属性字典填写的标准属性及品牌等真实字段
     */
    private List<Spec> specs;
    /**
     * 平台审核通过的商品标签
     */
    private List<String> tags;
    private List<EntityRef> upstreamMaterials;
    private List<EntityRef> upstreamProcesses;
    private List<RelatedProduct> relatedProducts;

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public List<String> getSliderPicUrls() {
        return sliderPicUrls;
    }

    public void setSliderPicUrls(List<String> sliderPicUrls) {
        this.sliderPicUrls = sliderPicUrls;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(Integer marketPrice) {
        this.marketPrice = marketPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(Integer minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getRiskNote() {
        return riskNote;
    }

    public void setRiskNote(String riskNote) {
        this.riskNote = riskNote;
    }

    public List<Spec> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Spec> specs) {
        this.specs = specs;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<EntityRef> getUpstreamMaterials() {
        return upstreamMaterials;
    }

    public void setUpstreamMaterials(List<EntityRef> upstreamMaterials) {
        this.upstreamMaterials = upstreamMaterials;
    }

    public List<EntityRef> getUpstreamProcesses() {
        return upstreamProcesses;
    }

    public void setUpstreamProcesses(List<EntityRef> upstreamProcesses) {
        this.upstreamProcesses = upstreamProcesses;
    }

    public List<RelatedProduct> getRelatedProducts() {
        return relatedProducts;
    }

    public void setRelatedProducts(List<RelatedProduct> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    public static class Spec {

        private String label;
        private String value;

        public Spec() {
        }

        public Spec(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class EntityRef {

        private Long id;
        private String name;
        private String lane;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
        }
    }

    public static class RelatedProduct {

        private Long id;
        private String name;
        private String picUrl;
        private Integer price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }
    }
}
