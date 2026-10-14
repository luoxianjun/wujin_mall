const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

test('wujin category API covers cascade delete and sibling sorting', () => {
  const api = read('apps/web-antd/src/api/wujin/category.ts');

  assert.match(api, /\/wujin\/category\/list/);
  assert.match(api, /\/wujin\/category\/create/);
  assert.match(api, /\/wujin\/category\/update'/);
  assert.match(api, /\/wujin\/category\/delete/);
  assert.match(api, /params: \{ cascade, id \}/);
  assert.match(api, /\/wujin\/category\/update-sort/);
});

test('wujin category route opens the multi-column page', () => {
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');

  assert.match(
    route,
    /path: '\/wujin\/platform\/category',\s*component: \(\) => import\('#\/views\/wujin\/category\/index\.vue'\)/,
  );
});

test('wujin category page renders cascading columns up to third-level categories', () => {
  const page = read('apps/web-antd/src/views/wujin/category/index.vue');
  const tree = read('apps/web-antd/src/views/wujin/category/tree.ts');

  assert.match(page, /defineOptions\(\{ name: 'WujinCategoryColumns' \}\)/);
  assert.match(tree, /MAX_CATEGORY_LEVEL = 4/);
  assert.match(tree, /'大行业',\s*'一级分类',\s*'二级分类',\s*'三级分类'/);
  assert.match(tree, /'新增大类',\s*'新增一级',\s*'新增二级',\s*'新增三级'/);
  // 点某行 → 右侧列刷新为下一级，更右侧清空
  assert.match(page, /selectedIds\.value\.slice\(0, column\.level - 1\)/);
  // 有子级的行才显示 ▶
  assert.match(page, /<ChevronRight\s+v-if="hasChildren\(categoryIndex, item\.id\)"/);
  // 每列独立滚动
  assert.match(page, /\.category-column-body \{[^}]*overflow-y: auto/);
});

test('wujin category page searches, highlights and expands the hit path', () => {
  const page = read('apps/web-antd/src/views/wujin/category/index.vue');

  assert.match(page, /matchCategories\(/);
  assert.match(page, /getAncestorPath\(categoryIndex\.value, id\)/);
  assert.match(page, /scrollIntoView/);
  assert.match(page, /<mark v-if="segment\.hit">/);
  assert.match(page, /'is-hit': searchHitIds\.includes/);
});

test('wujin category page supports inline add, edit, cascade delete and drag sorting', () => {
  const page = read('apps/web-antd/src/views/wujin/category/index.vue');
  const editor = read('apps/web-antd/src/views/wujin/category/modules/inline-editor.vue');

  assert.match(page, /buildChildCode\(state\.parentCode, state\.form\.codeSuffix\)/);
  assert.match(page, /将同时删除 \$\{childCount\} 个子级/);
  assert.match(page, /deleteWujinCategory\(\s*item\.id as number,\s*childCount > 0,?\s*\)/);
  assert.match(page, /updateWujinCategorySort\(/);
  assert.match(page, /@dragstart="handleDragStart/);
  assert.match(page, /@drop="handleDrop/);
  assert.match(page, /✎ 编辑/);
  assert.match(page, /＋ 添加子级/);
  assert.match(page, /🗑 删除/);
  assert.match(page, /wujin:category:create/);
  assert.match(page, /wujin:category:update/);
  assert.match(page, /wujin:category:delete/);
  ['类目编码', '说明', '搜索展示层级', '健康状态', '状态'].forEach((label) =>
    assert.match(editor, new RegExp(label)),
  );
});
