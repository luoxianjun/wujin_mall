const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

function loadMixin(filePath, overrides = {}) {
  const source = fs.readFileSync(filePath, "utf8");
  const scriptContent = source
    .replace(/export\s+const\s+(\w+)\s*=/g, "const $1 =")
    .replace(/export\s+function\s+(\w+)/g, "function $1")
    .replace(/export default/, "module.exports =");

  const sandbox = {
    module: { exports: {} },
    exports: {},
    console: overrides.console || console,
    uni: overrides.uni || {},
    getCurrentPages: overrides.getCurrentPages,
  };

  vm.runInNewContext(scriptContent, sandbox, { filename: filePath });
  return sandbox.module.exports;
}

test("main app registers the default share mixin globally", () => {
  const mainPath = path.resolve(__dirname, "../src/main.js");
  const source = fs.readFileSync(mainPath, "utf8");

  assert.match(source, /import defaultShareMixin from ["']@\/mixins\/defaultShare["']/);
  assert.match(source, /app\.mixin\(defaultShareMixin\)/);
});

test("default share mixin exposes friend and timeline share for current page", () => {
  const mixinPath = path.resolve(__dirname, "../src/mixins/defaultShare.js");
  const showShareMenuCalls = [];
  const mixin = loadMixin(mixinPath, {
    uni: {
      showShareMenu(options) {
        showShareMenuCalls.push(options);
      },
    },
    getCurrentPages: () => [
      {
        route: "pages/profile/settings",
        options: {
          tab: "privacy",
          keyword: "校园 活动",
          empty: "",
          ignored: undefined,
        },
      },
    ],
  });

  mixin.onLoad.call({});

  assert.deepEqual(JSON.parse(JSON.stringify(showShareMenuCalls)), [
    { menus: ["shareAppMessage", "shareTimeline"] },
  ]);
  assert.deepEqual(JSON.parse(JSON.stringify(mixin.onShareAppMessage.call({}))), {
    title: "荟星Planet",
    path: "/pages/profile/settings?tab=privacy&keyword=%E6%A0%A1%E5%9B%AD%20%E6%B4%BB%E5%8A%A8&empty=",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(mixin.onShareTimeline.call({}))), {
    title: "荟星Planet",
    query: "tab=privacy&keyword=%E6%A0%A1%E5%9B%AD%20%E6%B4%BB%E5%8A%A8&empty=",
  });
});

test("default share mixin falls back to the tab page when route is unavailable", () => {
  const mixinPath = path.resolve(__dirname, "../src/mixins/defaultShare.js");
  const mixin = loadMixin(mixinPath, {
    getCurrentPages: () => [],
  });

  assert.deepEqual(JSON.parse(JSON.stringify(mixin.onShareAppMessage.call({}))), {
    title: "荟星Planet",
    path: "/pages/tab/index",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(mixin.onShareTimeline.call({}))), {
    title: "荟星Planet",
    query: "",
  });
});
