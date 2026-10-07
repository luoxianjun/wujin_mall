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
  "../src/pages/events-sub/quiz-result.vue",
  "../src/pages/events/quiz-result.vue",
].forEach((relativePath) => {
  test(`${relativePath} hides leaderboard button when leaderboard size is zero`, () => {
    const filePath = path.resolve(__dirname, relativePath);
    const source = fs.readFileSync(filePath, "utf8");
    const component = loadComponent(filePath, {
      imports: {
        formatQuizElapsed() {
          return "0 分 5 秒";
        },
        getQuizResult: async () => ({}),
        showToast() {},
        statusBarMixin: {},
      },
    });
    const instance = createInstance(component);

    instance.result = { leaderboardSize: 0 };

    assert.equal(instance.canShowLeaderboard, false);
    assert.match(source, /class="ghost-btn"\s+v-if="canShowLeaderboard"/);
  });

  test(`${relativePath} returns to quiz detail from result page`, () => {
    const redirects = [];
    const filePath = path.resolve(__dirname, relativePath);
    const component = loadComponent(filePath, {
      uni: {
        redirectTo(options) {
          redirects.push(options);
        },
      },
      imports: {
        formatQuizElapsed() {
          return "0 分 5 秒";
        },
        getQuizResult: async () => ({}),
        showToast() {},
        statusBarMixin: {},
      },
    });
    const instance = createInstance(component);
    instance.activityId = "301";
    instance.quizActivityId = "901";

    instance.goActivity();

    assert.equal(redirects.length, 1);
    assert.equal(
      redirects[0].url,
      "/pages/events-sub/quiz-detail?id=301&quizActivityId=901"
    );
  });
});
