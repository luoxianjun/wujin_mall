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

async function flushPromises() {
  await new Promise((resolve) => setImmediate(resolve));
}

test("events page shows admin state on first activation without requiring tab switching", async () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/events/index.vue"
  );
  const listeners = new Map();

  const component = loadComponent(filePath, {
    uni: {
      $on(event, handler) {
        listeners.set(event, handler);
      },
      $off(event) {
        listeners.delete(event);
      },
      getStorageSync(key) {
        if (key === "token") {
          return "mock-token";
        }
        return "";
      },
    },
    imports: {
      PublishModal: {},
      EmptyState: {},
      BottomNavBar: {},
      getActivities: async () => ({
        list: [
          {
            id: 101,
            title: "管理员活动",
            adminMemberIds: [99],
          },
        ],
        total: 1,
      }),
      getMySignUpPage: async () => ({ list: [], total: 0 }),
      getCheckInQrCode: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getUserCenter: async () => ({ schoolEmailVerified: true }),
      drawQrcode() {},
      getActivityTypeDictList: async () => [],
      getActivityTypeDictMap: async () => ({}),
      getUserProfile: () => ({ userId: 99 }),
      statusBarMixin: {},
      pageTransition: {},
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);

  instance.initializeEventsPage();
  await flushPromises();
  instance.activateEventsPage();
  await flushPromises();

  assert.equal(instance.events.length, 1);
  assert.equal(instance.events[0].isAdmin, true);
});
