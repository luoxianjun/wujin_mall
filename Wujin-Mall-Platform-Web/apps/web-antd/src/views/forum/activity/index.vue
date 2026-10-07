<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumActivityApi } from '#/api/forum/activity';

import { useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteActivity,
  getActivityPage,
  hideActivity,
  showActivity,
} from '#/api/forum/activity';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import ActivityFormModal from './modules/form.vue';

defineOptions({ name: 'ForumActivityManage' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: ActivityFormModal,
  destroyOnClose: true,
});

const router = useRouter();

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

/** 新建活动 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑活动 */
function handleEdit(row: ForumActivityApi.Activity) {
  formModalApi.setData(row).open();
}

/** 查看详情 */
function handleDetail(row: ForumActivityApi.Activity) {
  router.push({
    name: 'ForumActivityDetail',
    params: { id: row.id },
  });
}

/** 查看报名列表 */
function handleSignUp(row: ForumActivityApi.Activity) {
  router.push({
    name: 'ForumActivitySignUp',
    query: { activityId: row.id },
  });
}

function handleConfigureQuiz(row: ForumActivityApi.Activity) {
  router.push({
    name: 'QuizActivity',
    query: { activityId: row.id },
  });
}

/** 删除活动 */
async function handleDelete(row: ForumActivityApi.Activity) {
  try {
    await confirm({
      content: $t('ui.actionMessage.deleteConfirm', [row.title]),
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: $t('ui.actionMessage.deleting', [row.title]),
    key: 'action_key_msg',
  });
  try {
    await deleteActivity(row.id as number);
    message.success($t('ui.actionMessage.deleteSuccess', [row.title]));
    onRefresh();
  } finally {
    hide();
  }
}

/** 隐藏活动 */
async function handleHide(row: ForumActivityApi.Activity) {
  try {
    await confirm({
      content: `确定要隐藏活动「${row.title}」吗？隐藏后用户将无法在列表中看到该活动。`,
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: `正在隐藏活动「${row.title}」...`,
    key: 'action_key_msg',
  });
  try {
    await hideActivity(row.id as number);
    message.success(`隐藏成功`);
    onRefresh();
  } finally {
    hide();
  }
}

/** 展示已隐藏的活动 */
async function handleShow(row: ForumActivityApi.Activity) {
  const hide = message.loading({
    content: `正在展示活动「${row.title}」...`,
    key: 'action_key_msg',
  });
  try {
    await showActivity(row.id as number);
    message.success(`展示成功`);
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
          return await getActivityPage({
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
  } as VxeTableGridOptions<ForumActivityApi.Activity>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />

    <Grid table-title="活动列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '创建活动',
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['forum:activity:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['forum:activity:query'],
              onClick: handleDetail.bind(null, row),
            },
            {
              label: '报名',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['forum:activity-sign-up:query'],
              onClick: handleSignUp.bind(null, row),
            },
            {
              label: row.hidden ? '展示' : '隐藏',
              type: 'link',
              danger: !row.hidden,
              auth: ['forum:activity:update'],
              onClick: row.hidden
                ? handleShow.bind(null, row)
                : handleHide.bind(null, row),
            },
            {
              label: '配置答题',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              onClick: handleConfigureQuiz.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['forum:activity:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['forum:activity:delete'],
              onClick: handleDelete.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
