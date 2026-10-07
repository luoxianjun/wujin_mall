<template>
  <view class="trace-graph">
    <view v-if="!empty" class="trace-board">
      <view class="trace-board-head">
        <view class="trace-board-title-block">
          <text class="trace-board-title">横向构成树</text>
          <text class="trace-board-hint">点击展开更多层，底部滑轨可拖动查看更多横向树</text>
        </view>
      </view>
      <view class="trace-board-primary-actions">
        <view class="trace-board-action trace-board-action--fullscreen" @tap.stop="showFullscreenGraph">
          <AppIcon name="open_in_new" variant="light" size="26rpx" />
          <text>全屏</text>
        </view>
        <view
          class="trace-board-action trace-board-action--expand"
          :class="{ disabled: !hasMoreCompactLevels }"
          @tap.stop="loadMoreCompactTreeInfo"
        >
          <AppIcon name="expand_more" variant="dark" size="24rpx" />
          <text>{{ hasMoreCompactLevels ? "展开" : "已全量" }}</text>
        </view>
      </view>
      <scroll-view
        scroll-x
        scroll-with-animation
        :bounces="false"
        class="trace-scroll"
        :scroll-left="compactScrollLeft"
        :scroll-into-view="compactScrollIntoView"
        @scroll="handleCompactScroll"
      >
        <view class="trace-tree trace-tree--compact" :style="{ width: `${compactTreeWidth}rpx` }">
          <view id="compact-tree-start" class="tree-level tree-root-level">
            <view class="tree-root-card" @tap.stop="emit('selectNode', rootNode.raw)">
              <text class="tree-node-label">当前成品</text>
              <text class="tree-root-title">{{ rootNode.name }}</text>
              <text class="tree-node-meta">{{ rootNode.meta }}</text>
            </view>
          </view>

          <template v-for="(level, levelIndex) in visibleBranchLevels" :key="`level-${levelIndex}`">
            <view class="tree-connector">
              <view class="tree-connector-line"></view>
              <text class="tree-connector-arrow">→</text>
            </view>

            <view :id="`compact-tree-level-${levelIndex + 1}`" class="tree-level tree-branch-level">
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
                  @tap.stop="emit('selectNode', node.raw)"
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

          <view v-if="!visibleBranchLevels.length" class="tree-empty-card">
            <text>等待补充组成材料数据</text>
          </view>
        </view>
      </scroll-view>
      <view class="trace-board-controls">
        <view class="trace-board-controls-head">
          <text class="trace-board-status">
            {{ hasMoreCompactLevels ? `还可展开${branchLevels.length - visibleBranchLevels.length}层` : "已展开全部层级" }}
          </text>
          <text class="trace-board-drag-tip">{{ compactSliderValue }}%</text>
        </view>
        <slider
          :value="compactSliderValue"
          min="0"
          max="100"
          block-size="20"
          activeColor="#ffd21e"
          backgroundColor="#d8dde5"
          @changing="handleCompactSliderChanging"
          @change="handleCompactSliderChange"
        />
        <view class="trace-board-controls-foot">
          <text>{{ compactScrollMax > 0 ? "拖动滑轨或左右滑动查看构成树" : "当前宽度已完整展示" }}</text>
        </view>
      </view>
    </view>

    <view v-else class="trace-empty">
      <text>{{ loading ? "加载溯源图谱..." : "暂无溯源数据" }}</text>
    </view>

    <view class="batch-card">
      <text class="batch-title">当前批次信息</text>
      <view class="batch-grid">
        <text>批次号</text>
        <text>{{ batch.batchNo || "-" }}</text>
        <text>生产日期</text>
        <text>{{ batch.productionDate || "-" }}</text>
        <text>来源工厂</text>
        <text>{{ batch.factory || "-" }}</text>
        <text>质检状态</text>
        <text class="pass">{{ batch.qualityStatus || "合格" }}</text>
      </view>
      <view class="batch-actions">
        <button class="line-btn" @tap="emit('report')">查看检测报告</button>
        <button class="yellow-btn" @tap="emit('export')">导出溯源报告</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";

defineOptions({ name: "HorizontalTraceGraph" });

const emit = defineEmits(["selectNode", "selectStage", "report", "export"]);

const props = defineProps({
  activeStage: {
    type: String,
    default: "PRODUCT",
  },
  stages: {
    type: Array,
    default: () => [],
  },
  nodes: {
    type: Array,
    default: () => [],
  },
  edges: {
    type: Array,
    default: () => [],
  },
  output: {
    type: Object,
    default: () => ({}),
  },
  currentBatch: {
    type: Object,
    default: () => ({}),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  empty: {
    type: Boolean,
    default: false,
  },
});

const fallbackNodes = [
  { id: "product", lane: "PRODUCT", name: "五金成品", description: "当前成品规格与批次入口" },
  { id: "part", lane: "PART", parentId: "product", name: "关键部件", description: "由平台模板补充的组成部件" },
  { id: "material", lane: "MATERIAL", parentId: "part", name: "原材料", description: "材料牌号、来源和批次" },
  { id: "process", lane: "PROCESS", parentId: "part", name: "热处理", description: "加工工艺与质检节点" },
  { id: "upstream", lane: "MATERIAL", parentId: "material", name: "上游原料", description: "更上游的来源材料" },
  { id: "source", lane: "MATERIAL", parentId: "upstream", name: "原料来源", description: "更早一级来源数据" },
];
const COMPACT_LEVEL_COUNT = 2;
const COMPACT_ROOT_WIDTH = 268;
const COMPACT_BRANCH_WIDTH = 292;
const COMPACT_CONNECTOR_WIDTH = 68;
const COMPACT_VIEWPORT_WIDTH = 690;
const COMPACT_EMPTY_CARD_WIDTH = 308;

const compactScrollLeft = ref(0);
const compactSliderValue = ref(0);
const compactScrollIntoView = ref("compact-tree-start");
const compactSliderScrollLock = ref(false);
let compactScrollUnlockTimer = null;

const normalizedNodes = computed(() => {
  const sourceNodes = props.nodes.length ? props.nodes : fallbackNodes;
  return sourceNodes.map(normalizeNode);
});

const nodeMap = computed(() =>
  normalizedNodes.value.reduce((result, node) => {
    result[node.id] = node;
    return result;
  }, {}),
);

const normalizedEdges = computed(() =>
  props.edges
    .map((edge) => normalizeEdge(edge, nodeMap.value))
    .filter((edge) => edge.from && edge.to && edge.from !== edge.to),
);

const displayOutput = computed(() => props.output.name ? props.output : {
  name: "五金成品",
  meta: "批次：-",
});

const rootNode = computed(() => {
  const outputId = displayOutput.value.id ? String(displayOutput.value.id) : "";
  const outputName = displayOutput.value.name || "";
  return (
    (outputId && nodeMap.value[outputId]) ||
    normalizedNodes.value.find((node) => node.lane === "PRODUCT" && node.name === outputName) ||
    normalizedNodes.value.find((node) => node.lane === "PRODUCT") ||
    normalizedNodes.value.find((node) => !incomingIds.value.has(node.id)) ||
    normalizedNodes.value[0] ||
    normalizeNode(fallbackNodes[0], 0)
  );
});

const incomingIds = computed(() =>
  normalizedEdges.value.reduce((result, edge) => {
    result.add(edge.to);
    return result;
  }, new Set()),
);

const treeLevels = computed(() => {
  const root = rootNode.value;
  if (!root) {
    return [];
  }

  const levels = [[root]];
  const visited = new Set([root.id]);
  const edgeMap = buildEdgeMap();
  let currentLevel = [root];

  for (let depth = 0; depth < 6; depth += 1) {
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

  if (levels.length === 1) {
    const directChildren = normalizedNodes.value
      .filter((node) => node.id !== root.id)
      .slice(0, 6)
      .map((node) => ({
        ...node,
        parentId: root.id,
        parentName: root.name,
        relation: node.relation || "直接构成",
      }));
    if (directChildren.length) {
      levels.push(directChildren);
    }
  }

  return levels;
});

const branchLevels = computed(() => treeLevels.value.slice(1));
const compactLevelCount = ref(COMPACT_LEVEL_COUNT);
const visibleBranchLevels = computed(() => branchLevels.value.slice(0, compactLevelCount.value));
const hasMoreTreeLevels = computed(() => branchLevels.value.length > visibleBranchLevels.value.length);
const hasMoreCompactLevels = computed(() => compactLevelCount.value < branchLevels.value.length);
const batch = computed(() => props.currentBatch || {});
const compactTreeWidth = computed(() => {
  const branchWidth = visibleBranchLevels.value.length
    ? visibleBranchLevels.value.length * (COMPACT_CONNECTOR_WIDTH + COMPACT_BRANCH_WIDTH)
    : COMPACT_CONNECTOR_WIDTH + COMPACT_EMPTY_CARD_WIDTH;
  return COMPACT_ROOT_WIDTH + branchWidth;
});
const compactScrollMax = computed(() =>
  rpxToPx(Math.max(0, compactTreeWidth.value - COMPACT_VIEWPORT_WIDTH)),
);

function showFullscreenGraph() {
  uni.navigateTo({
    url: `/pages/wujin/trace-fullscreen?${buildTraceQuery()}`,
  });
}

function handleCompactScroll(event = {}) {
  if (compactSliderScrollLock.value) {
    return;
  }
  const scrollLeft = clampCompactScrollLeft(event.detail?.scrollLeft);
  compactScrollLeft.value = scrollLeft;
  compactSliderValue.value = compactScrollPercentFor(scrollLeft);
}

function handleCompactSliderChanging(event = {}) {
  syncCompactScroll(event.detail?.value);
}

function handleCompactSliderChange(event = {}) {
  syncCompactScroll(event.detail?.value);
}

function syncCompactScroll(value = 0) {
  const sliderValue = compactScrollMax.value > 0 ? Math.max(0, Math.min(100, Number(value) || 0)) : 0;
  const nextScrollLeft = clampCompactScrollLeft(Math.round((sliderValue / 100) * compactScrollMax.value));
  compactSliderScrollLock.value = true;
  compactSliderValue.value = sliderValue;
  compactScrollIntoView.value = compactScrollAnchorFor(sliderValue);
  compactScrollLeft.value = nextScrollLeft;
  scheduleCompactScrollUnlock();
}

function clampCompactScrollLeft(value = 0) {
  return Math.max(0, Math.min(compactScrollMax.value, Number(value) || 0));
}

function compactScrollPercentFor(value = 0) {
  if (compactScrollMax.value <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(100, Math.round((clampCompactScrollLeft(value) / compactScrollMax.value) * 100)));
}

function scheduleCompactScrollUnlock() {
  if (compactScrollUnlockTimer) {
    clearTimeout(compactScrollUnlockTimer);
  }
  compactScrollUnlockTimer = setTimeout(() => {
    compactSliderScrollLock.value = false;
    compactScrollIntoView.value = "";
    compactScrollUnlockTimer = null;
  }, 260);
}

function compactScrollAnchorFor(value = 0) {
  if (value <= 3 || !visibleBranchLevels.value.length) {
    return "compact-tree-start";
  }
  const maxAnchorIndex = Math.max(1, visibleBranchLevels.value.length);
  const anchorIndex = Math.max(1, Math.min(maxAnchorIndex, Math.round((value / 100) * maxAnchorIndex)));
  return `compact-tree-level-${anchorIndex}`;
}

function loadMoreCompactTreeInfo() {
  if (!hasMoreCompactLevels.value) {
    return;
  }
  compactLevelCount.value += 1;
  setTimeout(() => {
    const expandedPercent = Math.round((compactLevelCount.value / Math.max(branchLevels.value.length, 1)) * 100);
    const targetSliderValue = Math.max(compactSliderValue.value, Math.min(100, expandedPercent));
    syncCompactScroll(targetSliderValue);
  }, 0);
}

function rpxToPx(value = 0) {
  try {
    const { windowWidth } = uni.getSystemInfoSync();
    return Math.round((Number(value) * Number(windowWidth || 375)) / 750);
  } catch (error) {
    return Math.round(Number(value) / 2);
  }
}

function normalizeNode(node = {}, index = 0) {
  const id = String(node.id || node.key || `node-${index}`);
  const lane = normalizeLane(node.lane || node.entityType || node.type || node.nodeType);
  return {
    raw: node,
    id,
    key: id,
    parentId: node.parentId !== undefined && node.parentId !== null ? String(node.parentId) : "",
    lane,
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

function buildTraceQuery() {
  const keyword = props.output?.name || rootNode.value?.name || "";
  const entityId = props.output?.id || rootNode.value?.id || "";
  const query = {
    id: /^\d+$/.test(String(entityId)) ? entityId : "",
    lane: rootNode.value?.lane || props.output?.lane || "PRODUCT",
    keyword,
    sourceKeyword: keyword,
  };
  return Object.entries(query)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
    .join("&");
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
  return index === 0 ? "组成材料" : "上游原料";
}
</script>

<style lang="scss" scoped>
.trace-graph {
  display: grid;
  gap: 18rpx;
  padding-bottom: calc(150rpx + env(safe-area-inset-bottom));
}

.line-btn::after,
.yellow-btn::after {
  border: none;
}

.trace-board {
  min-height: 0;
  padding: 18rpx;
  border-radius: 20rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(17, 17, 17, 0.06);
}

.trace-board-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  padding: 0 2rpx 12rpx;
}

.trace-board-title-block {
  display: grid;
  min-width: 0;
  gap: 6rpx;
}

.trace-board-title {
  color: #111111;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.2;
}

.trace-board-hint {
  color: #69717c;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 1.35;
}

.trace-board-primary-actions {
  display: grid;
  grid-template-columns: 148rpx 148rpx;
  justify-content: start;
  gap: 10rpx;
  padding-bottom: 12rpx;
}

.trace-board-action {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  flex-shrink: 0;
  width: 100%;
  min-width: 0;
  height: 56rpx;
  padding: 0 12rpx;
  border-radius: 12rpx;
  box-sizing: border-box;
  margin: 0;
}

.trace-board-action--fullscreen {
  background: #111111;
}

.trace-board-action--fullscreen text {
  color: #ffd21e;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.2;
}

.trace-board-action--expand {
  background: #ffd21e;
}

.trace-board-action--expand text {
  color: #111111;
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.2;
}

.trace-board-action.disabled {
  background: #eceff3;
}

.trace-board-action.disabled text {
  color: #8d96a3;
}

.trace-scroll {
  width: 100%;
  padding-bottom: 6rpx;
}

.trace-tree {
  display: flex;
  min-height: 340rpx;
  align-items: center;
  padding: 4rpx 0 8rpx;
}

.trace-tree--compact {
  min-width: 100%;
}

.tree-level {
  display: grid;
  flex-shrink: 0;
  gap: 12rpx;
}

.tree-root-level {
  width: 268rpx;
}

.tree-branch-level {
  width: 292rpx;
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
  min-height: 218rpx;
  padding: 18rpx;
  border: 3rpx solid #111111;
  background: #fff8d8;
}

.tree-branch-card {
  min-height: 106rpx;
  padding: 14rpx;
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
  font-size: 21rpx;
  font-weight: 900;
  line-height: 1.2;
}

.tree-root-title {
  color: #111111;
  font-size: 30rpx;
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
  font-size: 25rpx;
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
  gap: 12rpx;
  overflow: visible;
}

.tree-connector {
  position: relative;
  display: flex;
  width: 68rpx;
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
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  background: #ffd21e;
  color: #111111;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1;
}

.tree-empty-card {
  width: 308rpx;
  margin-left: 68rpx;
  padding: 28rpx;
  border: 2rpx dashed #d8dde5;
  background: #f7f8fa;
}

.tree-empty-card text {
  color: #8d96a3;
  font-size: 23rpx;
}

.trace-empty,
.batch-card {
  padding: 24rpx;
  border-radius: 20rpx;
  background: #ffffff;
}

.trace-empty {
  color: #5d6673;
  font-size: 24rpx;
  text-align: center;
}

.trace-board-controls {
  display: grid;
  gap: 8rpx;
  padding-top: 10rpx;
}

.trace-board-controls-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.trace-board-controls-foot {
  display: block;
}

.trace-board-status,
.trace-board-drag-tip,
.trace-board-controls-foot text {
  color: rgba(17, 17, 17, 0.62);
  font-size: 22rpx;
  font-weight: 900;
  line-height: 1.2;
}

.trace-board-drag-tip {
  flex-shrink: 0;
  color: #111111;
}

.trace-board-controls-foot text:last-child {
  color: #111111;
}

.trace-board-controls slider {
  width: 100%;
}

.trace-board-controls-foot {
  min-height: 28rpx;
}

.batch-card {
  display: grid;
  gap: 18rpx;
  border: 1rpx solid #eceff3;
  margin-bottom: calc(136rpx + env(safe-area-inset-bottom));
}

.batch-title {
  color: #111111;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1.25;
}

.batch-grid {
  display: grid;
  grid-template-columns: 150rpx 1fr;
  gap: 14rpx 18rpx;
  color: #2b2f36;
  font-size: 25rpx;
}

.pass {
  justify-self: start;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
  background: #dff6e8;
  color: #2f8f46;
  font-weight: 900;
}

.batch-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.line-btn,
.yellow-btn {
  height: 68rpx;
  margin: 0;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 68rpx;
  padding: 0 10rpx;
  box-sizing: border-box;
}

.line-btn {
  border: 1rpx solid #d8dde5;
  background: #ffffff;
  color: #111111;
}

.yellow-btn {
  background: #ffd21e;
  color: #111111;
}
</style>
