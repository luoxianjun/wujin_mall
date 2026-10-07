const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..");

function resolvePath(relativePath) {
  return path.resolve(projectRoot, relativePath);
}

function read(relativePath) {
  const filePath = resolvePath(relativePath);
  assert.ok(fs.existsSync(filePath), `Expected ${relativePath} to exist`);
  return fs.readFileSync(filePath, "utf8");
}

function readJson(relativePath) {
  return JSON.parse(read(relativePath));
}

test("wujin detail, supplier, lead progress, and trace pages are registered", () => {
  const pages = readJson("src/pages.json");
  const expectedPages = [
    ["pages/wujin/detail", "五金详情"],
    ["pages/wujin/supplier-capability", "供应能力"],
    ["pages/wujin/lead-progress", "线索进度"],
    ["pages/wujin/trace-map", "溯源图谱"],
    ["pages/wujin/trace-fullscreen", "全屏溯源"],
  ];

  for (const [pagePath, title] of expectedPages) {
    const page = pages.pages.find((item) => item.path === pagePath);
    assert.ok(page, `Expected ${pagePath} route`);
    assert.equal(page.style.navigationBarTitleText, title);
    assert.equal(page.style.navigationStyle, "custom");
  }
});

test("wujin detail API wraps detail and trace graph endpoints with source context", () => {
  const api = read("src/api/wujin/detail.js");

  assert.match(api, /from\s+["']@\/utils\/request["']/);
  assert.match(api, /export function getWujinEntityDetail/);
  assert.match(api, /\/wujin\/detail\/entity/);
  assert.match(api, /entityType/);
  assert.match(api, /lane/);
  assert.match(api, /sourceKeyword/);
  assert.match(api, /export function getWujinTraceGraph/);
  assert.match(api, /\/wujin\/trace\/graph/);
});

test("wujin detail page renders lane-specific details and cross-industry comparison", () => {
  const page = read("src/pages/wujin/detail.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniDetail["']\s*\}\)/);
  assert.match(page, /getWujinEntityDetail/);
  assert.match(page, /buildFallbackDetail/);
  assert.match(page, /detailSections/);
  assert.match(page, /industryComparison/);
  assert.match(page, /crossIndustryTips/);
  assert.match(page, /handleSupplierCapabilityOpen/);
  assert.match(page, /handleSourcingOpen/);
  assert.match(page, /handleMaterialLaneOpen/);
  assert.match(page, /handleTraceMapOpen/);
  assert.match(page, /商品详情/);
  assert.match(page, /工艺详情/);
  assert.match(page, /原材料详情/);
  assert.match(page, /跨行业差异提示/);
  assert.match(page, /对比展示/);
  assert.match(page, /供应能力/);
  assert.match(page, /溯源图谱/);
  assert.match(page, /一键寻源/);
  assert.match(page, /查看原材料/);
  assert.match(page, /pages\/wujin\/sourcing/);
  assert.match(page, /requestedLane:\s*["']MATERIAL["']/);
});

test("wujin detail page uses a purchase decision layout instead of loose gray blocks", () => {
  const page = read("src/pages/wujin/detail.vue");

  assert.match(page, /purchase-decision-card/);
  assert.match(page, /purchase-summary/);
  assert.match(page, /action-panel/);
  assert.match(page, /wujin-button--primary/);
  assert.match(page, /wujin-button--secondary/);
  assert.match(page, /spec-panel/);
  assert.match(page, /risk-ticket/);
  assert.match(page, /comparison-board/);
  assert.match(page, /compare-column/);
});

test("wujin detail page follows the industrial product detail reference layout", () => {
  const page = read("src/pages/wujin/detail.vue");

  assert.match(page, /detail-commerce-header/);
  assert.match(page, /detail-search-pill/);
  assert.match(page, /detail-gallery/);
  assert.match(page, /gallery-count/);
  assert.match(page, /单价/);
  assert.match(page, /起订量 \(MOQ\)/);
  assert.match(page, /现货库存/);
  assert.match(page, /24h发货/);
  assert.match(page, /规格参数/);
  assert.match(page, /技术图纸/);
  assert.match(page, /质检报告/);
  assert.match(page, /相关产品/);
  assert.match(page, /应用场景风险提示/);
  assert.match(page, /detail-action-list/);
  assert.match(page, /detail-product-actions/);
  assert.match(page, /detail-bottom-bar/);
  assert.match(page, /加入询价单/);
  assert.match(page, /查看供应商/);
});

test("wujin mini product images filter broken demo placeholders before rendering", () => {
  const imageUtils = read("src/utils/wujinImage.js");
  const detailPage = read("src/pages/wujin/detail.vue");
  const searchPage = read("src/pages/wujin/search.vue");
  const recommendList = read("src/components/wujin/ProductRecommendList.vue");

  assert.match(imageUtils, /export function sanitizeWujinImageUrl/);
  assert.match(imageUtils, /export function sanitizeWujinImageList/);
  assert.match(imageUtils, /example\\.com/);
  assert.match(imageUtils, /via\\.placeholder\\.com/);
  assert.match(imageUtils, /placeholder\\.com/);
  assert.match(imageUtils, /lh3\\.googleusercontent\\.com\\\/aida-public/);

  assert.match(searchPage, /sanitizeWujinImageUrl\(pick\(item, \["image", "imageUrl", "picUrl", "cover", "thumbnail"\]\)\)/);
  assert.match(recommendList, /sanitizeWujinImageUrl\(pick\(item, \["image", "imageUrl", "picUrl", "cover", "thumbnail"\]\)\)/);
  assert.match(detailPage, /sanitizeWujinImageList/);
  assert.match(detailPage, /v-if="activeProductImage"/);
  assert.match(detailPage, /galleryCountText/);
  assert.doesNotMatch(detailPage, /lh3\.googleusercontent\.com/);
  assert.doesNotMatch(detailPage, /example\.com\/wujin-product\.png/);
});

test("wujin search result page opens detail and fullscreen trace from result context", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /handleDetailOpen/);
  assert.match(page, /handleTraceMapOpen/);
  assert.match(page, /pages\/wujin\/detail/);
  assert.match(page, /pages\/wujin\/trace-fullscreen/);
  assert.match(page, /keyword=\$\{encodeURIComponent\(lastKeyword\.value/);
  assert.match(page, /lane=\$\{encodeURIComponent\(summary\.lane/);
  assert.match(page, /sourceKeyword=\$\{encodeURIComponent\(lastKeyword\.value/);
  assert.match(page, /查看详情/);
  assert.match(page, /溯源图谱/);
});

test("wujin sourcing API exposes supplier capability and lead progress placeholders", () => {
  const api = read("src/api/wujin/sourcing.js");

  assert.match(api, /export function getWujinSupplierCapability/);
  assert.match(api, /\/wujin\/sourcing\/supplier-capability/);
  assert.match(api, /supplierId/);
  assert.match(api, /keyword/);
  assert.match(api, /export function getWujinSourcingLeadProgress/);
  assert.match(api, /\/wujin\/sourcing\/lead\/progress/);
  assert.match(api, /leadId/);
});

test("wujin sourcing page links suppliers to capability detail and submitted leads to progress", () => {
  const page = read("src/pages/wujin/sourcing.vue");

  assert.match(page, /handleSupplierCapabilityOpen/);
  assert.match(page, /handleLeadProgressOpen/);
  assert.match(page, /pages\/wujin\/supplier-capability/);
  assert.match(page, /pages\/wujin\/lead-progress/);
  assert.match(page, /submittedLead/);
  assert.match(page, /供应能力详情/);
  assert.match(page, /线索进度/);
});

test("wujin supplier capability page reuses sourcing candidate context", () => {
  const page = read("src/pages/wujin/supplier-capability.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniSupplierCapability["']\s*\}\)/);
  assert.match(page, /getWujinSupplierCapability/);
  assert.match(page, /buildFallbackCapability/);
  assert.match(page, /candidatePayload/);
  assert.match(page, /供应能力详情/);
  assert.match(page, /主营能力/);
  assert.match(page, /可承接泳道/);
  assert.match(page, /联调接口占位/);
});

test("wujin lead progress and trace graph pages render first-version business views", () => {
  const leadPage = read("src/pages/wujin/lead-progress.vue");
  const tracePage = read("src/pages/wujin/trace-map.vue");

  assert.match(leadPage, /defineOptions\(\{\s*name:\s*["']WujinMiniLeadProgress["']\s*\}\)/);
  assert.match(leadPage, /getWujinSourcingLeadProgress/);
  assert.match(leadPage, /buildFallbackProgress/);
  assert.match(leadPage, /progressSteps/);
  assert.match(leadPage, /线索进度/);
  assert.match(leadPage, /已提交/);
  assert.match(leadPage, /供应商响应/);

  assert.match(tracePage, /defineOptions\(\{\s*name:\s*["']WujinMiniTraceMap["']\s*\}\)/);
  assert.match(tracePage, /getWujinTraceGraph/);
  assert.match(tracePage, /buildFallbackTraceGraph/);
  assert.match(tracePage, /traceNodes/);
  assert.match(tracePage, /traceEdges/);
  assert.match(tracePage, /溯源图谱/);
  assert.match(tracePage, /成品/);
  assert.match(tracePage, /加工/);
  assert.match(tracePage, /原材料/);
});

test("wujin trace graph renders a finished-product BOM tree from left to right", () => {
  const component = read("src/components/wujin/HorizontalTraceGraph.vue");
  const tracePage = read("src/pages/wujin/trace-map.vue");

  assert.match(component, /rootNode/);
  assert.match(component, /treeLevels/);
  assert.match(component, /trace-tree/);
  assert.match(component, /tree-root-card/);
  assert.match(component, /tree-branch-card/);
  assert.match(component, /当前成品/);
  assert.match(component, /组成材料/);
  assert.match(component, /直接构成/);
  assert.doesNotMatch(component, /class="trace-lanes"/);
  assert.doesNotMatch(component, /displayStages/);

  assert.match(tracePage, /"深沟球轴承"/);
  assert.match(tracePage, /BOM 溯源/);
  assert.match(tracePage, /当前成品为起点，向右展开部件、原材料和更上游原料的构成树。/);
  assert.match(tracePage, /内圈/);
  assert.match(tracePage, /外圈/);
  assert.match(tracePage, /钢球/);
  assert.match(tracePage, /保持架/);
  assert.match(tracePage, /轴承钢GCr15/);
  assert.match(tracePage, /构成关系/);
  assert.match(tracePage, /edge\.relation/);
});

test("wujin trace graph supports horizontal dragging and fullscreen slider exploration", () => {
  const component = read("src/components/wujin/HorizontalTraceGraph.vue");
  const fullscreenPage = read("src/pages/wujin/trace-fullscreen.vue");
  const tracePage = read("src/pages/wujin/trace-map.vue");

  assert.match(component, /scroll-x/);
  assert.match(component, /showFullscreenGraph/);
  assert.match(component, /pages\/wujin\/trace-fullscreen/);
  assert.doesNotMatch(component, /graph-fullscreen/);
  assert.doesNotMatch(component, /fullscreenVisible/);
  assert.match(component, /slider/);
  assert.match(component, /<AppIcon name="open_in_new" variant="light" size="26rpx" \/>/);
  assert.match(component, /<text>全屏<\/text>/);
  assert.match(component, /trace-board-primary-actions/);
  assert.match(component, /trace-board-action--fullscreen/);
  assert.match(component, /trace-board-action--expand/);
  assert.match(component, /\.trace-board-action\s*\{[^}]*width:\s*100%;/);
  assert.match(component, /visibleBranchLevels/);
  assert.doesNotMatch(component, /const COMPACT_SCROLL_MAX = \d+;/);
  assert.match(component, /compactScrollMax/);
  assert.match(component, /COMPACT_ROOT_WIDTH/);
  assert.match(component, /COMPACT_BRANCH_WIDTH/);
  assert.match(component, /clampCompactScrollLeft/);
  assert.match(component, /Math\.min\(compactScrollMax\.value/);
  assert.match(component, /compactSliderScrollLock/);
  assert.match(component, /scheduleCompactScrollUnlock\(\)/);
  assert.match(component, /compactScrollIntoView\.value = ""/);
  assert.doesNotMatch(component, /FULLSCREEN_SCROLL_MAX/);
  assert.doesNotMatch(component, /overflow:\s*hidden/);
  assert.doesNotMatch(component, /max-height:\s*430rpx/);

  assert.match(fullscreenPage, /defineOptions\(\{\s*name:\s*["']WujinMiniTraceFullscreen["']\s*\}\)/);
  assert.match(fullscreenPage, /scroll-x/);
  assert.match(fullscreenPage, /fullscreenScrollLeft/);
  assert.match(fullscreenPage, /handleFullscreenSliderChanging/);
  assert.match(fullscreenPage, /handleFullscreenSliderChange/);
  assert.match(fullscreenPage, /@changing="handleFullscreenSliderChanging"/);
  assert.match(fullscreenPage, /@change="handleFullscreenSliderChange"/);
  assert.match(fullscreenPage, /加载更多树信息/);
  assert.match(fullscreenPage, /:scroll-left="fullscreenScrollLeft"/);
  assert.match(fullscreenPage, /:scroll-into-view="fullscreenScrollIntoView"/);
  assert.match(fullscreenPage, /fullscreenSliderScrollLock/);
  assert.match(fullscreenPage, /scheduleFullscreenScrollUnlock\(\)/);
  assert.match(fullscreenPage, /fullscreenScrollAnchorFor/);
  assert.match(fullscreenPage, /fullscreenBranchLevels/);
  assert.doesNotMatch(fullscreenPage, /const FULLSCREEN_SCROLL_MAX/);
  assert.match(fullscreenPage, /fullscreenScrollMax/);
  assert.match(fullscreenPage, /FULLSCREEN_ROOT_WIDTH/);
  assert.match(fullscreenPage, /FULLSCREEN_BRANCH_WIDTH/);
  assert.match(fullscreenPage, /clampFullscreenScrollLeft/);
  assert.match(fullscreenPage, /Math\.min\(fullscreenScrollMax\.value/);
  assert.match(fullscreenPage, /const targetSliderValue = Math\.max\(fullscreenSliderValue\.value/);
  assert.match(fullscreenPage, /syncFullscreenScroll\(targetSliderValue\)/);
  assert.match(fullscreenPage, /fullscreenScrollIntoView\.value = ""/);

  assert.match(tracePage, /id:\s*graph\.value\.outputId/);
  assert.match(tracePage, /handleFullscreenOpen/);
  assert.match(tracePage, /trace-hero-actions/);
  assert.match(tracePage, /全屏查看/);
  assert.match(tracePage, /:loading="loading"/);
  assert.doesNotMatch(tracePage, /to:\s*"product",\s*\n\s*toName:\s*"成品"/);
  assert.match(tracePage, /to:\s*"steel-mill"/);
});

test("wujin trace graph keeps the drag surface dedicated to horizontal scrolling", () => {
  const component = read("src/components/wujin/HorizontalTraceGraph.vue");

  assert.doesNotMatch(component, /class="trace-board" @tap="showFullscreenGraph"/);
  assert.match(component, /@tap\.stop="showFullscreenGraph"/);
  assert.match(component, /const COMPACT_LEVEL_COUNT = 2/);
  assert.match(component, /hasMoreTreeLevels/);
  assert.match(component, /\.trace-board-primary-actions\s*\{[^}]*grid-template-columns:\s*148rpx\s+148rpx;/);
  assert.match(component, /\.trace-board-action\s*\{[^}]*height:\s*56rpx;/);
  assert.match(component, /拖动滑轨或左右滑动查看构成树/);
  assert.match(component, /margin-bottom:\s*calc\(136rpx \+ env\(safe-area-inset-bottom\)\)/);
  assert.match(component, /id:\s*"process"/);
  assert.match(component, /id:\s*"upstream"/);
  assert.match(component, /id:\s*"source"/);
});
