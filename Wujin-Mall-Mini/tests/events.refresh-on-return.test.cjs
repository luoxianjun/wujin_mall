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

test("events page can force reload when returning from detail", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");

  const component = loadComponent(filePath, {
    uni: {
      $on() {},
      $off() {},
      getStorageSync() {
        return "";
      },
    },
    getApp: () => ({ globalData: { unreadCount: 0 } }),
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
      getUserProfile: () => ({ userId: 99 }),
      statusBarMixin: {},
      pageTransition: {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage: () => false,
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  let resetCount = 0;

  instance.hasLoadedEvents = true;
  instance.currentUserId = 99;
  instance.loadActivityTypes = () => {};
  instance.checkUserVerified = () => {};
  instance.resetList = () => {
    resetCount += 1;
  };

  instance.activateEventsPage({ forceReload: true });

  assert.equal(resetCount, 1);
});

test("tab shell refreshes embedded events page on page show", () => {
  const filePath = path.resolve(__dirname, "../src/pages/tab/index.vue");

  const component = loadComponent(filePath, {
    uni: {
      $on() {},
      $off() {},
    },
    getApp: () => ({ globalData: { unreadCount: 3 } }),
    imports: {
      BottomNavBar: {},
      PublishModal: {},
      HomePage: {},
      EventsPage: {},
      ProfilePage: {},
      showToast() {},
    },
  });

  const instance = createInstance(component);
  let calls = 0;
  let lastArgs = null;

  instance.activeTab = "events";
  instance.$refs = {
    eventsPage: {
      activateEventsPage(args) {
        calls += 1;
        lastArgs = args;
      },
    },
  };

  component.onShow.call(instance);

  assert.equal(instance.unreadCount, 3);
  assert.equal(calls, 1);
  assert.equal(lastArgs.forceReload, true);
});
