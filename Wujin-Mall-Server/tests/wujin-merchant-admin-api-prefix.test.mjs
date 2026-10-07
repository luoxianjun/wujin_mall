import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { test } from 'node:test';

const controllerFiles = [
  '../yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/admin/merchant/WujinMerchantRelationSubmitController.java',
  '../yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/admin/sourcing/WujinMerchantSourcingLeadController.java',
  '../yudao-module-wujin/src/main/java/cn/iocoder/yudao/module/wujin/controller/admin/supply/WujinMerchantSupplyCapabilityController.java',
];

test('wujin merchant backend controllers are mounted under admin-api', async () => {
  for (const relativePath of controllerFiles) {
    const controller = await readFile(new URL(relativePath, import.meta.url), 'utf8');

    assert.match(controller, /package cn\.iocoder\.yudao\.module\.wujin\.controller\.admin\./);
    assert.doesNotMatch(controller, /package cn\.iocoder\.yudao\.module\.wujin\.controller\.merchant\./);
  }
});
