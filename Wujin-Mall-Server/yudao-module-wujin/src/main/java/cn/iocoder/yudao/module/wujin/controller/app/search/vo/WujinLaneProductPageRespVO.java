package cn.iocoder.yudao.module.wujin.controller.app.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "用户 App - 五金泳道商品分页 Response VO")
public class WujinLaneProductPageRespVO {

    private String lane;
    private Long total;
    private List<Item> list;
    /**
     * 当前关键词与分类下可选的品牌，用于品牌筛选
     */
    private List<BrandFacet> brands;
    /**
     * 加工、原材料泳道：由关键词或来源商品沿产业链找到的上游实体
     */
    private List<EntityRef> upstreamEntities;
    /**
     * 无结果时的友好提示
     */
    private String emptyHint;

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Item> getList() {
        return list;
    }

    public void setList(List<Item> list) {
        this.list = list;
    }

    public List<BrandFacet> getBrands() {
        return brands;
    }

    public void setBrands(List<BrandFacet> brands) {
        this.brands = brands;
    }

    public List<EntityRef> getUpstreamEntities() {
        return upstreamEntities;
    }

    public void setUpstreamEntities(List<EntityRef> upstreamEntities) {
        this.upstreamEntities = upstreamEntities;
    }

    public String getEmptyHint() {
        return emptyHint;
    }

    public void setEmptyHint(String emptyHint) {
        this.emptyHint = emptyHint;
    }

    public static class Item {

        private Long id;
        private String name;
        private String picUrl;
        private Integer price;
        private Integer marketPrice;
        private Integer stock;
        private Integer salesCount;
        private Long brandId;
        private String brandName;
        private Long merchantId;
        private Long entityId;
        private String entityName;
        private String lane;
        private Integer minOrderQuantity;
        private Integer deliveryDays;
        private String serviceArea;
        /**
         * 平台审核通过的商品标签
         */
        private List<String> tags;
        /**
         * 成品绑定的上游原材料数量
         */
        private Integer materialCount;

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

        public Long getBrandId() {
            return brandId;
        }

        public void setBrandId(Long brandId) {
            this.brandId = brandId;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
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

        public String getLane() {
            return lane;
        }

        public void setLane(String lane) {
            this.lane = lane;
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

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public Integer getMaterialCount() {
            return materialCount;
        }

        public void setMaterialCount(Integer materialCount) {
            this.materialCount = materialCount;
        }
    }

    public static class BrandFacet {

        private Long brandId;
        private String brandName;
        private Integer productCount;

        public Long getBrandId() {
            return brandId;
        }

        public void setBrandId(Long brandId) {
            this.brandId = brandId;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public Integer getProductCount() {
            return productCount;
        }

        public void setProductCount(Integer productCount) {
            this.productCount = productCount;
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
}
