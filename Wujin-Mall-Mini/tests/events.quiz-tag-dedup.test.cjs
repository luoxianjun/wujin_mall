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

test("quiz category activities do not add a duplicate answer tag", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");
  const component = loadComponent(filePath, {
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
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.activityTypeMap = {
    12: { label: "答题" },
  };

  const tags = instance.getTags({
    category: 12,
    categoryName: "答题",
    hasQuiz: true,
    statusName: "进行中",
  });

  const labels = tags.map((tag) => tag.label);

  assert.equal(labels.length, 1);
  assert.equal(labels[0], "进行中");
});

test("quiz category activities do not expose check-in entry", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");
  const component = loadComponent(filePath, {
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
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.activityTypeMap = {
    12: { label: "答题" },
  };

  const event = instance.formatEvent({
    id: 301,
    title: "答题答题",
    category: 12,
    categoryName: "答题",
    hasQuiz: true,
    quizActivityId: 901,
    signedUp: true,
    approvalStatus: 1,
    needCheckIn: true,
    checkInType: 1,
    checkInStartTime: "2026-04-27 10:00:00",
    checkInEndTime: "2026-04-30 10:00:00",
  });

  assert.equal(event.hasQuiz, true);
  assert.equal(event.needCheckIn, false);
  assert.equal(event.checkedIn, false);
});

test("unsigned quiz activities that require signup open registration first", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");
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
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);
  instance.activityTypeMap = {
    12: { label: "答题" },
  };
  instance.ensureLogin = () => true;
  instance.checkVerificationForActivity = () => true;

  const event = instance.formatEvent({
    id: 302,
    title: "需要报名的答题",
    category: 12,
    categoryName: "答题",
    hasQuiz: true,
    quizActivityId: 902,
    signedUp: false,
    signUpStartTime: "2026-04-27 10:00:00",
    signUpEndTime: "2026-04-30 10:00:00",
  });

  instance.onGoQuiz(event);

  assert.equal(navigations.length, 1);
  assert.equal(navigations[0].url, "/pages/events/register?id=302");
});
