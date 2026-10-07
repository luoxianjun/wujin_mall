const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

test("home page avoids deprecated getSystemInfoSync calls", () => {
  const filePath = path.resolve(__dirname, "../src/pages/index/index.vue");
  const source = fs.readFileSync(filePath, "utf8");

  assert.match(source, /function getHomeWindowInfo\(/);
  assert.match(source, /uni\.getWindowInfo\(\)/);
  assert.doesNotMatch(source, /uni\.getSystemInfoSync\(\)/);
});
