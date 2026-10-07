<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumConfigApi } from '#/api/forum/config';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteConfig, getConfigList } from '#/api/forum/config';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import ConfigFormModal from './modules/form.vue';

defineOptions({ name: 'ForumConfigManage' });

const configList = ref<ForumConfigApi.Config[]>([]);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ConfigFormModal,
  destroyOnClose: true,
});

/** 刷新表格 */
async function onRefresh() {
  gridApi.setLoading(true);
  try {
    const list = await getConfigList();
    console.log(list, 'list');
    configList.value = list || [];
    gridApi.setTableData(configList.value);
  } finally {
    gridApi.setLoading(false);
  }
}

/** 创建 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑 */
function handleEdit(row: ForumConfigApi.Config) {
  formModalApi
    .setData({
      ...row,
      key: row.configKey,
      value: row.configValue,
    })
    .open();
}

/** 删除 */
async function handleDelete(row: ForumConfigApi.Config) {
  try {
    await confirm({
      content: $t('ui.actionMessage.deleteConfirm', [
        row.name || row.configKey,
      ]),
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name || row.configKey]),
    key: 'action_key_msg',
  });
  try {
    await deleteConfig(row.configKey as string);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [
        row.name || row.configKey,
      ]),
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
        query: async ({ page }) => {
          const data = await getConfigList();
          console.log(data, 'configList data');
          configList.value = data || [];
          return {
            list: configList.value,
            total: configList.value.length,
            pageNo: page.currentPage,
            pageSize: page.pageSize,
          };
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
  } as VxeTableGridOptions<ForumConfigApi.Config>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />

    <Grid table-title="论坛配置管理">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '新增配置',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['forum:config:update'],
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
              auth: ['forum:config:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['forum:config:delete'],
              onClick: handleDelete.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
