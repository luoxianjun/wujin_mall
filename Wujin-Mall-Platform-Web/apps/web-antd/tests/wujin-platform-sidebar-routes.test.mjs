import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const viewSource = await readFile(
  new URL('../src/views/wujin/platform/index.vue', import.meta.url),
  'utf8',
);
const routeSource = await readFile(
  new URL('../src/router/routes/modules/wujin.ts', import.meta.url),
  'utf8',
);

const platformRouteSlugs = [
  'dashboard',
  'category',
  'mapping',
  'template',
  'audit',
  'search-rule',
  'search-log',
  'sourcing-lead',
  'monitor',
];

test('wujin platform page uses sidebar route sections instead of internal tabs', () => {
  assert.equal(viewSource.includes('<Tabs'), false);
  assert.equal(viewSource.includes('Tabs.TabPane'), false);
  assert.equal(viewSource.includes("from 'vue-router'"), true);
  assert.equal(viewSource.includes('activeSection'), true);

  for (const routeSlug of platformRouteSlugs) {
    assert.equal(
      viewSource.includes(`'${routeSlug}'`),
      true,
      `missing platform sidebar route slug: ${routeSlug}`,
    );
  }
});

test('wujin platform static routes expose each former tab as a sidebar route', () => {
  assert.match(routeSource, /redirect:\s*'\/wujin\/platform\/dashboard'/);
  assert.match(
    routeSource,
    /name:\s*'WujinPlatform'[\s\S]*?hideInMenu:\s*true[\s\S]*?children:/,
    'static Wujin routes should stay registered but hidden from the frontend menu',
  );

  for (const routeSlug of platformRouteSlugs) {
    assert.equal(
      routeSource.includes(`path: '/wujin/platform/${routeSlug}'`),
      true,
      `missing static platform route: ${routeSlug}`,
    );
  }
});
