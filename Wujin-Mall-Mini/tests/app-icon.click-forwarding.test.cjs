const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const filePath = path.resolve(__dirname, "../src/components/AppIcon.vue");

function readSource() {
  return fs.readFileSync(filePath, "utf8");
}

test("AppIcon forwards click events so icon-only actions stay interactive", () => {
  const source = readSource();

  assert.match(
    source,
    /<image[\s\S]*?@click="handleClick"/,
    "Expected AppIcon root image to listen for clicks"
  );
  assert.match(
    source,
    /emits:\s*\[\s*["']click["']\s*\]/,
    "Expected AppIcon to declare a click event"
  );
  assert.match(
    source,
    /handleClick\s*\(\s*event\s*\)\s*\{[\s\S]*?\$emit\(\s*["']click["']\s*,\s*event\s*\)/,
    "Expected AppIcon to emit click events to parent components"
  );
});
