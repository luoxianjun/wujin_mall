<template>
  <view class="wujin-page wujin-page--plain progress-page">
    <view class="wujin-hero progress-hero">
      <WujinBackBar />
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">{{ listMode ? "我的寻源线索" : "线索进度" }}</text>
        <text v-if="!listMode" class="hero-lane">{{ laneName(currentLane) }}</text>
      </view>
      <text class="wujin-hero__title">
        {{ listMode ? "我的寻源线索" : progress.title }}
      </text>
      <text class="wujin-hero__subtitle">
        {{ listMode ? "查看已提交线索的分发、联系与报价进度" : progress.summary }}
      </text>
    </view>

    <view class="wujin-content progress-content">
      <view v-if="loadError" class="wujin-card load-hint">
        <text class="wujin-muted">{{ loadError }}</text>
        <button
          v-if="listMode && needLogin"
          class="wujin-button wujin-button--yellow hint-action"
          @tap="handleLogin"
        >
          去登录
        </button>
      </view>

      <template v-if="listMode">
        <view
          v-for="lead in myLeads"
          :key="lead.leadId"
          class="wujin-card lead-item"
          @tap="handleLeadOpen(lead)"
        >
          <view class="wujin-section-head">
            <text class="step-title">{{ lead.keyword }}</text>
            <text class="lead-status">{{ lead.leadStatusName }}</text>
          </view>
          <text class="wujin-text">{{ lead.summary }}</text>
          <view class="lead-meta">
            <text>{{ laneName(lead.lane) }}</text>
            <text v-if="lead.supplierName">{{ lead.supplierName }}</text>
            <text>{{ formatTime(lead.createTime) }}</text>
          </view>
        </view>
        <view v-if="!myLeads.length && !loadError" class="wujin-card load-hint">
          <text class="wujin-muted">还没有提交过寻源线索</text>
          <button
            class="wujin-button wujin-button--yellow hint-action"
            @tap="handleSourcingOpen"
          >
            去一键寻源
          </button>
        </view>
      </template>

      <template v-else>
        <view class="wujin-card lead-summary">
          <view>
            <text class="summary-label">线索编号</text>
            <text class="summary-value">{{ leadId || "待生成" }}</text>
          </view>
          <view>
            <text class="summary-label">当前状态</text>
            <text class="summary-value">{{ progress.leadStatusName || "已提交" }}</text>
          </view>
          <view>
            <text class="summary-label">关键词</text>
            <text class="summary-value">{{ keyword || sourceKeyword || "五金寻源" }}</text>
          </view>
          <view>
            <text class="summary-label">对接供应商</text>
            <text class="summary-value">{{ progress.supplierName || "平台匹配中" }}</text>
          </view>
        </view>

        <view class="timeline">
          <view
            v-for="step in progressSteps"
            :key="step.key"
            class="timeline-step"
            :class="{ active: step.active, done: step.done }"
          >
            <view class="step-rail">
              <view class="step-dot"></view>
            </view>
            <view class="wujin-card step-card">
              <view class="wujin-section-head">
                <text class="step-title">{{ step.title }}</text>
                <text class="step-time">{{ step.time }}</text>
              </view>
              <text class="wujin-text">{{ step.description }}</text>
            </view>
          </view>
        </view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import {
  getWujinMyLeads,
  getWujinSourcingLeadProgress,
} from "@/api/wujin/sourcing";
import WujinBackBar from "@/components/WujinBackBar.vue";
import { getAccessToken } from "@/utils/session";

defineOptions({ name: "WujinMiniLeadProgress" });

const leadId = ref("");
const keyword = ref("");
const currentLane = ref("MATERIAL");
const sourceKeyword = ref("");
const progress = ref(buildFallbackProgress());
const myLeads = ref([]);
const listMode = ref(false);
const loadError = ref("");
const needLogin = ref(false);

const progressSteps = computed(() => progress.value.steps || []);

function laneName(lane) {
  const map = {
    PRODUCT: "成品",
    PROCESS: "加工",
    MATERIAL: "原材料",
  };
  return map[lane] || lane || "-";
}

function formatTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(Number(value) || value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const pad = (number) => `${number}`.padStart(2, "0");
  return `${date.getMonth() + 1}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function buildFallbackProgress() {
  const title = keyword.value
    ? `${keyword.value} 寻源线索`
    : "寻源线索";
  return {
    leadId: leadId.value,
    title,
    summary: "展示提交、匹配、供应商响应和后续沟通进度。",
    steps: [
      {
        key: "SUBMITTED",
        title: "已提交",
        time: "刚刚",
        description: "需求信息已记录，等待平台或供应商处理。",
        done: true,
      },
      {
        key: "MATCHING",
        title: "供应商匹配",
        time: "待处理",
        description: `根据${laneName(currentLane.value)}泳道和来源关键词筛选候选供应商。`,
        active: true,
      },
      {
        key: "RESPONDED",
        title: "供应商响应",
        time: "待响应",
        description: "供应商确认能力、交期、报价或补充资质信息。",
      },
      {
        key: "FOLLOW_UP",
        title: "后续沟通",
        time: "待跟进",
        description: "进入样品、报价、合同或替代方案确认。",
      },
    ],
  };
}

async function loadProgress() {
  loadError.value = "";
  try {
    const result = await getWujinSourcingLeadProgress({
      leadId: leadId.value,
    });
    if (result && Object.keys(result).length) {
      keyword.value = result.keyword || keyword.value;
      currentLane.value = result.lane || currentLane.value;
      sourceKeyword.value = result.sourceKeyword || sourceKeyword.value;
      progress.value = { ...buildFallbackProgress(), ...result };
    } else {
      progress.value = buildFallbackProgress();
    }
  } catch (error) {
    console.error("Wujin lead progress failed", error);
    progress.value = buildFallbackProgress();
    loadError.value = error?.msg || "线索进度暂时无法获取，以下为通用流程";
  }
}

async function loadMyLeads() {
  loadError.value = "";
  needLogin.value = !getAccessToken();
  if (needLogin.value) {
    myLeads.value = [];
    loadError.value = "登录后可查看已提交的寻源线索";
    return;
  }
  try {
    myLeads.value = (await getWujinMyLeads()) || [];
  } catch (error) {
    console.error("Wujin my leads failed", error);
    myLeads.value = [];
    loadError.value = error?.msg || "我的寻源线索暂时无法获取";
  }
}

function handleLeadOpen(lead) {
  uni.navigateTo({
    url: `/pages/wujin/lead-progress?leadId=${encodeURIComponent(lead.leadId)}&keyword=${encodeURIComponent(lead.keyword || "")}&lane=${encodeURIComponent(lead.lane || "")}`,
  });
}

function handleLogin() {
  uni.navigateTo({ url: "/pages/users/login" });
}

function handleSourcingOpen() {
  uni.navigateTo({ url: "/pages/wujin/sourcing" });
}

onLoad((options = {}) => {
  leadId.value = options.leadId || "";
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  currentLane.value = options.lane || "MATERIAL";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  listMode.value = !leadId.value;
  if (listMode.value) {
    loadMyLeads();
    return;
  }
  progress.value = buildFallbackProgress();
  loadProgress();
});
</script>

<style lang="scss" scoped>
.progress-hero {
  padding-bottom: 54rpx;
}

.hero-lane {
  font-size: 24rpx;
  font-weight: 900;
}

.progress-content {
  margin-top: -28rpx;
  padding-bottom: 48rpx;
}

.lead-summary {
  position: relative;
  z-index: 2;
  grid-template-columns: repeat(2, 1fr);
  margin-bottom: 18rpx;
}

.summary-label {
  display: block;
  color: #5d6673;
  font-size: 22rpx;
}

.summary-value {
  display: block;
  margin-top: 8rpx;
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
}

.timeline {
  display: grid;
  gap: 18rpx;
}

.timeline-step {
  display: grid;
  grid-template-columns: 38rpx 1fr;
  gap: 16rpx;
}

.step-rail {
  display: flex;
  justify-content: center;
  padding-top: 28rpx;
}

.step-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #cbd1d9;
}

.timeline-step.done .step-dot {
  background: #2f8f46;
}

.timeline-step.active .step-dot {
  background: #ffd21e;
  box-shadow: 0 0 0 8rpx rgba(255, 210, 30, 0.26);
}

.timeline-step.active .step-card {
  border-color: #f5b800;
  background: #fffdf0;
}

.step-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.step-time {
  color: #5d6673;
  font-size: 23rpx;
}

.load-hint {
  display: grid;
  gap: 16rpx;
  margin-bottom: 18rpx;
}

.hint-action {
  justify-self: start;
}

.lead-item {
  display: grid;
  gap: 10rpx;
  margin-bottom: 18rpx;
}

.lead-status {
  color: #2f8f46;
  font-size: 24rpx;
  font-weight: 900;
}

.lead-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  color: #5d6673;
  font-size: 22rpx;
}
</style>
