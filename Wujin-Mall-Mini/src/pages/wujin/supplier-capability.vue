<template>
  <view class="wujin-page wujin-page--plain capability-page">
    <view class="wujin-hero capability-hero">
      <WujinBackBar />
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">供应能力详情</text>
        <text class="hero-keyword">{{ keyword || "五金供应" }}</text>
      </view>
      <text class="wujin-hero__title">{{ capability.supplierName }}</text>
      <text class="wujin-hero__subtitle">{{ capability.serviceNote }}</text>
      <view class="score-panel">
        <text class="score-value">{{ capability.matchScore }}%</text>
        <text class="score-label">匹配度</text>
      </view>
    </view>

    <view class="wujin-content capability-content">
      <view class="wujin-action-grid top-actions">
        <button class="wujin-button wujin-button--yellow" @tap="handleChatOpen">
          发起沟通
        </button>
        <button class="wujin-button wujin-button--line" @tap="handleSourcingOpen">
          提交线索
        </button>
      </view>

      <view class="wujin-card">
        <text class="wujin-section-title">主营能力</text>
        <view class="wujin-chip-list">
          <text
            v-for="item in capability.mainCapabilities"
            :key="item"
            class="wujin-chip wujin-chip--yellow"
          >
            {{ item }}
          </text>
        </view>
      </view>

      <view class="wujin-card">
        <text class="wujin-section-title">可承接泳道</text>
        <view class="lane-list">
          <view
            v-for="lane in capability.lanes"
            :key="lane.value"
            class="lane-row"
          >
            <text class="lane-name">{{ lane.label }}</text>
            <text class="wujin-muted">{{ lane.note }}</text>
          </view>
        </view>
      </view>

      <view v-if="capability.approvedTags && capability.approvedTags.length" class="wujin-card">
        <view class="wujin-section-head">
          <text class="wujin-section-title">平台认证标签</text>
          <AppIcon name="verified" variant="success" size="34rpx" />
        </view>
        <view class="wujin-chip-list">
          <text
            v-for="tag in capability.approvedTags"
            :key="tag"
            class="wujin-chip wujin-chip--green"
          >
            {{ tag }}
          </text>
        </view>
      </view>

      <view class="wujin-card">
        <text class="wujin-section-title">供应明细</text>
        <view
          v-if="capability.capabilities && capability.capabilities.length"
          class="capability-list"
        >
          <view
            v-for="item in capability.capabilities"
            :key="`${item.productId}-${item.entityId}`"
            class="capability-row"
          >
            <text class="capability-name">{{ item.productName }}</text>
            <text class="wujin-muted">
              {{ item.entityName || "产业链实体" }} · 库存 {{ item.stockCount ?? "待确认" }} ·
              起订 {{ item.minOrderQuantity ?? "可议" }} ·
              {{ item.deliveryDays ? `${item.deliveryDays} 天交付` : "交期待确认" }}
            </text>
            <text v-if="item.serviceArea" class="wujin-muted">服务区域：{{ item.serviceArea }}</text>
          </view>
        </view>
        <text v-else class="wujin-text">
          暂无登记的供应明细，可提交寻源线索，由平台协助确认资质、产能与报价。
        </text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import { getWujinSupplierCapability } from "@/api/wujin/sourcing";
import { showToast } from "@/utils/toast";
import WujinBackBar from "@/components/WujinBackBar.vue";

defineOptions({ name: "WujinMiniSupplierCapability" });

const supplierId = ref("");
const keyword = ref("");
const currentLane = ref("");
const sourceKeyword = ref("");
const candidatePayload = ref(null);
const capability = ref(buildFallbackCapability());

function parseCandidatePayload(rawPayload) {
  if (!rawPayload) {
    return null;
  }
  try {
    return JSON.parse(decodeURIComponent(rawPayload));
  } catch (error) {
    console.error("Wujin candidate payload parse failed", error);
    return null;
  }
}

function buildFallbackCapability() {
  const candidate = candidatePayload.value || {};
  const name = candidate.supplierName || keyword.value || "五金供应商";
  const products = candidate.mainProducts || `${keyword.value || "五金件"}、材料、加工服务`;
  return {
    supplierId: supplierId.value || candidate.id,
    supplierName: name,
    matchScore: candidate.matchScore || 86,
    serviceNote:
      candidate.serviceNote ||
      "支持按搜索上下文进行材料、加工或成品供应能力联调。",
    mainCapabilities: products
      .split(/[、,，]/)
      .map((item) => item.trim())
      .filter(Boolean),
    lanes: [
      { label: "成品", value: "PRODUCT", note: "可按规格提供商品报价" },
      { label: "加工", value: "PROCESS", note: "可承接打样、小批量和批量加工" },
      { label: "原材料", value: "MATERIAL", note: "可提供材料库存、产地和替代建议" },
    ].filter((item) => !currentLane.value || item.value === currentLane.value || candidate.id),
  };
}

function mergeCapability(fallback, result) {
  const merged = { ...fallback };
  Object.entries(result || {}).forEach(([key, value]) => {
    const empty =
      value === undefined ||
      value === null ||
      value === "" ||
      (Array.isArray(value) && value.length === 0) ||
      (key === "matchScore" && !value);
    if (!empty) {
      merged[key] = value;
    }
  });
  merged.capabilities = result?.capabilities || [];
  merged.approvedTags = result?.approvedTags || [];
  return merged;
}

async function loadCapability() {
  try {
    const result = await getWujinSupplierCapability({
      supplierId: supplierId.value,
      supplierType: candidatePayload.value?.supplierType,
      keyword: keyword.value,
      lane: currentLane.value,
      sourceKeyword: sourceKeyword.value,
    });
    capability.value = mergeCapability(buildFallbackCapability(), result);
  } catch (error) {
    console.error("Wujin supplier capability failed", error);
    capability.value = buildFallbackCapability();
  }
}

function handleChatOpen() {
  const candidate = candidatePayload.value || {};
  const channelId =
    candidate.imUserId ||
    candidate.userId ||
    candidate.memberUserId ||
    capability.value.imUserId ||
    capability.value.userId ||
    capability.value.memberUserId ||
    capability.value.supplierId;
  if (!channelId) {
    showToast("供应商暂未开通沟通");
    return;
  }
  uni.navigateTo({
    url: `/pages/messages/chat?channelId=${encodeURIComponent(channelId)}&channelType=1&title=${encodeURIComponent(capability.value.supplierName || "供应商沟通")}`,
  });
}

function handleSourcingOpen() {
  uni.navigateTo({
    url: `/pages/wujin/sourcing?keyword=${encodeURIComponent(keyword.value || capability.value.supplierName)}&lane=${encodeURIComponent(currentLane.value || "MATERIAL")}&sourceKeyword=${encodeURIComponent(sourceKeyword.value)}`,
  });
}

onLoad((options = {}) => {
  supplierId.value = options.supplierId || options.id || "";
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  currentLane.value = options.lane || "";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  candidatePayload.value = parseCandidatePayload(options.candidate);
  capability.value = buildFallbackCapability();
  loadCapability();
});
</script>

<style lang="scss" scoped>
.capability-hero {
  padding-bottom: 54rpx;
}

.hero-keyword {
  font-size: 24rpx;
  font-weight: 900;
}

.score-panel {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 24rpx;
  padding: 14rpx 20rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.64);
}

.score-value {
  font-size: 46rpx;
  font-weight: 900;
}

.score-label {
  color: rgba(17, 17, 17, 0.68);
  font-size: 24rpx;
  font-weight: 800;
}

.capability-content {
  margin-top: -28rpx;
  padding-bottom: 48rpx;
}

.top-actions {
  position: relative;
  z-index: 2;
}

.lane-list {
  display: grid;
  gap: 14rpx;
}

.lane-row {
  display: grid;
  gap: 6rpx;
  padding: 18rpx;
  border-left: 8rpx solid #ffd21e;
  border-radius: 16rpx;
  background: #f7f8fa;
}

.lane-name {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}

.capability-list {
  display: grid;
  gap: 16rpx;
  margin-top: 12rpx;
}

.capability-row {
  display: grid;
  gap: 6rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #eceff3;
}

.capability-name {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}
</style>
