const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");

const projectRoot = path.resolve(__dirname, "..");

function read(relativePath) {
  return fs.readFileSync(path.resolve(projectRoot, relativePath), "utf8");
}

test("request base URL uses current Wujin mall API and clears stale overrides", () => {
  const source = read("src/utils/request.js");

  assert.doesNotMatch(source, /forum\.gbastu\.com/);
  assert.doesNotMatch(source, /sit\.halcyonz/);
  assert.doesNotMatch(source, /39\.107\.248\.167/);
  assert.doesNotMatch(source, /http:\/\/wj\.halcyonz\.com/);
  assert.match(source, /prod:\s*["']https:\/\/wj\.halcyonz\.com\/app-api["']/);
  assert.match(source, /VALID_API_ORIGINS[\s\S]*https:\/\/wj\.halcyonz\.com/);
  assert.match(source, /removeStorageSync\?\.\(API_BASE_KEY\)/);
});
