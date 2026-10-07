import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const migration = await readFile(
  new URL(
    '../sql/mysql/upgrade/20260624-split-wujin-platform-tabs-to-menus.sql',
    import.meta.url,
  ),
  'utf8',
);

test('wujin platform former tabs are split into sidebar menu routes', () => {
  for (const [menuName, routePath, permission] of [
    ['运营看板', 'dashboard', 'wujin:monitor-dashboard:query'],
    ['平台类目', 'category', 'wujin:category:query'],
    ['跨泳道映射', 'mapping', 'wujin:category-mapping:query'],
    ['行业模板', 'template', 'wujin:industry-template:query'],
    ['关系审核', 'audit', 'wujin:relation-audit-record:query'],
    ['搜索规则', 'search-rule', 'wujin:search-rule-config:query'],
    ['搜索监控', 'search-log', 'wujin:search-behavior-log:query'],
    ['寻源线索', 'sourcing-lead', 'wujin:sourcing-lead:query'],
    ['监控快照', 'monitor', 'wujin:monitor-snapshot:query'],
  ]) {
    assert.equal(migration.includes(`'${menuName}'`), true);
    assert.equal(migration.includes(`'${routePath}'`), true);
    assert.equal(migration.includes(`'${permission}'`), true);
  }
});

test('wujin platform button permissions move under section menus', () => {
  for (const variableName of [
    '@wujin_platform_dashboard_menu_id',
    '@wujin_platform_category_menu_id',
    '@wujin_platform_mapping_menu_id',
    '@wujin_platform_template_menu_id',
    '@wujin_platform_audit_menu_id',
    '@wujin_platform_search_rule_menu_id',
    '@wujin_platform_search_log_menu_id',
    '@wujin_platform_sourcing_lead_menu_id',
    '@wujin_platform_monitor_menu_id',
  ]) {
    assert.equal(
      migration.includes(`THEN ${variableName}`),
      true,
      `missing permission parent move for ${variableName}`,
    );
  }

  assert.match(migration, /UPDATE\s+`system_menu`/i);
  assert.match(migration, /INSERT\s+INTO\s+`system_role_menu`/i);
  assert.equal(migration.includes('granted_role'), true);
  assert.equal(migration.includes('section_menu'), true);
});
