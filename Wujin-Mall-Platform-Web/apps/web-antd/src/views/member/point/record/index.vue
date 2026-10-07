<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumPointRecordApi } from '#/api/forum/point-record';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportPointRecord,
  getForumPointRecordPage,
} from '#/api/forum/point-record';

import { useGridColumns, useGridFormSchema } from './data';

/** 导出积分记录 */
const exporting = ref(false);
async function handleExport() {
  try {
    exporting.value = true;
    const formValues = await gridApi.formApi?.getValues();
    const { createDate, ...rest } = formValues;
    const [startDate, endDate] = createDate || [];
    const params = {
      uid: rest.uid,
      userId: rest.userId,
      bizType: rest.bizType,
      title: rest.title,
      startDate,
      endDate,
    };

    const data = await exportPointRecord(params);
    downloadFileFromBlobPart({
      fileName: '积分明细列表.xlsx',
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
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const { createDate, ...rest } = formValues;
          const [startDate, endDate] = createDate || [];
          return await getForumPointRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            startDate,
            endDate,
            ...rest,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<ForumPointRecordApi.PointRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="积分记录列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '导出',
              type: 'primary',
              icon: 'lucide:download',
              auth: ['forum:point-record:export'],
              loading: exporting,
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
