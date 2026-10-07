const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const workspaceRoot = path.resolve(__dirname, '..');
const mainSqlPath = path.join(
  workspaceRoot,
  'Wujin-Mall-Server/sql/mysql/yudao-module-wujin.sql',
);
const upgradeSqlPath = path.join(
  workspaceRoot,
  'Wujin-Mall-Server/sql/mysql/upgrade/20260613-add-wujin-supply-capability.sql',
);
const demoSeedSqlPath = path.join(
  workspaceRoot,
  'Wujin-Mall-Server/sql/mysql/upgrade/20260626-seed-wujin-merchant-demo-data.sql',
);

function readSql(filePath) {
  assert.ok(fs.existsSync(filePath), `expected SQL file to exist: ${filePath}`);
  return fs.readFileSync(filePath, 'utf8');
}

function assertSupplyCapabilityTable(sql) {
  assert.match(sql, /CREATE TABLE IF NOT EXISTS `wujin_merchant_supply_capability`/);
  [
    '`merchant_id` bigint NOT NULL',
    '`product_id` bigint NOT NULL',
    '`product_name` varchar(128) NOT NULL',
    '`entity_id` bigint NOT NULL',
    '`lane` varchar(32) NOT NULL',
    '`industry` varchar(64) DEFAULT NULL',
    '`supply_status` tinyint NOT NULL',
    '`stock_count` int DEFAULT NULL',
    '`min_order_quantity` int DEFAULT NULL',
    '`delivery_days` int DEFAULT NULL',
    '`service_area` varchar(128) DEFAULT NULL',
  ].forEach((column) => assert.match(sql, new RegExp(column.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))));
  assert.match(sql, /idx_wujin_supply_capability_merchant/);
  assert.match(sql, /idx_wujin_supply_capability_entity/);
  assert.match(sql, /idx_wujin_supply_capability_lane/);
  assert.match(sql, /idx_wujin_supply_capability_status/);
}

test('main wujin MySQL schema includes merchant supply capability index', () => {
  assertSupplyCapabilityTable(readSql(mainSqlPath));
});

test('upgrade SQL can add merchant supply capability index idempotently', () => {
  const sql = readSql(upgradeSqlPath);

  assertSupplyCapabilityTable(sql);
  assert.match(sql, /COMMENT='五金商家供应能力索引'/);
});

test('demo seed SQL inserts merchant relation products for visible tables', () => {
  const sql = readSql(demoSeedSqlPath);

  [
    '米其林 205/55R16 乘用车轮胎',
    '8.8级 M12 外六角螺栓',
    '6204 深沟球轴承',
    '卡车全钢子午线轮胎',
    'GCr15 轴承钢供应',
    '304不锈钢内六角螺钉 M8',
    '碳钢45#线材供应',
  ].forEach((name) => assert.match(sql, new RegExp(name)));

  [
    'INSERT INTO product_spu',
    'INSERT INTO product_sku',
    'INSERT INTO wujin_merchant_relation_submission',
    'INSERT INTO wujin_merchant_relation_item',
    'INSERT INTO wujin_merchant_supply_capability',
    'INSERT INTO wujin_sourcing_lead',
    'ON DUPLICATE KEY UPDATE',
  ].forEach((snippet) =>
    assert.match(sql, new RegExp(snippet.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))),
  );
});
