const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const root = path.resolve(__dirname, '..');

function readSource(...parts) {
  return readFileSync(path.join(root, ...parts), 'utf8');
}

const platformHome = readSource(
  'Wujin-Mall-Platform-Web',
  'apps',
  'web-antd',
  'src',
  'views',
  'mall',
  'home',
  'index.vue',
);

const merchantHome = readSource(
  'Wujin-Mall-Merchant-Web',
  'apps',
  'web-antd',
  'src',
  'views',
  'mall',
  'home',
  'index.vue',
);

const platformAnalytics = readSource(
  'Wujin-Mall-Platform-Web',
  'apps',
  'web-antd',
  'src',
  'views',
  'dashboard',
  'analytics',
  'index.vue',
);

const merchantAnalytics = readSource(
  'Wujin-Mall-Merchant-Web',
  'apps',
  'web-antd',
  'src',
  'views',
  'dashboard',
  'analytics',
  'index.vue',
);

test('platform dashboard is framed as a platform operations cockpit', () => {
  assert.match(platformHome, /平台运营总览/);
  assert.match(platformAnalytics, /平台运营总览/);
  assert.match(platformHome, /平台待关注事项/);
  assert.match(platformAnalytics, /平台待关注事项/);
  assert.match(platformHome, /内容与活动趋势/);
  assert.match(platformAnalytics, /内容与活动趋势/);
  assert.match(platformHome, /平台类目/);
  assert.match(platformAnalytics, /平台类目/);
  assert.doesNotMatch(platformHome, /商家经营工作台/);
  assert.doesNotMatch(platformAnalytics, /商家经营工作台/);
});

test('merchant dashboard is framed as a merchant operations cockpit', () => {
  assert.match(merchantHome, /商家经营工作台/);
  assert.match(merchantAnalytics, /商家经营工作台/);
  assert.match(merchantHome, /经营待处理/);
  assert.match(merchantAnalytics, /经营待处理/);
  assert.match(merchantHome, /近七日经营趋势/);
  assert.match(merchantAnalytics, /近七日经营趋势/);
  assert.match(merchantHome, /商品管理/);
  assert.match(merchantAnalytics, /商品管理/);
  assert.doesNotMatch(merchantHome, /平台运营总览/);
  assert.doesNotMatch(merchantAnalytics, /平台运营总览/);
});

test('merchant quick entries point to reachable merchant paths', () => {
  [
    '/mall/product/spu',
    '/mall/trade/order',
    '/mall/trade/after-sale',
    '/mall/promotion/coupon',
    '/mall/product/comment',
    '/merchant/wujin',
  ].forEach((url) => {
    const pattern = new RegExp(
      `url:\\s*'${url.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}'`,
    );
    assert.match(merchantHome, pattern);
    assert.match(merchantAnalytics, pattern);
  });

  assert.doesNotMatch(merchantHome, /url:\s*'ProductSpu'/);
  assert.doesNotMatch(merchantAnalytics, /url:\s*'ProductSpu'/);
});
