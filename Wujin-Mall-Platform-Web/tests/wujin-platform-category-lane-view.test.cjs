const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

test('wujin platform category page exposes independent lane tree view', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');

  assert.match(page, /categoryLaneView/);
  assert.match(page, /categoryLaneViewOptions/);
  assert.match(page, /handleCategoryLaneViewChange/);
  assert.match(page, /reloadCategoryTreeByLane/);
  assert.match(page, /queryCategoryList/);
  assert.match(page, /lane:\s*categoryLaneView\.value/);
  assert.match(page, /categoryLaneView\.value === 'ALL'/);
  assert.match(page, /全部类目/);
  assert.match(page, /成品树/);
  assert.match(page, /加工树/);
  assert.match(page, /原材料树/);
  assert.match(page, /按泳道查看/);
});

test('wujin platform non-paged grids return arrays for vxe proxy loadData', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');

  assert.match(page, /pagerConfig:\s*\{\s*enabled:\s*false/);
  assert.match(
    page,
    /query:\s*async\s*\(_params,\s*formValues\)\s*=>\s*await queryApi\(formValues\)/,
  );
  assert.doesNotMatch(page, /listAsGridResult/);
});

test('wujin platform non-paged grids keep a usable body height', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');

  assert.doesNotMatch(page, /height:\s*'auto'/);
  assert.match(page, /height:\s*520/);
});
