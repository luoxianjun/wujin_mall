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

test('wujin platform attribute dictionary maintains server-side dictionary records', () => {
  const api = read('apps/web-antd/src/api/wujin/platform.ts');
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');

  assert.match(api, /interface AttributeDictionary/);
  assert.match(api, /'\/wujin\/attribute-dictionary\/list'/);
  assert.match(api, /'\/wujin\/attribute-dictionary\/create'/);
  assert.match(api, /'\/wujin\/attribute-dictionary\/update'/);
  assert.match(api, /'\/wujin\/attribute-dictionary\/delete'/);
  assert.doesNotMatch(api, /getPlatformAttributeDictionaryPlaceholder/);
  assert.doesNotMatch(api, /serverReady:\s*false/);

  assert.match(route, /WujinPlatformAttributeDictionary/);
  assert.match(route, /\/wujin\/platform\/attribute-dictionary/);
  assert.match(route, /attribute-dictionary\.vue/);
  assert.match(route, /平台属性字典/);

  const page = read(
    'apps/web-antd/src/views/wujin/platform/attribute-dictionary.vue',
  );
  assert.match(page, /WujinPlatformAttributeDictionary/);
  assert.match(page, /getAttributeDictionaryList/);
  assert.match(page, /deleteAttributeDictionary/);
  assert.match(page, /wujin:attribute-dictionary:create/);
  assert.match(page, /wujin:attribute-dictionary:update/);
  assert.match(page, /wujin:attribute-dictionary:delete/);
  assert.match(page, /AttributeDictionaryForm/);
  assert.doesNotMatch(page, /服务端接口未接入/);

  const form = read(
    'apps/web-antd/src/views/wujin/platform/modules/attribute-dictionary-form.vue',
  );
  assert.match(form, /createAttributeDictionary/);
  assert.match(form, /updateAttributeDictionary/);
  assert.match(form, /lane:\s*values\.lane \|\| undefined/);

  assert.match(data, /useAttributeDictionaryEditFormSchema/);
  assert.match(data, /fieldName:\s*'valueOptions'/);
  assert.match(data, /mode:\s*'tags'/);
  assert.match(data, /\['ENUM', 'MULTI_ENUM'\]\.includes\(values\.valueType\) \? 'required'/);
  assert.match(data, /属性分组/);
  assert.match(data, /可选值/);
  assert.match(data, /三泳道通用/);
});

test('wujin platform reviews merchant custom tags', () => {
  const api = read('apps/web-antd/src/api/wujin/platform.ts');
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');
  const page = read('apps/web-antd/src/views/wujin/platform/custom-tag-audit.vue');
  const form = read(
    'apps/web-antd/src/views/wujin/platform/modules/custom-tag-review-form.vue',
  );
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');

  assert.match(api, /'\/wujin\/product-attribute\/custom-tag\/list'/);
  assert.match(api, /'\/wujin\/product-attribute\/custom-tag\/review'/);
  assert.match(route, /WujinPlatformCustomTagAudit/);
  assert.match(route, /custom-tag-audit\.vue/);
  assert.match(page, /getProductCustomTagList/);
  assert.match(page, /wujin:product-custom-tag:review/);
  assert.match(page, /ifShow:\s*row\.auditStatus === 10/);
  assert.match(form, /reviewProductCustomTag/);
  assert.match(form, /action:\s*'APPROVE'/);
  assert.match(data, /customTagAuditStatusOptions/);
  assert.match(data, /values\.action === 'REJECT' \? 'required'/);
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
  assert.match(page, /getMonitorTrend/);
  assert.match(page, /searchMonitorRangeDays/);
  assert.match(page, /searchTrendDailyItems/);
  assert.match(page, /searchTrendTopKeywords/);
  assert.match(page, /activeSection\.value === 'searchLog'/);
  assert.match(page, /分日搜索趋势/);
  assert.match(page, /热搜词 Top10/);
  assert.match(page, /搜索行为日志按日聚合/);
  assert.match(page, /平均响应耗时/);
  assert.match(page, /高风险预警/);
  assert.match(page, /SearchLogGrid/);
  assert.match(page, /MonitorSnapshotGrid/);
  assert.match(data, /fieldName:\s*'createTimeRange'/);
  assert.match(data, /component:\s*'RangePicker'/);
});
