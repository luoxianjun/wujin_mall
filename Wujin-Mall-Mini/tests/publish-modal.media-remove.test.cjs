const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const filePath = path.resolve(__dirname, "../src/components/PublishModal.vue");

function readSource() {
  return fs.readFileSync(filePath, "utf8");
}

test("publish modal uses a dedicated clickable wrapper for removing uploaded images", () => {
  const source = readSource();

  assert.match(
    source,
    /<view\s+class="media-remove"\s+@click\.stop="removeMedia\(index\)">[\s\S]*?<AppIcon[\s\S]*?name="close"/
  );
  assert.doesNotMatch(
    source,
    /<AppIcon[\s\S]*?class="material-icon media-remove"[\s\S]*?@click\.stop="removeMedia\(index\)"/
  );
});
