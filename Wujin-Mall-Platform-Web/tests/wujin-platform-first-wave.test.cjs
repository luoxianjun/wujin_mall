const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

test('wujin category tab shows aggregated health cards and lane distribution', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');

  assert.match(page, /categoryHealthSummary/);
  assert.match(page, /categoryHealthCards/);
  assert.match(page, /categoryLaneHealthRows/);
  assert.match(page, /buildCategoryHealthSummary/);
  assert.match(page, /categoryItems\.value\.reduce/);
  assert.match(page, /healthStatusOptions/);
  assert.match(page, /laneOptions/);
  assert.match(page, /HEALTHY/);
  assert.match(page, /NEEDS_SPLIT/);
  assert.match(page, /UNBOUND/);
  assert.match(page, /PRODUCT/);
  assert.match(page, /PROCESS/);
  assert.match(page, /MATERIAL/);
  assert.match(page, /类目健康度总览/);
  assert.match(page, /总类目/);
  assert.match(page, /健康/);
  assert.match(page, /需拆分/);
  assert.match(page, /无绑定/);
  assert.match(page, /按泳道健康分布/);
});

test('wujin industry template items support low-risk copy workflow', () => {
  const manageModal = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-item-manage-modal.vue',
  );

  assert.match(manageModal, /createIndustryTemplateItem/);
  assert.match(manageModal, /handleCopyTemplateItem/);
  assert.match(manageModal, /复制/);
  assert.match(manageModal, /关系项已复制/);
  assert.match(manageModal, /currentTemplate\.value\?\.id/);
  assert.match(manageModal, /sort:\s*templateItems\.value\.length\s*\+\s*1/);
  assert.match(manageModal, /id:\s*undefined/);
  assert.match(manageModal, /refreshTemplateItemGrid/);
});

test('wujin platform exposes attribute dictionary placeholder without backend dependency', () => {
  const api = read('apps/web-antd/src/api/wujin/platform.ts');
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');

  assert.match(api, /PlatformAttributeDictionaryItem/);
  assert.match(api, /PlatformAttributeDictionaryStatus/);
  assert.match(api, /getPlatformAttributeDictionaryPlaceholder/);
  assert.match(api, /serverReady:\s*false/);
  assert.match(api, /Promise\.resolve/);
  assert.doesNotMatch(api, /\/wujin\/platform-attribute/);

  assert.match(route, /WujinPlatformAttributeDictionary/);
  assert.match(route, /\/wujin\/platform\/attribute-dictionary/);
  assert.match(route, /attribute-dictionary\.vue/);
  assert.match(route, /平台属性字典/);

  const page = read(
    'apps/web-antd/src/views/wujin/platform/attribute-dictionary.vue',
  );
  assert.match(page, /WujinPlatformAttributeDictionary/);
  assert.match(page, /getPlatformAttributeDictionaryPlaceholder/);
  assert.match(page, /平台属性字典/);
  assert.match(page, /服务端接口未接入/);
  assert.match(page, /字典入口骨架/);
  assert.match(page, /属性分组/);
  assert.match(page, /可选值/);
});

test('wujin search monitor has trend filters and operations chart skeleton', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');

  assert.match(page, /searchMonitorTimeRange/);
  assert.match(page, /searchMonitorTimeRangeOptions/);
  assert.match(page, /monitorSnapshots/);
  assert.match(page, /loadMonitorSnapshots/);
  assert.match(page, /searchMonitorTrendItems/);
  assert.match(page, /searchMonitorOperationCards/);
  assert.match(page, /filterMonitorSnapshotsByRange/);
  assert.match(page, /搜索监控趋势/);
  assert.match(page, /近7天/);
  assert.match(page, /近30天/);
  assert.match(page, /全部数据/);
  assert.match(page, /运营图表/);
  assert.match(page, /趋势数据来自监控快照/);
  assert.match(page, /平均响应耗时/);
  assert.match(page, /高风险预警/);
  assert.match(page, /SearchLogGrid/);
  assert.match(page, /MonitorSnapshotGrid/);
  assert.match(data, /fieldName:\s*'createTimeRange'/);
  assert.match(data, /component:\s*'RangePicker'/);
});
