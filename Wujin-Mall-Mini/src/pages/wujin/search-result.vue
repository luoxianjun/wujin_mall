<template>
  <view class="wujin-page search-result-page">
    <view class="wujin-hero search-hero" :style="heroSafeStyle">
      <view class="result-toolbar">
        <button class="result-back" @tap="handleBack">
          <AppIcon name="arrow_back" variant="dark" size="34rpx" />
        </button>
        <text class="result-title">搜索结果</text>
      </view>

      <view class="search-box">
        <AppIcon name="search" variant="muted" size="34rpx" />
        <input
          v-model="keyword"
          class="search-input"
          confirm-type="search"
          placeholder="搜索螺栓、电镀、天然橡胶"
          type="text"
          @confirm="submitSearch"
        />
        <button class="search-button" @tap="submitSearch">搜索</button>
      </view>
    </view>

    <view class="wujin-content search-content">
      <view class="lane-tabs">
        <view
          v-for="lane in laneOptions"
          :key="lane.value"
          class="lane-tab"
          :class="{ active: activeLane === lane.value }"
          @tap="handleLaneSwitch(lane.value)"
        >
          <text class="lane-name">{{ lane.label }}{{ laneCount(lane.value) }}</text>
          <text class="lane-desc">{{ lane.description }}</text>
        </view>
      </view>

      <view v-if="searchResult" class="category-suggestion-panel">
        <view class="category-suggestion-head">
          <text class="category-suggestion-title">{{ activeLaneName }}分类</text>
          <text class="category-suggestion-note">按分类继续筛选</text>
        </view>
        <view v-if="categoryGroups.length" class="category-suggestion-groups">
          <view
            v-for="group in categoryGroups"
            :key="group.level"
            class="category-suggestion-group"
          >
            <scroll-view
              class="category-suggestion-list"
              scroll-x
              :show-scrollbar="false"
            >
              <view class="category-suggestion-list-inner">
                <button
                  v-for="item in group.categories"
                  :key="item.categoryId || item.categoryPath"
                  class="category-suggestion-chip"
                  @tap="handleCategorySuggestionSelect(item)"
                >
                  <text class="category-suggestion-label">{{ categorySuggestionLabel(item) }}</text>
                </button>
              </view>
            </scroll-view>
          </view>
        </view>
        <text v-else class="category-suggestion-empty">暂无匹配的{{ activeLaneName }}分类</text>
      </view>

      <view v-if="!searchResult" class="wujin-card empty-result-card">
        <text class="empty-result-title">{{ loading ? "正在搜索" : "输入关键词查看产业链结果" }}</text>
        <text class="empty-result-text">搜索结果会在这里按成品、加工、原材料三泳道展示。</text>
      </view>

      <view v-if="searchResult" class="result-stack">
        <view class="chain-result-head">
          <text class="chain-title">产业链寻源</text>
          <text class="chain-subtitle">{{ activeLane === "PRODUCT" ? "三泳道 · 成品优先" : "三泳道 · 上游溯源" }}</text>
        </view>

        <view v-if="riskWarningRequired" class="wujin-card wujin-risk">
          <view class="wujin-section-head">
            <text class="wujin-section-title">跨行业风险提示</text>
            <AppIcon name="report" variant="warning" size="34rpx" />
          </view>
          <text class="wujin-text">{{ riskWarningText }}</text>
        </view>

        <view v-if="activeLane === 'PRODUCT'" class="product-result-list">
          <view
            v-for="item in productCards"
            :key="item.key"
            class="product-result-card"
            @tap="handleRecommendSelect(item.raw)"
          >
            <view class="product-main">
              <view class="product-image-wrap">
                <image
                  v-if="item.image"
                  class="product-image"
                  :src="item.image"
                  mode="aspectFill"
                  lazy-load
                />
                <view v-else class="product-image product-image--empty"></view>
              </view>
              <view class="product-copy">
                <text class="product-title">{{ item.title }}</text>
                <text class="product-supplier">{{ item.supplier }}</text>
                <text class="product-price">{{ item.price }}</text>
              </view>
            </view>
            <view class="product-tags">
              <text class="material-tag">CCC认证</text>
              <text class="material-tag">绑定4类原材料</text>
            </view>
          </view>
        </view>

        <template v-else>
          <view class="wujin-card material-card">
            <view class="material-head">
              <text class="material-title">{{ chainMaterial.name }}</text>
              <text class="material-ratio">{{ materialRatio }}%</text>
            </view>
            <view class="material-progress">
              <view class="material-progress-bar" :style="{ width: `${materialRatio}%` }"></view>
            </view>
            <view class="material-tags">
              <text
                v-for="tag in chainMaterial.tags"
                :key="tag"
                class="material-tag"
              >
                {{ tag }}
              </text>
            </view>
            <button class="wujin-button wujin-button--yellow material-action" @tap="handleDetailOpen(activeLaneSummary)">
              查看详情
            </button>
            <text class="wujin-muted">{{ activeLaneSummary.summary }}</text>
            <view class="material-actions">
              <button class="wujin-button wujin-button--yellow" @tap="handleTraceSearch">
                一键寻源
              </button>
              <button class="wujin-button wujin-button--line" @tap="handleTraceMapOpen(activeLaneSummary)">
                溯源图谱
              </button>
            </view>
          </view>

          <view
            class="downstream-section"
            :class="{ 'raw-material-products': activeLane === 'MATERIAL' }"
          >
            <view class="wujin-section-head">
              <text class="wujin-section-title">
                {{ activeLane === "MATERIAL" ? "原材料商品" : "相关商品" }}
              </text>
              <text class="wujin-section-note">{{ downstreamProducts.length }} 条</text>
            </view>
            <view v-if="downstreamProducts.length" class="downstream-grid">
              <view
                v-for="item in downstreamProducts"
                :key="item.key"
                class="downstream-card"
                @tap="handleRecommendSelect(item.raw)"
              >
                <view class="downstream-image-wrap">
                  <image
                    v-if="item.image"
                    class="downstream-image"
                    :src="item.image"
                    mode="aspectFill"
                    lazy-load
                />
                <view v-else class="downstream-image downstream-image--empty"></view>
              </view>
              <text v-if="item.entityName" class="downstream-material">{{ item.entityName }}</text>
              <text class="downstream-title">{{ item.title }}</text>
              <text class="downstream-supplier">{{ item.supplier }}</text>
              <text class="downstream-price">{{ item.price }}</text>
              <button class="downstream-more" @tap.stop="handleRecommendSelect(item.raw)">
                查看更多
              </button>
              </view>
            </view>
            <view v-else class="lane-product-empty">
              <text class="lane-product-empty-title">当前分类暂无可售商品</text>
              <text class="wujin-muted">可通过一键寻源提交原材料采购需求</text>
            </view>
          </view>
        </template>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";

import { getWujinProductRecommendations } from "@/api/wujin/recommend";
import { searchWujin } from "@/api/wujin/search";
import { showToast } from "@/utils/toast";
import { sanitizeWujinImageUrl } from "@/utils/wujinImage";

defineOptions({ name: "WujinMiniSearchResult" });

const laneOptions = [
  { label: "成品", value: "PRODUCT", description: "商品与规格" },
  { label: "加工", value: "PROCESS", description: "工艺与能力" },
  { label: "原材料", value: "MATERIAL", description: "材料与供给" },
];

const keyword = ref("");
const lastKeyword = ref("");
const requestedLane = ref("");
const sourceProductId = ref("");
const sourceEntityId = ref("");
const searchResult = ref(null);
const loading = ref(false);
const statusBarHeight = ref(0);
const recommendProducts = ref([]);
const recommendPageNo = ref(1);
const recommendPageSize = 10;
const recommendLoading = ref(false);
const recommendFinished = ref(false);
const recommendSearchKeyword = ref("");

const heroSafeStyle = computed(() => ({
  paddingTop: `calc(${statusBarHeight.value}px + 24rpx)`,
}));

const defaultLane = computed(() => searchResult.value?.defaultLane || "PRODUCT");
const activeLane = computed(() => requestedLane.value || defaultLane.value);
const activeLaneName = computed(
  () => laneOptions.find((item) => item.value === activeLane.value)?.label || "相关",
);
const laneSummaries = computed(() => searchResult.value?.laneSummaries || []);
const categorySuggestions = computed(() => {
  const list = Array.isArray(searchResult.value?.categorySuggestions)
    ? searchResult.value.categorySuggestions
    : [];
  return list
    .filter((item) => item?.lane === activeLane.value)
    .filter((item) => item?.categoryPath || item?.categoryName)
    .slice(0, 8);
});
const categoryGroups = computed(() => {
  const apiGroups = Array.isArray(searchResult.value?.categoryGroups)
    ? searchResult.value.categoryGroups
    : [];
  const normalizedGroups = apiGroups
    .map((group) => {
      const categories = Array.isArray(group?.categories)
        ? group.categories
            .filter((item) => item?.lane === activeLane.value)
            .filter((item) => item?.categoryPath || item?.categoryName)
        : [];
      const level = Number(group?.level) || categoryLevel(categories[0]);
      return {
        level,
        categories,
      };
    })
    .filter((group) => group.categories.length)
    .sort((left, right) => left.level - right.level);
  if (normalizedGroups.length) {
    return normalizedGroups;
  }

  const legacyGroups = new Map();
  categorySuggestions.value.forEach((item) => {
    const level = categoryLevel(item);
    const items = legacyGroups.get(level) || [];
    items.push(item);
    legacyGroups.set(level, items);
  });
  return [...legacyGroups.entries()]
    .sort(([left], [right]) => left - right)
    .map(([level, categories]) => ({
      level,
      categories,
    }));
});
const relatedProducts = computed(() =>
  Array.isArray(searchResult.value?.relatedProducts)
    ? searchResult.value.relatedProducts
    : [],
);
const activeLaneSummary = computed(
  () =>
    laneSummaries.value.find((item) => item.lane === activeLane.value) ||
    laneSummaries.value.find((item) => item.lane === "MATERIAL") ||
    {},
);
const materialRatio = computed(() => {
  const count = Number(activeLaneSummary.value.resultCount || 0);
  return Math.max(18, Math.min(78, count + 10));
});
const chainMaterial = computed(() => {
  const keywordText = currentKeywordText();
  if (activeLane.value === "MATERIAL" && categorySuggestions.value.length) {
    const firstPath = categorySuggestions.value[0].categoryPath || "";
    const segments = firstPath.split(/[>＞/]/).map((item) => item.trim()).filter(Boolean);
    return {
      name: segments.length > 1 ? segments.at(-2) : activeLaneName.value,
      tags: categorySuggestions.value.slice(0, 4).map(categorySuggestionLabel),
    };
  }
  const materialName = keywordText.includes("不锈钢")
    ? "不锈钢材料"
    : keywordText.includes("螺丝") || keywordText.includes("螺栓")
      ? "钢材与表面处理"
      : keywordText.includes("轮胎")
        ? "橡胶材料"
        : keywordText.includes("橡胶")
          ? keywordText
          : `${keywordText}上游材料`;
  const tags = keywordText.includes("不锈钢")
    ? ["304不锈钢", "316不锈钢", "冷镦线材"]
    : keywordText.includes("螺丝") || keywordText.includes("螺栓")
      ? ["碳钢线材", "不锈钢线材", "镀锌处理"]
      : ["天然橡胶", "合成橡胶", "再生橡胶"];
  return {
    name: materialName,
    tags,
  };
});
const productCards = computed(() => {
  const list = keywordMatchedProducts.value.length
    ? keywordMatchedProducts.value
    : buildFallbackProducts(currentKeywordText());
  return list.slice(0, 3).map(normalizeProductCard);
});
const downstreamProducts = computed(() => {
  if (activeLane.value === "MATERIAL") {
    return relatedProducts.value.slice(0, 8).map(normalizeProductCard);
  }
  return keywordMatchedProducts.value.slice(0, 4).map(normalizeProductCard);
});
const keywordMatchedProducts = computed(() => {
  const searchText = recommendSearchKeyword.value || currentKeywordText();
  if (activeLane.value !== "PRODUCT" || !recommendProducts.value.length || !searchText) {
    return recommendProducts.value;
  }
  const matched = recommendProducts.value.filter((item) => productMatchesKeyword(item, searchText));
  return matched.length ? matched : [];
});
const riskWarningRequired = computed(
  () => searchResult.value?.riskWarningRequired,
);
const riskWarningText = computed(
  () => searchResult.value?.riskWarningText || "不同应用场景可能存在不可互换风险。",
);

function laneCount(lane) {
  const summary = laneSummaries.value.find((item) => item.lane === lane);
  return searchResult.value && summary ? ` ${summary.resultCount || 0}` : "";
}

function categorySuggestionLabel(item = {}) {
  if (item.categoryName) {
    return item.categoryName;
  }
  const categoryPath = item.categoryPath || "";
  const segments = categoryPath.split(/[>＞/]/).map((segment) => segment.trim()).filter(Boolean);
  return segments.at(-1) || categoryPath || "分类";
}

function categoryLevel(item = {}) {
  const level = Number(item.level);
  if (Number.isInteger(level) && level > 0) {
    return level;
  }
  const segments = String(item.categoryPath || "")
    .split(/[>＞/]/)
    .map((segment) => segment.trim())
    .filter(Boolean);
  return Math.max(1, segments.length);
}

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

function currentKeywordText() {
  return (lastKeyword.value || keyword.value || searchResult.value?.keyword || "五金商品").trim();
}

function splitSearchTokens(text) {
  return String(text || "")
    .replace(/[，,、/\\|]+/g, " ")
    .split(/\s+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function productMatchesKeyword(item = {}, searchText = "") {
  const tokens = splitSearchTokens(searchText);
  if (!tokens.length) {
    return true;
  }
  const haystack = [
    item.name,
    item.title,
    item.productName,
    item.goodsName,
    item.keyword,
    item.supplierName,
    item.shopName,
    item.companyName,
  ].filter(Boolean).join(" ");
  return tokens.some((token) => haystack.includes(token));
}

function buildFallbackProducts(searchText = "") {
  const keywordText = searchText || "五金商品";
  if (keywordText.includes("不锈钢") || keywordText.includes("螺丝") || keywordText.includes("螺栓")) {
    return [
      { id: "fallback-stainless-screw-1", name: "304不锈钢螺丝 M4*20", price: 1280, shopName: "华固标准件工厂" },
      { id: "fallback-stainless-screw-2", name: "十字沉头不锈钢螺丝 M5*30", price: 1560, shopName: "坚成紧固件供应商" },
    ];
  }
  if (keywordText.includes("橡胶") || keywordText.includes("轮胎")) {
    return [
      { id: "fallback-rubber-1", name: "高耐磨乘用车轮胎 205/55R16", price: 36800, shopName: "鑫诚轮胎旗舰店" },
      { id: "fallback-rubber-2", name: "工程车橡胶轮胎 12.00R20", price: 125000, shopName: "宏量优质店" },
    ];
  }
  return [
    { id: "fallback-generic-1", name: `${keywordText} 标准现货`, price: 9800, shopName: "五金优选供应商" },
    { id: "fallback-generic-2", name: `${keywordText} 定制加工件`, price: 16800, shopName: "本地工业品工厂" },
  ];
}

function normalizeProductCard(item = {}, index) {
  return {
    raw: item,
    key: pick(item, ["id", "productId", "goodsId", "skuId"], index),
    image: sanitizeWujinImageUrl(pick(item, ["image", "imageUrl", "picUrl", "cover", "thumbnail"])),
    title: pick(item, ["title", "name", "productName", "goodsName"], "五金商品"),
    price: formatPrice(pick(item, ["price", "salePrice", "referencePrice", "minPrice"])),
    supplier: pick(item, ["supplier", "supplierName", "shopName", "companyName"], "优选供应商"),
    entityName: pick(item, ["entityName", "materialName"]),
  };
}

async function submitSearch(extra = {}) {
  const searchKeyword = (extra.keyword || keyword.value || "").trim();
  if (!searchKeyword) {
    showToast("请输入五金商品、材料或工艺关键词");
    return;
  }
  if (loading.value) {
    return;
  }
  loading.value = true;
  const startedAt = Date.now();
  try {
    const result = await searchWujin({
      keyword: searchKeyword,
      requestedLane: extra.requestedLane || "PRODUCT",
      entryPath: extra.entryPath || "DIRECT_SEARCH",
      sourceLane: extra.sourceLane,
      sourceKeyword: extra.sourceKeyword,
      sourceProductId: extra.sourceProductId || sourceProductId.value || undefined,
      sourceEntityId: extra.sourceEntityId || sourceEntityId.value || undefined,
      industry: extra.industry,
      categoryId: extra.categoryId,
      categoryPath: extra.categoryPath,
      chainViewed: true,
      responseTimeMillis: Date.now() - startedAt,
    });
    searchResult.value = result;
    keyword.value = result?.keyword || searchKeyword;
    lastKeyword.value = result?.keyword || searchKeyword;
    requestedLane.value = extra.requestedLane || "PRODUCT";
    resetRecommendProducts();
    loadRecommendProducts();
  } catch (error) {
    console.error("Wujin search failed", error);
  } finally {
    loading.value = false;
  }
}

function resetRecommendProducts() {
  recommendProducts.value = [];
  recommendPageNo.value = 1;
  recommendFinished.value = false;
  recommendSearchKeyword.value = "";
}

function recommendationKeywords() {
  if (recommendSearchKeyword.value) {
    return [recommendSearchKeyword.value];
  }
  const originalKeyword = lastKeyword.value || keyword.value;
  if (activeLane.value === "PRODUCT") {
    return [originalKeyword].filter(Boolean);
  }
  const laneKeywords = categorySuggestions.value.map(categorySuggestionLabel);
  return [...new Set([...laneKeywords, originalKeyword].filter(Boolean))].slice(0, 4);
}

async function loadRecommendProducts() {
  if (recommendLoading.value || recommendFinished.value) {
    return;
  }
  if (activeLane.value === "MATERIAL") {
    recommendFinished.value = true;
    return;
  }
  recommendLoading.value = true;
  try {
    let result = null;
    let list = [];
    let matchedKeyword = "";
    for (const searchKeyword of recommendationKeywords()) {
      result = await getWujinProductRecommendations({
        pageNo: recommendPageNo.value,
        pageSize: recommendPageSize,
        keyword: searchKeyword,
        sortField: "salesCount",
        sortAsc: false,
      });
      list = Array.isArray(result?.list) ? result.list : [];
      matchedKeyword = searchKeyword;
      if (list.length) {
        break;
      }
    }
    recommendSearchKeyword.value = matchedKeyword;
    recommendProducts.value = recommendProducts.value.concat(list);
    recommendFinished.value =
      list.length < recommendPageSize ||
      recommendProducts.value.length >= (result?.total || Number.MAX_SAFE_INTEGER);
    recommendPageNo.value += 1;
  } catch (error) {
    console.error("Wujin recommendations failed", error);
    recommendFinished.value = true;
  } finally {
    recommendLoading.value = false;
  }
}

function handleRecommendSelect(item = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?keyword=${encodeURIComponent(item.name || "五金商品")}&lane=PRODUCT&id=${encodeURIComponent(item.id || "")}&entityType=PRODUCT`,
  });
}

function handleLaneSwitch(lane) {
  requestedLane.value = lane;
  submitSearch({
    requestedLane: lane,
    entryPath: "LANE_SWITCH",
    sourceLane: defaultLane.value,
    sourceKeyword: lastKeyword.value,
    sourceProductId: sourceProductId.value || undefined,
    sourceEntityId: sourceEntityId.value || undefined,
  });
}

function handleTraceSearch() {
  const targetKeyword = lastKeyword.value || keyword.value;
  if (!targetKeyword) {
    showToast("请先搜索五金商品、材料或工艺关键词");
    return;
  }
  uni.navigateTo({
    url: `/pages/wujin/sourcing?keyword=${encodeURIComponent(targetKeyword)}&lane=MATERIAL&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`,
  });
}

function buildSummaryContextUrl(pagePath, summary = {}) {
  const baseUrl = `${pagePath}?keyword=${encodeURIComponent(lastKeyword.value || keyword.value)}&lane=${encodeURIComponent(summary.lane || activeLane.value)}&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`;
  const extraQuery = {
    id: summary.id,
    entityType: summary.entityType,
  };
  const extraSearch = Object.entries(extraQuery)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
  return `${baseUrl}${extraSearch ? `&${extraSearch}` : ""}`;
}

function handleDetailOpen(summary = {}) {
  if (!lastKeyword.value && !keyword.value) {
    showToast("请先搜索五金商品、材料或工艺关键词");
    return;
  }
  uni.navigateTo({
    url: buildSummaryContextUrl("/pages/wujin/detail", summary),
  });
}

function handleTraceMapOpen(summary = {}) {
  if (!lastKeyword.value && !keyword.value) {
    showToast("请先搜索五金商品、材料或工艺关键词");
    return;
  }
  uni.navigateTo({
    url: buildSummaryContextUrl("/pages/wujin/trace-fullscreen", summary),
  });
}

function handleCategorySuggestionSelect(item = {}) {
  const nextKeyword = item.categoryName || item.categoryPath || keyword.value;
  keyword.value = nextKeyword;
  submitSearch({
    keyword: nextKeyword,
    requestedLane: item.lane || activeLane.value || "PRODUCT",
    entryPath: "CATEGORY_SUGGESTION",
    sourceLane: activeLane.value,
    sourceKeyword: lastKeyword.value || keyword.value,
    categoryId: item.categoryId,
    categoryPath: item.categoryPath,
  });
}

function handleBack() {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.reLaunch({ url: "/pages/wujin/search" });
}

onLoad((options = {}) => {
  try {
    const sysInfo = uni.getWindowInfo ? uni.getWindowInfo() : uni.getSystemInfoSync();
    statusBarHeight.value = sysInfo?.statusBarHeight || 0;
  } catch (error) {
    statusBarHeight.value = 0;
  }
  if (options.keyword) {
    keyword.value = decodeURIComponent(options.keyword);
    requestedLane.value = options.requestedLane ? decodeURIComponent(options.requestedLane) : "";
    sourceProductId.value = options.sourceProductId
      ? decodeURIComponent(options.sourceProductId)
      : options.entityType === "PRODUCT" && options.id
        ? decodeURIComponent(options.id)
        : "";
    sourceEntityId.value = options.sourceEntityId ? decodeURIComponent(options.sourceEntityId) : "";
    submitSearch({
      requestedLane: requestedLane.value || "PRODUCT",
      entryPath: options.entryPath ? decodeURIComponent(options.entryPath) : "DIRECT_SEARCH",
      sourceLane: options.sourceLane ? decodeURIComponent(options.sourceLane) : undefined,
      sourceKeyword: options.sourceKeyword ? decodeURIComponent(options.sourceKeyword) : undefined,
      sourceProductId: sourceProductId.value || undefined,
      sourceEntityId: sourceEntityId.value || undefined,
      industry: options.industry ? decodeURIComponent(options.industry) : undefined,
    });
  }
});

onReachBottom(() => {
  if (searchResult.value) {
    loadRecommendProducts();
  }
});
</script>

<style lang="scss" scoped>
.search-hero {
  display: grid;
  gap: 24rpx;
  padding-bottom: 32rpx;
}

.result-toolbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  min-height: 64rpx;
  gap: 16rpx;
}

.result-back {
  display: flex;
  width: 64rpx;
  height: 64rpx;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  line-height: 1;
}

.result-title {
  color: #111111;
  font-size: 34rpx;
  font-weight: 900;
}

.search-box {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx;
  border: 2rpx solid rgba(17, 17, 17, 0.12);
  border-radius: 20rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 28rpx rgba(17, 17, 17, 0.08);
}

.search-input {
  flex: 1;
  height: 70rpx;
  min-width: 0;
  color: #111111;
  font-size: 28rpx;
}

.search-button {
  width: 124rpx;
  height: 70rpx;
  margin: 0;
  border-radius: 16rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 27rpx;
  line-height: 70rpx;
  font-weight: 900;
}

.search-button::after,
.result-back::after {
  border: none;
}

.search-content {
  display: flex;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  overflow: hidden;
  flex-direction: column;
  margin-top: -18rpx;
}

.category-suggestion-panel {
  position: relative;
  z-index: 3;
  display: grid;
  gap: 14rpx;
  margin-bottom: 14rpx;
  padding: 18rpx;
  border: 1rpx solid #eceff3;
  border-radius: 16rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(17, 17, 17, 0.06);
}

.category-suggestion-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.category-suggestion-title {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}

.category-suggestion-note {
  color: #6c7580;
  font-size: 22rpx;
  font-weight: 700;
}

.category-suggestion-list {
  width: 100%;
  min-width: 0;
  white-space: nowrap;
}

.category-suggestion-list-inner {
  display: inline-flex;
  min-width: 100%;
  gap: 12rpx;
  padding-bottom: 2rpx;
}

.category-suggestion-groups {
  display: grid;
  gap: 14rpx;
}

.category-suggestion-group {
  min-width: 0;
}

.category-suggestion-chip {
  display: flex;
  align-items: center;
  max-width: 100%;
  height: 58rpx;
  flex-shrink: 0;
  margin: 0;
  padding: 0 16rpx;
  border: none;
  border-radius: 999rpx;
  background: #f7f8fa;
  color: #111111;
  line-height: 58rpx;
}

.category-suggestion-chip::after {
  border: none;
}

.category-suggestion-label {
  overflow: hidden;
  max-width: 220rpx;
  font-size: 23rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-suggestion-empty {
  color: #8a929d;
  font-size: 23rpx;
  line-height: 1.5;
}

.lane-tabs {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.lane-tab {
  display: grid;
  gap: 6rpx;
  padding: 18rpx 12rpx;
  border: 1rpx solid #eceff3;
  border-radius: 18rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(17, 17, 17, 0.06);
}

.lane-tab.active {
  border-color: #111111;
  background: #ffd21e;
}

.lane-name {
  color: #111111;
  font-size: 29rpx;
  font-weight: 900;
}

.lane-desc {
  color: #5d6673;
  font-size: 21rpx;
}

.lane-tab.active .lane-desc {
  color: rgba(17, 17, 17, 0.72);
}

.empty-result-card {
  align-items: flex-start;
}

.empty-result-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.empty-result-text {
  color: #6c7580;
  font-size: 24rpx;
}

.result-stack {
  display: grid;
  gap: 18rpx;
}

.chain-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 0 4rpx;
}

.chain-title {
  color: #111111;
  font-size: 34rpx;
  line-height: 1.35;
  font-weight: 900;
}

.chain-subtitle {
  color: #167f61;
  font-size: 24rpx;
  font-weight: 800;
}

.product-result-list {
  display: grid;
  gap: 16rpx;
}

.product-result-card {
  display: grid;
  gap: 14rpx;
  padding: 16rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 16rpx;
  background: #ffffff;
}

.product-main {
  display: flex;
  gap: 16rpx;
  min-width: 0;
}

.product-image-wrap {
  overflow: hidden;
  width: 112rpx;
  height: 112rpx;
  flex-shrink: 0;
  border-radius: 12rpx;
  background: #eef1f5;
}

.product-image {
  width: 100%;
  height: 100%;
}

.product-image--empty {
  background: radial-gradient(circle at 50% 50%, #5d6673 0 14rpx, #111111 15rpx 38rpx, #eef1f5 39rpx);
}

.product-copy {
  display: grid;
  gap: 8rpx;
  min-width: 0;
  flex: 1;
}

.product-title {
  overflow: hidden;
  color: #111111;
  font-size: 27rpx;
  line-height: 1.35;
  font-weight: 900;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-supplier {
  overflow: hidden;
  color: #6c7580;
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #d92d20;
  font-size: 27rpx;
  font-weight: 900;
}

.product-tags {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
}

.material-card {
  border-radius: 16rpx;
}

.material-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.material-title {
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
}

.material-ratio {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.material-progress {
  overflow: hidden;
  height: 12rpx;
  border-radius: 999rpx;
  background: #e8edf2;
}

.material-progress-bar {
  height: 100%;
  border-radius: 999rpx;
  background: #167f61;
}

.material-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.material-tag {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #f3f6fa;
  color: #2b2f36;
  font-size: 23rpx;
  font-weight: 800;
}

.material-tag:first-child {
  background: #dff6ea;
  color: #167f61;
}

.material-action {
  width: 188rpx;
  height: 64rpx;
  padding: 0 20rpx;
  font-size: 24rpx;
  line-height: 64rpx;
}

.material-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
  padding-top: 4rpx;
}

.downstream-section {
  display: grid;
  gap: 16rpx;
}

.lane-product-empty {
  display: grid;
  gap: 8rpx;
  padding: 24rpx;
  border: 1rpx solid #e2e6eb;
  border-radius: 14rpx;
  background: #ffffff;
}

.lane-product-empty-title {
  color: #111111;
  font-size: 26rpx;
  font-weight: 800;
}

.downstream-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx;
}

.downstream-card {
  display: grid;
  gap: 10rpx;
  min-width: 0;
  padding: 12rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 14rpx;
  background: #ffffff;
}

.downstream-image-wrap {
  overflow: hidden;
  height: 128rpx;
  border-radius: 10rpx;
  background: #eef1f5;
}

.downstream-image {
  width: 100%;
  height: 100%;
}

.downstream-image--empty {
  background: linear-gradient(160deg, #f4f6f8 0%, #dce2e8 100%);
}

.downstream-material {
  overflow: hidden;
  color: #8a6500;
  font-size: 20rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.downstream-title {
  overflow: hidden;
  color: #111111;
  font-size: 24rpx;
  line-height: 1.35;
  font-weight: 800;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.downstream-supplier {
  overflow: hidden;
  color: #6c7580;
  font-size: 21rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.downstream-price {
  color: #d92d20;
  font-size: 27rpx;
  font-weight: 900;
}

.downstream-more {
  height: 48rpx;
  margin: 0;
  border: none;
  border-radius: 10rpx;
  background: #eef2f6;
  color: #111111;
  font-size: 22rpx;
  line-height: 48rpx;
  font-weight: 800;
}

.downstream-more::after {
  border: none;
}
</style>
