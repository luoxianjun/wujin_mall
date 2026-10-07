<script lang="ts" setup>
import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Popconfirm, Space } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { requestClient } from '#/api/request';

import { useGridColumns, useGridFormSchema } from './data';
import PrizeForm from './PrizeForm.vue';

defineOptions({ name: 'ForumLotteryPrize' });

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: PrizeForm,
});

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useGridColumns(),
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const params = {
            ...gridApi.formApi.form.values,
            pageNo: page.currentPage,
            pageSize: page.pageSize,
          };
          const res = await requestClient.get(
            '/gamification/lottery/prize/page',
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

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleEdit(row: any) {
  formModalApi.setData(row).open();
}

async function handleDelete(id: number) {
  await requestClient.delete('/gamification/lottery/prize/delete', {
    params: { id },
  });
  message.success('删除成功');
  gridApi.grid.commitProxy('query');
}

function handleFormSuccess() {
  gridApi.grid.commitProxy('query');
}
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleFormSuccess" />
    <Grid>
      <template #toolbar-tools>
        <Button type="primary" @click="handleCreate">新增奖品</Button>
      </template>
      <template #actions="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleEdit(row)">
            编辑
          </Button>
          <Popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
