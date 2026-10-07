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
    uni: overrides.uni || {},
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

function toPlainObject(value) {
  return JSON.parse(JSON.stringify(value));
}

test("lottery draw page shares loaded lottery activity by lottery id", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const component = loadComponent(filePath, {
    imports: {
      showToast() {},
      getLotteryByActivityId: async () => ({}),
      getLotteryActivity: async () => ({}),
      drawLottery: async () => ({}),
      canDraw: async () => true,
      getWinners: async () => [],
      hasParticipated: async () => false,
      getParticipantCount: async () => 0,
    },
  });
  const instance = createInstance(component);

  instance.activity = { name: "春日幸运抽奖" };
  instance.prizes = [{ imageUrl: "https://cdn.example.com/prize.png" }];
  instance.lotteryId = 42;
  instance.forumActivityId = 9;

  const shareInfo = component.onShareAppMessage.call(instance);

  assert.deepEqual(toPlainObject(shareInfo), {
    title: "春日幸运抽奖",
    path: "/pages/lottery/draw?id=42",
    imageUrl: "https://cdn.example.com/prize.png",
  });
});

test("lottery draw page falls back to activity id before lottery data loads", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const component = loadComponent(filePath, {
    imports: {
      showToast() {},
      getLotteryByActivityId: async () => ({}),
      getLotteryActivity: async () => ({}),
      drawLottery: async () => ({}),
      canDraw: async () => true,
      getWinners: async () => [],
      hasParticipated: async () => false,
      getParticipantCount: async () => 0,
    },
  });
  const instance = createInstance(component);

  instance.activity = {};
  instance.prizes = [];
  instance.lotteryId = null;
  instance.forumActivityId = 108;

  const shareInfo = component.onShareAppMessage.call(instance);

  assert.deepEqual(toPlainObject(shareInfo), {
    title: "幸运抽奖",
    path: "/pages/lottery/draw?activityId=108",
    imageUrl: "",
  });
});
