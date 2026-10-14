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
          @confirm="handleKeywordSubmit"
        />
        <button class="search-button" @tap="handleKeywordSubmit">搜索</button>
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

      <view v-if="!searchResult" class="wujin-card empty-result-card">
        <text class="empty-result-title">{{ loading ? "正在搜索" : "输入关键词查看产业链结果" }}</text>
        <text class="empty-result-text">搜索结果会在这里按成品、加工、原材料三泳道展示。</text>
      </view>

      <view v-else-if="noResult" class="search-empty-state">
        <view class="search-empty-illustration">
          <AppIcon name="search" variant="muted" size="72rpx" />
        </view>
        <text class="search-empty-title">未找到相关结果，试试其他关键词</text>
        <text class="search-empty-text">没有与“{{ lastKeyword }}”匹配的商品、加工工艺或原材料</text>
        <view class="hot-keyword-head">
          <text class="hot-keyword-title">推荐热搜词</text>
        </view>
        <view class="hot-keyword-list">
          <button
            v-for="item in HOT_WUJIN_SEARCH_KEYWORDS"
            :key="item"
            class="hot-keyword-chip"
            @tap="handleHotKeywordSearch(item)"
          >
            {{ item }}
          </button>
        </view>
      </view>

      <template v-else>
        <view class="filter-bar">
          <view
            class="filter-item"
            :class="{ active: activeDropdown === 'category' || selectedCategory }"
            @tap="toggleDropdown('category')"
          >
            <text class="filter-text">{{ selectedCategory ? selectedCategory.name : `${activeLaneName}分类` }}</text>
            <AppIcon
              :name="activeDropdown === 'category' ? 'expand_less' : 'expand_more'"
              variant="dark"
              size="28rpx"
            />
          </view>
          <view
            class="filter-item"
            :class="{ active: activeDropdown === 'brand' || selectedBrand }"
            @tap="toggleDropdown('brand')"
          >
            <text class="filter-text">{{ selectedBrand ? selectedBrand.brandName : "品牌" }}</text>
            <AppIcon
              :name="activeDropdown === 'brand' ? 'expand_less' : 'expand_more'"
              variant="dark"
              size="28rpx"
            />
          </view>
          <view
            class="filter-item"
            :class="{ active: activeDropdown === 'sort' || sortValue !== 'DEFAULT' }"
            @tap="toggleDropdown('sort')"
          >
            <text class="filter-text">{{ activeSortLabel }}</text>
            <AppIcon
              :name="activeDropdown === 'sort' ? 'expand_less' : 'expand_more'"
              variant="dark"
              size="28rpx"
            />
          </view>

          <view v-if="activeDropdown" class="filter-dropdown">
            <scroll-view class="filter-dropdown-list" scroll-y>
              <template v-if="activeDropdown === 'category'">
                <view
                  class="dropdown-option"
                  :class="{ selected: !selectedCategory }"
                  @tap="clearCategory"
                >
                  <text>全部{{ activeLaneName }}分类</text>
                </view>
                <view
                  v-for="node in categoryRoots"
                  :key="node.categoryId"
                  class="dropdown-option"
                  :class="{ selected: isCategoryInSelection(node) }"
                  @tap="handleCategoryRootSelect(node)"
                >
                  <text>{{ node.name }}</text>
                  <text v-if="hasChildren(node)" class="dropdown-option-more">选择{{ childLevelName(node) }}</text>
                </view>
                <view v-if="!categoryRoots.length" class="dropdown-empty">
                  暂无匹配的{{ activeLaneName }}分类
                </view>
              </template>
              <template v-else-if="activeDropdown === 'brand'">
                <view
                  class="dropdown-option"
                  :class="{ selected: !selectedBrand }"
                  @tap="handleBrandSelect(null)"
                >
                  <text>全部品牌</text>
                </view>
                <view
                  v-for="brand in brandFacets"
                  :key="brand.brandId"
                  class="dropdown-option"
                  :class="{ selected: selectedBrand && selectedBrand.brandId === brand.brandId }"
                  @tap="handleBrandSelect(brand)"
                >
                  <text>{{ brand.brandName }}</text>
                  <text class="dropdown-option-more">{{ brand.productCount }} 件</text>
                </view>
                <view v-if="!brandFacets.length" class="dropdown-empty">当前结果暂无品牌信息</view>
              </template>
              <template v-else>
                <view
                  v-for="option in sortOptions"
                  :key="option.value"
                  class="dropdown-option"
                  :class="{ selected: sortValue === option.value }"
                  @tap="handleSortSelect(option.value)"
                >
                  <text>{{ option.label }}</text>
                </view>
              </template>
            </scroll-view>
          </view>
        </view>
        <view
          v-if="activeDropdown"
          class="filter-dropdown-mask"
          @tap="closeDropdown"
          @touchmove.stop.prevent
        ></view>

        <view v-if="selectedCategory || selectedBrand" class="active-filters">
          <view v-if="selectedCategory" class="active-filter-chip" @tap="clearCategory">
            <text class="active-filter-text">{{ selectedCategory.pathNames.join(" / ") }}</text>
            <AppIcon name="close" variant="dark" size="24rpx" />
          </view>
          <view v-if="selectedBrand" class="active-filter-chip" @tap="handleBrandSelect(null)">
            <text class="active-filter-text">{{ selectedBrand.brandName }}</text>
            <AppIcon name="close" variant="dark" size="24rpx" />
          </view>
        </view>

        <view class="result-stack">
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

          <view v-if="activeLane !== 'PRODUCT'" class="upstream-panel">
            <text class="upstream-title">{{ upstreamTitle }}</text>
            <view v-if="laneUpstream.length" class="upstream-list">
              <button
                v-for="entity in laneUpstream"
                :key="entity.id"
                class="upstream-chip"
                @tap="handleEntityOpen(entity)"
              >
                {{ entity.name }}
              </button>
            </view>
            <text v-else-if="laneLoaded" class="upstream-empty">{{ upstreamEmptyText }}</text>
            <view class="upstream-actions">
              <button class="wujin-button wujin-button--yellow" @tap="handleTraceSearch">
                一键寻源
              </button>
              <button class="wujin-button wujin-button--line" @tap="handleTraceMapOpen">
                溯源图谱
              </button>
            </view>
          </view>

          <view v-if="productCards.length" class="product-result-list">
            <view
              v-for="item in productCards"
              :key="item.key"
              class="product-result-card"
              @tap="handleProductOpen(item.raw)"
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
                  <text v-if="item.subtitle" class="product-supplier">{{ item.subtitle }}</text>
                  <text class="product-price">{{ item.price }}</text>
                  <text v-if="item.meta" class="product-meta">{{ item.meta }}</text>
                </view>
              </view>
              <view v-if="item.tags.length" class="product-tag-list">
                <text v-for="tag in item.tags" :key="tag" class="product-tag">{{ tag }}</text>
              </view>
            </view>
          </view>

          <view v-else-if="laneLoaded" class="lane-product-empty">
            <text class="lane-product-empty-title">{{ laneEmptyTitle }}</text>
            <text v-if="activeLane !== 'PRODUCT'" class="wujin-muted">可通过一键寻源提交采购需求</text>
          </view>

          <view v-if="productCards.length" class="load-more">
            <text>{{ laneLoading ? "加载中..." : laneFinished ? "没有更多了" : "上拉加载更多" }}</text>
          </view>
        </view>
      </template>
    </view>

    <view
      v-if="categoryPopupVisible"
      class="category-popup-mask"
      @tap="closeCategoryPopup"
      @touchmove.stop.prevent
    ></view>
    <view v-if="categoryPopupVisible" class="category-popup">
      <view class="category-popup-head">
        <text class="category-popup-title">{{ popupRoot ? popupRoot.name : "" }}</text>
        <button class="category-popup-close" @tap="closeCategoryPopup">
          <AppIcon name="close" variant="dark" size="32rpx" />
        </button>
      </view>
      <text class="category-popup-path">{{ popupPathText }}</text>
      <view class="category-popup-columns">
        <scroll-view
          v-for="(column, columnIndex) in popupColumns"
          :key="column.parent.categoryId"
          class="category-popup-column"
          scroll-y
        >
          <text class="category-popup-level">{{ childLevelName(column.parent) }}</text>
          <view
            class="popup-option"
            :class="{ selected: popupPath.length === columnIndex }"
            @tap="handlePopupAllSelect(columnIndex)"
          >
            全部{{ column.parent.name }}
          </view>
          <view
            v-for="node in column.parent.children"
            :key="node.categoryId"
            class="popup-option"
            :class="{ selected: popupPath[columnIndex] && popupPath[columnIndex].categoryId === node.categoryId }"
            @tap="handlePopupNodeSelect(columnIndex, node)"
          >
            <text>{{ node.name }}</text>
            <text v-if="hasChildren(node)" class="popup-option-arrow">›</text>
          </view>
        </scroll-view>
      </view>
      <view class="category-popup-footer">
        <button class="wujin-button wujin-button--line" @tap="handlePopupReset">重置</button>
        <button class="wujin-button wujin-button--yellow" @tap="handlePopupConfirm">确定</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";

import { getWujinLaneProducts, searchWujin } from "@/api/wujin/search";
import { showToast } from "@/utils/toast";
import { sanitizeWujinImageUrl } from "@/utils/wujinImage";
import { HOT_WUJIN_SEARCH_KEYWORDS } from "./search-history.mjs";

defineOptions({ name: "WujinMiniSearchResult" });

const laneOptions = [
  { label: "成品", value: "PRODUCT", description: "商品与规格" },
  { label: "加工", value: "PROCESS", description: "工艺与能力" },
  { label: "原材料", value: "MATERIAL", description: "材料与供给" },
];

const sortOptions = [
  { label: "综合排序", value: "DEFAULT" },
  { label: "销量优先", value: "SALES" },
  { label: "价格从低到高", value: "PRICE_ASC" },
  { label: "价格从高到低", value: "PRICE_DESC" },
];

const LANE_PAGE_SIZE = 10;

// keyword 是输入框内容；lastKeyword 是当前结果围绕的搜索词，分类/品牌筛选不会改写它
const keyword = ref("");
const lastKeyword = ref("");
const requestedLane = ref("");
const sourceProductId = ref("");
const sourceEntityId = ref("");
const searchResult = ref(null);
const loading = ref(false);
const statusBarHeight = ref(0);

const laneProducts = ref([]);
const laneTotal = ref(0);
const lanePageNo = ref(1);
const laneLoading = ref(false);
const laneLoaded = ref(false);
const laneFinished = ref(false);
const laneEmptyHint = ref("");
const laneUpstream = ref([]);
const brandFacets = ref([]);
let laneRequestSeq = 0;

const selectedCategory = ref(null);
const selectedBrand = ref(null);
const sortValue = ref("DEFAULT");
const activeDropdown = ref("");
const categoryPopupVisible = ref(false);
const popupRoot = ref(null);
const popupPath = ref([]);

const heroSafeStyle = computed(() => ({
  paddingTop: `calc(${statusBarHeight.value}px + 24rpx)`,
}));

const defaultLane = computed(() => searchResult.value?.defaultLane || "PRODUCT");
const activeLane = computed(() => requestedLane.value || defaultLane.value);
const activeLaneName = computed(
  () => laneOptions.find((item) => item.value === activeLane.value)?.label || "相关",
);
const activeSortLabel = computed(
  () => sortOptions.find((item) => item.value === sortValue.value)?.label || "综合排序",
);
const laneSummaries = computed(() => searchResult.value?.laneSummaries || []);
const activeLaneSummary = computed(
  () => laneSummaries.value.find((item) => item.lane === activeLane.value) || {},
);
const categoryRoots = computed(() => {
  const tree = Array.isArray(searchResult.value?.categoryTree) ? searchResult.value.categoryTree : [];
  return tree.filter((node) => node?.lane === activeLane.value);
});
const totalLaneCount = computed(() =>
  laneSummaries.value.reduce((sum, item) => sum + Number(item.resultCount || 0), 0),
);
const noResult = computed(
  () =>
    Boolean(searchResult.value) &&
    laneLoaded.value &&
    totalLaneCount.value === 0 &&
    !categoryRoots.value.length &&
    !laneUpstream.value.length,
);
const upstreamTitle = computed(
  () =>
    activeLaneSummary.value.summary ||
    `与“${lastKeyword.value}”相关的上游${activeLane.value === "PROCESS" ? "加工工艺" : "原材料"}`,
);
const upstreamEmptyText = computed(() =>
  activeLane.value === "PROCESS" ? "暂无上游关联的加工工艺" : "暂无上游关联的原材料",
);
const laneEmptyTitle = computed(() => laneEmptyHint.value || `当前${activeLaneName.value}泳道暂无商品`);
const productCards = computed(() => laneProducts.value.map(normalizeProductCard));
const riskWarningRequired = computed(() => searchResult.value?.riskWarningRequired);
const riskWarningText = computed(
  () => searchResult.value?.riskWarningText || "不同应用场景可能存在不可互换风险。",
);
const popupColumns = computed(() => {
  if (!popupRoot.value) {
    return [];
  }
  const columns = [{ parent: popupRoot.value }];
  popupPath.value.forEach((node) => {
    if (hasChildren(node)) {
      columns.push({ parent: node });
    }
  });
  return columns;
});
const popupPathText = computed(() =>
  [popupRoot.value?.name, ...popupPath.value.map((node) => node.name)].filter(Boolean).join(" / "),
);

function laneCount(lane) {
  const summary = laneSummaries.value.find((item) => item.lane === lane);
  return searchResult.value && summary ? ` ${summary.resultCount || 0}` : "";
}

function hasChildren(node) {
  return Array.isArray(node?.children) && node.children.length > 0;
}

function childLevelName(node) {
  const names = { 1: "一级分类", 2: "二级分类", 3: "三级分类" };
  return names[Number(node?.level)] || "下级分类";
}

function isCategoryInSelection(node) {
  return Boolean(selectedCategory.value?.pathIds?.includes(node.categoryId));
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

function normalizeProductCard(item = {}, index) {
  const tags = Array.isArray(item.tags) ? item.tags.filter(Boolean) : [];
  if (Number(item.materialCount) > 0) {
    tags.push(`绑定${item.materialCount}类原材料`);
  }
  const meta = [
    item.minOrderQuantity ? `起订 ${item.minOrderQuantity}` : "",
    item.deliveryDays ? `${item.deliveryDays}天发货` : "",
    item.serviceArea || "",
  ].filter(Boolean);
  return {
    raw: item,
    key: pick(item, ["id"], index),
    image: sanitizeWujinImageUrl(pick(item, ["image", "imageUrl", "picUrl", "cover", "thumbnail"])),
    title: pick(item, ["name", "title"], "五金商品"),
    price: formatPrice(item.price),
    subtitle: [item.brandName, item.entityName].filter(Boolean).join(" · "),
    meta: meta.join(" · "),
    tags: [...new Set(tags)].slice(0, 4),
  };
}

function resetFilters() {
  selectedCategory.value = null;
  selectedBrand.value = null;
  sortValue.value = "DEFAULT";
  activeDropdown.value = "";
  categoryPopupVisible.value = false;
}

function resetLaneProducts() {
  laneRequestSeq += 1;
  laneProducts.value = [];
  laneTotal.value = 0;
  lanePageNo.value = 1;
  laneLoading.value = false;
  laneLoaded.value = false;
  laneFinished.value = false;
  laneEmptyHint.value = "";
  laneUpstream.value = [];
  brandFacets.value = [];
}

async function loadLaneProducts() {
  if (laneLoading.value || laneFinished.value || !lastKeyword.value) {
    return;
  }
  const requestSeq = laneRequestSeq;
  laneLoading.value = true;
  try {
    const page = await getWujinLaneProducts({
      keyword: lastKeyword.value,
      lane: activeLane.value,
      categoryId: selectedCategory.value?.categoryId,
      brandId: selectedBrand.value?.brandId,
      sourceProductId: sourceProductId.value || undefined,
      sort: sortValue.value,
      pageNo: lanePageNo.value,
      pageSize: LANE_PAGE_SIZE,
    });
    if (requestSeq !== laneRequestSeq) {
      return;
    }
    const list = Array.isArray(page?.list) ? page.list : [];
    laneProducts.value = laneProducts.value.concat(list);
    laneTotal.value = Number(page?.total || 0);
    laneEmptyHint.value = page?.emptyHint || "";
    laneUpstream.value = Array.isArray(page?.upstreamEntities) ? page.upstreamEntities : [];
    brandFacets.value = Array.isArray(page?.brands) ? page.brands : [];
    // 最后一页之后后端返回空列表，这里据此结束分页
    laneFinished.value = list.length < LANE_PAGE_SIZE || laneProducts.value.length >= laneTotal.value;
    lanePageNo.value += 1;
  } catch (error) {
    console.error("Wujin lane products failed", error);
    if (requestSeq === laneRequestSeq) {
      laneFinished.value = true;
    }
  } finally {
    if (requestSeq === laneRequestSeq) {
      laneLoading.value = false;
      laneLoaded.value = true;
    }
  }
}

function reloadLaneProducts() {
  resetLaneProducts();
  loadLaneProducts();
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
      chainViewed: true,
      responseTimeMillis: Date.now() - startedAt,
    });
    searchResult.value = result;
    keyword.value = result?.keyword || searchKeyword;
    lastKeyword.value = result?.keyword || searchKeyword;
    requestedLane.value = extra.requestedLane || "PRODUCT";
    resetFilters();
    reloadLaneProducts();
  } catch (error) {
    console.error("Wujin search failed", error);
  } finally {
    loading.value = false;
  }
}

function handleKeywordSubmit() {
  sourceProductId.value = "";
  sourceEntityId.value = "";
  // 新关键词回到成品泳道（成品优先），与首页进入结果页一致
  submitSearch({ requestedLane: "PRODUCT", entryPath: "DIRECT_SEARCH" });
}

function handleHotKeywordSearch(item) {
  keyword.value = item;
  handleKeywordSubmit();
}

function handleLaneSwitch(lane) {
  if (!lastKeyword.value) {
    requestedLane.value = lane;
    return;
  }
  // 切换泳道围绕当前搜索词，不使用输入框里尚未提交的内容
  submitSearch({
    keyword: lastKeyword.value,
    requestedLane: lane,
    entryPath: "LANE_SWITCH",
    sourceLane: activeLane.value,
    sourceKeyword: lastKeyword.value,
    sourceProductId: sourceProductId.value || undefined,
    sourceEntityId: sourceEntityId.value || undefined,
  });
}

function toggleDropdown(name) {
  activeDropdown.value = activeDropdown.value === name ? "" : name;
}

function closeDropdown() {
  activeDropdown.value = "";
}

function applyCategory(path) {
  const target = path[path.length - 1];
  selectedCategory.value = target
    ? {
        categoryId: target.categoryId,
        name: target.name,
        pathIds: path.map((node) => node.categoryId),
        pathNames: path.map((node) => node.name),
      }
    : null;
  closeDropdown();
  categoryPopupVisible.value = false;
  reloadLaneProducts();
  if (target) {
    logCategoryFilter();
  }
}

// 只记录筛选行为，不刷新搜索结果，搜索框仍保留原搜索词
function logCategoryFilter() {
  searchWujin(
    {
      keyword: lastKeyword.value,
      requestedLane: activeLane.value,
      entryPath: "CATEGORY_SUGGESTION",
      sourceLane: activeLane.value,
      sourceKeyword: lastKeyword.value,
      categoryId: selectedCategory.value.categoryId,
      categoryPath: selectedCategory.value.pathNames.join(" > "),
      chainViewed: true,
    },
    { silent: true },
  ).catch((error) => console.warn("Wujin category filter log failed", error));
}

function clearCategory() {
  if (!selectedCategory.value) {
    closeDropdown();
    return;
  }
  applyCategory([]);
}

function handleCategoryRootSelect(node) {
  if (!hasChildren(node)) {
    applyCategory([node]);
    return;
  }
  closeDropdown();
  popupRoot.value = node;
  const selectedIds = selectedCategory.value?.pathIds || [];
  popupPath.value = selectedIds[0] === node.categoryId ? restorePopupPath(node, selectedIds.slice(1)) : [];
  categoryPopupVisible.value = true;
}

function restorePopupPath(root, ids) {
  const path = [];
  let current = root;
  ids.forEach((id) => {
    const next = (current?.children || []).find((child) => child.categoryId === id);
    if (next) {
      path.push(next);
      current = next;
    }
  });
  return path;
}

function handlePopupAllSelect(columnIndex) {
  popupPath.value = popupPath.value.slice(0, columnIndex);
}

function handlePopupNodeSelect(columnIndex, node) {
  popupPath.value = [...popupPath.value.slice(0, columnIndex), node];
}

function handlePopupReset() {
  popupPath.value = [];
}

function handlePopupConfirm() {
  applyCategory([popupRoot.value, ...popupPath.value]);
}

function closeCategoryPopup() {
  categoryPopupVisible.value = false;
}

function handleBrandSelect(brand) {
  selectedBrand.value = brand ? { brandId: brand.brandId, brandName: brand.brandName } : null;
  closeDropdown();
  reloadLaneProducts();
}

function handleSortSelect(value) {
  sortValue.value = value;
  closeDropdown();
  reloadLaneProducts();
}

function handleProductOpen(item = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?productId=${encodeURIComponent(item.id || "")}&lane=${encodeURIComponent(item.lane || activeLane.value)}&keyword=${encodeURIComponent(item.name || "")}&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`,
  });
}

function handleEntityOpen(entity = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?id=${encodeURIComponent(entity.id || "")}&lane=${encodeURIComponent(entity.lane || activeLane.value)}&keyword=${encodeURIComponent(entity.name || "")}&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`,
  });
}

function handleTraceSearch() {
  if (!lastKeyword.value) {
    showToast("请先搜索五金商品、材料或工艺关键词");
    return;
  }
  uni.navigateTo({
    url: `/pages/wujin/sourcing?keyword=${encodeURIComponent(lastKeyword.value)}&lane=${encodeURIComponent(activeLane.value)}&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`,
  });
}

function handleTraceMapOpen() {
  if (!lastKeyword.value) {
    showToast("请先搜索五金商品、材料或工艺关键词");
    return;
  }
  uni.navigateTo({
    url: `/pages/wujin/trace-fullscreen?keyword=${encodeURIComponent(lastKeyword.value)}&lane=${encodeURIComponent(activeLane.value)}&sourceKeyword=${encodeURIComponent(lastKeyword.value)}`,
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
    sourceProductId.value = options.sourceProductId ? decodeURIComponent(options.sourceProductId) : "";
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
  if (searchResult.value && !noResult.value) {
    loadLaneProducts();
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
.result-back::after {
  border: none;
}
.search-content {
  display: flex;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  flex-direction: column;
  margin-top: -18rpx;
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
/* 无图时用中性占位，避免看起来像某类商品（旧样式像轮胎） */
.product-image--empty {
  background: linear-gradient(160deg, #f4f6f8 0%, #dce2e8 100%);
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

.search-empty-state {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 14rpx;
  padding: 56rpx 32rpx 40rpx;
  border: 1rpx solid #eceff3;
  border-radius: 18rpx;
  background: #ffffff;
}

.search-empty-illustration {
  display: flex;
  width: 148rpx;
  height: 148rpx;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f3f5f8;
}

.search-empty-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
  text-align: center;
}

.search-empty-text {
  color: #6c7580;
  font-size: 24rpx;
  text-align: center;
}

.hot-keyword-head {
  align-self: stretch;
  margin-top: 20rpx;
}

.hot-keyword-title {
  color: #111111;
  font-size: 26rpx;
  font-weight: 800;
}

.hot-keyword-list {
  display: flex;
  align-self: stretch;
  flex-wrap: wrap;
  gap: 14rpx;
}

.hot-keyword-chip {
  height: 58rpx;
  margin: 0;
  padding: 0 22rpx;
  border: none;
  border-radius: 999rpx;
  background: #fff6cc;
  color: #111111;
  font-size: 24rpx;
  line-height: 58rpx;
  font-weight: 700;
}

.hot-keyword-chip::after,
.upstream-chip::after,
.category-popup-close::after {
  border: none;
}

.filter-bar {
  position: relative;
  z-index: 20;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-bottom: 14rpx;
  border: 1rpx solid #eceff3;
  border-radius: 16rpx;
  background: #ffffff;
}

.filter-item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  height: 76rpx;
  gap: 6rpx;
  padding: 0 12rpx;
}

.filter-text {
  overflow: hidden;
  color: #2b2f36;
  font-size: 25rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filter-item.active .filter-text {
  color: #b07d00;
}

.filter-dropdown {
  position: absolute;
  top: 84rpx;
  right: 0;
  left: 0;
  overflow: hidden;
  border: 1rpx solid #eceff3;
  border-radius: 16rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 40rpx rgba(17, 17, 17, 0.12);
}

.filter-dropdown-list {
  max-height: 600rpx;
}

.dropdown-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f2f4f7;
  color: #111111;
  font-size: 26rpx;
}

.dropdown-option.selected {
  color: #b07d00;
  font-weight: 900;
}

.dropdown-option-more {
  flex-shrink: 0;
  color: #8a929d;
  font-size: 22rpx;
}

.dropdown-empty {
  padding: 28rpx;
  color: #8a929d;
  font-size: 24rpx;
}

.filter-dropdown-mask,
.category-popup-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 15;
  background: rgba(17, 17, 17, 0.32);
}

.active-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.active-filter-chip {
  display: flex;
  align-items: center;
  max-width: 100%;
  gap: 8rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #fff1b8;
}

.active-filter-text {
  overflow: hidden;
  color: #111111;
  font-size: 23rpx;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upstream-panel {
  display: grid;
  gap: 16rpx;
  padding: 22rpx;
  border: 1rpx solid #e2e6eb;
  border-radius: 16rpx;
  background: #ffffff;
}

.upstream-title {
  color: #111111;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.4;
}

.upstream-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.upstream-chip {
  height: 56rpx;
  margin: 0;
  padding: 0 20rpx;
  border: none;
  border-radius: 999rpx;
  background: #dff6ea;
  color: #167f61;
  font-size: 24rpx;
  line-height: 56rpx;
  font-weight: 800;
}

.upstream-empty {
  color: #8a929d;
  font-size: 24rpx;
}

.upstream-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.product-meta {
  color: #8a929d;
  font-size: 21rpx;
}

.product-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.product-tag {
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: #f3f6fa;
  color: #2b2f36;
  font-size: 22rpx;
  font-weight: 700;
}

.product-tag:first-child {
  background: #dff6ea;
  color: #167f61;
}

.load-more {
  padding: 12rpx 0 28rpx;
  color: #8a929d;
  font-size: 23rpx;
  text-align: center;
}

.category-popup-mask {
  z-index: 40;
}

.category-popup {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 41;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 28rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
  border-radius: 28rpx 28rpx 0 0;
  background: #ffffff;
}

.category-popup-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.category-popup-title {
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
}

.category-popup-close {
  display: flex;
  width: 56rpx;
  height: 56rpx;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0;
  border: none;
  background: transparent;
}

.category-popup-path {
  color: #6c7580;
  font-size: 23rpx;
}

.category-popup-columns {
  display: flex;
  height: 620rpx;
  border: 1rpx solid #eceff3;
  border-radius: 16rpx;
}

.category-popup-column {
  flex: 1;
  min-width: 0;
  height: 620rpx;
  border-right: 1rpx solid #eceff3;
}

.category-popup-column:last-child {
  border-right: none;
}

.category-popup-level {
  display: block;
  padding: 18rpx 20rpx 10rpx;
  color: #8a929d;
  font-size: 22rpx;
  font-weight: 800;
}

.popup-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8rpx;
  padding: 20rpx;
  color: #111111;
  font-size: 25rpx;
}

.popup-option.selected {
  background: #fff6cc;
  color: #b07d00;
  font-weight: 900;
}

.popup-option-arrow {
  color: #8a929d;
}

.category-popup-footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  padding-top: 8rpx;
}
</style>
