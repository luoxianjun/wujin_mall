const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

test("invitation records entry uses a mini-program-safe arrow glyph", () => {
  const filePath = path.resolve(
    __dirname,
    "../src/pages/invitation/share.vue"
  );
  const source = fs.readFileSync(filePath, "utf8");

  assert.match(
    source,
    /<text class="records-entry-arrow">查看 ›<\/text>/
  );
  assert.doesNotMatch(source, /<text class="records-entry-arrow">查看 >/);
  assert.doesNotMatch(source, /&gt;/);
});
