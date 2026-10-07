<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumSignRecordApi } from '#/api/forum/sign-record';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getForumSignRecordPage } from '#/api/forum/sign-record';

import { useGridColumns, useGridFormSchema } from './data';

const [Grid] = useVbenVxeGrid({
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
          const { dateRange, ...rest } = formValues;
          const [startDate, endDate] = dateRange || [];
          return await getForumSignRecordPage({
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
  } as VxeTableGridOptions<ForumSignRecordApi.SignRecord>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="签到记录列表" />
  </Page>
</template>
