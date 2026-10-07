const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

function readJson(relativePath) {
  const filePath = path.resolve(__dirname, "..", relativePath);
  return JSON.parse(fs.readFileSync(filePath, "utf8"));
}

test("mp-weixin manifest only declares supported app.json permissions", () => {
  const manifest = readJson("src/manifest.json");
  const permission = manifest["mp-weixin"]?.permission || {};

  assert.ok(permission["scope.userLocation"]);
  assert.equal(permission["scope.record"], undefined);
});

test("project config disables multi-frame runtime to avoid subpackage frame crashes in devtools", () => {
  const projectConfig = readJson("project.config.json");

  assert.equal(projectConfig.setting?.useMultiFrameRuntime, false);
});
