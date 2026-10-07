<template>
  <view class="wujin-page detail-page">
    <view class="detail-commerce-header" :style="headerStyle">
      <button class="header-icon-button" @tap="handleBack">
        <AppIcon name="arrow_back" variant="dark" size="38rpx" />
      </button>
      <view class="detail-search-pill">
        <AppIcon name="search" variant="muted" size="28rpx" />
        <text class="detail-search-text">{{ detail.name }}</text>
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

    <scroll-view class="detail-scroll" scroll-y>
      <view class="detail-gallery">
        <image
          v-if="activeProductImage"
          class="detail-gallery-image"
          :src="activeProductImage"
          mode="aspectFill"
        />
        <view v-else class="detail-gallery-image detail-gallery-image--empty">
          <text class="detail-gallery-empty-title">五金商品</text>
          <text class="detail-gallery-empty-text">暂无商品主图</text>
        </view>
        <view class="gallery-count">
          <text>{{ galleryCountText }}</text>
        </view>
      </view>

      <view class="purchase-decision-card">
        <view class="purchase-summary">
          <view class="purchase-copy">
            <text class="purchase-name">{{ detail.name }}</text>
            <view class="detail-tags detail-tags--compact">
              <text
                v-for="tag in productTags"
                :key="tag"
                :class="['detail-chip', tag === activeDetailTag ? 'detail-chip--active' : '']"
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
              <text
                v-if="priceParts.symbol"
                class="price-symbol"
              >
                {{ priceParts.symbol }}
              </text>
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
            <text class="detail-tab detail-tab--active">规格参数</text>
            <text class="detail-tab">技术图纸</text>
            <text class="detail-tab">质检报告</text>
            <text class="detail-tab">相关产品</text>
          </view>
        </scroll-view>
        <view class="spec-list">
          <view
            v-for="item in specificationRows"
            :key="item.label"
            class="spec-row"
          >
            <text class="spec-label">{{ item.label }}</text>
            <text class="spec-value">{{ item.value }}</text>
          </view>
        </view>
      </view>

      <view class="risk-ticket">
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

      <view class="comparison-board">
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

    <view class="detail-bottom-bar">
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

import { getWujinEntityDetail } from "@/api/wujin/detail";
import { showToast } from "@/utils/toast";
import { sanitizeWujinImageList } from "@/utils/wujinImage";

defineOptions({ name: "WujinMiniDetail" });

const DEFAULT_PRODUCT_IMAGE = "";

const laneOptions = [
  { label: "商品", value: "PRODUCT" },
  { label: "工艺", value: "PROCESS" },
  { label: "原材料", value: "MATERIAL" },
];

const entityId = ref("");
const entityType = ref("");
const currentLane = ref("PRODUCT");
const keyword = ref("");
const sourceKeyword = ref("");
const statusBarHeight = ref(0);
const detail = ref(buildFallbackDetail());

const headerStyle = computed(() => ({
  paddingTop: `${statusBarHeight.value}px`,
}));

const detailSections = computed(() => detail.value.sections || []);
const crossIndustryTips = computed(() => detail.value.crossIndustryTips || []);
const industryComparison = computed(() => detail.value.industryComparison || []);

const detailImages = computed(() => {
  const candidates = [
    detail.value.images,
    detail.value.imageList,
    detail.value.gallery,
    detail.value.coverImage,
    detail.value.coverUrl,
    detail.value.imageUrl,
    detail.value.picUrl,
  ];
  return candidates
    .flatMap((item) => normalizeImageList(item))
    .flatMap((item) => sanitizeWujinImageList(item))
    .slice(0, 5);
});

const activeProductImage = computed(() => detailImages.value[0] || DEFAULT_PRODUCT_IMAGE);
const galleryCountText = computed(() => detailImages.value.length ? `1/${detailImages.value.length}` : "无图");
const activeDetailTag = computed(
  () => detail.value.displayTag || laneDisplayTag(currentLane.value) || detail.value.entityLabel,
);

const productTags = computed(() => {
  const tags = [
    activeDetailTag.value,
    ...(Array.isArray(detail.value.tags) ? detail.value.tags : []),
    detail.value.industry,
    detail.value.certification,
  ];
  return tags
    .map((item) => (typeof item === "string" ? item.trim() : ""))
    .filter(Boolean)
    .slice(0, 4);
});

const priceParts = computed(() => parsePrice(detail.value.price || detail.value.unitPrice));
const moqText = computed(() => firstText(detail.value.moq, detail.value.minOrder, "100个"));
const inventoryText = computed(() =>
  firstText(detail.value.stock, detail.value.inventory, detail.value.stockText, "56,800 个"),
);
const deliveryText = computed(() =>
  firstText(detail.value.delivery, detail.value.deliveryTime, detail.value.leadTime, "24h发货"),
);
const riskNotice = computed(() =>
  firstText(
    detail.value.riskNotice,
    detail.value.riskNote,
    crossIndustryTips.value[0],
    "注意：该型号适用于混凝土基材。不同基材的钻孔深度及承载力衰减需符合专业工程要求，请勿直接替代使用以免发生锚固失效。",
  ),
);

const specificationRows = computed(() => {
  const rows = [
    ["规格型号", firstText(detail.value.spec, detail.value.model, "M10*80")],
    ["材料", firstText(detail.value.material, detail.value.materialName, "碳钢 Q235")],
    ["表面处理", firstText(detail.value.surfaceTreatment, detail.value.finish, "热镀锌 ≥ 8μm")],
    ["钻孔直径", firstText(detail.value.holeDiameter, detail.value.drillDiameter, "Φ12mm")],
    ["埋入深度", firstText(detail.value.embedDepth, detail.value.anchorDepth, "≥ 70mm")],
    ["抗拉承载", firstText(detail.value.tensileLoad, detail.value.loadCapacity, "≥ 7.00kN (混凝土 C20)")],
    ["执行标准", firstText(detail.value.standard, detail.value.certification, "GB/T 22795-2008")],
  ];

  const sectionRows = detailSections.value
    .filter((item) => item && item.label && item.value)
    .map((item) => [item.label, item.value]);

  const mergedRows = currentLane.value === "PRODUCT" ? rows : sectionRows.concat(rows.slice(0, 3));
  const seen = new Set();
  return mergedRows
    .filter(([label]) => {
      if (seen.has(label)) {
        return false;
      }
      seen.add(label);
      return true;
    })
    .map(([label, value]) => ({ label, value }));
});

function normalizeImageList(value) {
  if (!value) {
    return [];
  }
  if (Array.isArray(value)) {
    return value;
  }
  if (typeof value !== "string") {
    return [];
  }
  const trimmed = value.trim();
  if (!trimmed) {
    return [];
  }
  if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
    try {
      const parsed = JSON.parse(trimmed);
      return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
      console.warn("Wujin detail image list parse failed", error);
    }
  }
  return trimmed.split(/[,，]/).map((item) => item.trim());
}

function parsePrice(rawValue) {
  const rawText = firstText(rawValue, "¥0.28/个");
  if (/询价|报价|可议/.test(rawText)) {
    return { symbol: "", amount: rawText, unit: "" };
  }
  const matched = rawText.match(/^([¥￥])?\s*([0-9]+(?:\.[0-9]+)?)(.*)$/);
  if (!matched) {
    return { symbol: "", amount: rawText, unit: "" };
  }
  return {
    symbol: matched[1] || "¥",
    amount: matched[2],
    unit: matched[3] || "/个",
  };
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

function laneName(lane) {
  return laneOptions.find((item) => item.value === lane)?.label || lane || "-";
}

function laneEntityLabel(lane) {
  const map = {
    PRODUCT: "商品详情",
    PROCESS: "工艺详情",
    MATERIAL: "原材料详情",
  };
  return map[lane] || "五金详情";
}

function laneDisplayTag(lane) {
  const map = {
    PRODUCT: "成品",
    PROCESS: "工艺",
    MATERIAL: "原材料",
  };
  return map[lane] || "";
}

function buildFallbackDetail() {
  const name = keyword.value || "碳钢膨胀螺丝 M10*80";
  const lane = currentLane.value || "PRODUCT";
  const label = laneEntityLabel(lane);
  const laneSections = {
    PRODUCT: [
      { label: "规格型号", value: "M10*80" },
      { label: "材料", value: "碳钢 Q235" },
      { label: "表面处理", value: "热镀锌 ≥ 8μm" },
    ],
    PROCESS: [
      { label: "工艺能力", value: "支持打样、小批量和批量加工的能力说明" },
      { label: "设备要求", value: "展示加工设备、精度、表面处理和检测能力" },
      { label: "适配商品", value: "承接搜索上下文中的成品或半成品需求" },
    ],
    MATERIAL: [
      { label: "材料牌号", value: "展示牌号、产地、规格和库存状态" },
      { label: "适用成品", value: "关联下游成品与典型应用场景" },
      { label: "供应方式", value: "现货、期货、分切、配送等供给能力" },
    ],
  };

  return {
    id: entityId.value,
    name,
    entityLabel: label,
    displayTag: laneDisplayTag(lane),
    sectionTitle: `${laneName(lane)}详情`,
    summary: `${name} 的${label}已整理关键规格、供应能力与跨行业差异。`,
    industry: "国标",
    price: "¥0.28/个",
    moq: "100个",
    stock: "56,800 个",
    delivery: "24h发货",
    spec: "M10*80",
    material: "碳钢 Q235",
    surfaceTreatment: "热镀锌 ≥ 8μm",
    certification: "碳钢",
    images: [],
    sections: laneSections[lane] || laneSections.PRODUCT,
    crossIndustryTips: [
      "注意：该型号适用于混凝土基材。不同基材（如砖墙、空心砖）的钻孔深度及承载力衰减需符合专业工程要求，请勿直接替代使用以免发生锚固失效。",
      "同名工艺在耐腐蚀、食品接触、承压件场景下不能默认等价。",
    ],
    industryComparison: [
      {
        industry: "通用五金",
        standard: "关注规格、交期、批量价格和常规质检。",
        note: "适合普通采购和快速比价。",
      },
      {
        industry: "高要求行业",
        standard: "关注认证、追溯批次、检测报告和稳定供应。",
        note: "需要供应商补充资质和样品验证。",
      },
    ],
  };
}

async function loadDetail() {
  try {
    const result = await getWujinEntityDetail({
      id: entityId.value,
      entityType: entityType.value,
      lane: currentLane.value,
      keyword: keyword.value,
      sourceKeyword: sourceKeyword.value,
    });
    detail.value = result && Object.keys(result).length
      ? {
          ...buildFallbackDetail(),
          ...result,
        }
      : buildFallbackDetail();
  } catch (error) {
    console.error("Wujin detail failed", error);
    detail.value = buildFallbackDetail();
  }
}

function buildContextQuery(extra = {}) {
  const query = {
    id: entityId.value,
    entityType: entityType.value,
    lane: currentLane.value,
    keyword: keyword.value || detail.value.name,
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

function handleSupplierCapabilityOpen() {
  uni.navigateTo({
    url: `/pages/wujin/supplier-capability?${buildContextQuery()}`,
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
      keyword: keyword.value || detail.value.name,
      sourceKeyword: sourceKeyword.value || detail.value.name,
      sourceLane: "PRODUCT",
      entryPath: "PRODUCT_DETAIL",
    })}`,
  });
}

function handleMaterialLaneOpen() {
  uni.navigateTo({
    url: `/pages/wujin/search-result?${buildContextQuery({
      keyword: keyword.value || detail.value.name,
      sourceKeyword: sourceKeyword.value || detail.value.name,
      sourceLane: "PRODUCT",
      sourceProductId: entityType.value === "PRODUCT" ? entityId.value : undefined,
      requestedLane: "MATERIAL",
      entryPath: "PRODUCT_DETAIL_MATERIAL",
    })}`,
  });
}

function initStatusBarHeight() {
  try {
    statusBarHeight.value = uni.getSystemInfoSync()?.statusBarHeight || 0;
  } catch (error) {
    statusBarHeight.value = 0;
  }
}

onLoad((options = {}) => {
  initStatusBarHeight();
  entityId.value = options.id || "";
  entityType.value = options.entityType || "";
  currentLane.value = options.lane || "PRODUCT";
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  detail.value = buildFallbackDetail();
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
</style>
