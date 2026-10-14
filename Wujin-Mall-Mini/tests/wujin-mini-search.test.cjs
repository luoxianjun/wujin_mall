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

test("wujin mini search API wraps lane product paging and silent filter logging", () => {
  const api = read("src/api/wujin/search.js");

  assert.match(api, /export function getWujinLaneProducts/);
  assert.match(api, /\/wujin\/search\/products/);
  assert.match(api, /lane,/);
  assert.match(api, /brandId/);
  assert.match(api, /sort/);
  assert.match(api, /pageNo/);
  assert.match(api, /showLoading:\s*!options\.silent/);
});

test("wujin mini dedicated search result page owns the three-lane result flow", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /defineOptions\(\{\s*name:\s*["']WujinMiniSearchResult["']\s*\}\)/);
  assert.match(page, /searchWujin/);
  assert.match(page, /searchResult\s*=\s*ref\(null\)/);
  assert.match(page, /onLoad\(\(options = \{\}\) =>/);
  assert.match(page, /options\.keyword/);
  assert.match(page, /submitSearch\(\{\s*[\s\S]*entryPath:/);
  assert.match(page, /laneOptions/);
  assert.match(page, /PRODUCT/);
  assert.match(page, /PROCESS/);
  assert.match(page, /MATERIAL/);
  assert.match(page, /defaultLane/);
  assert.match(page, /laneSummaries/);
  assert.match(page, /requestedLane\.value\s*=\s*extra\.requestedLane\s*\|\|\s*["']PRODUCT["']/);
  assert.match(page, /riskWarningRequired/);
  assert.match(page, /handleLaneSwitch/);
  assert.match(page, /entryPath:\s*["']LANE_SWITCH["']/);
  assert.match(page, /三泳道/);
  assert.match(page, /产业链寻源/);
  assert.match(page, /一键寻源/);
  assert.match(page, /pages\/wujin\/trace-fullscreen/);
});

test("wujin mini search result loads products per lane so one product never shows in two tabs", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /getWujinLaneProducts\(\{[\s\S]*?lane:\s*activeLane\.value/);
  assert.match(page, /laneRequestSeq/);
  assert.match(page, /onReachBottom/);
  assert.match(page, /laneFinished\.value\s*=\s*list\.length < LANE_PAGE_SIZE/);
  assert.match(page, /没有更多了/);
  assert.doesNotMatch(page, /getWujinProductRecommendations/);
  assert.doesNotMatch(page, /relatedProducts/);
  assert.doesNotMatch(page, /keywordMatchedProducts/);
});

test("wujin mini search result never fabricates products, counts or upstream materials", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.doesNotMatch(page, /buildFallbackProducts/);
  assert.doesNotMatch(page, /fallback-/);
  assert.doesNotMatch(page, /chainMaterial/);
  assert.doesNotMatch(page, /materialRatio/);
  assert.doesNotMatch(page, /CCC认证/);
  assert.doesNotMatch(page, /绑定4类原材料/);
  assert.doesNotMatch(page, /优选供应商/);
  assert.match(page, /绑定\$\{item\.materialCount\}类原材料/);
  assert.match(page, /laneUpstream/);
});

test("wujin mini search result shows friendly empty states with hot keywords", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /class="search-empty-state"/);
  assert.match(page, /未找到相关结果，试试其他关键词/);
  assert.match(page, /推荐热搜词/);
  assert.match(page, /HOT_WUJIN_SEARCH_KEYWORDS/);
  assert.match(page, /handleHotKeywordSearch/);
  assert.match(page, /暂无上游关联的加工工艺/);
  assert.match(page, /暂无上游关联的原材料/);
  assert.match(page, /laneEmptyHint/);
});

test("wujin mini search result filters by category dropdown and bottom popup without replacing the keyword", () => {
  const page = read("src/pages/wujin/search-result.vue");

  assert.match(page, /class="filter-bar"/);
  assert.match(page, /class="filter-dropdown"/);
  assert.match(page, /class="category-popup"/);
  assert.match(page, /categoryTree/);
  assert.match(page, /node\?\.lane === activeLane\.value/);
  assert.match(page, /popupColumns/);
  assert.match(page, /childLevelName/);
  assert.match(page, /二级分类/);
  assert.match(page, /三级分类/);
  assert.match(page, /handlePopupConfirm/);
  assert.match(page, /handleBrandSelect/);
  assert.match(page, /handleSortSelect/);
  assert.match(page, /categoryId:\s*selectedCategory\.value\?\.categoryId/);
  assert.match(page, /brandId:\s*selectedBrand\.value\?\.brandId/);
  assert.match(page, /entryPath:\s*["']CATEGORY_SUGGESTION["']/);

  const applyCategory = page.slice(page.indexOf("function applyCategory"), page.indexOf("function clearCategory"));
  assert.doesNotMatch(applyCategory, /keyword\.value\s*=/);
  assert.doesNotMatch(applyCategory, /lastKeyword\.value\s*=/);
  assert.doesNotMatch(page, /function handleCategorySuggestionSelect/);
});

test("wujin mini search result opens real product detail by product id", () => {
  const page = read("src/pages/wujin/search-result.vue");
  const home = read("src/pages/wujin/search.vue");

  assert.match(page, /pages\/wujin\/detail\?productId=/);
  assert.match(page, /pages\/wujin\/detail\?id=\$\{encodeURIComponent\(entity\.id/);
  assert.match(home, /pages\/wujin\/detail\?productId=/);
  assert.doesNotMatch(page, /entityType=PRODUCT/);
});
