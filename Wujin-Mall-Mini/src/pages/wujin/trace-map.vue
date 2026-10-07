<template>
  <view class="wujin-page trace-page">
    <view class="wujin-hero trace-hero">
      <WujinBackBar />
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">溯源图谱</text>
        <text class="hero-lane">{{ laneName(currentLane) }}</text>
      </view>
      <text class="wujin-hero__title">{{ graph.title }}</text>
      <text class="wujin-hero__subtitle">{{ graph.summary }}</text>
      <view class="trace-hero-actions">
        <button class="trace-hero-action trace-hero-action--dark" @tap="handleFullscreenOpen">
          全屏查看
        </button>
        <button class="trace-hero-action trace-hero-action--light" @tap="handleSourcingOpen">
          一键寻源
        </button>
      </view>
    </view>

    <view class="wujin-content trace-content">
      <HorizontalTraceGraph
        :stages="laneStages"
        :active-stage="currentLane"
        :nodes="traceNodes"
        :edges="traceEdges"
        :output="traceOutput"
        :current-batch="currentBatch"
        :loading="loading"
        @selectStage="handleLaneSwitch"
        @selectNode="handleNodeSelect"
        @report="handleReportOpen"
        @export="handleReportExport"
      />

      <view class="wujin-card">
        <view class="wujin-section-head">
          <text class="wujin-section-title">构成关系</text>
          <button class="wujin-button wujin-button--yellow source-btn" @tap="handleSourcingOpen">
            一键寻源
          </button>
        </view>
        <view class="edge-list">
          <view
            v-for="edge in traceEdges"
            :key="`${edge.from}-${edge.to}`"
            class="edge-row"
          >
            <text class="edge-name">{{ edge.fromName }} → {{ edge.toName }}</text>
            <text class="wujin-muted">{{ edge.relation }}</text>
          </view>
        </view>
      </view>
    </view>

    <BottomNavBar active="trace" />
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import BottomNavBar from "@/components/BottomNavBar.vue";
import HorizontalTraceGraph from "@/components/wujin/HorizontalTraceGraph.vue";
import WujinBackBar from "@/components/WujinBackBar.vue";
import { getWujinTraceGraph } from "@/api/wujin/detail";

defineOptions({ name: "WujinMiniTraceMap" });

const keyword = ref("");
const currentLane = ref("PRODUCT");
const sourceKeyword = ref("");
const loading = ref(false);
const graph = ref(buildFallbackTraceGraph());
const laneStages = [
  { label: "成品", value: "PRODUCT" },
  { label: "加工", value: "PROCESS" },
  { label: "原材料", value: "MATERIAL" },
];

const traceNodes = computed(() => graph.value.nodes || []);
const traceEdges = computed(() => graph.value.edges || []);
const traceOutput = computed(() => ({
  id: graph.value.outputId,
  lane: graph.value.outputLane || currentLane.value,
  name: graph.value.outputName || graph.value.title || "五金成品",
  meta: graph.value.outputMeta || `批次：${currentBatch.value.batchNo || "-"}`,
}));
const currentBatch = computed(() => graph.value.currentBatch || {
  batchNo: "P24051501",
  productionDate: "2024-05-15",
  factory: "江苏紧固件有限公司",
  qualityStatus: "合格",
});

function laneName(lane) {
  const map = {
    PRODUCT: "成品",
    PROCESS: "加工",
    MATERIAL: "原材料",
  };
  return map[lane] || lane || "-";
}

function buildFallbackTraceGraph() {
  const name = keyword.value || sourceKeyword.value || "深沟球轴承";
  return {
    title: `${name} BOM 溯源`,
    summary: "当前成品为起点，向右展开部件、原材料和更上游原料的构成树。",
    outputName: name,
    outputMeta: "当前成品树根节点",
    nodes: [
      {
        id: "product",
        lane: "PRODUCT",
        name,
        description: "当前成品，所有右侧节点都是它的组成来源。",
      },
      {
        id: "inner-ring",
        lane: "PART",
        parentId: "product",
        name: "内圈",
        description: "轴承承载部件，由轴承钢车削热处理形成。",
      },
      {
        id: "outer-ring",
        lane: "PART",
        parentId: "product",
        name: "外圈",
        description: "外侧承载部件，决定轴承安装和受力稳定性。",
      },
      {
        id: "steel-ball",
        lane: "PART",
        parentId: "product",
        name: "钢球",
        description: "滚动体部件，影响低噪音和旋转精度。",
      },
      {
        id: "retainer",
        lane: "PART",
        parentId: "product",
        name: "保持架",
        description: "保持滚动体间距，常用钢板或增强尼龙。",
      },
      {
        id: "steel-bead-material",
        lane: "MATERIAL",
        parentId: "steel-ball",
        name: "高碳铬轴承钢",
        description: "钢球常用原料，强调硬度、洁净度和疲劳寿命。",
      },
      {
        id: "gcr15",
        lane: "MATERIAL",
        parentId: "inner-ring",
        name: "轴承钢GCr15",
        description: "内外圈与钢球的主要原材料，高碳铬轴承钢。",
      },
      {
        id: "ring-forging",
        lane: "PROCESS",
        parentId: "gcr15",
        name: "精密锻造 / 退火",
        description: "更上游的成形与热处理来源，展示前序加工链条。",
      },
      {
        id: "steel-mill",
        lane: "MATERIAL",
        parentId: "ring-forging",
        name: "特钢厂炉批",
        description: "对应炉批号、洁净度和材质证明文件。",
      },
      {
        id: "nylon",
        lane: "MATERIAL",
        parentId: "retainer",
        name: "增强尼龙/钢板",
        description: "保持架可选原料，按转速、温度和成本选择。",
      },
    ],
    edges: [
      {
        from: "product",
        fromName: name,
        to: "inner-ring",
        toName: "内圈",
        relation: "直接构成",
      },
      {
        from: "product",
        fromName: name,
        to: "outer-ring",
        toName: "外圈",
        relation: "直接构成",
      },
      {
        from: "product",
        fromName: name,
        to: "steel-ball",
        toName: "钢球",
        relation: "直接构成",
      },
      {
        from: "product",
        fromName: name,
        to: "retainer",
        toName: "保持架",
        relation: "直接构成",
      },
      {
        from: "inner-ring",
        fromName: "内圈",
        to: "gcr15",
        toName: "轴承钢GCr15",
        relation: "由该原材料加工形成",
      },
      {
        from: "outer-ring",
        fromName: "外圈",
        to: "gcr15",
        toName: "轴承钢GCr15",
        relation: "由该原材料加工形成",
      },
      {
        from: "steel-ball",
        fromName: "钢球",
        to: "steel-bead-material",
        toName: "高碳铬轴承钢",
        relation: "由该原材料加工形成",
      },
      {
        from: "retainer",
        fromName: "保持架",
        to: "nylon",
        toName: "增强尼龙/钢板",
        relation: "由该原材料加工形成",
      },
      {
        from: "gcr15",
        fromName: "轴承钢GCr15",
        to: "ring-forging",
        toName: "精密锻造 / 退火",
        relation: "更上游加工环节",
      },
      {
        from: "ring-forging",
        fromName: "精密锻造 / 退火",
        to: "steel-mill",
        toName: "特钢厂炉批",
        relation: "更早一级来源数据",
      },
    ],
  };
}

async function loadTraceGraph() {
  loading.value = true;
  try {
    const result = await getWujinTraceGraph({
      keyword: keyword.value,
      lane: currentLane.value,
      sourceKeyword: sourceKeyword.value,
    });
    graph.value = result && Object.keys(result).length
      ? {
          ...buildFallbackTraceGraph(),
          ...result,
        }
      : buildFallbackTraceGraph();
  } catch (error) {
    console.error("Wujin trace graph failed", error);
    graph.value = buildFallbackTraceGraph();
  } finally {
    loading.value = false;
  }
}

function buildTraceQuery() {
  const target = keyword.value || sourceKeyword.value || graph.value.outputName || "";
  const query = {
    keyword: target,
    lane: currentLane.value,
    sourceKeyword: sourceKeyword.value || keyword.value || target,
  };
  return Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
}

function handleFullscreenOpen() {
  uni.navigateTo({
    url: `/pages/wujin/trace-fullscreen?${buildTraceQuery()}`,
  });
}

function handleSourcingOpen() {
  const target = keyword.value || sourceKeyword.value || "";
  uni.navigateTo({
    url: `/pages/wujin/sourcing?keyword=${encodeURIComponent(target)}&lane=MATERIAL&sourceKeyword=${encodeURIComponent(sourceKeyword.value || keyword.value)}`,
  });
}

function handleLaneSwitch(lane) {
  if (lane === currentLane.value) return;
  currentLane.value = lane;
  loadTraceGraph();
}

function handleNodeSelect(node = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?keyword=${encodeURIComponent(node.name || keyword.value)}&lane=${encodeURIComponent(node.lane || currentLane.value)}&sourceKeyword=${encodeURIComponent(sourceKeyword.value || keyword.value)}`,
  });
}

function handleReportOpen() {
  uni.showToast({ title: "检测报告待后端接入", icon: "none" });
}

function handleReportExport() {
  uni.showToast({ title: "导出能力待后端接入", icon: "none" });
}

onLoad((options = {}) => {
  keyword.value = options.keyword ? decodeURIComponent(options.keyword) : "";
  currentLane.value = options.lane || "PRODUCT";
  sourceKeyword.value = options.sourceKeyword
    ? decodeURIComponent(options.sourceKeyword)
    : "";
  graph.value = buildFallbackTraceGraph();
  loadTraceGraph();
});
</script>

<style lang="scss" scoped>
.trace-hero {
  padding-bottom: 34rpx;
}

.hero-lane {
  font-size: 24rpx;
  font-weight: 900;
}

.trace-content {
  margin-top: -18rpx;
  padding-bottom: calc(178rpx + env(safe-area-inset-bottom));
}

.trace-content .batch-card {
  margin-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.trace-hero-actions {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-top: 24rpx;
}

.trace-hero-action {
  height: 72rpx;
  margin: 0;
  border: none;
  border-radius: 16rpx;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 72rpx;
}

.trace-hero-action::after {
  border: none;
}

.trace-hero-action--dark {
  background: #111111;
  color: #ffd21e;
}

.trace-hero-action--light {
  background: rgba(255, 255, 255, 0.9);
  color: #111111;
}

.edge-name {
  color: #111111;
  font-size: 30rpx;
  line-height: 1.35;
  font-weight: 900;
}

.source-btn {
  width: 166rpx;
  flex-shrink: 0;
}

.edge-list {
  display: grid;
  gap: 14rpx;
}

.edge-row {
  display: grid;
  gap: 8rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: #f7f8fa;
}
</style>
