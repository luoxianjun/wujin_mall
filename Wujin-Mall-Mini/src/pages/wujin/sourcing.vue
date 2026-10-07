<template>
  <view class="wujin-page sourcing-page">
    <view class="wujin-hero sourcing-hero">
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">一键寻源</text>
        <text class="hero-lane">{{ laneName(currentLane) }}</text>
      </view>
      <text class="wujin-hero__title">{{ searchKeyword || "五金材料" }}</text>
      <text class="wujin-hero__subtitle">
        基于三泳道搜索上下文，优先推荐可承接材料、工艺或成品供应的商家。
      </text>
    </view>

    <view class="wujin-content sourcing-content">
      <view class="context-strip">
        <view class="context-item">
          <text class="context-label">当前泳道</text>
          <text class="context-value">{{ laneName(currentLane) }}</text>
        </view>
        <view class="context-item">
          <text class="context-label">来源关键词</text>
          <text class="context-value">{{ sourceKeyword || "直接寻源" }}</text>
        </view>
      </view>

      <view class="wujin-card">
        <view class="wujin-section-head">
          <text class="wujin-section-title">供应商候选</text>
          <text class="wujin-section-note">{{ supplierCandidates.length }} 家</text>
        </view>

        <view class="supplier-list">
          <view
            v-for="item in supplierCandidates"
            :key="item.id || item.supplierName"
            class="supplier-card"
            :class="{ active: leadForm.supplierId === item.id }"
            @tap="handleSupplierSelect(item)"
          >
            <view class="supplier-head">
              <view>
                <text class="supplier-name">{{ item.supplierName }}</text>
                <text class="supplier-tags">{{ item.mainProducts }}</text>
              </view>
              <view class="supplier-score">
                <text>{{ item.matchScore || 0 }}%</text>
                <text>匹配</text>
              </view>
            </view>
            <text class="wujin-muted">{{ item.serviceNote }}</text>
            <view class="supplier-actions">
              <button class="wujin-button wujin-button--yellow" @tap.stop="handleSupplierCapabilityOpen(item)">
                供应能力详情
              </button>
              <button class="wujin-button wujin-button--line" @tap.stop="handleSupplierChatOpen(item)">
                发起沟通
              </button>
            </view>
          </view>
        </view>
      </view>

      <view class="wujin-card lead-card">
        <view class="wujin-section-head">
          <text class="wujin-section-title">提交寻源线索</text>
          <text class="wujin-section-note">规格 / 数量 / 交期</text>
        </view>
        <view class="lead-form">
          <input
            v-model="leadForm.contactName"
            class="wujin-input"
            placeholder="联系人"
            type="text"
          />
          <input
            v-model="leadForm.contactPhone"
            class="wujin-input"
            placeholder="联系方式"
            type="text"
          />
          <textarea
            v-model="leadForm.requirement"
            class="wujin-textarea"
            maxlength="300"
            placeholder="需求说明：规格、数量、交期、认证要求等"
          />
          <button class="wujin-button submit-button" @tap="handleLeadSubmit">
            提交寻源线索
          </button>
          <button
            v-if="submittedLead"
            class="wujin-button wujin-button--yellow"
            @tap="handleLeadProgressOpen"
          >
            查看线索进度
          </button>
        </view>
      </view>
    </view>

    <BottomNavBar active="sourcing" />
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import BottomNavBar from "@/components/BottomNavBar.vue";
import {
  getWujinSupplierCandidates,
  submitWujinSourcingLead,
} from "@/api/wujin/sourcing";
import { showToast } from "@/utils/toast";

defineOptions({ name: "WujinMiniSourcing" });

const laneOptions = [
  { label: "成品", value: "PRODUCT" },
  { label: "加工", value: "PROCESS" },
  { label: "原材料", value: "MATERIAL" },
];

const searchKeyword = ref("");
const currentLane = ref("MATERIAL");
const sourceKeyword = ref("");
const supplierCandidates = ref([]);
const submittedLead = ref(null);

const leadForm = reactive({
  contactName: "",
  contactPhone: "",
  requirement: "",
  supplierId: undefined,
  supplierName: "",
});

function laneName(lane) {
  return laneOptions.find((item) => item.value === lane)?.label || lane || "-";
}

function buildFallbackSuppliers() {
  const keyword = searchKeyword.value || "五金材料";
  return [
    {
      id: 101,
      supplierName: "华北五金材料供应中心",
      matchScore: 96,
      mainProducts: `${keyword}、标准件、工业辅材`,
      serviceNote: "支持样品确认、批量报价和区域配送。",
    },
    {
      id: 102,
      supplierName: "长三角精密加工协作厂",
      matchScore: 88,
      mainProducts: "电镀、热处理、冲压加工",
      serviceNote: "适合需要工艺承接和小批量打样的寻源需求。",
    },
  ];
}

async function loadSupplierCandidates() {
  if (!searchKeyword.value.trim()) {
    supplierCandidates.value = buildFallbackSuppliers();
    return;
  }
  try {
    const result = await getWujinSupplierCandidates({
      keyword: searchKeyword.value,
      lane: currentLane.value,
      sourceKeyword: sourceKeyword.value,
    });
    supplierCandidates.value = Array.isArray(result) && result.length
      ? result
      : buildFallbackSuppliers();
  } catch (error) {
    console.error("Wujin supplier candidates failed", error);
    supplierCandidates.value = buildFallbackSuppliers();
  }
}

function handleSupplierSelect(item) {
  leadForm.supplierId = item.id;
  leadForm.supplierName = item.supplierName;
}

function buildSupplierCapabilityUrl(item = {}) {
  const query = {
    supplierId: item.id || leadForm.supplierId,
    keyword: searchKeyword.value,
    lane: currentLane.value,
    sourceKeyword: sourceKeyword.value,
    candidate: JSON.stringify(item),
  };
  const search = Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
  return `/pages/wujin/supplier-capability?${search}`;
}

function handleSupplierCapabilityOpen(item = {}) {
  uni.navigateTo({
    url: buildSupplierCapabilityUrl(item),
  });
}

function handleSupplierChatOpen(item = {}) {
  const channelId = item.imUserId || item.userId || item.memberUserId || item.id;
  if (!channelId) {
    showToast("供应商暂未开通沟通");
    return;
  }
  uni.navigateTo({
    url: `/pages/messages/chat?channelId=${encodeURIComponent(channelId)}&channelType=1&title=${encodeURIComponent(item.supplierName || "供应商沟通")}`,
  });
}

async function handleLeadSubmit() {
  if (!leadForm.contactPhone.trim()) {
    showToast("请填写联系方式");
    return;
  }
  if (!leadForm.requirement.trim()) {
    showToast("请填写需求说明");
    return;
  }
  const result = await submitWujinSourcingLead({
    keyword: searchKeyword.value,
    lane: currentLane.value,
    sourceKeyword: sourceKeyword.value,
    ...leadForm,
  });
  submittedLead.value = result || {
    leadId: `${Date.now()}`,
    keyword: searchKeyword.value,
    lane: currentLane.value,
  };
  showToast("寻源线索已提交");
}

function handleLeadProgressOpen() {
  const leadId =
    submittedLead.value?.leadId || submittedLead.value?.id || "";
  uni.navigateTo({
    url: `/pages/wujin/lead-progress?leadId=${encodeURIComponent(leadId)}&keyword=${encodeURIComponent(searchKeyword.value)}&lane=${encodeURIComponent(currentLane.value)}&sourceKeyword=${encodeURIComponent(sourceKeyword.value)}`,
  });
}

onLoad((options = {}) => {
  searchKeyword.value = options.keyword
    ? decodeURIComponent(options.keyword)
    : "五金材料";
  currentLane.value = options.lane || "MATERIAL";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  leadForm.requirement = searchKeyword.value
    ? `我想寻找 ${searchKeyword.value} 相关供应商，请联系报价。`
    : "";
  loadSupplierCandidates();
});
</script>

<style lang="scss" scoped>
.sourcing-hero {
  padding-bottom: 54rpx;
}

.hero-lane {
  font-size: 24rpx;
  font-weight: 900;
}

.sourcing-content {
  margin-top: -28rpx;
}

.context-strip {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
}

.context-item {
  display: grid;
  gap: 8rpx;
  padding: 20rpx;
  border: 1rpx solid #eceff3;
  border-radius: 18rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(17, 17, 17, 0.06);
}

.context-label {
  color: #5d6673;
  font-size: 22rpx;
}

.context-value {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}

.supplier-list,
.lead-form {
  display: grid;
  gap: 16rpx;
}

.supplier-card {
  display: grid;
  gap: 14rpx;
  padding: 22rpx;
  border: 2rpx solid #eceff3;
  border-radius: 18rpx;
  background: #ffffff;
}

.supplier-card.active {
  border-color: #111111;
  background: #fffdf0;
}

.supplier-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.supplier-name {
  display: block;
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.supplier-tags {
  display: block;
  margin-top: 8rpx;
  color: #2b2f36;
  font-size: 25rpx;
  line-height: 1.45;
}

.supplier-score {
  display: grid;
  flex-shrink: 0;
  gap: 2rpx;
  min-width: 88rpx;
  padding: 10rpx 12rpx;
  border-radius: 16rpx;
  background: #ffd21e;
  color: #111111;
  text-align: center;
  font-size: 20rpx;
  font-weight: 800;
}

.supplier-score text:first-child {
  font-size: 28rpx;
  font-weight: 900;
}

.supplier-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.submit-button {
  margin-top: 4rpx;
}
</style>
