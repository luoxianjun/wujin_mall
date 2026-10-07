import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath, pathToFileURL } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.resolve(projectRoot, relativePath), "utf8");
}

test("wujin mini search shortcuts expose hot keywords", async () => {
  const shortcutsModule = await import(
    pathToFileURL(
      path.resolve(projectRoot, "src/pages/wujin/search-history.mjs"),
    ).href
  );

  assert.deepEqual(shortcutsModule.HOT_WUJIN_SEARCH_KEYWORDS, [
    "轮胎",
    "天然橡胶",
    "电镀工艺",
    "医用乳胶手套",
    "不锈钢螺栓",
    "轴承",
  ]);
});

test("wujin mini search page renders hot keywords without history or empty intro cards", () => {
  const page = read("src/pages/wujin/search.vue");

  assert.match(page, /HOT_WUJIN_SEARCH_KEYWORDS/);
  assert.match(page, /handleKeywordQuickSearch/);
  assert.match(page, /热门搜索/);
  assert.match(page, /class="hot-keyword-list"/);
  assert.match(page, /class="hot-keyword-row"/);
  assert.match(page, /\.hot-keyword-list\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(page, /\.hot-keyword-row\s*\{[\s\S]*flex-wrap:\s*wrap;/);
  assert.match(page, /\.keyword-chip\.hot\s*\{[\s\S]*background:\s*#eef0f2;/);
  assert.doesNotMatch(page, /searchHistory/);
  assert.doesNotMatch(page, /handleClearSearchHistory/);
  assert.doesNotMatch(page, /最近搜索/);
  assert.doesNotMatch(page, /清空/);
  assert.doesNotMatch(page, /从成品搜起/);
});

test("wujin product recommendations keep two columns inside the viewport", () => {
  const page = read("src/pages/wujin/search.vue");
  const component = read("src/components/wujin/ProductRecommendList.vue");

  assert.match(page, /\.search-content\s*\{[\s\S]*display:\s*flex;/);
  assert.match(page, /\.search-content\s*\{[\s\S]*box-sizing:\s*border-box;/);
  assert.match(page, /\.search-content\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(component, /\.recommend-list\s*\{[\s\S]*max-width:\s*694rpx;/);
  assert.match(component, /\.recommend-grid\s*\{[\s\S]*display:\s*flex;/);
  assert.match(component, /\.recommend-grid\s*\{[\s\S]*width:\s*694rpx;/);
  assert.match(component, /\.recommend-grid\s*\{[\s\S]*box-sizing:\s*border-box;/);
  assert.match(component, /\.recommend-grid\s*\{[\s\S]*overflow:\s*hidden;/);
  assert.match(component, /\.recommend-column\s*\{[\s\S]*flex:\s*0 0 339rpx;/);
  assert.match(component, /\.recommend-column\s*\{[\s\S]*width:\s*339rpx;/);
  assert.match(component, /\.recommend-column\s*\{[\s\S]*min-width:\s*0;/);
  assert.doesNotMatch(component, /grid-template-columns/);
  assert.doesNotMatch(component, /minmax/);
  assert.doesNotMatch(component, /width:\s*calc\(\(100% - 16rpx\) \/ 2\);/);
});

test("wujin product recommendations omit the right black heading badge", () => {
  const component = read("src/components/wujin/ProductRecommendList.vue");

  assert.doesNotMatch(component, /class="recommend-tag"/);
  assert.doesNotMatch(component, /\.recommend-tag\s*\{/);
  assert.doesNotMatch(component, /优选货源/);
});
