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

test("wujin mini uses search as the c-end first page while keeping legacy pages available", () => {
  const pages = readJson("src/pages.json");

  assert.equal(pages.pages[0].path, "pages/wujin/search");
  assert.ok(pages.pages.some((item) => item.path === "pages/events/index"));
  assert.ok(pages.subPackages.some((item) => item.root === "pages/messages"));
});

test("wujin mini does not preload forum IM packages in the main flow", () => {
  const pages = readJson("src/pages.json");
  const preloadRule = pages.preloadRule || {};

  assert.equal(preloadRule["pages/messages/index"], undefined);
  assert.deepEqual(preloadRule["pages/wujin/search"]?.packages || [], []);
  assert.doesNotMatch(JSON.stringify(preloadRule), /TUIKit/);
});

test("bottom nav hides forum activity IM and publish entries from visible navigation", () => {
  const bottomNav = read("src/components/BottomNavBar.vue");

  assert.match(bottomNav, /label:\s*["']五金["']/);
  assert.match(bottomNav, /label:\s*["']寻源["']/);
  assert.match(bottomNav, /label:\s*["']溯源["']/);
  assert.doesNotMatch(bottomNav, /label:\s*["']活动["']/);
  assert.doesNotMatch(bottomNav, /label:\s*["']消息["']/);
  assert.doesNotMatch(bottomNav, /label:\s*["']发布["']/);
  assert.doesNotMatch(bottomNav, /\/pages\/events\/index/);
  assert.doesNotMatch(bottomNav, /\/pages\/messages\/index/);
});
