<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumBannerApi } from '#/api/forum/banner';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { nextTick } from 'vue';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deleteBanner, getBannerPage } from '#/api/forum/banner';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import BannerFormModal from './modules/form.vue';

defineOptions({ name: 'ForumBannerManage' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: BannerFormModal,
  destroyOnClose: true,
});

/** 刷新表格 */
async function onRefresh() {
  // 使用 nextTick 确保在 DOM 更新后再刷新
  await nextTick();
  // 添加短暂延迟，确保 modal 完全关闭
  await new Promise((resolve) => setTimeout(resolve, 100));
  // 使用 reload 强制重新加载数据，比 query 更彻底
  // 如果 reload 不行，尝试直接访问 grid 实例
  try {
    await gridApi.reload();
  } catch (error) {
    console.error('Reload failed, trying query:', error);
    await gridApi.query();
  }
}

/** 创建 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑 */
function handleEdit(row: ForumBannerApi.Banner) {
  formModalApi.setData(row).open();
}

/** 删除 */
async function handleDelete(row: ForumBannerApi.Banner) {
  try {
    await confirm({
      content: $t('ui.actionMessage.deleteConfirm', [row.title || row.id]),
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: $t('ui.actionMessage.deleting', [row.title || row.id]),
    key: 'action_key_msg',
  });
  try {
    await deleteBanner(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.title || row.id]),
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
    columns: useGridColumns(onRefresh),
    height: 'auto',
    keepSource: false, // 改为 false，确保数据更新
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          // 添加时间戳参数避免缓存
          const result = await getBannerPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            _t: Date.now(), // 添加时间戳避免缓存
          });
          // 确保返回正确的数据格式
          return {
            list: result.list || [],
            total: result.total || 0,
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
  } as VxeTableGridOptions<ForumBannerApi.Banner>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />

    <Grid table-title="Banner 管理">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '创建 Banner',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['forum:banner:create'],
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
              auth: ['forum:banner:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['forum:banner:delete'],
              onClick: handleDelete.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
