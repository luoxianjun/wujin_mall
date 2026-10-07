const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

test("welfare float stops touch propagation while dragging", () => {
  const filePath = path.resolve(__dirname, "../src/pages/index/index.vue");
  const source = fs.readFileSync(filePath, "utf8");
  const floatBlockMatch = source.match(
    /<view\s+class="welfare-float"[\s\S]*?<\/view>\s*<\/root-portal>/
  );

  assert.ok(floatBlockMatch, "Expected welfare float block in index page");

  const floatBlock = floatBlockMatch[0];

  assert.ok(
    floatBlock.includes('@touchstart.stop="onWelfareTouchStart"'),
    "Expected welfare float touchstart to stop propagation"
  );
  assert.ok(
    floatBlock.includes('@touchmove.stop.prevent="onWelfareTouchMove"'),
    "Expected welfare float touchmove to stop propagation and prevent scrolling"
  );
  assert.ok(
    floatBlock.includes('@touchend.stop="onWelfareTouchEnd"'),
    "Expected welfare float touchend to stop propagation"
  );
  assert.ok(
    floatBlock.includes('@click.stop="handleWelfareClick"'),
    "Expected welfare float click to stop propagation"
  );
});
