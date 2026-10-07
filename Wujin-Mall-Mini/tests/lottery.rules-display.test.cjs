const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

function readFile(relativePath) {
  return fs.readFileSync(path.resolve(__dirname, "..", relativePath), "utf8");
}

function assertRuleGuards(relativePath, expectedSnippets) {
  const source = readFile(relativePath);

  expectedSnippets.forEach((snippet) => {
    assert.match(
      source,
      snippet,
      `Expected ${relativePath} to contain rule guard ${snippet}`
    );
  });
}

test("lottery draw page only shows draw-limit rules for non-scheduled activities", () => {
  assertRuleGuards("src/pages/lottery/draw.vue", [
    /v-if="!isScheduled && activity\.maxDrawsPerDay"/,
    /v-if="!isScheduled && activity\.maxDrawsTotal"/,
    /v-if="isScheduled && activity\.drawTime"/,
  ]);
});

test("lottery index page only shows draw-limit rules for non-scheduled activities", () => {
  assertRuleGuards("src/pages/lottery/index.vue", [
    /v-if="!isScheduled && activity\.maxDrawsPerDay"/,
    /v-if="!isScheduled && activity\.maxDrawsTotal"/,
    /v-if="isScheduled && activity\.drawTime"/,
  ]);
});
