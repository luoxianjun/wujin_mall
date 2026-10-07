<script lang="ts" setup>
import type { QuizAttemptApi } from '#/api/gamification/quiz';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { downloadFileFromBlobPart, formatDateTime } from '@vben/utils';

import {
  Button,
  Card,
  Input,
  InputNumber,
  message,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import { exportQuizAttempt, getQuizAttemptPage } from '#/api/gamification/quiz';

defineOptions({ name: 'QuizAttemptsPage' });

const DEFAULT_PAGE_SIZE = 20;
const MAX_PAGE_SIZE = 100;

const loading = ref(false);
const exporting = ref(false);
const records = ref<QuizAttemptApi.QuizAttempt[]>([]);
const filters = reactive({
  quizActivityId: undefined as number | undefined,
  activityId: undefined as number | undefined,
  status: undefined as string | undefined,
  userMobile: undefined as string | undefined,
  userNickname: undefined as string | undefined,
});
const pagination = reactive({
  current: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  pageSizeOptions: ['10', '20', '50', '100'],
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  total: 0,
});

const statusLabelMap: Record<string, { color: string; label: string }> = {
  IN_PROGRESS: { color: 'processing', label: '答题中' },
  INVALIDATED: { color: 'error', label: '已失效' },
  SUBMITTED: { color: 'success', label: '已提交' },
  TIMEOUT_SUBMITTED: { color: 'warning', label: '超时提交' },
};

const columns = [
  { dataIndex: 'id', title: 'ID', width: 70 },
  { dataIndex: 'activityTitle', title: '活动名称', width: 180 },
  { dataIndex: 'activityId', title: '活动ID', width: 90 },
  { dataIndex: 'quizActivityId', title: '答题活动ID', width: 110 },
  { dataIndex: 'userNickname', title: '用户名', width: 140 },
  { dataIndex: 'uid', title: 'UID', width: 120 },
  { dataIndex: 'userMobile', title: '手机号', width: 130 },
  { dataIndex: 'activityRank', title: '活动排名', width: 90 },
  { dataIndex: 'attemptNo', title: '第N次', width: 80 },
  { dataIndex: 'score', title: '得分', width: 80 },
  { dataIndex: 'elapsedMillis', title: '用时', width: 100 },
  { dataIndex: 'status', title: '状态', width: 100 },
  { dataIndex: 'signUpRemark', title: '报名信息', width: 220 },
  { dataIndex: 'invalidatedReason', title: '失效原因', width: 130 },
  { dataIndex: 'startedAt', title: '开始时间', width: 170 },
  { dataIndex: 'submittedAt', title: '提交时间', width: 170 },
];

function buildQueryParams() {
  return {
    activityId: filters.activityId,
    quizActivityId: filters.quizActivityId,
    status: filters.status,
    userMobile: filters.userMobile || undefined,
    userNickname: filters.userNickname || undefined,
  };
}

function formatTime(value?: string) {
  return value ? formatDateTime(value) : '-';
}

function formatElapsed(ms?: number) {
  if (ms === undefined || ms === null) return '-';
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}分${String(remainingSeconds).padStart(2, '0')}秒`;
}

async function loadRecords() {
  loading.value = true;
  try {
    const response = await getQuizAttemptPage({
      ...buildQueryParams(),
      pageNo: pagination.current,
      pageSize: Math.min(pagination.pageSize, MAX_PAGE_SIZE),
    });
    records.value = response.list;
    pagination.total = response.total;
  } catch (error: any) {
    records.value = [];
    pagination.total = 0;
    message.error(error?.message || '加载答题记录失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  void loadRecords();
}

async function handleExport() {
  exporting.value = true;
  try {
    const data = await exportQuizAttempt(buildQueryParams());
    downloadFileFromBlobPart({
      fileName: '答题记录.xlsx',
      source: data,
    });
    message.success('导出成功');
  } catch (error: any) {
    message.error(error?.message || '导出答题记录失败');
  } finally {
    exporting.value = false;
  }
}

function handleReset() {
  filters.quizActivityId = undefined;
  filters.activityId = undefined;
  filters.status = undefined;
  filters.userMobile = undefined;
  filters.userNickname = undefined;
  handleSearch();
}

function handleTableChange(page: { current?: number; pageSize?: number }) {
  pagination.current = page.current ?? 1;
  pagination.pageSize = Math.min(
    page.pageSize ?? DEFAULT_PAGE_SIZE,
    MAX_PAGE_SIZE,
  );
  void loadRecords();
}

onMounted(() => {
  void loadRecords();
});
</script>

<template>
  <Page
    auto-content-height
    content-class="flex flex-col gap-4"
    description="查看所有用户的答题记录、成绩与排名。"
    title="答题记录"
  >
    <Card>
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <InputNumber
          v-model:value="filters.activityId"
          :controls="false"
          class="w-[160px]"
          placeholder="活动 ID"
        />
        <InputNumber
          v-model:value="filters.quizActivityId"
          :controls="false"
          class="w-[160px]"
          placeholder="答题活动 ID"
        />
        <Input
          v-model:value="filters.userNickname"
          allow-clear
          class="w-[160px]"
          placeholder="用户昵称"
        />
        <Input
          v-model:value="filters.userMobile"
          allow-clear
          class="w-[160px]"
          placeholder="手机号"
        />
        <Select
          v-model:value="filters.status"
          :options="[
            { label: '已提交', value: 'SUBMITTED' },
            { label: '答题中', value: 'IN_PROGRESS' },
            { label: '超时提交', value: 'TIMEOUT_SUBMITTED' },
            { label: '已失效', value: 'INVALIDATED' },
          ]"
          allow-clear
          class="w-[150px]"
          placeholder="选择状态"
        />
        <Space>
          <Button type="primary" @click="handleSearch">查询</Button>
          <Button @click="handleReset">重置</Button>
          <Button :loading="exporting" type="primary" @click="handleExport">
            <template #icon>
              <IconifyIcon icon="lucide:download" />
            </template>
            导出
          </Button>
        </Space>
      </div>

      <Table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 2100 }"
        row-key="id"
        size="middle"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <span v-if="column.dataIndex === 'activityTitle'">
            {{ record.activityTitle || '-' }}
          </span>

          <span v-else-if="column.dataIndex === 'userNickname'">
            {{ record.userNickname || `用户#${record.userId}` }}
          </span>

          <span v-else-if="column.dataIndex === 'uid'">
            {{ record.uid || '-' }}
          </span>

          <span v-else-if="column.dataIndex === 'userMobile'">
            {{ record.userMobile || '-' }}
          </span>

          <span v-else-if="column.dataIndex === 'activityRank'">
            {{ record.activityRank || '-' }}
          </span>

          <span
            v-else-if="column.dataIndex === 'score'"
            :class="
              record.score >= 10
                ? 'text-green-600'
                : record.score >= 5
                  ? 'text-orange-500'
                  : 'text-red-500'
            "
            class="text-lg font-bold"
          >
            {{ record.score }}
          </span>

          <span v-else-if="column.dataIndex === 'elapsedMillis'">
            {{ formatElapsed(record.elapsedMillis) }}
          </span>

          <Tag
            v-else-if="column.dataIndex === 'status'"
            :color="statusLabelMap[record.status]?.color || 'default'"
          >
            {{ statusLabelMap[record.status]?.label || record.status }}
          </Tag>

          <span
            v-else-if="column.dataIndex === 'signUpRemark'"
            :title="record.signUpRemark || '-'"
            class="block max-w-[210px] truncate"
          >
            {{ record.signUpRemark || '-' }}
          </span>

          <span
            v-else-if="column.dataIndex === 'invalidatedReason'"
            class="text-xs text-gray-400"
          >
            {{ record.invalidatedReason || '-' }}
          </span>

          <span v-else-if="column.dataIndex === 'startedAt'">
            {{ formatTime(record.startedAt) }}
          </span>

          <span v-else-if="column.dataIndex === 'submittedAt'">
            {{ formatTime(record.submittedAt) }}
          </span>
        </template>
      </Table>
    </Card>
  </Page>
</template>
