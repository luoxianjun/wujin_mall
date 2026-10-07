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
    setTimeout: overrides.setTimeout || setTimeout,
    clearTimeout: overrides.clearTimeout || clearTimeout,
    setInterval: overrides.setInterval || setInterval,
    clearInterval: overrides.clearInterval || clearInterval,
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

test("ensureLotteryAccess blocks direct draw entry when linked activity requires approved sign-up", async () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/lottery/draw.vue"
  );
  const redirects = [];
  const toasts = [];

  const component = loadComponent(filePath, {
    uni: {
      redirectTo(options) {
        redirects.push(options);
      },
    },
    setTimeout(fn) {
      fn();
      return 1;
    },
    clearTimeout() {},
    imports: {
      getActivity: async () => ({
        id: 42,
        signedUp: false,
        approvalStatus: null,
        needApproval: true,
        signUpStartTime: "2026-04-01 10:00:00",
        signUpEndTime: "2026-04-30 18:00:00",
      }),
      getLotteryByActivityId: async () => ({}),
      getLotteryActivity: async () => ({}),
      drawLottery: async () => ({}),
      canDraw: async () => false,
      getWinners: async () => [],
      hasParticipated: async () => false,
      getParticipantCount: async () => 0,
      showToast(message) {
        toasts.push(message);
      },
    },
  });

  const instance = createInstance(component);
  instance.activity = {
    id: 99,
    activityId: 42,
    participationCondition: 0,
  };
  instance.forumActivityId = 42;

  const allowed = await instance.ensureLotteryAccess();

  assert.equal(allowed, false);
  assert.deepEqual(toasts, ["请先填写报名信息后再抽奖"]);
  assert.equal(redirects.length, 1);
  assert.equal(
    redirects[0].url,
    "/pages/events/register?id=42&fromLottery=1"
  );
});
