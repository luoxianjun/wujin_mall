const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

function loadLoginComponent(overrides = {}) {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/users/login.vue"
  );
  const source = fs.readFileSync(filePath, "utf8");
  const match = source.match(/<script>([\s\S]*?)<\/script>/);

  assert.ok(match, "Expected <script> block in login.vue");

  const scriptContent = match[1]
    .replace(/^import\s+.*$/gm, "")
    .replace(/export default/, "module.exports =");

  const sandbox = {
    module: { exports: {} },
    exports: {},
    console: overrides.console || console,
    uni: overrides.uni,
    getCurrentPages: overrides.getCurrentPages || (() => []),
    getApp: overrides.getApp || (() => ({ globalData: {} })),
    setTimeout: overrides.setTimeout || ((fn) => {
      fn();
      return 1;
    }),
    clearTimeout: overrides.clearTimeout || (() => {}),
    setInterval: overrides.setInterval || (() => 1),
    clearInterval: overrides.clearInterval || (() => {}),
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

test("finishLogin logs in to IM when profile has userSig", async () => {
  let imLoginCalls = 0;
  let imLogoutCalls = 0;
  let unreadWatcherSetupCalls = 0;
  const errors = [];

  const TUILogin = {
    getContext() {
      return { chat: { isReady: () => true } };
    },
    async logout() {
      imLogoutCalls += 1;
    },
    async login() {
      imLoginCalls += 1;
    },
  };

  const component = loadLoginComponent({
    console: {
      log() {},
      warn() {},
      error(...args) {
        errors.push(args);
      },
    },
    uni: {
      $SDKAppID: 123456,
      $userID: null,
      $imLoginPromise: null,
      getStorageSync() {
        return "";
      },
      removeStorageSync() {},
      getSystemInfoSync() {
        return {};
      },
      reLaunch() {},
      navigateTo() {},
    },
    imports: {
      loginByWeixinPhone: async () => ({}),
      sendLoginSmsCode: async () => ({}),
      smsLogin: async () => ({}),
      getUserCenter: async () => ({
        userId: 9527,
        userSig: "test-user-sig",
        schoolEmailVerified: true,
      }),
      getUserProfileDetail: async () => null,
      registerWithInvitationCode: async () => ({}),
      saveAuthTokens() {},
      saveUserProfile() {},
      waitForSDKReady: async () => {},
      getIMModules() {
        return {
          TUILogin,
          TUIChatEngine: {
            isReady() {
              return true;
            },
          },
        };
      },
      setupGlobalIMUnreadWatcher() {
        unreadWatcherSetupCalls += 1;
      },
      showToast() {},
      showSuccess() {},
    },
  });

  const instance = createInstance(component);

  await instance.finishLogin({ accessToken: "token" });

  assert.equal(imLogoutCalls, 1, "expected previous IM session to be cleaned up");
  assert.equal(imLoginCalls, 1, "expected IM login to execute");
  assert.equal(unreadWatcherSetupCalls, 1, "expected unread watcher to be reattached");
  assert.equal(errors.length, 0, `expected no IM login errors, got: ${JSON.stringify(errors)}`);
});
