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

test("leaderboard page shows unavailable state when leaderboard size is zero", async () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/events-sub/quiz-leaderboard.vue"
  );
  const component = loadComponent(filePath, {
    imports: {
      EmptyState: {},
      statusBarMixin: {},
      formatQuizElapsed() {
        return "0 分 5 秒";
      },
      getQuizLeaderboard: async () => ({
        leaderboardSize: 0,
        myRank: { rank: 1, userId: 31, score: 90, elapsedMillis: 5000 },
        rankings: [{ rank: 1, userId: 31, score: 90, elapsedMillis: 5000 }],
      }),
      showToast() {},
    },
  });

  const instance = createInstance(component);
  instance.quizActivityId = "3";

  await instance.loadLeaderboard();

  assert.equal(instance.leaderboardSize, 0);
  assert.equal(instance.isLeaderboardHidden, true);
  assert.equal(instance.rankings.length, 0);
  assert.equal(instance.myRank, null);
  assert.equal(instance.emptyMessage, "排行榜暂时不可见");
});

test("activity formatter preserves zero leaderboard size", () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/index.vue");
  const component = loadComponent(filePath, {
    imports: {
      PublishModal: {},
      EmptyState: {},
      BottomNavBar: {},
      getActivities: async () => ({ list: [], total: 0 }),
      getActivity: async () => ({}),
      getMySignUpPage: async () => ({ list: [], total: 0 }),
      getCheckInQrCode: async () => ({}),
      cancelSignUp: async () => ({}),
      checkInActivity: async () => ({}),
      getUserCenter: async () => ({}),
      getLotteryByActivityId: async () => ({}),
      drawQrcode() {},
      getActivityTypeDictList: async () => [],
      getActivityTypeDictMap: async () => ({}),
      getUserProfile: () => ({ userId: 99 }),
      statusBarMixin: {},
      pageTransition: {},
      getQuizUnavailableReason: () => "",
      showToast() {},
      showSuccess() {},
      LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
      isLocationTooFarErrorMessage: () => false,
    },
  });

  const instance = createInstance(component);
  const event = instance.formatEvent({
    id: 301,
    title: "隐藏排行榜答题",
    category: 12,
    hasQuiz: true,
    quizActivityId: 901,
    quizLeaderboardSize: 0,
  });

  assert.equal(event.quizLeaderboardSize, 0);
});
