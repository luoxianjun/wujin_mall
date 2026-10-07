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

test("openEvent routes lottery activities to the rich draw page", async () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/events/index.vue"
  );
  const navigations = [];

  const component = loadComponent(filePath, {
    uni: {
      navigateTo(options) {
        navigations.push(options);
      },
    },
    imports: {
      PublishModal: {},
      EmptyState: {},
      BottomNavBar: {},
      getActivities: async () => [],
      checkInActivity: async () => ({}),
      getCheckInQrCode: async () => ({}),
      cancelSignUp: async () => ({}),
      getUserCenter: async () => ({}),
      getActivity: async () => ({
        id: 42,
        signedUp: true,
        approvalStatus: 1,
        needApproval: true,
        signUpStartTime: "2026-04-01 10:00:00",
        signUpEndTime: "2026-04-30 18:00:00",
      }),
      getLotteryByActivityId: async () => ({
        activity: {
          id: 99,
          activityId: 42,
          participationCondition: 0,
        },
      }),
      drawQrcode() {},
      getActivityTypeDictList: async () => [],
      getActivityTypeDictMap: async () => ({}),
      getUserProfile: () => ({ userId: 1 }),
      statusBarMixin: {},
      pageTransition: {},
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.ensureLogin = () => true;
  instance.checkVerificationForActivity = () => true;
  instance.onGoQuiz = () => {
    throw new Error("quiz route should not be used for lottery activities");
  };

  await instance.openEvent({
    id: 42,
    category: 11,
    hasQuiz: false,
    signUpRange: "04-01 10:00 - 04-30 18:00",
    signedUp: true,
    isApproved: true,
    needApproval: true,
  });

  assert.equal(navigations.length, 1);
  assert.equal(
    navigations[0].url,
    "/pages/lottery/draw?activityId=42"
  );
});

test("openLotteryEntry routes unsigned users to register when linked activity requires sign-up approval", async () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/events/index.vue"
  );
  const navigations = [];

  const component = loadComponent(filePath, {
    uni: {
      navigateTo(options) {
        navigations.push(options);
      },
    },
    imports: {
      PublishModal: {},
      EmptyState: {},
      BottomNavBar: {},
      getActivities: async () => [],
      getMySignUpPage: async () => ({ list: [] }),
      getCheckInQrCode: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getUserCenter: async () => ({}),
      getActivity: async () => ({
        id: 42,
        signedUp: false,
        approvalStatus: null,
        needApproval: true,
        signUpStartTime: "2026-04-01 10:00:00",
        signUpEndTime: "2026-04-30 18:00:00",
      }),
      getLotteryByActivityId: async () => ({
        activity: {
          id: 99,
          activityId: 42,
          participationCondition: 0,
        },
      }),
      drawQrcode() {},
      getActivityTypeDictList: async () => [],
      getActivityTypeDictMap: async () => ({}),
      getUserProfile: () => ({ userId: 1 }),
      statusBarMixin: {},
      pageTransition: {},
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.ensureLogin = () => true;
  instance.checkVerificationForActivity = () => true;

  await instance.openLotteryEntry({
    id: 42,
    category: 11,
    signUpRange: "04-01 10:00 - 04-30 18:00",
    signedUp: false,
    isApproved: false,
    needApproval: true,
  });

  assert.equal(navigations.length, 1);
  assert.equal(
    navigations[0].url,
    "/pages/events/register?id=42&fromLottery=1"
  );
});
