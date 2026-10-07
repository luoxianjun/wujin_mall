<template>
  <view class="wujin-page search-page">
    <view class="wujin-hero search-hero" :style="heroSafeStyle">
      <view class="home-toolbar">
        <button class="location-button" @tap="handleChooseLocation">
          <AppIcon name="location_on" variant="dark" size="31rpx" />
          <text class="location-name">{{ currentLocation }}</text>
          <AppIcon name="expand_more" variant="dark" size="26rpx" />
        </button>

        <view class="toolbar-actions">
          <button class="toolbar-action" @tap="handleMessagesOpen">
            <AppIcon name="notifications_none" variant="dark" size="34rpx" />
          </button>
          <button class="toolbar-action" @tap="handleScanCode">
            <AppIcon name="qr_code_scanner" variant="dark" size="34rpx" />
          </button>
        </view>
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
      <view class="wujin-card wujin-card--flat">
        <view class="wujin-section-head">
          <text class="wujin-section-title">热门搜索</text>
          <text class="wujin-section-note">工业高频词</text>
        </view>
        <view class="hot-keyword-list">
          <view class="hot-keyword-row">
            <button
              v-for="item in HOT_WUJIN_SEARCH_KEYWORDS"
              :key="item"
              class="keyword-chip hot"
              @tap="handleKeywordQuickSearch(item)"
            >
              {{ item }}
            </button>
          </view>
        </view>
      </view>

      <ProductRecommendList
        :items="safeRecommendProducts"
        :loading="recommendLoading"
        :finished="recommendFinished"
        @loadMore="loadRecommendProducts"
        @select="handleRecommendSelect"
      />
    </view>

    <BottomNavBar active="home" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onReachBottom } from "@dcloudio/uni-app";

import BottomNavBar from "@/components/BottomNavBar.vue";
import ProductRecommendList from "@/components/wujin/ProductRecommendList.vue";
import { getWujinProductRecommendations } from "@/api/wujin/recommend";
import { showToast } from "@/utils/toast";
import { sanitizeWujinImageUrl } from "@/utils/wujinImage";
import { HOT_WUJIN_SEARCH_KEYWORDS } from "./search-history.mjs";

defineOptions({ name: "WujinMiniSearch" });

const keyword = ref("");
const currentLocation = ref("全国");
const statusBarHeight = ref(0);
const recommendProducts = ref([]);
const recommendPageNo = ref(1);
const recommendPageSize = 10;
const recommendLoading = ref(false);
const recommendFinished = ref(false);

const heroSafeStyle = computed(() => ({
  paddingTop: `calc(${statusBarHeight.value}px + 24rpx)`,
}));

const safeRecommendProducts = computed(() =>
  recommendProducts.value.map((item) => ({
    ...item,
    image: sanitizeWujinImageUrl(pick(item, ["image", "imageUrl", "picUrl", "cover", "thumbnail"])),
  })),
);

function pick(item, keys, fallback = "") {
  const key = keys.find((name) => {
    const value = item?.[name];
    return value !== undefined && value !== null && value !== "";
  });
  return key ? item[key] : fallback;
}

function submitSearch(extra = {}) {
  const searchKeyword = (extra.keyword || keyword.value || "").trim();
  if (!searchKeyword) {
    showToast("请输入五金商品、材料或工艺关键词");
    return;
  }
  navigateToSearchResult(searchKeyword, {
    requestedLane: extra.requestedLane,
    entryPath: extra.entryPath || "DIRECT_SEARCH",
    sourceLane: extra.sourceLane,
    sourceKeyword: extra.sourceKeyword,
    industry: extra.industry,
  });
}

function navigateToSearchResult(searchKeyword, extra = {}) {
  const extraSearch = Object.entries({
    requestedLane: extra.requestedLane,
    entryPath: extra.entryPath,
    sourceLane: extra.sourceLane,
    sourceKeyword: extra.sourceKeyword,
    industry: extra.industry,
  })
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
  uni.navigateTo({
    url: `/pages/wujin/search-result?keyword=${encodeURIComponent(searchKeyword)}${extraSearch ? `&${extraSearch}` : ""}`,
  });
}

async function loadRecommendProducts() {
  if (recommendLoading.value || recommendFinished.value) {
    return;
  }
  recommendLoading.value = true;
  try {
    const result = await getWujinProductRecommendations({
      pageNo: recommendPageNo.value,
      pageSize: recommendPageSize,
      sortField: "salesCount",
      sortAsc: false,
    });
    const list = Array.isArray(result?.list) ? result.list : [];
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

function handleKeywordQuickSearch(searchKeyword) {
  keyword.value = searchKeyword;
  submitSearch({
    keyword: searchKeyword,
    requestedLane: "",
    entryPath: "HOT_KEYWORD",
  });
}

function resolveLocationName(location = {}) {
  const rawName = (location.name || location.address || "").trim();
  if (!rawName) {
    return "";
  }
  const cityMatch = rawName.match(/([^省市区县镇街道路号]{2,8}市)/);
  if (cityMatch?.[1]) {
    return cityMatch[1].replace("市", "");
  }
  return rawName.length > 8 ? rawName.slice(0, 8) : rawName;
}

function handleChooseLocation() {
  uni.chooseLocation({
    success(location) {
      const nextLocation = resolveLocationName(location);
      if (nextLocation) {
        currentLocation.value = nextLocation;
        showToast(`已切换到${nextLocation}`);
      }
    },
    fail(error = {}) {
      if (String(error.errMsg || "").includes("cancel")) {
        return;
      }
      showToast("定位失败，可继续使用全国供应");
    },
  });
}

function handleMessagesOpen() {
  uni.navigateTo({
    url: "/pages/messages/index",
  });
}

function handleScanCode() {
  uni.scanCode({
    scanType: ["barCode", "qrCode"],
    success(result = {}) {
      const scanResult = String(result.result || "").trim();
      if (!scanResult) {
        showToast("未识别到有效内容");
        return;
      }
      const scanKeyword = scanResult.length > 30 ? scanResult.slice(0, 30) : scanResult;
      keyword.value = scanKeyword;
      submitSearch({
        keyword: scanKeyword,
        requestedLane: "",
        entryPath: "SCAN_CODE",
      });
    },
    fail(error = {}) {
      if (String(error.errMsg || "").includes("cancel")) {
        return;
      }
      showToast("扫码失败，请重试");
    },
  });
}

onLoad((options = {}) => {
  try {
    const sysInfo = uni.getWindowInfo ? uni.getWindowInfo() : uni.getSystemInfoSync();
    statusBarHeight.value = sysInfo?.statusBarHeight || 0;
  } catch (error) {
    statusBarHeight.value = 0;
  }
  if (options.keyword) {
    const searchKeyword = decodeURIComponent(options.keyword);
    keyword.value = searchKeyword;
    navigateToSearchResult(searchKeyword, {
      entryPath: "LEGACY_HOME_QUERY",
    });
    return;
  }
  loadRecommendProducts();
});

onReachBottom(() => {
  loadRecommendProducts();
});
</script>

<style lang="scss" scoped>
.search-hero {
  display: grid;
  gap: 24rpx;
  padding-bottom: 36rpx;
}

.home-toolbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 70rpx;
  gap: 20rpx;
  padding-right: 184rpx;
  box-sizing: border-box;
}

.location-button,
.toolbar-action {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: none;
  background: transparent;
  color: #111111;
  line-height: 1;
}

.location-button {
  max-width: 310rpx;
  min-width: 0;
  justify-content: flex-start;
  gap: 5rpx;
  padding-right: 8rpx;
}

.location-name {
  max-width: 210rpx;
  overflow: hidden;
  color: #111111;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 14rpx;
  flex-shrink: 0;
}

.toolbar-action {
  width: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.46);
}

.location-button::after,
.toolbar-action::after {
  border: none;
}

.search-box {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 0;
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
.keyword-chip::after {
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

.hot-keyword-list {
  width: 100%;
  overflow: hidden;
}

.hot-keyword-row {
  display: flex;
  width: 100%;
  flex-wrap: wrap;
  gap: 12rpx;
  box-sizing: border-box;
}

.keyword-chip {
  height: 56rpx;
  margin: 0;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #f7f8fa;
  color: #2b2f36;
  font-size: 24rpx;
  line-height: 56rpx;
  font-weight: 700;
}

.keyword-chip.hot {
  background: #eef0f2;
  color: #4b5563;
}
</style>
