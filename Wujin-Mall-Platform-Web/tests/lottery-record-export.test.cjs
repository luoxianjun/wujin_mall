const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

const recordPage = read('apps/web-antd/src/views/forum/lotteryRecord/index.vue');

test('lottery record page exposes export button and downloads current filters', () => {
  assert.match(recordPage, /downloadFileFromBlobPart/);
  assert.match(recordPage, /const exporting = ref\(false\)/);
  assert.match(recordPage, /function buildQueryParams\(\)/);
  assert.match(
    recordPage,
    /requestClient\.download\(\s*'\/gamification\/lottery\/record\/export'/,
  );
  assert.match(recordPage, /fileName: '抽奖记录\.xlsx'/);
  assert.match(recordPage, /auth: \['forum:lottery-record:export'\]/);
  assert.match(recordPage, /loading: exporting/);
});
