const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");

const filePath = path.resolve(__dirname, "../src/pages/users/points.vue");

function readSource() {
  return fs.readFileSync(filePath, "utf8");
}

test("points page requests both forum and member point record feeds", () => {
  const source = readSource();

  assert.match(source, /getPointRecordPage/);
  assert.match(source, /getMemberPointRecordPage/);
  assert.match(source, /Promise\.all\(\s*\[/);
});

test("points page merges records by create time before rendering", () => {
  const source = readSource();

  assert.match(source, /mergePointRecords/);
  assert.match(
    source,
    /new Date\(b\.createTime\)\.getTime\(\)\s*-\s*new Date\(a\.createTime\)\.getTime\(\)/
  );
});

test("points page uses a stable composite key for merged records", () => {
  const source = readSource();

  assert.match(source, /:key="item\.recordKey \|\| item\.id \|\| index"/);
});
