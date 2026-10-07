<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemRoleApi } from '#/api/system/role';

import { ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { Button, message, Tabs, Avatar } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import * as MatrixAcctApi from '#/api/matrix/acct';
import { deleteRole, deleteRoleList, exportRole } from '#/api/system/role';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import AssignDataPermissionForm from './modules/assign-data-permission-form.vue';
import AssignMenuForm from './modules/assign-menu-form.vue';
import DyAuthForm from './modules/dy-auth-form.vue';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [AssignDataPermissionFormModel, assignDataPermissionFormApi] =
  useVbenModal({
    connectedComponent: AssignDataPermissionForm,
    destroyOnClose: true,
  });

const [AssignMenuFormModel, assignMenuFormApi] = useVbenModal({
  connectedComponent: AssignMenuForm,
  destroyOnClose: true,
});

const [DyAuthFormModal, dyAuthFormModalApi] = useVbenModal({
  connectedComponent: DyAuthForm,
  destroyOnClose: true,
});

const sceneType = ref('1');

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportRole(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '角色.xls', source: data });
}

/** 编辑角色 */
function handleEdit(row: SystemRoleApi.Role) {
  formModalApi.setData(row).open();
}

/** 创建角色 */
function handleCreate() {
  formModalApi.setData(null).open();
}

function handleDyAuth() {
  dyAuthFormModalApi.setData(null).open();
}

/** 删除角色 */
async function handleDelete(row: SystemRoleApi.Role) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    key: 'action_key_msg',
  });
  try {
    await MatrixAcctApi.deleteAcct(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
      key: 'action_key_msg',
    });
    onRefresh();
  } finally {
    hideLoading();
  }
}

const checkedIds = ref<number[]>([]);

function handleRowCheckboxChange({
  records,
}: {
  records: SystemRoleApi.Role[];
}) {
  checkedIds.value = records.map((item) => item.id as number);
}

/** 批量删除角色 */
async function handleDeleteBatch() {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting'),
    duration: 0,
    key: 'action_process_msg',
  });
  try {
    await deleteRoleList(checkedIds.value);
    message.success($t('ui.actionMessage.deleteSuccess'));
    onRefresh();
  } finally {
    hideLoading();
  }
}

/** 分配角色的数据权限 */
function handleAssignDataPermission(row: SystemRoleApi.Role) {
  assignDataPermissionFormApi.setData(row).open();
}

/** 分配角色的菜单权限 */
function handleAssignMenu(row: SystemRoleApi.Role) {
  assignMenuFormApi.setData(row).open();
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
          return await MatrixAcctApi.getPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
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
  } as VxeTableGridOptions<SystemRoleApi.Role>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});

function onChangeSceneType(key: number | string) {
  sceneType.value = key.toString();
  gridApi.query();
}
</script>

<template>
  <Page auto-content-height>
    <Tabs class="border-none" @change="onChangeSceneType">
      <Tabs.TabPane tab="抖音" key="1" />
      <Tabs.TabPane tab="快手" key="2" />
      <Tabs.TabPane tab="小红书" key="3" />
    </Tabs>
    <FormModal @success="onRefresh" />
    <DyAuthFormModal @success="onRefresh" />
    <AssignDataPermissionFormModel @success="onRefresh" />
    <AssignMenuFormModel @success="onRefresh" />
    <Grid table-title="">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['账号']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['system:role:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['system:role:export'],
              onClick: handleExport,
            },
            {
              label: '批量删除',
              type: 'primary',
              danger: true,
              disabled: isEmpty(checkedIds),
              icon: ACTION_ICON.DELETE,
              auth: ['system:role:delete'],
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #acctName="{ row }">
        <div class="flex items-center">
          <Avatar :src="row.avatar" />
          <div class="flex flex-col items-start justify-around">
            <span class="ml-2">{{ row.acctName }}</span>
            <span class="text-12px ml-2 text-black/45">
              ID:{{ row.acctNo }}
            </span>
          </div>
        </div>
      </template>
      <template #dyAuth="{ row }">
        <span>未授权</span>
        <Button type="link" size="small" @click="handleDyAuth">授权</Button>
      </template>
      <template #dyChzzAuth="{ row }">
        <span>未授权</span>
        <Button type="link" size="small">授权</Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['system:role:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['system:role:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
          :drop-down-actions="[
            {
              label: '数据权限',
              type: 'link',
              auth: ['system:permission:assign-role-data-scope'],
              onClick: handleAssignDataPermission.bind(null, row),
            },
            {
              label: '菜单权限',
              type: 'link',
              auth: ['system:permission:assign-role-menu'],
              onClick: handleAssignMenu.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
