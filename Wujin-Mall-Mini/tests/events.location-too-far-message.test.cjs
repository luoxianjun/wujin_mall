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

  Object.entries(component.methods || {}).forEach(([name, method]) => {
    instance[name] = method.bind(instance);
  });

  return instance;
}

test("events list location check-in shows the unified too-far message", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");
  const toastMessages = [];

  const component = loadComponent(filePath, {
    uni: {
      showLoading() {},
      hideLoading() {},
    },
    imports: {
      PublishModal: {},
      EmptyState: {},
      BottomNavBar: {},
      getActivities: async () => ({ list: [], total: 0 }),
      getMySignUpPage: async () => ({ list: [], total: 0 }),
      getCheckInQrCode: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getUserCenter: async () => ({}),
      drawQrcode() {},
      getActivityTypeDictList: async () => [],
      getActivityTypeDictMap: async () => ({}),
      getUserProfile: () => ({ userId: 1 }),
      statusBarMixin: {},
      pageTransition: {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage(message) {
        const text = String(message || "");
        return (
          text.includes("签到位置过远") ||
          text.includes("签到位置距离活动地点过远")
        );
      },
      getQuizUnavailableReason: () => "",
      showToast(message) {
        toastMessages.push(message);
      },
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.getLocation = async () => ({ latitude: 30, longitude: 120 });
  instance.calculateDistance = () => 18282;

  await instance.doDirectCheckIn({
    id: 1,
    checkInType: 2,
    latitude: 31,
    longitude: 121,
    checkInDistance: 100,
  });

  assert.deepEqual(toastMessages, ["签到位置过远"]);
});

test("event detail location check-in normalizes backend too-far message", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const toastMessages = [];

  const component = loadComponent(filePath, {
    uni: {
      showLoading() {},
      hideLoading() {},
      getSetting(options) {
        options.success({ authSetting: {} });
      },
      getLocation(options) {
        options.success({ latitude: 30, longitude: 120 });
      },
      showModal() {},
      openSetting() {},
      getSystemInfoSync() {
        return { windowWidth: 375 };
      },
    },
    imports: {
      getActivity: async () => ({}),
      signUpActivity: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => {
        throw { msg: "签到位置距离活动地点过远" };
      },
      getCheckInQrCode: async () => ({}),
      getQuizLeaderboard: async () => ({}),
      getUserCenter: async () => ({}),
      statusBarMixin: {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage(message) {
        const text = String(message || "");
        return (
          text.includes("签到位置过远") ||
          text.includes("签到位置距离活动地点过远")
        );
      },
      drawQrcode() {},
      showToast(message) {
        toastMessages.push(message);
      },
      showSuccess() {},
      wx: {
        getMenuButtonBoundingClientRect() {
          return { left: 300 };
        },
      },
    },
  });

  const instance = createInstance(component);
  instance.eventId = "123";

  await instance.doLocationCheckIn();

  assert.deepEqual(toastMessages, ["签到位置过远"]);
});
