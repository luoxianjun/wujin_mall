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

  Object.entries(component.computed || {}).forEach(([name, getter]) => {
    Object.defineProperty(instance, name, {
      enumerable: true,
      get: getter.bind(instance),
    });
  });

  return instance;
}

function toPlainObject(value) {
  return JSON.parse(JSON.stringify(value));
}

function createLotteryImports(overrides = {}) {
  return {
    showToast() {},
    getActivity: async () => ({}),
    getLotteryByActivityId: async () => ({}),
    getLotteryActivity: async () => ({}),
    drawLottery: async () => ({}),
    canDraw: async () => true,
    getWinners: async () => [],
    hasParticipated: async () => false,
    getParticipantCount: async () => 0,
    getMyRecords: async () => [],
    ...overrides,
  };
}

test("scheduled lottery draw resolves the latest personal result after draw ends", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const component = loadComponent(filePath, {
    imports: createLotteryImports({
      getMyRecords: async () => [
        { id: 10, won: false, prizeName: "谢谢参与", drawTime: "2026-04-22 20:00:00" },
        { id: 12, won: true, prizeName: "100积分", drawTime: "2026-04-22 21:00:00" },
      ],
    }),
  });
  const instance = createInstance(component);

  instance.activity = { type: 0 };
  instance.drawEnded = true;
  instance.lotteryId = 88;

  await instance.loadMyDrawResult();

  assert.deepEqual(toPlainObject(instance.myDrawRecord), {
    id: 12,
    won: true,
    prizeName: "100积分",
    drawTime: "2026-04-22 21:00:00",
  });
  assert.deepEqual(toPlainObject(instance.scheduledResultMeta), {
    badge: "已开奖",
    detail: "100积分",
    icon: "🎉",
    title: "恭喜你中奖了",
    won: true,
  });
});

test("scheduled lottery draw uses the requested losing copy for non-winning users", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const component = loadComponent(filePath, {
    imports: createLotteryImports(),
  });
  const instance = createInstance(component);

  instance.activity = { type: 0 };
  instance.drawEnded = true;
  instance.myDrawRecord = {
    id: 9,
    won: false,
    prizeName: "谢谢参与",
  };
  instance.myDrawResultLoaded = true;

  assert.deepEqual(toPlainObject(instance.scheduledResultMeta), {
    badge: "已开奖",
    detail: "查看下方记录了解结果",
    icon: "📭",
    title: "很遗憾未中奖",
    won: false,
  });
});

test("scheduled lottery draw template renders personalized result copy in the ended state", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const source = fs.readFileSync(filePath, "utf8");

  assert.match(source, /scheduledResultMeta\.title/);
  assert.match(source, /scheduledResultMeta\.detail/);
});

test("lottery draw shows a registration detail shortcut between prizes and rules", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  const source = fs.readFileSync(filePath, "utf8");

  const prizeIndex = source.indexOf("<!-- ===== 奖品一览 ===== -->");
  const shortcutIndex = source.indexOf("返回报名详情");
  const rulesIndex = source.indexOf("<!-- ===== 活动规则 ===== -->");

  assert.ok(prizeIndex >= 0, "expected prize section");
  assert.ok(shortcutIndex > prizeIndex, "expected shortcut after prize section");
  assert.ok(rulesIndex > shortcutIndex, "expected shortcut before rules section");
  assert.match(source, /@click="goRegistrationDetail"/);
});

test("lottery draw registration detail shortcut opens the linked event detail", () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  let navigatedUrl = "";
  const component = loadComponent(filePath, {
    imports: createLotteryImports(),
    uni: {
      navigateTo({ url }) {
        navigatedUrl = url;
      },
    },
  });
  const instance = createInstance(component);

  instance.activity = { activityId: 321 };
  instance.forumActivityId = 99;
  instance.goRegistrationDetail();

  assert.equal(navigatedUrl, "/pages/events/register?id=321&viewRegistration=1");
});

test("lottery draw scheduled participation keeps the lottery registration flow flag", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/lottery/draw.vue");
  let navigatedUrl = "";
  const component = loadComponent(filePath, {
    imports: createLotteryImports(),
    uni: {
      navigateTo({ url }) {
        navigatedUrl = url;
      },
    },
  });
  const instance = createInstance(component);

  instance.activity = { activityId: 321 };
  await instance.joinLottery();

  assert.equal(navigatedUrl, "/pages/events/register?id=321&fromLottery=1");
});
