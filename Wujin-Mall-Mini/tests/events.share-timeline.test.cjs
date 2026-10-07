const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

function loadComponent(filePath, overrides = {}) {
  const source = fs.readFileSync(filePath, "utf8");
  const match = source.match(/<script>([\s\S]*?)<\/script>/);

  assert.ok(match, `Expected <script> block in ${filePath}`);

  const scriptContent = match[1]
    .replace(/import\s+[\s\S]*?\s+from\s+["'][^"']+["'];?\s*/g, "")
    .replace(/export default/, "module.exports =");

  const sandbox = {
    module: { exports: {} },
    exports: {},
    console: overrides.console || console,
    uni: overrides.uni,
    wx: overrides.wx || {
      getMenuButtonBoundingClientRect() {
        return {};
      },
    },
    getCurrentPages: overrides.getCurrentPages || (() => []),
    setTimeout,
    clearTimeout,
    setInterval,
    clearInterval,
    ...overrides.imports,
  };

  vm.runInNewContext(scriptContent, sandbox, { filename: filePath });
  return sandbox.module.exports;
}

function createInstance(component) {
  const instance = {
    ...(typeof component.data === "function" ? component.data() : {}),
  };

  Object.entries(component.methods || {}).forEach(([name, method]) => {
    instance[name] = method.bind(instance);
  });

  return instance;
}

test("activity register page configures WeChat timeline sharing", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const shareMenuCalls = [];
  const component = loadComponent(filePath, {
    uni: {
      showShareMenu(options) {
        shareMenuCalls.push(options);
      },
      getStorageSync() {
        return "";
      },
    },
    imports: {
      getActivity: async () => ({}),
      signUpActivity: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getCheckInQrCode: async () => ({}),
      getQuizLeaderboard: async () => ({}),
      getUserCenter: async () => ({}),
      statusBarMixin: {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage: () => false,
      drawQrcode() {},
      showToast() {},
      showSuccess() {},
    },
  });
  const instance = createInstance(component);
  instance.loadEventDetail = () => {};
  instance.checkVerificationStatus = () => {};

  component.onLoad.call(instance, { id: "308" });

  assert.deepEqual(JSON.parse(JSON.stringify(shareMenuCalls)), [
    { menus: ["shareAppMessage", "shareTimeline"] },
  ]);
});

test("activity register page shares activity details to timeline", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const component = loadComponent(filePath, {
    uni: {},
    imports: {
      getActivity: async () => ({}),
      signUpActivity: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getCheckInQrCode: async () => ({}),
      getQuizLeaderboard: async () => ({}),
      getUserCenter: async () => ({}),
      statusBarMixin: {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage: () => false,
      drawQrcode() {},
      showToast() {},
      showSuccess() {},
    },
  });
  const instance = createInstance(component);
  instance.eventId = "308";
  instance.event = {
    title: "校园音乐节",
    cover: "https://cdn.example.com/activity-cover.jpg",
  };

  const shareInfo = component.onShareTimeline.call(instance);

  assert.deepEqual(JSON.parse(JSON.stringify(shareInfo)), {
    title: "校园音乐节",
    query: "id=308",
    imageUrl: "https://cdn.example.com/activity-cover.jpg",
  });
});

test("activity register timeline mode keeps native navigation from covering the header", () => {
  const pagesJsonPath = path.resolve(__dirname, "../src/pages.json");
  const pagesJson = JSON.parse(fs.readFileSync(pagesJsonPath, "utf8"));
  const registerPage = pagesJson.pages.find(
    (page) => page.path === "pages/events/register"
  );

  assert.equal(
    registerPage?.style?.singlePage?.navigationBarFit,
    "squeezed"
  );
});
