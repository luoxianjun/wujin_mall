<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumSignRuleApi } from '#/api/forum/signRule';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteSignRule,
  getSignRuleList,
} from '#/api/forum/signRule';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import SignRuleFormModal from './modules/form.vue';

defineOptions({ name: 'ForumSignRuleManage' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: SignRuleFormModal,
  destroyOnClose: true,
});

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

/** 创建 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑 */
function handleEdit(row: ForumSignRuleApi.SignRule) {
  formModalApi.setData(row).open();
}

/** 删除 */
async function handleDelete(row: ForumSignRuleApi.SignRule) {
  try {
    await confirm({
      content: $t('ui.actionMessage.deleteConfirm', [row.id]),
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    key: 'action_key_msg',
  });
  try {
    await deleteSignRule(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
      key: 'action_key_msg',
    });
    onRefresh();
  } finally {
    hide();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const list = await getSignRuleList(formValues);
          return { list, total: list.length, pageSize: page.pageSize, pageNo: page.currentPage };
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<ForumSignRuleApi.SignRule>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />

    <Grid table-title="签到规则配置">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '创建规则',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['forum:sign-rule:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['forum:sign-rule:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['forum:sign-rule:delete'],
              onClick: handleDelete.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
