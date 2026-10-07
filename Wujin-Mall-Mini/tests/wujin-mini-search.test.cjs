const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.resolve(projectRoot, relativePath), "utf8");
}

function readJson(relativePath) {
  return JSON.parse(read(relativePath));
}

test("wujin mini search API wraps app search endpoint", () => {
  const api = read("src/api/wujin/search.js");

  assert.match(api, /from\s+["']@\/utils\/request["']/);
  assert.match(api, /export function searchWujin/);
  assert.match(api, /\/wujin\/search\/result/);
  assert.match(api, /params/);
  assert.match(api, /keyword/);
  assert.match(api, /requestedLane/);
  assert.match(api, /entryPath/);
  assert.match(api, /sourceProductId/);
  assert.match(api, /sourceEntityId/);
});

test("wujin mini search home and dedicated result pages are registered", () => {
  const pages = readJson("src/pages.json");
  const homePage = pages.pages.find((item) => item.path === "pages/wujin/search");
  const resultPage = pages.pages.find((item) => item.path === "pages/wujin/search-result");

  assert.ok(homePage);
  assert.equal(homePage.style.navigationBarTitleText, "五金搜索");
  assert.equal(homePage.style.navigationStyle, "custom");
  assert.ok(resultPage);
  assert.equal(resultPage.style.navigationBarTitleText, "搜索结果");
  assert.equal(resultPage.style.navigationStyle, "custom");
});

test("wujin mini search home navigates to the dedicated result page instead of filtering products in place", () => {
  const homePage = read("src/pages/wujin/search.vue");

  assert.match(homePage, /defineOptions\(\{\s*name:\s*["']WujinMiniSearch["']\s*\}\)/);
  assert.match(homePage, /function\s+navigateToSearchResult/);
  assert.match(homePage, /url:\s*`\/pages\/wujin\/search-result\?keyword=\$\{encodeURIComponent\(searchKeyword\)\}/);
  assert.match(homePage, /@confirm="submitSearch"/);
  assert.match(homePage, /@tap="submitSearch"/);
  assert.match(homePage, /ProductRecommendList/);
  assert.doesNotMatch(homePage, /import\s+\{\s*searchWujin\s*\}/);
  assert.doesNotMatch(homePage, /searchWujin\(/);
  assert.doesNotMatch(homePage, /searchResult\s*=\s*ref\(null\)/);
  assert.doesNotMatch(homePage, /keywordMatchedProducts/);
  assert.doesNotMatch(homePage, /productMatchesKeyword/);
});

test("wujin mini dedicated search result page owns the three-lane result flow", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniSearchResult["']\s*\}\)/);
  assert.match(page, /searchWujin/);
  assert.match(page, /searchResult\s*=\s*ref\(null\)/);
  assert.match(page, /onLoad\(\(options = \{\}\) =>/);
  assert.match(page, /options\.keyword/);
  assert.match(page, /submitSearch\(\{\s*[\s\S]*entryPath:/);
});

test("wujin mini search result page renders three-lane result and trace context", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniSearchResult["']\s*\}\)/);
  assert.match(page, /searchWujin/);
  assert.match(page, /laneOptions/);
  assert.match(page, /PRODUCT/);
  assert.match(page, /PROCESS/);
  assert.match(page, /MATERIAL/);
  assert.match(page, /defaultLane/);
  assert.match(page, /laneSummaries/);
  assert.match(page, /requestedLane:\s*extra\.requestedLane\s*\|\|\s*["']PRODUCT["']/);
  assert.match(page, /requestedLane\.value\s*=\s*extra\.requestedLane\s*\|\|\s*["']PRODUCT["']/);
  assert.match(page, /riskWarningRequired/);
  assert.match(page, /riskWarningText/);
  assert.match(page, /handleLaneSwitch/);
  assert.match(page, /entryPath:\s*["']LANE_SWITCH["']/);
  assert.match(page, /sourceKeyword:\s*lastKeyword\.value/);
  assert.match(page, /三泳道/);
  assert.match(page, /上游溯源/);
  assert.match(page, /一键寻源/);
  assert.doesNotMatch(page, /查看原材料/);
  assert.match(page, /产业链寻源/);
  assert.match(page, /activeLaneSummary/);
  assert.match(page, /chainMaterial/);
  assert.match(page, /materialRatio/);
  assert.match(page, /class="material-actions"/);
  assert.doesNotMatch(page, /class="product-actions"/);
  assert.doesNotMatch(page, /class="wujin-action-grid"/);
  assert.match(page, /pages\/wujin\/trace-fullscreen/);
  assert.match(page, /原材料商品/);
  assert.match(page, /查看更多/);
});

test("wujin mini search result page removes explanatory copy cards from result list", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.doesNotMatch(page, /class="chain-brief"/);
  assert.doesNotMatch(page, /class="wujin-card chain-guide"/);
  assert.doesNotMatch(page, /chainBriefText/);
  assert.doesNotMatch(page, /chainGuideText/);
});

test("wujin mini search result page does not use tire fallback for unrelated product keyword", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /buildFallbackProducts/);
  assert.match(page, /不锈钢螺丝/);
  assert.match(page, /keywordText\.includes\("不锈钢"\)/);
  assert.doesNotMatch(page, /product-fallback-1["'],\s*name:\s*["']高耐磨乘用车轮胎/);
  assert.doesNotMatch(page, /product-fallback-2["'],\s*name:\s*["']工程车橡胶轮胎/);
});

test("wujin mini search result renders each category level as one unlabeled scrolling row", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /class="category-suggestion-panel"/);
  assert.match(page, /\{\{\s*activeLaneName\s*\}\}分类/);
  assert.match(page, /categorySuggestions/);
  assert.match(page, /searchResult\.value\?\.categoryGroups/);
  assert.match(page, /v-for="group in categoryGroups"/);
  assert.match(page, /<scroll-view[\s\S]*?scroll-x/);
  assert.match(page, /class="category-suggestion-list-inner"/);
  assert.match(page, /v-for="item in group\.categories"/);
  assert.match(page, /\.category-suggestion-list-inner\s*\{[\s\S]*?display:\s*inline-flex/);
  assert.match(page, /\.category-suggestion-chip\s*\{[\s\S]*?flex-shrink:\s*0/);
  assert.doesNotMatch(page, /category-suggestion-level/);
  assert.doesNotMatch(page, /group\.levelName/);
  assert.match(page, /handleCategorySuggestionSelect/);
  assert.match(page, /class="category-suggestion-label"/);
  assert.match(page, /\{\{\s*categorySuggestionLabel\(item\)\s*\}\}/);
  assert.match(page, /function\s+categorySuggestionLabel\(item\s*=\s*\{\}\)/);
  assert.match(page, /categoryPath\.split\(\s*\/\[>＞\/\]\//);
  assert.doesNotMatch(page, /class="category-suggestion-path"/);
  assert.doesNotMatch(page, /class="category-suggestion-lane"/);
  assert.doesNotMatch(page, /laneName\s*\|\|\s*laneDisplayName\(item\.lane\)/);
  assert.match(page, /categoryPath/);
  assert.match(page, /categoryId/);
  assert.match(page, /entryPath:\s*["']CATEGORY_SUGGESTION["']/);
});

test("wujin mini search result falls back to grouping legacy suggestions by level", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /const\s+legacyGroups\s*=\s*new Map\(\)/);
  assert.match(page, /const\s+level\s*=\s*categoryLevel\(item\)/);
  assert.match(
    page,
    /\.map\(\(\[level,\s*categories\]\)\s*=>\s*\(\{\s*level,\s*categories,/,
  );
  assert.doesNotMatch(page, /function\s+categoryLevelName/);
  assert.doesNotMatch(page, /[一二三]级分类/);
});

test("wujin mini search result keeps lane tabs directly below search and scopes categories to the active lane", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.ok(page.indexOf('class="lane-tabs"') < page.indexOf('class="category-suggestion-panel"'));
  assert.match(page, /item\?\.lane\s*===\s*activeLane\.value/);
  assert.match(page, /暂无匹配的\{\{\s*activeLaneName\s*\}\}分类/);
});

test("wujin mini material lane searches and renders real material products", () => {
  const page = read("src/pages/wujin/search-result.vue");
  const detail = read("src/pages/wujin/detail.vue");

  assert.match(page, /class="downstream-section"/);
  assert.match(page, /raw-material-products/);
  assert.match(page, /原材料商品/);
  assert.match(page, /searchResult\.value\?\.relatedProducts/);
  assert.match(page, /relatedProducts\.value\.slice\(0, 8\)/);
  assert.match(page, /activeLane\.value\s*===\s*["']MATERIAL["']/);
  assert.match(page, /recommendFinished\.value\s*=\s*true/);
  assert.match(page, /sourceProductId:\s*extra\.sourceProductId/);
  assert.match(detail, /sourceProductId:\s*entityType\.value\s*===\s*["']PRODUCT["']/);
  assert.match(page, /class="downstream-material"/);
  assert.match(page, /当前分类暂无可售商品/);
  assert.doesNotMatch(page, /fallback-material-/);
});
