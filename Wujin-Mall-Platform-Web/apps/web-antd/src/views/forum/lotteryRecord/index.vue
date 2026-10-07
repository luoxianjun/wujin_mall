<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Popconfirm, Space } from 'ant-design-vue';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { requestClient } from '#/api/request';

import { useGridColumns, useGridFormSchema } from './data';

defineOptions({ name: 'ForumLotteryRecord' });

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    proxyConfig: {
      ajax: {
        query: async ({ page }: any) => {
          const params = {
            ...gridApi.formApi.form.values,
            pageNo: page.currentPage,
            pageSize: page.pageSize,
          };
          const res = await requestClient.get(
            '/gamification/lottery/record/page',
            { params },
          );
          return res;
        },
      },
    },
    pagerConfig: { enabled: true },
    toolbarConfig: {
      refresh: { code: 'query' },
    },
  },
  formOptions: {
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
});

const exporting = ref(false);

function buildQueryParams() {
  const values = gridApi.formApi.form.values;
  return {
    delivered: values.delivered,
    lotteryActivityId: values.lotteryActivityId,
    userId: values.userId,
    won: values.won,
  };
}

async function handleExport() {
  try {
    exporting.value = true;
    const data = await requestClient.download(
      '/gamification/lottery/record/export',
      {
        params: buildQueryParams(),
      },
    );
    downloadFileFromBlobPart({
      fileName: '抽奖记录.xlsx',
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

async function handleDeliver(id: number) {
  await requestClient.put('/gamification/lottery/record/deliver', null, {
    params: { id },
  });
  message.success('发放成功');
  gridApi.grid.commitProxy('query');
}
</script>

<template>
  <Page auto-content-height>
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '导出',
              type: 'primary',
              icon: 'lucide:download',
              auth: ['forum:lottery-record:export'],
              loading: exporting,
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <Space>
          <Popconfirm
            v-if="row.won && !row.delivered"
            title="确认发放奖品?"
            @confirm="handleDeliver(row.id)"
          >
            <Button size="small" type="link">发放</Button>
          </Popconfirm>
          <span v-else-if="row.delivered" style="color: #52c41a">已发放</span>
          <span v-else style="color: #999">-</span>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
