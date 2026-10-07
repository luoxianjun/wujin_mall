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
    getApp: overrides.getApp || (() => ({ globalData: {} })),
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

  Object.entries(component.computed || {}).forEach(([name, getter]) => {
    Object.defineProperty(instance, name, {
      get: getter.bind(instance),
      configurable: true,
    });
  });

  Object.entries(component.methods || {}).forEach(([name, method]) => {
    instance[name] = method.bind(instance);
  });

  return instance;
}

test("successful quiz activity registration replaces the registration page with quiz detail", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const navigations = [];
  const redirects = [];

  const component = loadComponent(filePath, {
    uni: {
      getStorageSync(key) {
        return key === "token" ? "mock-token" : "";
      },
      showLoading() {},
      hideLoading() {},
      navigateTo(options) {
        navigations.push(options);
      },
      redirectTo(options) {
        redirects.push(options);
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
  instance.eventId = "301";
  instance.quizActivityId = "901";
  instance.event = { full: false };
  instance.form.agree = true;
  instance.customFields = [];
  instance.needApproval = false;
  instance.allowUnverified = true;
  instance.canSignUp = true;
  instance.signUpClosed = false;
  instance.isEnded = false;
  instance.isCancelled = false;
  instance.fromLottery = false;
  instance.requestSubscribeMessage = async () => {};

  await instance.submitRegistration();

  assert.equal(instance.status, "unregistered");
  assert.equal(navigations.length, 0);
  assert.equal(redirects.length, 1);
  assert.equal(
    redirects[0].url,
    "/pages/events-sub/quiz-detail?id=301&quizActivityId=901"
  );
});

test("successful lottery activity registration redirects to the draw page without entry flag", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const redirects = [];

  const component = loadComponent(filePath, {
    uni: {
      getStorageSync(key) {
        return key === "token" ? "mock-token" : "";
      },
      showLoading() {},
      hideLoading() {},
      navigateTo() {},
      redirectTo(options) {
        redirects.push(options);
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
  instance.eventId = "42";
  instance.event = instance.formatEventDetail({
    id: 42,
    category: 11,
    title: "幸运抽奖",
  });
  instance.form.agree = true;
  instance.customFields = [];
  instance.needApproval = false;
  instance.allowUnverified = true;
  instance.canSignUp = true;
  instance.signUpClosed = false;
  instance.isEnded = false;
  instance.isCancelled = false;
  instance.fromLottery = false;
  instance.requestSubscribeMessage = async () => {};

  await instance.submitRegistration();
  await new Promise((resolve) => setTimeout(resolve, 350));

  assert.equal(instance.status, "registered");
  assert.equal(redirects.length, 1);
  assert.equal(redirects[0].url, "/pages/lottery/draw?activityId=42");
});
