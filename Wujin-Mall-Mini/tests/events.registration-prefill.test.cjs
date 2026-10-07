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
    $set(target, key, value) {
      target[key] = value;
    },
    $nextTick(fn) {
      fn();
    },
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

function createRegisterImports(overrides = {}) {
  return {
    getActivity: async () => ({}),
    signUpActivity: async () => ({}),
    cancelSignUp: async () => ({}),
    checkInActivity: async () => ({}),
    getCheckInQrCode: async () => ({}),
    getMySignUpPage: async () => ({ list: [], total: 0 }),
    uploadFile: async () => "",
    getQuizLeaderboard: async () => ({}),
    getUserCenter: async () => ({}),
    statusBarMixin: {},
    LOCATION_TOO_FAR_MESSAGE: "签到位置过远",
    isLocationTooFarErrorMessage: () => false,
    drawQrcode() {},
    showToast() {},
    showSuccess() {},
    ...overrides,
  };
}

function toPlainObject(value) {
  return JSON.parse(JSON.stringify(value));
}

test("registration page backfills submitted form data from my signup record when activity detail omits the remark", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const component = loadComponent(filePath, {
    uni: {
      showLoading() {},
      hideLoading() {},
      getStorageSync() {
        return "";
      },
      redirectTo() {
        throw new Error("viewing registration detail should not redirect to lottery");
      },
    },
    imports: createRegisterImports({
      getActivity: async () => ({
        id: 321,
        title: "抽奖报名活动",
        category: 11,
        signedUp: true,
        approvalStatus: 1,
        needApproval: true,
        customFields: JSON.stringify([
          { key: "姓名", type: "input", required: true },
          { key: "联系方式", type: "input", required: true },
        ]),
      }),
      getMySignUpPage: async (params) => {
        assert.equal(params.activityId, "321");
        return {
          list: [
            {
              activityId: 321,
              remark: JSON.stringify({
                姓名: "张三",
                联系方式: "13800138000",
              }),
            },
          ],
          total: 1,
        };
      },
    }),
  });
  const instance = createInstance(component);
  instance.eventId = "321";
  instance.viewRegistration = true;
  instance.checkIfActivityCreator = () => {};
  instance.checkQuizRecord = async () => {};

  await instance.loadEventDetail();

  assert.equal(instance.status, "registered");
  assert.deepEqual(toPlainObject(instance.registeredData), {
    姓名: "张三",
    联系方式: "13800138000",
  });
  assert.equal(instance.getRegisteredFieldValue({ key: "姓名" }), "张三");
});

test("registration detail view restores registered state from my signup record when activity detail omits signedUp", async () => {
  const filePath = path.resolve(__dirname, "../src/pages/events/register.vue");
  const component = loadComponent(filePath, {
    uni: {
      showLoading() {},
      hideLoading() {},
      getStorageSync() {
        return "";
      },
      redirectTo() {
        throw new Error("viewing registration detail should not redirect to lottery");
      },
    },
    imports: createRegisterImports({
      getActivity: async () => ({
        id: 321,
        title: "抽奖报名活动",
        category: 11,
        signedUp: false,
        needApproval: true,
        customFields: JSON.stringify([
          { key: "姓名", type: "input", required: true },
          { key: "联系方式", type: "input", required: true },
        ]),
      }),
      getMySignUpPage: async (params) => {
        assert.equal(params.activityId, "321");
        return {
          list: [
            {
              activityId: 321,
              approvalStatus: 1,
              checkedIn: false,
              remark: JSON.stringify({
                姓名: "李四",
                联系方式: "13900139000",
              }),
            },
          ],
          total: 1,
        };
      },
    }),
  });
  const instance = createInstance(component);
  instance.eventId = "321";
  instance.viewRegistration = true;
  instance.checkIfActivityCreator = () => {};
  instance.checkQuizRecord = async () => {};

  await instance.loadEventDetail();

  assert.equal(instance.status, "registered");
  assert.equal(instance.approvalStatus, 1);
  assert.deepEqual(toPlainObject(instance.registeredData), {
    姓名: "李四",
    联系方式: "13900139000",
  });
});
