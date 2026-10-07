const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const filePath = path.resolve(
  __dirname,
  "../src/pages/lottery/records.vue"
);

function readSource() {
  return fs.readFileSync(filePath, "utf8");
}

test("lottery records page explicitly registers BackButton for custom navigation", () => {
  const source = readSource();

  assert.match(
    source,
    /import\s+BackButton\s+from\s+["']@\/components\/BackButton\.vue["']/
  );
  assert.match(source, /components\s*:\s*\{[\s\S]*BackButton[\s\S]*\}/);
});

test("lottery records page includes a physical spacer equal to nav bar height", () => {
  const source = readSource();

  assert.match(
    source,
    /<view\s+class="nav-spacer"\s+:style="\{\s*height:\s*navBarStyle\.height\s*\}"\s*\/>/
  );
});

test("lottery records page only shows the address button before a shipping address is submitted", () => {
  const source = readSource();

  assert.match(
    source,
    /v-if="record\.won && record\.prizeType === 2 && !record\.deliveryAddress"/
  );
  assert.match(source, />\s*填写地址\s*</);
});

test("lottery records page renders the submitted delivery address for physical prizes", () => {
  const source = readSource();

  assert.match(
    source,
    /v-if="record\.won && record\.prizeType === 2 && record\.deliveryAddress"/
  );
  assert.match(source, /<text class="address-label">\s*已选发放地址\s*<\/text>/);
  assert.match(source, /<text class="address-value">{{ record\.deliveryAddress }}<\/text>/);
});

test("lottery records page offers an address change action for undelivered physical prizes", () => {
  const source = readSource();

  assert.match(
    source,
    /v-else-if="record\.won && record\.prizeType === 2 && record\.deliveryAddress && !record\.delivered"/
  );
  assert.match(
    source,
    /@click="goAddress\(record\.id,\s*record\.deliveryAddress\)"/
  );
  assert.match(source, />\s*更改地址\s*</);
  assert.match(source, />\s*待发放\s*</);
});

test("lottery records page keeps delivered physical prize addresses read-only", () => {
  const source = readSource();

  assert.match(
    source,
    /v-else-if="record\.won && record\.delivered"/
  );
  assert.doesNotMatch(
    source,
    /v-else-if="record\.won && record\.delivered"[\s\S]*@\s*click="goAddress\(record\.id,\s*record\.deliveryAddress\)"/
  );
  assert.match(source, />\s*已发放\s*</);
});
