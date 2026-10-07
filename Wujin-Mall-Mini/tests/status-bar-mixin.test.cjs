const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

function loadMixin(filePath, overrides = {}) {
  const source = fs.readFileSync(filePath, "utf8");
  const scriptContent = source.replace(/export default/, "module.exports =");

  const sandbox = {
    module: { exports: {} },
    exports: {},
    console: overrides.console || console,
    uni: overrides.uni || {},
  };

  vm.runInNewContext(scriptContent, sandbox, { filename: filePath });
  return sandbox.module.exports;
}

test("status bar mixin computes pure pixel offsets for custom navigation", () => {
  const filePath = path.resolve(__dirname, "../src/mixins/statusBar.js");
  const mixin = loadMixin(filePath, {
    uni: {
      getSystemInfoSync() {
        return {
          statusBarHeight: 44,
          windowWidth: 375,
        };
      },
    },
  });

  const instance = {
    ...(typeof mixin.data === "function" ? mixin.data() : {}),
  };

  Object.entries(mixin.methods || {}).forEach(([name, method]) => {
    instance[name] = method.bind(instance);
  });

  instance.initStatusBarHeight();

  const navBarStyle = mixin.computed.navBarStyle.call(instance);
  const topContentStyle = mixin.computed.topContentStyle.call(instance);
  const stickyTopStyle = mixin.computed.stickyTopStyle.call(instance);

  assert.deepEqual(JSON.parse(JSON.stringify(navBarStyle)), {
    height: "88px",
    paddingTop: "44px",
    boxSizing: "border-box",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(topContentStyle)), {
    paddingTop: "88px",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(stickyTopStyle)), {
    top: "88px",
  });
});

test("status bar mixin prefers menu button metrics when available", () => {
  const filePath = path.resolve(__dirname, "../src/mixins/statusBar.js");
  const mixin = loadMixin(filePath, {
    uni: {
      getSystemInfoSync() {
        return {
          statusBarHeight: 47,
          windowWidth: 393,
        };
      },
      getMenuButtonBoundingClientRect() {
        return {
          top: 59,
          height: 32,
        };
      },
    },
  });

  const instance = {
    ...(typeof mixin.data === "function" ? mixin.data() : {}),
  };

  Object.entries(mixin.methods || {}).forEach(([name, method]) => {
    instance[name] = method.bind(instance);
  });

  instance.initStatusBarHeight();

  const navBarStyle = mixin.computed.navBarStyle.call(instance);
  const topContentStyle = mixin.computed.topContentStyle.call(instance);
  const stickyTopStyle = mixin.computed.stickyTopStyle.call(instance);

  assert.deepEqual(JSON.parse(JSON.stringify(navBarStyle)), {
    height: "103px",
    paddingTop: "47px",
    boxSizing: "border-box",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(topContentStyle)), {
    paddingTop: "103px",
  });
  assert.deepEqual(JSON.parse(JSON.stringify(stickyTopStyle)), {
    top: "103px",
  });
});
