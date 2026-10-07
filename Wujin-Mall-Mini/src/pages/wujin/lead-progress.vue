<template>
  <view class="wujin-page wujin-page--plain progress-page">
    <view class="wujin-hero progress-hero">
      <WujinBackBar />
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">线索进度</text>
        <text class="hero-lane">{{ laneName(currentLane) }}</text>
      </view>
      <text class="wujin-hero__title">{{ progress.title }}</text>
      <text class="wujin-hero__subtitle">{{ progress.summary }}</text>
    </view>

    <view class="wujin-content progress-content">
      <view class="wujin-card lead-summary">
        <view>
          <text class="summary-label">线索编号</text>
          <text class="summary-value">{{ leadId || "待生成" }}</text>
        </view>
        <view>
          <text class="summary-label">关键词</text>
          <text class="summary-value">{{ keyword || sourceKeyword || "五金寻源" }}</text>
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
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import { getWujinSourcingLeadProgress } from "@/api/wujin/sourcing";
import WujinBackBar from "@/components/WujinBackBar.vue";

defineOptions({ name: "WujinMiniLeadProgress" });

const leadId = ref("");
const keyword = ref("");
const currentLane = ref("MATERIAL");
const sourceKeyword = ref("");
const progress = ref(buildFallbackProgress());

const progressSteps = computed(() => progress.value.steps || []);

function laneName(lane) {
  const map = {
    PRODUCT: "成品",
    PROCESS: "加工",
    MATERIAL: "原材料",
  };
  return map[lane] || lane || "-";
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
  try {
    const result = await getWujinSourcingLeadProgress({
      leadId: leadId.value,
      keyword: keyword.value,
      lane: currentLane.value,
      sourceKeyword: sourceKeyword.value,
    });
    progress.value = result && Object.keys(result).length
      ? {
          ...buildFallbackProgress(),
          ...result,
        }
      : buildFallbackProgress();
  } catch (error) {
    console.error("Wujin lead progress failed", error);
    progress.value = buildFallbackProgress();
  }
}

onLoad((options = {}) => {
  leadId.value = options.leadId || "";
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  currentLane.value = options.lane || "MATERIAL";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
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
</style>
