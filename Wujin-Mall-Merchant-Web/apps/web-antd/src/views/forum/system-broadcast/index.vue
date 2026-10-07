<script lang="ts" setup>
import type { TableColumnsType } from 'ant-design-vue';
import type { SystemBroadcastApi } from '#/api/forum/systemBroadcast';

import { useAccess } from '@vben/access';
import { Page, useVbenModal } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import { Button, Card, Table, Tag } from 'ant-design-vue';
import { h, ref } from 'vue';

import { getBroadcastPage } from '#/api/forum/systemBroadcast';

import SendForm from './modules/send-form.vue';

defineOptions({ name: 'SystemBroadcast' });

const { hasAccessByCodes } = useAccess();

const loading = ref(false);
const tableData = ref<SystemBroadcastApi.Broadcast[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const columns: TableColumnsType<SystemBroadcastApi.Broadcast> = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '标题', dataIndex: 'title', ellipsis: true },
  {
    title: '发送时间',
    dataIndex: 'createTime',
    width: 180,
    customRender: ({ record }) =>
      record.createTime
        ? formatDate(record.createTime, 'YYYY-MM-DD HH:mm:ss')
        : '-',
  },
  { title: '成功', dataIndex: 'successCount', width: 80 },
  { title: '失败', dataIndex: 'failCount', width: 80 },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const statusMap: Record<number, { color: string; text: string }> = {
        0: { color: 'processing', text: '发送中' },
        1: { color: 'success', text: '已完成' },
        2: { color: 'error', text: '失败' },
      };
      const status = statusMap[record.status ?? 0] || {
        color: 'default',
        text: '未知',
      };
      return h(Tag, { color: status.color }, () => status.text);
    },
  },
];

async function loadData() {
  loading.value = true;
  try {
    const { list, total } = await getBroadcastPage({
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
    });
    tableData.value = list ?? [];
    pagination.value.total = total ?? 0;
  } finally {
    loading.value = false;
  }
}

function handleTableChange(pag: { current?: number; pageSize?: number }) {
  pagination.value.current = pag.current || 1;
  pagination.value.pageSize = pag.pageSize || 10;
  void loadData();
}

const [SendModal, sendModalApi] = useVbenModal({
  connectedComponent: SendForm,
  onOpenChange(open) {
    if (!open) {
      void loadData();
    }
  },
});

function openSendModal() {
  sendModalApi.open();
}

void loadData();
</script>

<template>
  <Page title="系统广播消息">
    <Card>
      <template #extra>
        <Button
          v-if="hasAccessByCodes(['forum:system-broadcast:send'])"
          type="primary"
          @click="openSendModal"
        >
          发送消息
        </Button>
      </template>

      <Table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      />
    </Card>

    <SendModal />
  </Page>
</template>
