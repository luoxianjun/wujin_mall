const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const addressFilePath = path.resolve(
  __dirname,
  "../src/pages/lottery/address.vue"
);

const recordsFilePath = path.resolve(
  __dirname,
  "../src/pages/lottery/records.vue"
);

function readAddressSource() {
  return fs.readFileSync(addressFilePath, "utf8");
}

function readRecordsSource() {
  return fs.readFileSync(recordsFilePath, "utf8");
}

test("lottery address page explicitly registers BackButton for custom navigation", () => {
  const source = readAddressSource();

  assert.match(
    source,
    /import\s+BackButton\s+from\s+["']@\/components\/BackButton\.vue["']/
  );
  assert.match(source, /components\s*:\s*\{[\s\S]*BackButton[\s\S]*\}/);
});

test("lottery records page passes the current delivery address to the address selection page", () => {
  const source = readRecordsSource();

  assert.match(
    source,
    /goAddress\(recordId,\s*deliveryAddress = ""\)/
  );
  assert.match(
    source,
    /const deliveryAddressQuery = deliveryAddress\s*\?\s*`&deliveryAddress=\$\{encodeURIComponent\(deliveryAddress\)\}`\s*:\s*"";/
  );
  assert.match(
    source,
    /url:\s*`\/pages\/lottery\/address\?recordId=\$\{recordId\}&lotteryId=\$\{this\.lotteryId\}\$\{deliveryAddressQuery\}`/
  );
});

test("lottery address page decodes the current delivery address from navigation params", () => {
  const source = readAddressSource();

  assert.match(
    source,
    /currentDeliveryAddress:\s*""/
  );
  assert.match(
    source,
    /this\.currentDeliveryAddress = query\.deliveryAddress \? decodeURIComponent\(query\.deliveryAddress\) : "";/ 
  );
});

test("lottery address page matches submitted addresses using the same formatted string used for submission", () => {
  const source = readAddressSource();

  assert.match(
    source,
    /formatAddressText\(addr = \{\}\)\s*\{[\s\S]*return \[addr\.name, addr\.mobile, `\$\{addr\.areaName \|\| ""\}\$\{addr\.detailAddress \|\| ""\}`\][\s\S]*\.join\(" "\);[\s\S]*\}/
  );
  assert.match(
    source,
    /const matchedAddr = this\.currentDeliveryAddress[\s\S]*this\.formatAddressText\(a\) === this\.currentDeliveryAddress/
  );
  assert.match(
    source,
    /const addressStr = this\.formatAddressText\(addr\);/
  );
});

test("lottery address page only falls back to the default address when no submitted address exists", () => {
  const source = readAddressSource();

  assert.match(
    source,
    /if \(matchedAddr\) \{[\s\S]*this\.selectedId = matchedAddr\.id;[\s\S]*return;[\s\S]*\}/
  );
  assert.match(
    source,
    /if \(!this\.currentDeliveryAddress\) \{[\s\S]*const defaultAddr = this\.addressList\.find\(\(a\) => a\.defaultStatus\);[\s\S]*this\.selectedId = defaultAddr \? defaultAddr\.id : null;[\s\S]*return;[\s\S]*\}/
  );
  assert.match(source, /this\.selectedId = null;/);
});
