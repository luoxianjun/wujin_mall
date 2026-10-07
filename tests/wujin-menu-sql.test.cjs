const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const workspaceRoot = path.resolve(__dirname, '..');
const menuSqlPath = path.join(
  workspaceRoot,
  'Wujin-Mall-Server/sql/mysql/upgrade/20260610-add-wujin-admin-menus.sql',
);
// 后续新增五金菜单/按钮权限的升级脚本，与首版菜单脚本共同构成完整的权限菜单
const followUpMenuSqlPaths = [
  'Wujin-Mall-Server/sql/mysql/upgrade/20261007-add-wujin-attribute-tag-import.sql',
].map((relativePath) => path.join(workspaceRoot, relativePath));
const roleSqlPath = path.join(
  workspaceRoot,
  'Wujin-Mall-Server/sql/mysql/upgrade/20260613-init-wujin-role-permissions.sql',
);

function read(relativePath) {
  return fs.readFileSync(path.join(workspaceRoot, relativePath), 'utf8');
}

function walkFiles(dir, predicate) {
  if (!fs.existsSync(dir)) {
    return [];
  }
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return walkFiles(fullPath, predicate);
    }
    return predicate(fullPath) ? [fullPath] : [];
  });
}

function collectWujinPermissions() {
  const sourceRoots = [
    'Wujin-Mall-Server/yudao-module-wujin/src/main/java',
    'Wujin-Mall-Platform-Web/apps/web-antd/src/views/wujin',
    'Wujin-Mall-Merchant-Web/apps/web-antd/src/views/wujin',
  ];
  const permissions = new Set();
  sourceRoots
    .flatMap((relativeRoot) =>
      walkFiles(path.join(workspaceRoot, relativeRoot), (file) =>
        /\.(java|ts|vue)$/.test(file),
      ),
    )
    .forEach((file) => {
      const content = fs.readFileSync(file, 'utf8');
      for (const match of content.matchAll(/wujin:[a-z0-9:-]+/g)) {
        permissions.add(match[0]);
      }
    });
  return [...permissions].sort();
}

function readMenuSql() {
  assert.ok(
    fs.existsSync(menuSqlPath),
    'expected Wujin menu SQL upgrade script to exist',
  );
  return fs.readFileSync(menuSqlPath, 'utf8');
}

function readRoleSql() {
  assert.ok(
    fs.existsSync(roleSqlPath),
    'expected Wujin role permission SQL upgrade script to exist',
  );
  return fs.readFileSync(roleSqlPath, 'utf8');
}

function escapeRegExp(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

test('wujin menu SQL creates separate platform and merchant backend entries', () => {
  const sql = readMenuSql();

  [
    '五金运营后台',
    '平台运营配置',
    '五金商家后台',
    '/wujin',
    '/merchant/wujin',
    'wujin/platform/index',
    'WujinPlatformManage',
    'wujin/merchant/index',
    'WujinMerchantManage',
  ].forEach((text) => assert.match(sql, new RegExp(escapeRegExp(text))));

  assert.match(sql, /@wujin_platform_root_id/);
  assert.match(sql, /@wujin_platform_menu_id/);
  assert.match(sql, /@wujin_merchant_menu_id/);
});

test('wujin menu SQL repairs duplicate merchant backend menus', () => {
  const sql = readMenuSql();

  assert.match(sql, /@wujin_merchant_keep_id/);
  assert.match(sql, /UPDATE `system_menu` dup/);
  assert.match(sql, /dup\.`name` = '五金商家后台'/);
  assert.match(sql, /dup\.`component` = 'wujin\/merchant\/index'/);
  assert.match(sql, /dup\.`id` <> @wujin_merchant_keep_id/);
  assert.match(sql, /dup\.`deleted` = b'1'/);
});

test('wujin menu SQL covers every declared backend and web permission', () => {
  const sql = [menuSqlPath, ...followUpMenuSqlPaths]
    .map((sqlPath) => fs.readFileSync(sqlPath, 'utf8'))
    .join('\n');
  const permissions = collectWujinPermissions();

  assert.ok(permissions.length > 20, 'expected to discover Wujin permissions');
  permissions.forEach((permission) => {
    assert.match(sql, new RegExp(escapeRegExp(permission)));
  });
});

test('wujin menu SQL uses idempotent insert guards', () => {
  const sql = readMenuSql();

  assert.match(sql, /INSERT INTO `system_menu`/);
  assert.match(sql, /WHERE NOT EXISTS/);
  assert.match(sql, /WHERE `deleted` = b'0' AND `path` = '\/wujin'/);
  assert.match(sql, /WHERE `deleted` = b'0' AND `path` = '\/merchant\/wujin'/);
  assert.match(sql, /WHERE `deleted` = b'0' AND `permission` = 'wujin:category:query'/);
});

test('wujin role permission SQL creates platform and merchant operator roles', () => {
  const sql = readRoleSql();

  [
    '五金平台运营员',
    'wujin_platform_operator',
    '五金商家运营员',
    'wujin_merchant_operator',
    '@wujin_platform_role_id',
    '@wujin_merchant_role_id',
    'system_role',
    'system_role_menu',
  ].forEach((text) => assert.match(sql, new RegExp(escapeRegExp(text))));

  assert.match(sql, /INSERT INTO `system_role`/);
  assert.match(sql, /INSERT INTO `system_role_menu`/);
  assert.match(sql, /WHERE NOT EXISTS/);
  assert.match(sql, /WHERE `deleted` = b'0' AND `code` = 'wujin_platform_operator'/);
  assert.match(sql, /WHERE `deleted` = b'0' AND `code` = 'wujin_merchant_operator'/);
});

test('wujin role permission SQL assigns platform and merchant menu scopes separately', () => {
  const sql = readRoleSql();

  assert.match(
    sql,
    /@wujin_platform_role_id[\s\S]*@wujin_platform_menu_id[\s\S]*m\.`permission` LIKE 'wujin:%'[\s\S]*m\.`permission` NOT LIKE 'wujin:merchant-%'/,
  );
  assert.match(
    sql,
    /@wujin_merchant_role_id[\s\S]*@wujin_merchant_menu_id[\s\S]*m\.`permission` LIKE 'wujin:merchant-%'/,
  );
  assert.doesNotMatch(
    sql,
    /@wujin_merchant_role_id[\s\S]*m\.`permission` = 'wujin:relation-audit-review:update'/,
  );
  assert.match(sql, /m\.`id` IN \(@wujin_platform_root_id, @wujin_platform_menu_id\)/);
  assert.match(sql, /m\.`id` = @wujin_merchant_menu_id/);
  assert.match(sql, /rm\.`role_id` = @wujin_platform_role_id AND rm\.`menu_id` = m\.`id`/);
  assert.match(sql, /rm\.`role_id` = @wujin_merchant_role_id AND rm\.`menu_id` = m\.`id`/);
});

test('wujin attribute, tag and import upgrade SQL is idempotent and grants default roles', () => {
  const sql = fs.readFileSync(followUpMenuSqlPaths[0], 'utf8');

  [
    'wujin_attribute_dictionary',
    'wujin_product_attribute_value',
    'wujin_product_custom_tag',
    'dispatch_time',
    'follow_stage',
    'next_follow_time',
    'quoted_amount',
    'win_probability',
    'wujin/platform/attribute-dictionary',
    'wujin/platform/custom-tag-audit',
    'wujin:merchant-import:import',
    'wujin_platform_operator',
    'wujin_merchant_operator',
  ].forEach((text) => assert.match(sql, new RegExp(escapeRegExp(text))));

  assert.match(sql, /CREATE TABLE IF NOT EXISTS `wujin_attribute_dictionary`/);
  assert.match(sql, /information_schema\.COLUMNS/);
  assert.match(sql, /PREPARE wujin_stmt FROM @wujin_ddl/);
  assert.doesNotMatch(sql, /ALTER TABLE `wujin_sourcing_lead`\s+ADD COLUMN `dispatch_time`[^']*;\n/);
  const menuInserts = sql.match(/INSERT INTO `system_menu`/g) ?? [];
  const menuGuards = sql.match(/AND NOT EXISTS \(SELECT 1 FROM `system_menu`/g) ?? [];
  assert.equal(menuInserts.length, menuGuards.length);
});
