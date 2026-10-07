<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumActivityApi } from '#/api/forum/activity';

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportActivitySignUp,
  getActivitySignUpPage,
} from '#/api/forum/activity';

import { useGridColumns, useGridFormSchema } from './data';
import ApproveModal from './modules/approve-modal.vue';
import SignUpDetailDrawer from './modules/detail.vue';
import FeedbackModal from './modules/feedback-modal.vue';

defineOptions({ name: 'ForumActivitySignUpManage' });

const route = useRoute();
const activityIdRaw = Number(route?.query?.activityId);
const hasActivityId = Number.isFinite(activityIdRaw);
const activityIdParam = hasActivityId ? activityIdRaw : undefined;

const [Approve, approveModalApi] = useVbenModal({
  connectedComponent: ApproveModal,
  destroyOnClose: true,
});

const [Feedback, feedbackModalApi] = useVbenModal({
  connectedComponent: FeedbackModal,
  destroyOnClose: true,
});

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: SignUpDetailDrawer,
});

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

function handleDetail(row: ForumActivityApi.SignUp) {
  detailDrawerApi.setData(row).open();
}

function handleApprove(row: ForumActivityApi.SignUp) {
  approveModalApi.setData(row).open();
}

function handleFeedback(row: ForumActivityApi.SignUp) {
  feedbackModalApi.setData(row).open();
}

/** 导出报名列表 */
const exporting = ref(false);
async function handleExport() {
  try {
    exporting.value = true;
    const formValues = await gridApi.formApi?.getValues();
    const params = {
      activityId:
        formValues?.activityId === undefined
          ? activityIdParam
          : formValues.activityId,
      approvalStatus: formValues?.approvalStatus,
    };

    const data = await exportActivitySignUp(params);
    downloadFileFromBlobPart({
      fileName: '活动报名列表.xlsx',
      source: data,
    });
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败');
  } finally {
    exporting.value = false;
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
    defaultValues: {
      activityId: activityIdParam,
    },
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getActivitySignUpPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
            activityId:
              formValues.activityId === undefined
                ? activityIdParam
                : formValues.activityId,
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
  } as VxeTableGridOptions<ForumActivityApi.SignUp>,
});

onMounted(async () => {
  if (hasActivityId && activityIdParam !== undefined) {
    await gridApi.formApi?.setValues({ activityId: activityIdParam });
    await gridApi.query({ activityId: activityIdParam });
  }
  if (hasActivityId && gridApi.formApi?.resetForm) {
    const originalReset = gridApi.formApi.resetForm.bind(gridApi.formApi);
    gridApi.formApi.resetForm = async (...args: any[]) => {
      await originalReset(...args);
      await gridApi.formApi?.setValues({ activityId: activityIdParam });
    };
  }
});
</script>

<template>
  <Page auto-content-height>
    <Approve @success="onRefresh" />
    <Feedback @success="onRefresh" />
    <DetailDrawer />

    <Grid table-title="活动报名管理">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '导出',
              type: 'primary',
              icon: 'lucide:download',
              auth: ['forum:activity-sign-up:export'],
              loading: exporting,
              onClick: handleExport,
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
              onClick: handleDetail.bind(null, row),
            },
            ...(row.approvalStatus !== 3
              ? [
                  {
                    label: '审核',
                    type: 'link',
                    auth: ['forum:activity-sign-up:approve'],
                    onClick: handleApprove.bind(null, row),
                  },
                ]
              : []),
            {
              label: '点评',
              type: 'link',
              auth: ['forum:activity-sign-up:feedback'],
              onClick: handleFeedback.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
