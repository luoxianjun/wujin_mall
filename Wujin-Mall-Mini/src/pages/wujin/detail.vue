<template>
  <view class="wujin-page detail-page">
    <view class="detail-commerce-header" :style="headerStyle">
      <button class="header-icon-button" @tap="handleBack">
        <AppIcon name="arrow_back" variant="dark" size="38rpx" />
      </button>
      <view class="detail-search-pill" @tap="handleBackToSearch">
        <AppIcon name="search" variant="muted" size="28rpx" />
        <text class="detail-search-text">{{ displayName }}</text>
      </view>
      <view class="header-actions">
        <button class="header-icon-button" @tap="handleFavorite">
          <AppIcon name="bookmark_border" variant="dark" size="34rpx" />
        </button>
        <button class="header-icon-button" @tap="handleShare">
          <AppIcon name="share" variant="dark" size="34rpx" />
        </button>
      </view>
    </view>

    <view v-if="!loaded" class="detail-state-card">
      <text class="detail-state-text">正在加载详情...</text>
    </view>

    <view v-else-if="notFound" class="detail-state-card detail-not-found">
      <view class="detail-state-icon">
        <AppIcon name="block" variant="muted" size="64rpx" />
      </view>
      <text class="detail-state-title">{{ notFoundText }}</text>
      <text v-if="detail && detail.name" class="detail-state-text">{{ detail.name }}</text>
      <button class="wujin-button wujin-button--yellow detail-state-button" @tap="handleBackToSearch">
        返回搜索列表
      </button>
    </view>

    <scroll-view v-else class="detail-scroll" scroll-y>
      <view class="detail-gallery">
        <image
          v-if="activeProductImage"
          class="detail-gallery-image"
          :src="activeProductImage"
          mode="aspectFill"
        />
        <view v-else class="detail-gallery-image detail-gallery-image--empty">
          <text class="detail-gallery-empty-title">{{ laneDisplayTag(currentLane) || "五金" }}</text>
          <text class="detail-gallery-empty-text">暂无图片</text>
        </view>
        <view class="gallery-count">
          <text>{{ galleryCountText }}</text>
        </view>
      </view>

      <view class="purchase-decision-card">
        <view class="purchase-summary">
          <view class="purchase-copy">
            <text class="purchase-name">{{ displayName }}</text>
            <text v-if="summaryText" class="purchase-intro">{{ summaryText }}</text>
            <view v-if="productTags.length" class="detail-tags detail-tags--compact">
              <text
                v-for="(tag, index) in productTags"
                :key="tag"
                :class="['detail-chip', index === 0 ? 'detail-chip--active' : '']"
              >
                {{ tag }}
              </text>
            </view>
          </view>
        </view>

        <view class="purchase-grid">
          <view class="purchase-item">
            <text class="purchase-label">单价</text>
            <view class="price-line">
              <text v-if="priceParts.symbol" class="price-symbol">{{ priceParts.symbol }}</text>
              <text class="purchase-value purchase-price">{{ priceParts.amount }}</text>
              <text class="price-unit">{{ priceParts.unit }}</text>
            </view>
          </view>
          <view class="purchase-item">
            <text class="purchase-label">起订量 (MOQ)</text>
            <text class="purchase-value">{{ moqText }}</text>
          </view>
          <view class="purchase-item">
            <text class="purchase-label">现货库存</text>
            <text class="purchase-value">{{ inventoryText }}</text>
          </view>
          <view class="purchase-item">
            <text class="purchase-label">交期</text>
            <text class="purchase-value purchase-delivery">{{ deliveryText }}</text>
          </view>
        </view>
      </view>

      <view class="spec-panel">
        <scroll-view class="detail-tabs" scroll-x>
          <view class="detail-tabs-inner">
            <text
              v-for="tab in detailTabs"
              :key="tab.value"
              :class="['detail-tab', activeTab === tab.value ? 'detail-tab--active' : '']"
              @tap="activeTab = tab.value"
            >
              {{ tab.label }}
            </text>
          </view>
        </scroll-view>

        <view v-if="activeTab === 'spec'" class="spec-list">
          <view v-for="item in specificationRows" :key="item.label" class="spec-row">
            <text class="spec-label">{{ item.label }}</text>
            <text class="spec-value">{{ item.value }}</text>
          </view>
          <view v-if="!specificationRows.length" class="tab-empty">
            <text class="tab-empty-text">商家暂未填写规格参数</text>
          </view>
          <view v-if="descriptionNodes" class="detail-description">
            <text class="detail-description-title">商品介绍</text>
            <rich-text :nodes="descriptionNodes" />
          </view>
        </view>

        <view v-else-if="activeTab === 'drawing'" class="tab-empty">
          <text class="tab-empty-text">商家暂未上传技术图纸</text>
          <button class="wujin-button wujin-button--line tab-empty-button" @tap="handleSourcingOpen">
            向供应商索取
          </button>
        </view>

        <view v-else-if="activeTab === 'report'" class="tab-empty">
          <text class="tab-empty-text">商家暂未上传质检报告</text>
          <button class="wujin-button wujin-button--line tab-empty-button" @tap="handleSourcingOpen">
            向供应商索取
          </button>
        </view>

        <view v-else class="related-panel">
          <view v-if="relatedProducts.length" class="related-grid">
            <view
              v-for="item in relatedProducts"
              :key="item.id"
              class="related-card"
              @tap="handleRelatedOpen(item)"
            >
              <image
                v-if="item.image"
                class="related-image"
                :src="item.image"
                mode="aspectFill"
                lazy-load
              />
              <view v-else class="related-image related-image--empty"></view>
              <text class="related-name">{{ item.name }}</text>
              <text class="related-price">{{ item.price }}</text>
            </view>
          </view>
          <view v-else class="tab-empty">
            <text class="tab-empty-text">暂无相关产品</text>
          </view>
        </view>
      </view>

      <view v-if="isProductMode" class="upstream-board">
        <view class="upstream-group">
          <text class="upstream-group-title">上游原材料</text>
          <view v-if="upstreamMaterials.length" class="upstream-chip-list">
            <text
              v-for="item in upstreamMaterials"
              :key="item.id"
              class="upstream-chip upstream-chip--material"
              @tap="handleEntityOpen(item)"
            >
              {{ item.name }}
            </text>
          </view>
          <text v-else class="upstream-empty">暂无上游关联的原材料</text>
        </view>
        <view class="upstream-group">
          <text class="upstream-group-title">上游加工工艺</text>
          <view v-if="upstreamProcesses.length" class="upstream-chip-list">
            <text
              v-for="item in upstreamProcesses"
              :key="item.id"
              class="upstream-chip upstream-chip--process"
              @tap="handleEntityOpen(item)"
            >
              {{ item.name }}
            </text>
          </view>
          <text v-else class="upstream-empty">暂无上游关联的加工工艺</text>
        </view>
      </view>

      <view v-if="riskNotice" class="risk-ticket">
        <view class="risk-ticket__head">
          <AppIcon name="report" variant="warning" size="40rpx" />
          <view class="risk-head-copy">
            <text class="risk-kicker">跨行业差异提示</text>
            <text class="risk-title">应用场景风险提示</text>
            <text class="risk-text">{{ riskNotice }}</text>
          </view>
        </view>
      </view>

      <view class="detail-action-list">
        <view class="detail-action-row" @tap="handleSupplierCapabilityOpen">
          <view class="detail-action-icon detail-action-icon--blue">
            <AppIcon name="assignment" variant="primary" size="34rpx" />
          </view>
          <view class="detail-action-copy">
            <text class="detail-action-title">供应能力评估</text>
            <text class="detail-action-desc">查看工厂资质与产能数据</text>
          </view>
          <AppIcon name="chevron_right" variant="dark" size="34rpx" />
        </view>
        <view class="detail-action-row" @tap="handleTraceMapOpen">
          <view class="detail-action-icon detail-action-icon--green">
            <AppIcon name="qr_code_scanner" variant="success" size="34rpx" />
          </view>
          <view class="detail-action-copy">
            <text class="detail-action-title">溯源图谱</text>
            <text class="detail-action-desc">查看原材料至成品全链路</text>
          </view>
          <AppIcon name="chevron_right" variant="dark" size="34rpx" />
        </view>
        <view class="detail-product-actions">
          <button class="wujin-button wujin-button--yellow" @tap="handleSourcingOpen">
            一键寻源
          </button>
          <button class="wujin-button wujin-button--line" @tap="handleMaterialLaneOpen">
            查看原材料
          </button>
        </view>
      </view>

      <view v-if="industryComparison.length" class="comparison-board">
        <view class="comparison-head">
          <text class="comparison-title">对比展示</text>
          <text class="comparison-note">同名不默认互换</text>
        </view>
        <view class="compare-columns">
          <view
            v-for="item in industryComparison"
            :key="item.industry"
            class="compare-column"
          >
            <text class="compare-title">{{ item.industry }}</text>
            <text class="compare-standard">{{ item.standard }}</text>
            <text class="compare-note">{{ item.note }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view v-if="loaded && !notFound" class="detail-bottom-bar">
      <view class="bottom-secondary-actions">
        <button class="bottom-mini-action" @tap="handleSupplierCapabilityOpen">
          <AppIcon name="badge" variant="dark" size="34rpx" />
          <text>供应商</text>
        </button>
        <button class="bottom-mini-action" @tap="handleChatOpen">
          <AppIcon name="chat_bubble_outline" variant="dark" size="34rpx" />
          <text>沟通</text>
        </button>
      </view>
      <view class="detail-action-buttons action-panel">
        <button
          class="wujin-button wujin-button--secondary detail-cart-button"
          @tap="handleInquiryOpen"
        >
          <AppIcon name="add" variant="dark" size="28rpx" />
          <text>加入询价单</text>
        </button>
        <button
          class="wujin-button wujin-button--primary detail-primary-button"
          @tap="handleSupplierCapabilityOpen"
        >
          查看供应商
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import { getWujinEntityDetail, getWujinProductDetail } from "@/api/wujin/detail";
import { getWujinLaneProducts } from "@/api/wujin/search";
import { showToast } from "@/utils/toast";
import { sanitizeWujinImageList, sanitizeWujinImageUrl } from "@/utils/wujinImage";

defineOptions({ name: "WujinMiniDetail" });

const detailTabs = [
  { label: "规格参数", value: "spec" },
  { label: "技术图纸", value: "drawing" },
  { label: "质检报告", value: "report" },
  { label: "相关产品", value: "related" },
];

// productId 打开商城商品真实详情；否则按产业链实体（id 或 keyword + lane）打开工艺/原材料详情
const productId = ref("");
const entityId = ref("");
const entityType = ref("");
const currentLane = ref("PRODUCT");
const keyword = ref("");
const sourceKeyword = ref("");
const statusBarHeight = ref(0);
const detail = ref(null);
const loaded = ref(false);
const activeTab = ref("spec");
const entityRelatedProducts = ref([]);

const headerStyle = computed(() => ({
  paddingTop: `${statusBarHeight.value}px`,
}));

const isProductMode = computed(() => Boolean(productId.value));
const notFound = computed(() => loaded.value && (!detail.value || detail.value.exists === false));
const notFoundText = computed(() => {
  if (isProductMode.value) {
    return "该商品已下架或不存在";
  }
  return detail.value?.summary || "该实体已下架或不存在";
});
const displayName = computed(() => detail.value?.name || keyword.value || "五金详情");
const summaryText = computed(() =>
  isProductMode.value ? detail.value?.introduction || "" : detail.value?.summary || "",
);

const detailImages = computed(() => {
  if (!detail.value) {
    return [];
  }
  const candidates = [detail.value.picUrl, ...(detail.value.sliderPicUrls || [])];
  return [...new Set(sanitizeWujinImageList(candidates.filter(Boolean)))].slice(0, 5);
});
const activeProductImage = computed(() => detailImages.value[0] || "");
const galleryCountText = computed(() => (detailImages.value.length ? `1/${detailImages.value.length}` : "无图"));

const productTags = computed(() => {
  const tags = [
    laneDisplayTag(detail.value?.lane || currentLane.value),
    detail.value?.brandName,
    detail.value?.entityName,
    ...(Array.isArray(detail.value?.tags) ? detail.value.tags : []),
    detail.value?.industry,
  ];
  return [...new Set(tags.map((item) => (typeof item === "string" ? item.trim() : "")).filter(Boolean))].slice(0, 5);
});

const priceParts = computed(() => {
  const price = detail.value?.price;
  if (typeof price === "number") {
    return { symbol: "¥", amount: (price / 100).toFixed(2), unit: "" };
  }
  const text = firstText(price);
  const matched = text.match(/^([¥￥])\s*([0-9]+(?:\.[0-9]+)?)(.*)$/);
  if (matched) {
    return { symbol: matched[1], amount: matched[2], unit: matched[3] };
  }
  return { symbol: "", amount: text || "询价", unit: "" };
});
const moqText = computed(() => {
  const moq = detail.value?.minOrderQuantity;
  return moq ? `${moq}起订` : firstText(detail.value?.moq, "面议");
});
const inventoryText = computed(() => {
  const stock = detail.value?.stock;
  return stock === undefined || stock === null ? "以供应商确认为准" : `${stock}`;
});
const deliveryText = computed(() => {
  const days = detail.value?.deliveryDays;
  return days ? `${days}天发货` : "以供应商确认为准";
});
const riskNotice = computed(() =>
  firstText(detail.value?.riskNote, (detail.value?.crossIndustryTips || [])[0]),
);
const industryComparison = computed(() => detail.value?.industryComparison || []);
const upstreamMaterials = computed(() => detail.value?.upstreamMaterials || []);
const upstreamProcesses = computed(() => detail.value?.upstreamProcesses || []);

const specificationRows = computed(() => {
  const rows = isProductMode.value ? detail.value?.specs : detail.value?.sections;
  return (Array.isArray(rows) ? rows : [])
    .filter((item) => item && item.label && item.value)
    .map((item) => ({ label: item.label, value: item.value }));
});

const descriptionNodes = computed(() => {
  const html = isProductMode.value ? firstText(detail.value?.description) : "";
  // 商品图文详情中的图片按屏宽自适应
  return html ? html.replace(/<img/gi, '<img style="max-width:100%;height:auto;display:block;"') : "";
});

const relatedProducts = computed(() => {
  const list = isProductMode.value ? detail.value?.relatedProducts : entityRelatedProducts.value;
  return (Array.isArray(list) ? list : []).map((item) => ({
    id: item.id,
    name: item.name,
    lane: item.lane,
    image: sanitizeWujinImageUrl(item.picUrl),
    price: formatPrice(item.price),
  }));
});

function formatPrice(value) {
  if (value === undefined || value === null || value === "") {
    return "待报价";
  }
  return typeof value === "number" ? `¥${(value / 100).toFixed(2)}` : String(value);
}

function firstText(...values) {
  for (const value of values) {
    if (value === undefined || value === null) {
      continue;
    }
    const text = String(value).trim();
    if (text) {
      return text;
    }
  }
  return "";
}

function laneDisplayTag(lane) {
  const map = {
    PRODUCT: "成品",
    PROCESS: "工艺",
    MATERIAL: "原材料",
  };
  return map[lane] || "";
}

async function loadDetail() {
  loaded.value = false;
  try {
    if (isProductMode.value) {
      detail.value = await getWujinProductDetail(productId.value);
      if (detail.value?.lane) {
        currentLane.value = detail.value.lane;
      }
    } else {
      detail.value = await getWujinEntityDetail({
        id: entityId.value,
        entityType: entityType.value,
        lane: currentLane.value,
        keyword: keyword.value,
        sourceKeyword: sourceKeyword.value,
      });
      loadEntityRelatedProducts();
    }
  } catch (error) {
    console.error("Wujin detail failed", error);
    detail.value = null;
  } finally {
    loaded.value = true;
  }
}

async function loadEntityRelatedProducts() {
  if (!detail.value?.exists || !detail.value?.name) {
    return;
  }
  try {
    const page = await getWujinLaneProducts({
      keyword: detail.value.name,
      lane: currentLane.value,
      pageNo: 1,
      pageSize: 6,
    });
    entityRelatedProducts.value = Array.isArray(page?.list) ? page.list : [];
  } catch (error) {
    console.warn("Wujin entity related products failed", error);
  }
}

function contextKeyword() {
  if (isProductMode.value) {
    return detail.value?.entityName || detail.value?.name || keyword.value;
  }
  return detail.value?.name || keyword.value;
}

function buildContextQuery(extra = {}) {
  const query = {
    id: isProductMode.value ? detail.value?.entityId : entityId.value || detail.value?.id,
    entityType: isProductMode.value ? "PRODUCT" : entityType.value,
    lane: currentLane.value,
    keyword: contextKeyword(),
    sourceKeyword: sourceKeyword.value,
    ...extra,
  };
  return Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
}

function handleBack() {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.reLaunch({ url: "/pages/wujin/search" });
}

function handleBackToSearch() {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  const previous = pages.length > 1 ? pages[pages.length - 2] : null;
  if (previous?.route === "pages/wujin/search-result") {
    uni.navigateBack();
    return;
  }
  const searchKeyword = sourceKeyword.value || keyword.value;
  if (searchKeyword) {
    uni.redirectTo({
      url: `/pages/wujin/search-result?keyword=${encodeURIComponent(searchKeyword)}`,
    });
    return;
  }
  uni.reLaunch({ url: "/pages/wujin/search" });
}

function handleFavorite() {
  showToast("已收藏，后续可在采购工作台查看");
}

function handleShare() {
  showToast("可通过右上角菜单分享给同事");
}

function handleInquiryOpen() {
  showToast("已加入询价单");
}

function handleChatOpen() {
  uni.navigateTo({ url: "/pages/messages/index" });
}

function handleRelatedOpen(item = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?productId=${encodeURIComponent(item.id || "")}&lane=${encodeURIComponent(item.lane || currentLane.value)}&keyword=${encodeURIComponent(item.name || "")}&sourceKeyword=${encodeURIComponent(sourceKeyword.value)}`,
  });
}

function handleEntityOpen(item = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?id=${encodeURIComponent(item.id || "")}&lane=${encodeURIComponent(item.lane || "")}&keyword=${encodeURIComponent(item.name || "")}&sourceKeyword=${encodeURIComponent(sourceKeyword.value)}`,
  });
}

function handleSupplierCapabilityOpen() {
  const supplierQuery = isProductMode.value && detail.value?.merchantId
    ? { supplierId: detail.value.merchantId }
    : {};
  uni.navigateTo({
    url: `/pages/wujin/supplier-capability?${buildContextQuery(supplierQuery)}`,
  });
}

function handleTraceMapOpen() {
  uni.navigateTo({
    url: `/pages/wujin/trace-map?${buildContextQuery()}`,
  });
}

function handleSourcingOpen() {
  uni.navigateTo({
    url: `/pages/wujin/sourcing?${buildContextQuery({
      keyword: contextKeyword(),
      sourceKeyword: sourceKeyword.value || displayName.value,
      sourceLane: currentLane.value,
      entryPath: "PRODUCT_DETAIL",
    })}`,
  });
}

function handleMaterialLaneOpen() {
  uni.navigateTo({
    url: `/pages/wujin/search-result?${buildContextQuery({
      id: undefined,
      entityType: undefined,
      lane: undefined,
      keyword: displayName.value,
      sourceKeyword: sourceKeyword.value || displayName.value,
      sourceLane: currentLane.value,
      sourceProductId: isProductMode.value ? productId.value : undefined,
      requestedLane: "MATERIAL",
      entryPath: "PRODUCT_DETAIL_MATERIAL",
    })}`,
  });
}

function initStatusBarHeight() {
  try {
    const sysInfo = uni.getWindowInfo ? uni.getWindowInfo() : uni.getSystemInfoSync();
    statusBarHeight.value = sysInfo?.statusBarHeight || 0;
  } catch (error) {
    statusBarHeight.value = 0;
  }
}

onLoad((options = {}) => {
  initStatusBarHeight();
  // 兼容旧链接：entityType=PRODUCT&id=<商品编号>
  productId.value = options.productId
    || (options.entityType === "PRODUCT" && options.id ? options.id : "");
  entityId.value = productId.value ? "" : options.id || "";
  entityType.value = options.entityType || "";
  currentLane.value = options.lane || "PRODUCT";
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  loadDetail();
});
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: calc(136rpx + env(safe-area-inset-bottom));
  background: #f5f7f9;
  color: #15171a;
}

.detail-commerce-header {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 92rpx;
  padding-right: 24rpx;
  padding-left: 24rpx;
  border-bottom: 1rpx solid #d8cbb5;
  background: rgba(255, 255, 255, 0.96);
  box-sizing: border-box;
}

.header-icon-button,
.bottom-mini-action,
.detail-cart-button,
.detail-primary-button {
  margin: 0;
  padding: 0;
  border: none;
  background: transparent;
  line-height: 1;
}

.header-icon-button::after,
.bottom-mini-action::after,
.detail-cart-button::after,
.detail-primary-button::after {
  border: none;
}

.header-icon-button {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 60rpx;
  height: 60rpx;
}

.detail-search-pill {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  height: 62rpx;
  gap: 12rpx;
  padding: 0 22rpx;
  border: 1rpx solid #d8dce0;
  border-radius: 31rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.detail-search-text {
  overflow: hidden;
  color: #3b3020;
  font-size: 24rpx;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 4rpx;
}

.detail-scroll {
  height: calc(100vh - 92rpx - env(safe-area-inset-bottom));
}

.detail-gallery {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  border-bottom: 1rpx solid #d8cbb5;
  background: #ffffff;
}

.detail-gallery-image {
  width: 100%;
  height: 100%;
}

.detail-gallery-image--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 12rpx;
  background: linear-gradient(145deg, #f6f0df 0%, #ffffff 58%, #eceff3 100%);
}

.detail-gallery-empty-title {
  color: #111111;
  font-size: 46rpx;
  font-weight: 900;
}

.detail-gallery-empty-text {
  color: #6d6250;
  font-size: 26rpx;
  font-weight: 700;
}

.gallery-count {
  position: absolute;
  right: 28rpx;
  bottom: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 62rpx;
  height: 42rpx;
  padding: 0 10rpx;
  border-radius: 8rpx;
  background: rgba(45, 49, 51, 0.66);
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
  box-sizing: border-box;
}

.purchase-decision-card {
  display: grid;
  gap: 22rpx;
  padding: 34rpx 32rpx 30rpx;
  border-top: 1rpx solid #d8cbb5;
  border-bottom: 1rpx solid #d8cbb5;
  background: #ffffff;
  box-sizing: border-box;
}

.purchase-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.purchase-copy {
  display: grid;
  min-width: 0;
  gap: 16rpx;
}

.purchase-name {
  display: block;
  color: #111111;
  font-size: 40rpx;
  line-height: 1.25;
  font-weight: 900;
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.detail-tags--compact {
  margin-top: 0;
}

.detail-chip {
  display: inline-flex;
  align-items: center;
  min-height: 38rpx;
  padding: 0 14rpx;
  border-radius: 6rpx;
  background: #e7e9ec;
  color: #515965;
  font-size: 21rpx;
  font-weight: 700;
}

.detail-chip--active {
  border: 1rpx solid #f4b400;
  background: #fff8df;
  color: #d79d00;
}

.purchase-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  overflow: hidden;
  border: 1rpx solid #d8cbb5;
  border-radius: 14rpx;
  background: #d8cbb5;
  gap: 1rpx;
}

.purchase-item {
  display: grid;
  align-content: center;
  gap: 10rpx;
  min-height: 136rpx;
  padding: 20rpx 24rpx;
  background: #ffffff;
  box-sizing: border-box;
}

.purchase-label {
  color: #6d6250;
  font-size: 22rpx;
  line-height: 1.25;
}

.purchase-value {
  color: #111111;
  font-size: 30rpx;
  line-height: 1.25;
  font-weight: 800;
}

.price-line {
  display: flex;
  align-items: baseline;
  min-width: 0;
}

.price-symbol {
  color: #8b6100;
  font-size: 24rpx;
  font-weight: 800;
}

.purchase-price {
  color: #8b6100;
  font-size: 42rpx;
}

.price-unit {
  margin-left: 6rpx;
  color: #514735;
  font-size: 24rpx;
}

.purchase-delivery {
  color: #007c3f;
}

.spec-panel {
  margin-top: 46rpx;
  border-top: 1rpx solid #d8cbb5;
  border-bottom: 1rpx solid #d8cbb5;
  background: #ffffff;
}

.detail-tabs {
  width: 100%;
  border-bottom: 1rpx solid #d8cbb5;
  white-space: nowrap;
}

.detail-tabs-inner {
  display: inline-flex;
  min-width: 100%;
  padding: 0 28rpx;
  box-sizing: border-box;
}

.detail-tab {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 138rpx;
  height: 88rpx;
  color: #3d3429;
  font-size: 26rpx;
  font-weight: 700;
}

.detail-tab + .detail-tab {
  margin-left: 34rpx;
}

.detail-tab--active {
  color: #8b6100;
}

.detail-tab--active::after {
  content: "";
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 4rpx;
  border-radius: 4rpx 4rpx 0 0;
  background: #f4b400;
}

.spec-list {
  display: grid;
  padding: 24rpx 32rpx 26rpx;
}

.spec-row {
  display: grid;
  grid-template-columns: 210rpx minmax(0, 1fr);
  gap: 22rpx;
  min-height: 76rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #e7e0d5;
  box-sizing: border-box;
}

.spec-row:last-child {
  border-bottom: none;
}

.spec-label {
  color: #514735;
  font-size: 25rpx;
  line-height: 1.5;
}

.spec-value {
  color: #15171a;
  font-size: 25rpx;
  line-height: 1.5;
}

.risk-ticket {
  margin: 48rpx 32rpx 0;
  padding: 26rpx 24rpx;
  border: 2rpx solid #f4b400;
  border-radius: 16rpx;
  background: #fff8df;
  box-sizing: border-box;
}

.risk-ticket__head {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.risk-head-copy {
  display: grid;
  flex: 1;
  min-width: 0;
  gap: 8rpx;
}

.risk-title {
  color: #795900;
  font-size: 28rpx;
  line-height: 1.35;
  font-weight: 900;
}

.risk-kicker {
  color: #a57900;
  font-size: 20rpx;
  line-height: 1.3;
  font-weight: 800;
}

.risk-text {
  color: #4d3d14;
  font-size: 25rpx;
  line-height: 1.65;
}

.detail-action-list {
  margin-top: 48rpx;
  border-top: 1rpx solid #d8cbb5;
  border-bottom: 1rpx solid #d8cbb5;
  background: #ffffff;
}

.detail-action-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-height: 126rpx;
  padding: 0 32rpx;
  box-sizing: border-box;
}

.detail-action-row + .detail-action-row {
  border-top: 1rpx solid #e7e0d5;
}

.detail-product-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  padding: 20rpx 32rpx;
  border-top: 1rpx solid #e7e0d5;
}

.detail-action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
}

.detail-action-icon--blue {
  background: #dfe5f8;
}

.detail-action-icon--green {
  background: #d8f4e4;
}

.detail-action-copy {
  display: grid;
  flex: 1;
  min-width: 0;
  gap: 4rpx;
}

.detail-action-title {
  color: #111111;
  font-size: 28rpx;
  line-height: 1.35;
  font-weight: 900;
}

.detail-action-desc {
  color: #6b7280;
  font-size: 22rpx;
  line-height: 1.35;
}

.comparison-board {
  display: grid;
  gap: 20rpx;
  margin: 32rpx;
  padding: 24rpx;
  border: 1rpx solid #e1e5eb;
  border-radius: 16rpx;
  background: #ffffff;
  box-sizing: border-box;
}

.comparison-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.comparison-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.comparison-note {
  color: #6b7280;
  font-size: 22rpx;
}

.compare-columns {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.compare-column {
  display: grid;
  align-content: start;
  gap: 10rpx;
  min-height: 178rpx;
  padding: 18rpx;
  border-radius: 14rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}

.compare-column:last-child {
  border: 1rpx solid #f0d46b;
  background: #fffaf0;
}

.compare-title {
  color: #111111;
  font-size: 26rpx;
  line-height: 1.35;
  font-weight: 900;
}

.compare-standard {
  color: #2b2f36;
  font-size: 23rpx;
  line-height: 1.5;
}

.compare-note {
  color: #6b7280;
  font-size: 21rpx;
  line-height: 1.45;
}

.detail-bottom-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-height: 104rpx;
  padding: 14rpx 32rpx calc(14rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #d8cbb5;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
}

.bottom-secondary-actions {
  display: flex;
  align-items: center;
  gap: 22rpx;
  flex-shrink: 0;
}

.bottom-mini-action {
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 6rpx;
  width: 58rpx;
  height: 74rpx;
  color: #3d3429;
  font-size: 19rpx;
}

.detail-action-buttons {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.1fr);
  flex: 1;
  min-width: 0;
  gap: 14rpx;
}

.detail-cart-button,
.detail-primary-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 74rpx;
  border-radius: 14rpx;
  font-size: 26rpx;
  font-weight: 900;
  box-sizing: border-box;
}

.wujin-button--primary {
  background: #f4b400;
  color: #261a00;
}

.wujin-button--secondary {
  gap: 8rpx;
  border: 1rpx solid #cbbda4;
  background: #f0f2f4;
  color: #111111;
}

.detail-state-card {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 18rpx;
  margin: 48rpx 24rpx;
  padding: 64rpx 32rpx;
  border-radius: 20rpx;
  background: #ffffff;
}

.detail-state-icon {
  display: flex;
  width: 132rpx;
  height: 132rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f3f5f8;
}

.detail-state-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
  text-align: center;
}

.detail-state-text {
  color: #6c7580;
  font-size: 25rpx;
  text-align: center;
}

.detail-state-button {
  width: 320rpx;
  margin-top: 12rpx;
}

.purchase-intro {
  color: #5d6673;
  font-size: 24rpx;
  line-height: 1.5;
}

.detail-tab {
  cursor: pointer;
}

.tab-empty {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 18rpx;
  padding: 48rpx 24rpx;
}

.tab-empty-text {
  color: #8a929d;
  font-size: 25rpx;
}

.tab-empty-button {
  width: 260rpx;
}

.detail-description {
  display: grid;
  gap: 12rpx;
  padding-top: 20rpx;
}

.detail-description-title {
  color: #111111;
  font-size: 27rpx;
  font-weight: 900;
}

.related-panel {
  padding: 20rpx 0;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.related-card {
  display: grid;
  gap: 8rpx;
  min-width: 0;
  padding: 12rpx;
  border: 1rpx solid #e2e6eb;
  border-radius: 14rpx;
  background: #ffffff;
}

.related-image {
  width: 100%;
  height: 200rpx;
  border-radius: 10rpx;
  background: #eef1f5;
}

.related-image--empty {
  background: linear-gradient(160deg, #f4f6f8 0%, #dce2e8 100%);
}

.related-name {
  overflow: hidden;
  color: #111111;
  font-size: 24rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-price {
  color: #d92d20;
  font-size: 26rpx;
  font-weight: 900;
}

.upstream-board {
  display: grid;
  gap: 20rpx;
  margin: 20rpx 24rpx 0;
  padding: 24rpx;
  border-radius: 18rpx;
  background: #ffffff;
}

.upstream-group {
  display: grid;
  gap: 12rpx;
}

.upstream-group-title {
  color: #111111;
  font-size: 27rpx;
  font-weight: 900;
}

.upstream-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.upstream-chip {
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 800;
}

.upstream-chip--material {
  background: #dff6ea;
  color: #167f61;
}

.upstream-chip--process {
  background: #e8f0ff;
  color: #2457c5;
}

.upstream-empty {
  color: #8a929d;
  font-size: 24rpx;
}
</style>
