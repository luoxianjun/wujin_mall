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
    getCurrentPages: overrides.getCurrentPages || (() => []),
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

[
  "../src/pages/events-sub/quiz-leaderboard.vue",
  "../src/pages/events/quiz-leaderboard.vue",
].forEach((relativePath) => {
  test(`${relativePath} returns to quiz detail when it exists in page stack`, () => {
    const calls = [];
    const filePath = path.resolve(__dirname, relativePath);
    const component = loadComponent(filePath, {
      imports: {
        EmptyState: {},
        statusBarMixin: {},
        formatQuizElapsed() {
          return "0 分 5 秒";
        },
        getQuizLeaderboard: async () => ({}),
        showToast() {},
        uni: {
          navigateBack(options) {
            calls.push({ type: "navigateBack", ...options });
          },
          redirectTo(options) {
            calls.push({ type: "redirectTo", ...options });
          },
          reLaunch(options) {
            calls.push({ type: "reLaunch", ...options });
          },
        },
      },
      getCurrentPages: () => [
        { route: "pages/events/register" },
        { route: "pages/events-sub/quiz-detail" },
        { route: "pages/events-sub/quiz-leaderboard" },
      ],
    });
    const instance = createInstance(component);
    instance.activityId = "301";
    instance.quizActivityId = "901";

    instance.goBack();

    assert.deepEqual(calls[0], { type: "navigateBack", delta: 1, fail: calls[0].fail });
  });

  test(`${relativePath} falls back to quiz detail instead of activity registration`, () => {
    const calls = [];
    const filePath = path.resolve(__dirname, relativePath);
    const component = loadComponent(filePath, {
      imports: {
        EmptyState: {},
        statusBarMixin: {},
        formatQuizElapsed() {
          return "0 分 5 秒";
        },
        getQuizLeaderboard: async () => ({}),
        showToast() {},
        uni: {
          navigateBack(options) {
            calls.push({ type: "navigateBack", ...options });
          },
          redirectTo(options) {
            calls.push({ type: "redirectTo", ...options });
          },
          reLaunch(options) {
            calls.push({ type: "reLaunch", ...options });
          },
        },
      },
      getCurrentPages: () => [{ route: "pages/events-sub/quiz-leaderboard" }],
    });
    const instance = createInstance(component);
    instance.activityId = "301";
    instance.quizActivityId = "901";

    instance.goBack();

    assert.deepEqual(calls, [
      {
        type: "redirectTo",
        url: "/pages/events-sub/quiz-detail?id=301&quizActivityId=901",
      },
    ]);
  });
});
