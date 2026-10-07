<template>
  <view class="recommend-list">
    <view class="recommend-head">
      <text class="recommend-title">推荐商品</text>
    </view>

    <view v-if="hasItems" class="recommend-grid">
      <view
        v-for="(column, columnIndex) in columns"
        :key="columnIndex"
        class="recommend-column"
      >
        <view
          v-for="item in column"
          :key="item.key"
          class="product-card"
          @tap="emit('select', item.raw)"
        >
          <view class="product-media">
            <image
              v-if="item.image"
              class="product-image"
              :src="item.image"
              mode="aspectFill"
              lazy-load
            />
            <view v-else class="product-image product-image--empty">
              <text>五金</text>
            </view>
          </view>

          <view class="product-info">
            <text class="product-title">{{ item.title }}</text>
            <text class="product-price">{{ item.price }}</text>
            <text class="product-supplier">{{ item.supplier }}</text>
            <view class="product-meta">
              <text v-if="item.location" class="meta-text">{{ item.location }}</text>
              <text v-if="item.metric" class="meta-text meta-text--strong">
                {{ item.metric }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-else-if="!loading" class="state state--empty">
      <text class="state-title">暂无推荐商品</text>
      <text class="state-text">稍后再来看看新的五金货源</text>
    </view>

    <view v-if="showFooter" class="state state--footer" @tap="handleLoadMore">
      <text v-if="loading" class="state-text">加载中...</text>
      <text v-else-if="finished" class="state-text">已加载全部</text>
      <text v-else class="load-more">加载更多</text>
    </view>
  </view>
</template>

<script setup>
import { computed } from "vue";

import { sanitizeWujinImageUrl } from "@/utils/wujinImage";

defineOptions({ name: "ProductRecommendList" });

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  finished: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["loadMore", "select"]);

const products = computed(() => props.items.map(normalizeProduct));
const hasItems = computed(() => products.value.length > 0);
const columns = computed(() =>
  products.value.reduce(
    (result, item, index) => {
      result[index % 2].push(item);
      return result;
    },
    [[], []],
  ),
);
const showFooter = computed(() => props.loading || props.finished || hasItems.value);

function pick(item, keys, fallback = "") {
  const key = keys.find((name) => {
    const value = item?.[name];
    return value !== undefined && value !== null && value !== "";
  });
  return key ? item[key] : fallback;
}

function formatPrice(value) {
  if (value === undefined || value === null || value === "") {
    return "待报价";
  }
  const text = typeof value === "number" ? (value / 100).toFixed(2) : String(value);
  return text.startsWith("¥") || text.startsWith("￥") ? text : `¥${text}`;
}

function normalizeProduct(item = {}, index) {
  const location = [item.province, item.city].filter(Boolean).join(" ");

  return {
    raw: item,
    key: pick(item, ["id", "productId", "goodsId", "skuId"], index),
    image: sanitizeWujinImageUrl(pick(item, ["image", "imageUrl", "picUrl", "cover", "thumbnail"])),
    title: pick(item, ["title", "name", "productName", "goodsName"], "五金商品"),
    price: formatPrice(pick(item, ["price", "salePrice", "referencePrice", "minPrice"])),
    supplier: pick(item, ["supplier", "supplierName", "shopName", "companyName"], "优选供应商"),
    location: pick(item, ["location", "deliveryArea"], location),
    metric: pick(item, ["salesText", "soldText", "dealText", "stockText", "stockStatus", "inventoryText"], item.salesCount ? `已售 ${item.salesCount}` : item.stock ? `库存 ${item.stock}` : ""),
  };
}

function handleLoadMore() {
  if (!props.loading && !props.finished) {
    emit("loadMore");
  }
}
</script>

<style lang="scss" scoped>
.recommend-list {
  display: flex;
  width: 100%;
  max-width: 694rpx;
  box-sizing: border-box;
  flex-direction: column;
  gap: 18rpx;
}

.recommend-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: 16rpx;
  border-left: 8rpx solid #ffd21e;
}

.recommend-title {
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.25;
}

.recommend-grid {
  display: flex;
  gap: 16rpx;
  width: 694rpx;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  align-items: flex-start;
}

.recommend-column {
  display: flex;
  flex: 0 0 339rpx;
  width: 339rpx;
  min-width: 0;
  flex-direction: column;
  gap: 16rpx;
}

.product-card {
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  border: 2rpx solid #111111;
  border-radius: 16rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 0 #ffd21e;
}

.product-media {
  width: 100%;
  height: 248rpx;
  background: #f5f6f8;
}

.product-image {
  width: 100%;
  height: 100%;
}

.product-image--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111111;
}

.product-image--empty text {
  color: #ffd21e;
  font-size: 34rpx;
  font-weight: 900;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  padding: 16rpx;
}

.product-title {
  overflow: hidden;
  color: #111111;
  font-size: 27rpx;
  font-weight: 800;
  line-height: 1.35;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-price {
  color: #d89200;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.15;
}

.product-supplier {
  overflow: hidden;
  color: #2b2f36;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  min-height: 34rpx;
}

.meta-text {
  max-width: 100%;
  padding: 5rpx 10rpx;
  background: #f3f4f6;
  color: #5d6673;
  font-size: 20rpx;
  line-height: 1.2;
}

.meta-text--strong {
  background: #ffd21e;
  color: #111111;
  font-weight: 800;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 96rpx;
  padding: 22rpx;
  border: 2rpx dashed #d7dbe2;
  border-radius: 16rpx;
  background: #ffffff;
  text-align: center;
}

.state--empty {
  min-height: 180rpx;
}

.state-title {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}

.state-text {
  margin-top: 8rpx;
  color: #8d96a3;
  font-size: 24rpx;
  line-height: 1.4;
}

.state--footer {
  border-style: solid;
}

.load-more {
  color: #111111;
  font-size: 26rpx;
  font-weight: 900;
}
</style>
