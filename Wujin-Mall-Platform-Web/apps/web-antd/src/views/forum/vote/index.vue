<script lang="ts" setup>
import { Page, useVbenModal } from '@vben/common-ui';

import { Button, message, Popconfirm, Space, Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { requestClient } from '#/api/request';

import { useGridColumns, useGridFormSchema } from './data';
import VoteForm from './VoteForm.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: VoteForm,
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
            '/gamification/vote/activity/page',
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

async function handleEdit(row: any) {
  const detail = await requestClient.get(
    '/gamification/vote/activity/get',
    { params: { id: row.id } },
  );
  formModalApi.setData(detail).open();
}

async function handleDelete(id: number) {
  await requestClient.delete('/gamification/vote/activity/delete', {
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
        <Button type="primary" @click="handleCreate">新增投票活动</Button>
      </template>
      <template #actions="{ row }">
        <Space>
          <Button size="small" type="link" @click="handleEdit(row)">
            编辑
          </Button>
          <Popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
            <Button danger size="small" type="link">删除</Button>
          </Popconfirm>
          <Tag v-if="row.status === 0" color="green">启用</Tag>
          <Tag v-else color="red">禁用</Tag>
        </Space>
      </template>
    </Grid>
  </Page>
</template>
