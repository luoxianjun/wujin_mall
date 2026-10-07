const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

function loadQuizUtils() {
  const filePath = path.resolve(__dirname, "../src/utils/quiz.js");
  const source = fs.readFileSync(filePath, "utf8");
  const scriptContent = source
    .replace(/export function /g, "function ")
    .concat(
      "\nmodule.exports = { formatQuizLeaderboardSummary, formatQuizDuration, formatQuizElapsed };"
    );
  const sandbox = {
    module: { exports: {} },
    exports: {},
  };
  vm.runInNewContext(scriptContent, sandbox, { filename: filePath });
  return sandbox.module.exports;
}

test("formatQuizLeaderboardSummary hides zero leaderboard size", () => {
  const { formatQuizLeaderboardSummary } = loadQuizUtils();

  assert.equal(formatQuizLeaderboardSummary(0), "排行榜暂时不可见");
  assert.equal(formatQuizLeaderboardSummary(null), "前 10 名");
  assert.equal(formatQuizLeaderboardSummary(5), "前 5 名");
});
