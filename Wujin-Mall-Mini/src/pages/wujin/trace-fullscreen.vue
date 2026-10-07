<template>
  <view class="trace-fullscreen-page">
    <view class="fullscreen-header">
      <button class="header-back" @tap="handleBack">
        <AppIcon name="arrow_back" variant="dark" size="38rpx" />
      </button>
      <view class="fullscreen-title-block">
        <text class="fullscreen-kicker">BOM 横向树</text>
        <text class="fullscreen-title">{{ graph.title }}</text>
      </view>
      <view class="fullscreen-count">{{ branchLevels.length }}层</view>
    </view>

    <scroll-view
      scroll-x
      scroll-with-animation
      :bounces="false"
      class="fullscreen-scroll"
      :scroll-left="fullscreenScrollLeft"
      :scroll-into-view="fullscreenScrollIntoView"
      @scroll="handleFullscreenScroll"
    >
      <view class="trace-tree" :style="{ width: `${fullscreenTreeWidth}rpx` }">
        <view id="fullscreen-tree-start" class="tree-level tree-root-level">
          <view class="tree-root-card" @tap.stop="handleNodeSelect(rootNode.raw)">
            <text class="tree-node-label">当前成品</text>
            <text class="tree-root-title">{{ rootNode.name }}</text>
            <text class="tree-node-meta">{{ rootNode.meta }}</text>
          </view>
        </view>

        <template v-for="(level, levelIndex) in fullscreenBranchLevels" :key="`fullscreen-level-${levelIndex}`">
          <view class="tree-connector">
            <view class="tree-connector-line"></view>
            <text class="tree-connector-arrow">→</text>
          </view>

          <view :id="`fullscreen-tree-level-${levelIndex + 1}`" class="tree-level tree-branch-level">
            <view class="tree-level-head">
              <text class="tree-level-title">{{ levelTitle(levelIndex) }}</text>
              <text class="tree-level-count">{{ level.length }}项</text>
            </view>
            <view class="tree-node-stack">
              <view
                v-for="node in level"
                :key="node.key"
                class="tree-branch-card"
                :class="`tree-node-${node.lane}`"
                @tap.stop="handleNodeSelect(node.raw)"
              >
                <view class="branch-card-head">
                  <text class="branch-node-title">{{ node.name }}</text>
                  <text class="branch-node-lane">{{ laneName(node.lane) }}</text>
                </view>
                <text class="branch-relation">{{ node.relation || "直接构成" }}</text>
                <text class="tree-node-meta">{{ node.meta }}</text>
              </view>
            </view>
          </view>
        </template>

        <view v-if="!fullscreenBranchLevels.length" class="tree-empty-card">
          <text>{{ loading ? "加载溯源图谱..." : "等待补充组成材料数据" }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="fullscreen-controls">
      <view class="fullscreen-slider-head">
        <text>横向拖动展示更多树信息</text>
        <text>{{ fullscreenSliderValue }}%</text>
      </view>
      <slider
        :value="fullscreenSliderValue"
        min="0"
        max="100"
        block-size="22"
        activeColor="#ffd21e"
        backgroundColor="#d8dde5"
        @changing="handleFullscreenSliderChanging"
        @change="handleFullscreenSliderChange"
      />
      <button
        class="load-more-btn"
        :disabled="!hasMoreFullscreenLevels"
        @tap="loadMoreTreeInfo"
      >
        {{ hasMoreFullscreenLevels ? "加载更多树信息" : "已展示全部树信息" }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

import { getWujinTraceGraph } from "@/api/wujin/detail";

defineOptions({ name: "WujinMiniTraceFullscreen" });

const FULLSCREEN_INITIAL_LEVEL_COUNT = 2;
const FULLSCREEN_ROOT_WIDTH = 292;
const FULLSCREEN_BRANCH_WIDTH = 316;
const FULLSCREEN_CONNECTOR_WIDTH = 78;
const FULLSCREEN_TREE_HORIZONTAL_PADDING = 84;
const FULLSCREEN_VIEWPORT_WIDTH = 750;
const FULLSCREEN_EMPTY_CARD_WIDTH = 320;

const keyword = ref("");
const currentLane = ref("PRODUCT");
const sourceKeyword = ref("");
const entityId = ref("");
const entityType = ref("");
const loading = ref(false);
const fullscreenScrollLeft = ref(0);
const fullscreenSliderValue = ref(0);
const fullscreenScrollIntoView = ref("fullscreen-tree-start");
const fullscreenSliderScrollLock = ref(false);
const fullscreenLevelCount = ref(FULLSCREEN_INITIAL_LEVEL_COUNT);
const graph = ref(buildFallbackTraceGraph());
let fullscreenScrollUnlockTimer = null;

const traceNodes = computed(() => graph.value.nodes || []);
const traceEdges = computed(() => graph.value.edges || []);
const displayOutput = computed(() => ({
  id: graph.value.outputId || "",
  name: graph.value.outputName || graph.value.title || "五金成品",
  meta: graph.value.outputMeta || "当前成品树根节点",
}));
const normalizedNodes = computed(() => traceNodes.value.map(normalizeNode));
const nodeMap = computed(() =>
  normalizedNodes.value.reduce((result, node) => {
    result[node.id] = node;
    return result;
  }, {}),
);
const normalizedEdges = computed(() =>
  traceEdges.value
    .map((edge) => normalizeEdge(edge, nodeMap.value))
    .filter((edge) => edge.from && edge.to && edge.from !== edge.to),
);
const incomingIds = computed(() =>
  normalizedEdges.value.reduce((result, edge) => {
    result.add(edge.to);
    return result;
  }, new Set()),
);
const rootNode = computed(() => {
  const outputId = displayOutput.value.id ? String(displayOutput.value.id) : "";
  const outputName = displayOutput.value.name || "";
  return (
    (outputId && nodeMap.value[outputId]) ||
    normalizedNodes.value.find((node) => node.lane === "PRODUCT" && node.name === outputName) ||
    normalizedNodes.value.find((node) => node.lane === "PRODUCT") ||
    normalizedNodes.value.find((node) => !incomingIds.value.has(node.id)) ||
    normalizedNodes.value[0] ||
    normalizeNode({ id: "product", lane: "PRODUCT", name: "五金成品" }, 0)
  );
});
const treeLevels = computed(() => {
  const root = rootNode.value;
  if (!root) {
    return [];
  }
  const levels = [[root]];
  const visited = new Set([root.id]);
  const edgeMap = buildEdgeMap();
  let currentLevel = [root];

  for (let depth = 0; depth < 8; depth += 1) {
    const nextLevel = [];
    currentLevel.forEach((parent) => {
      const childEdges = edgeMap[parent.id] || [];
      childEdges.forEach((edge) => {
        const child = nodeMap.value[edge.to];
        if (!child || visited.has(child.id)) {
          return;
        }
        visited.add(child.id);
        nextLevel.push({
          ...child,
          key: `${child.key}-${parent.id}`,
          parentId: parent.id,
          parentName: parent.name,
          relation: edge.relation || child.relation || "直接构成",
        });
      });
    });
    if (!nextLevel.length) {
      break;
    }
    levels.push(nextLevel);
    currentLevel = nextLevel;
  }

  return levels;
});
const branchLevels = computed(() => treeLevels.value.slice(1));
const fullscreenBranchLevels = computed(() => branchLevels.value.slice(0, fullscreenLevelCount.value));
const hasMoreFullscreenLevels = computed(() => fullscreenLevelCount.value < branchLevels.value.length);
const fullscreenTreeWidth = computed(() => {
  const branchWidth = fullscreenBranchLevels.value.length
    ? fullscreenBranchLevels.value.length * (FULLSCREEN_CONNECTOR_WIDTH + FULLSCREEN_BRANCH_WIDTH)
    : FULLSCREEN_CONNECTOR_WIDTH + FULLSCREEN_EMPTY_CARD_WIDTH;
  return FULLSCREEN_TREE_HORIZONTAL_PADDING + FULLSCREEN_ROOT_WIDTH + branchWidth;
});
const fullscreenScrollMax = computed(() =>
  rpxToPx(Math.max(0, fullscreenTreeWidth.value - FULLSCREEN_VIEWPORT_WIDTH)),
);

function handleBack() {
  const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
  if (pages.length > 1) {
    uni.navigateBack();
    return;
  }
  uni.reLaunch({ url: "/pages/wujin/trace-map" });
}

function handleFullscreenScroll(event = {}) {
  if (fullscreenSliderScrollLock.value) {
    return;
  }
  const scrollLeft = clampFullscreenScrollLeft(event.detail?.scrollLeft);
  fullscreenScrollLeft.value = scrollLeft;
  fullscreenSliderValue.value = fullscreenScrollPercentFor(scrollLeft);
}

function handleFullscreenSliderChanging(event = {}) {
  syncFullscreenScroll(event.detail?.value);
}

function handleFullscreenSliderChange(event = {}) {
  syncFullscreenScroll(event.detail?.value);
}

function syncFullscreenScroll(value = 0) {
  const sliderValue = fullscreenScrollMax.value > 0 ? Math.max(0, Math.min(100, Number(value) || 0)) : 0;
  const nextScrollLeft = clampFullscreenScrollLeft(Math.round((sliderValue / 100) * fullscreenScrollMax.value));
  fullscreenSliderScrollLock.value = true;
  fullscreenSliderValue.value = sliderValue;
  fullscreenScrollIntoView.value = fullscreenScrollAnchorFor(sliderValue);
  fullscreenScrollLeft.value = nextScrollLeft;
  scheduleFullscreenScrollUnlock();
}

function clampFullscreenScrollLeft(value = 0) {
  return Math.max(0, Math.min(fullscreenScrollMax.value, Number(value) || 0));
}

function fullscreenScrollPercentFor(value = 0) {
  if (fullscreenScrollMax.value <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round((clampFullscreenScrollLeft(value) / fullscreenScrollMax.value) * 100)));
}

function scheduleFullscreenScrollUnlock() {
  if (fullscreenScrollUnlockTimer) {
    clearTimeout(fullscreenScrollUnlockTimer);
  }
  fullscreenScrollUnlockTimer = setTimeout(() => {
    fullscreenSliderScrollLock.value = false;
    fullscreenScrollIntoView.value = "";
    fullscreenScrollUnlockTimer = null;
  }, 260);
}

function fullscreenScrollAnchorFor(value = 0) {
  if (value <= 3 || !fullscreenBranchLevels.value.length) {
    return "fullscreen-tree-start";
  }
  const maxAnchorIndex = Math.max(1, fullscreenBranchLevels.value.length);
  const anchorIndex = Math.max(1, Math.min(maxAnchorIndex, Math.round((value / 100) * maxAnchorIndex)));
  return `fullscreen-tree-level-${anchorIndex}`;
}

function loadMoreTreeInfo() {
  if (!hasMoreFullscreenLevels.value) {
    return;
  }
  fullscreenLevelCount.value += 1;
  setTimeout(() => {
    const expandedPercent = Math.round((fullscreenLevelCount.value / Math.max(branchLevels.value.length, 1)) * 100);
    const targetSliderValue = Math.max(fullscreenSliderValue.value, Math.min(100, expandedPercent));
    syncFullscreenScroll(targetSliderValue);
  }, 0);
}

function resetFullscreenScroll() {
  fullscreenScrollLeft.value = 0;
  fullscreenSliderValue.value = 0;
  fullscreenScrollIntoView.value = "fullscreen-tree-start";
  fullscreenSliderScrollLock.value = false;
}

function handleNodeSelect(node = {}) {
  uni.navigateTo({
    url: `/pages/wujin/detail?keyword=${encodeURIComponent(node.name || keyword.value)}&lane=${encodeURIComponent(node.lane || currentLane.value)}&sourceKeyword=${encodeURIComponent(sourceKeyword.value || keyword.value)}`,
  });
}

async function loadTraceGraph() {
  loading.value = true;
  try {
    const result = await getWujinTraceGraph({
      id: entityId.value,
      entityType: entityType.value,
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
    resetFullscreenScroll();
  } catch (error) {
    console.error("Wujin trace fullscreen failed", error);
    graph.value = buildFallbackTraceGraph();
    resetFullscreenScroll();
  } finally {
    loading.value = false;
  }
}

function rpxToPx(value = 0) {
  try {
    const { windowWidth } = uni.getSystemInfoSync();
    return Math.round((Number(value) * Number(windowWidth || 375)) / 750);
  } catch (error) {
    return Math.round(Number(value) / 2);
  }
}

function buildFallbackTraceGraph() {
  const name = keyword.value || sourceKeyword.value || "深沟球轴承";
  return {
    title: `${name} BOM 溯源`,
    summary: "当前成品为起点，向右展开部件、原材料和更上游原料的构成树。",
    outputName: name,
    outputMeta: "当前成品树根节点",
    nodes: [
      { id: "product", lane: "PRODUCT", name, description: "当前成品，所有右侧节点都是它的组成来源。" },
      { id: "inner-ring", lane: "PART", parentId: "product", name: "内圈", description: "轴承承载部件，由轴承钢车削热处理形成。" },
      { id: "outer-ring", lane: "PART", parentId: "product", name: "外圈", description: "外侧承载部件，决定轴承安装和受力稳定性。" },
      { id: "steel-ball", lane: "PART", parentId: "product", name: "钢球", description: "滚动体部件，影响低噪音和旋转精度。" },
      { id: "retainer", lane: "PART", parentId: "product", name: "保持架", description: "保持滚动体间距，常用钢板或增强尼龙。" },
      { id: "gcr15", lane: "MATERIAL", parentId: "inner-ring", name: "轴承钢GCr15", description: "内外圈的主要原材料，高碳铬轴承钢。" },
      { id: "steel-bead-material", lane: "MATERIAL", parentId: "steel-ball", name: "高碳铬轴承钢", description: "钢球常用原料，强调硬度、洁净度和疲劳寿命。" },
      { id: "nylon", lane: "MATERIAL", parentId: "retainer", name: "增强尼龙/钢板", description: "保持架可选原料，按转速、温度和成本选择。" },
      { id: "ring-forging", lane: "PROCESS", parentId: "gcr15", name: "精密锻造 / 退火", description: "更上游的成形与热处理来源，展示前序加工链条。" },
      { id: "steel-mill", lane: "MATERIAL", parentId: "ring-forging", name: "特钢厂炉批", description: "对应炉批号、洁净度和材质证明文件。" },
    ],
    edges: [
      { from: "product", fromName: name, to: "inner-ring", toName: "内圈", relation: "直接构成" },
      { from: "product", fromName: name, to: "outer-ring", toName: "外圈", relation: "直接构成" },
      { from: "product", fromName: name, to: "steel-ball", toName: "钢球", relation: "直接构成" },
      { from: "product", fromName: name, to: "retainer", toName: "保持架", relation: "直接构成" },
      { from: "inner-ring", fromName: "内圈", to: "gcr15", toName: "轴承钢GCr15", relation: "由该原材料加工形成" },
      { from: "outer-ring", fromName: "外圈", to: "gcr15", toName: "轴承钢GCr15", relation: "由该原材料加工形成" },
      { from: "steel-ball", fromName: "钢球", to: "steel-bead-material", toName: "高碳铬轴承钢", relation: "由该原材料加工形成" },
      { from: "retainer", fromName: "保持架", to: "nylon", toName: "增强尼龙/钢板", relation: "由该原材料加工形成" },
      { from: "gcr15", fromName: "轴承钢GCr15", to: "ring-forging", toName: "精密锻造 / 退火", relation: "更上游加工环节" },
      { from: "ring-forging", fromName: "精密锻造 / 退火", to: "steel-mill", toName: "特钢厂炉批", relation: "更早一级来源数据" },
    ],
  };
}

function normalizeNode(node = {}, index = 0) {
  const id = String(node.id || node.key || `node-${index}`);
  return {
    raw: node,
    id,
    key: id,
    parentId: node.parentId !== undefined && node.parentId !== null ? String(node.parentId) : "",
    lane: normalizeLane(node.lane || node.entityType || node.type || node.nodeType),
    name: node.name || node.title || "溯源节点",
    meta: node.meta || node.description || node.summary || node.remark || "-",
    relation: node.relation || "",
  };
}

function normalizeEdge(edge = {}, nodes = {}) {
  const from = resolveNodeId(edge.from || edge.source || edge.sourceId, edge.fromName || edge.sourceName, nodes);
  const to = resolveNodeId(edge.to || edge.target || edge.targetId, edge.toName || edge.targetName, nodes);
  return {
    from,
    to,
    relation: edge.relation || edge.relationType || "直接构成",
  };
}

function resolveNodeId(id, name, nodes) {
  if (id !== undefined && id !== null && nodes[String(id)]) {
    return String(id);
  }
  const matchedNode = Object.values(nodes).find((node) => node.name === name);
  return matchedNode ? matchedNode.id : "";
}

function buildEdgeMap() {
  const map = {};
  normalizedEdges.value.forEach((edge) => {
    const bucket = map[edge.from] || [];
    bucket.push(edge);
    map[edge.from] = bucket;
  });
  normalizedNodes.value.forEach((node) => {
    if (!node.parentId || map[node.parentId]?.some((edge) => edge.to === node.id)) {
      return;
    }
    const bucket = map[node.parentId] || [];
    bucket.push({ from: node.parentId, to: node.id, relation: node.relation || "直接构成" });
    map[node.parentId] = bucket;
  });
  return map;
}

function normalizeLane(lane) {
  const value = String(lane || "").toUpperCase();
  if (value.includes("MATERIAL") || value.includes("RAW")) {
    return "MATERIAL";
  }
  if (value.includes("PART") || value.includes("COMPONENT")) {
    return "PART";
  }
  if (value.includes("PROCESS") || value.includes("SURFACE") || value.includes("QUALITY")) {
    return "PROCESS";
  }
  return "PRODUCT";
}

function laneName(lane) {
  const map = {
    PRODUCT: "成品",
    PART: "部件",
    MATERIAL: "原材料",
    PROCESS: "工艺",
  };
  return map[lane] || "节点";
}

function levelTitle(index) {
  if (index === 0) {
    return "组成材料";
  }
  return index === 1 ? "上游原料" : `第${index + 1}层来源`;
}

onLoad((options = {}) => {
  entityId.value = /^\d+$/.test(String(options.id || "")) ? String(options.id) : "";
  entityType.value = options.entityType || "";
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
.trace-fullscreen-page {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  background: #f7f8fa;
}

.fullscreen-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: calc(54rpx + env(safe-area-inset-top)) 24rpx 18rpx;
  background: #ffd21e;
  box-shadow: 0 10rpx 24rpx rgba(17, 17, 17, 0.08);
  box-sizing: border-box;
}

.header-back {
  display: flex;
  width: 64rpx;
  height: 64rpx;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.54);
}

.header-back::after,
.load-more-btn::after {
  border: none;
}

.fullscreen-title-block {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 6rpx;
}

.fullscreen-kicker {
  color: rgba(17, 17, 17, 0.62);
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.2;
}

.fullscreen-title {
  overflow: hidden;
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.25;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fullscreen-count {
  flex-shrink: 0;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 22rpx;
  font-weight: 900;
}

.fullscreen-scroll {
  flex: 1;
  min-height: 0;
  background: #ffffff;
}

.trace-tree {
  display: flex;
  min-height: calc(100vh - 274rpx - env(safe-area-inset-top) - env(safe-area-inset-bottom));
  align-items: center;
  padding: 28rpx 42rpx 36rpx;
  box-sizing: border-box;
}

.tree-level {
  display: grid;
  flex-shrink: 0;
  gap: 14rpx;
}

.tree-root-level {
  width: 292rpx;
}

.tree-branch-level {
  width: 316rpx;
}

.tree-root-card,
.tree-branch-card,
.tree-empty-card {
  display: grid;
  gap: 10rpx;
  box-sizing: border-box;
  border-radius: 18rpx;
}

.tree-root-card {
  min-height: 236rpx;
  padding: 20rpx;
  border: 3rpx solid #111111;
  background: #fff8d8;
}

.tree-branch-card {
  min-height: 116rpx;
  padding: 16rpx;
  border: 2rpx solid #d8dde5;
  background: #ffffff;
}

.tree-node-PART {
  border-left: 8rpx solid #111111;
}

.tree-node-MATERIAL {
  border-left: 8rpx solid #2f8f46;
}

.tree-node-PROCESS {
  border-left: 8rpx solid #ffd21e;
}

.tree-node-label,
.tree-level-title,
.branch-node-lane {
  color: rgba(17, 17, 17, 0.66);
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.2;
}

.tree-root-title {
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1.25;
}

.branch-card-head,
.tree-level-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10rpx;
}

.branch-node-title {
  min-width: 0;
  color: #111111;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.3;
}

.branch-node-lane,
.tree-level-count {
  flex-shrink: 0;
  padding: 5rpx 10rpx;
  border-radius: 999rpx;
  background: #f3f4f6;
}

.tree-level-count {
  color: #111111;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 1.2;
}

.branch-relation {
  color: #167f61;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.35;
}

.tree-node-meta {
  color: #5d6673;
  font-size: 21rpx;
  line-height: 1.35;
}

.tree-node-stack {
  display: grid;
  gap: 14rpx;
  overflow: visible;
}

.tree-connector {
  position: relative;
  display: flex;
  width: 78rpx;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tree-connector-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 4rpx;
  background: #111111;
}

.tree-connector-arrow {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  background: #ffd21e;
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1;
}

.tree-empty-card {
  width: 320rpx;
  margin-left: 78rpx;
  padding: 28rpx;
  border: 2rpx dashed #d8dde5;
  background: #f7f8fa;
}

.tree-empty-card text {
  color: #8d96a3;
  font-size: 24rpx;
}

.fullscreen-controls {
  display: grid;
  flex-shrink: 0;
  gap: 10rpx;
  padding: 18rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eceff3;
  background: #ffffff;
  box-shadow: 0 -10rpx 24rpx rgba(17, 17, 17, 0.06);
}

.fullscreen-slider-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  color: #2b2f36;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1.2;
}

.fullscreen-slider-head text:first-child {
  min-width: 0;
}

.load-more-btn {
  height: 70rpx;
  margin: 0;
  border-radius: 16rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 25rpx;
  font-weight: 900;
  line-height: 70rpx;
}

.load-more-btn[disabled] {
  background: #eceff3;
  color: #8d96a3;
}
</style>
