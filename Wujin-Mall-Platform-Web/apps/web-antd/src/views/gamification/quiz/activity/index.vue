<script lang="ts" setup>
import type { QuizApi } from '#/api/gamification/quiz';

import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';
import { formatDateTime } from '@vben/utils';

import {
  Button,
  Card,
  InputNumber,
  message,
  Popover,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  createQuizActivity,
  getQuizActivityPage,
  updateQuizActivity,
} from '#/api/gamification/quiz';

import QuizActivityForm from './modules/form.vue';

defineOptions({ name: 'QuizActivityConfig' });

const DEFAULT_PAGE_SIZE = 20;
const MAX_PAGE_SIZE = 100;

function parseQueryId(value: unknown) {
  const normalized = Array.isArray(value) ? value[0] : value;
  const parsed = Number(normalized);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
}

const route = useRoute();
const lockedActivityId = parseQueryId(route.query.activityId);
const loading = ref(false);
const visible = ref(false);
const records = ref<QuizApi.QuizActivity[]>([]);
const currentRecord = ref<null | Partial<QuizApi.QuizActivity>>(null);
const filters = reactive({
  activityId: lockedActivityId,
  status: undefined as QuizApi.QuizActivity['status'] | undefined,
});
const pagination = reactive({
  current: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  pageSizeOptions: ['10', '20', '50', '100'],
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  total: 0,
});

const statusLabelMap: Record<string, string> = {
  DISABLED: '已停用',
  DRAFT: '草稿',
  ENABLED: '已启用',
};

const revealModeLabelMap: Record<string, string> = {
  AFTER_ACTIVITY_END: '活动结束后公布',
  AFTER_SUBMIT: '提交后公布',
  HIDDEN: '不公布',
  PER_QUESTION: '本题作答后公布',
};

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '活动', dataIndex: 'activityTitle', width: 240 },
  { title: '题库', dataIndex: 'questionBankName', width: 240 },
  { title: '抽题数量', dataIndex: 'questionCount', width: 120 },
  { title: '最大答题次数', dataIndex: 'maxAttempts', width: 140 },
  { title: '答题时长(秒)', dataIndex: 'durationSeconds', width: 140 },
  { title: '排行榜人数', dataIndex: 'leaderboardSize', width: 140 },
  { title: '答案公布方式', dataIndex: 'answerRevealMode', width: 180 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '奖励规则', dataIndex: 'rewardRules', width: 120 },
  { title: '操作', dataIndex: 'action', width: 120, fixed: 'right' as const },
];

function formatTime(value?: string) {
  return value ? formatDateTime(value) : '-';
}

function getActivityDisplay(record: QuizApi.QuizActivity) {
  return record.activityTitle || `活动 #${record.activityId}`;
}

function getQuestionBankDisplay(record: QuizApi.QuizActivity) {
  return record.questionBankName || `题库 #${record.questionBankId}`;
}

async function loadRecords() {
  loading.value = true;
  try {
    const response = await getQuizActivityPage({
      activityId: filters.activityId,
      pageNo: pagination.current,
      pageSize: Math.min(pagination.pageSize, MAX_PAGE_SIZE),
      status: filters.status,
    });
    records.value = response.list;
    pagination.total = response.total;
  } catch (error: any) {
    records.value = [];
    pagination.total = 0;
    message.error(error?.message || '加载答题配置列表失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  currentRecord.value = {
    activityId: lockedActivityId ?? filters.activityId ?? 0,
  };
  visible.value = true;
}

function openEdit(record: QuizApi.QuizActivity) {
  currentRecord.value = { ...record };
  visible.value = true;
}

async function handleSave(payload: QuizApi.QuizActivity) {
  try {
    if (payload.id) {
      await updateQuizActivity(payload);
      message.success('答题配置已更新');
    } else {
      await createQuizActivity(payload);
      message.success('答题配置已创建');
    }
    visible.value = false;
    await loadRecords();
  } catch (error: any) {
    message.error(error?.message || '保存答题配置失败');
  }
}

function handleSearch() {
  pagination.current = 1;
  void loadRecords();
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
    description="为论坛活动配置答题时长、题目数量、排行榜与奖励规则。"
    title="答题配置"
  >
    <Card>
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <InputNumber
          v-model:value="filters.activityId"
          :controls="false"
          class="w-[220px]"
          placeholder="按活动 ID 筛选"
        />
        <Select
          v-model:value="filters.status"
          :options="[
            { label: '草稿', value: 'DRAFT' },
            { label: '已启用', value: 'ENABLED' },
            { label: '已停用', value: 'DISABLED' },
          ]"
          allow-clear
          class="w-[180px]"
          placeholder="选择状态"
        />
        <Space>
          <Button @click="handleSearch">查询</Button>
          <Button type="primary" @click="openCreate">新建配置</Button>
        </Space>
      </div>

      <Table
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1500 }"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <Popover
            v-if="column.dataIndex === 'activityTitle'"
            placement="topLeft"
          >
            <template #content>
              <div class="w-[280px] space-y-2">
                <div class="text-sm font-semibold text-slate-900">
                  {{ getActivityDisplay(record) }}
                </div>
                <div class="text-xs text-slate-500">
                  活动 ID：{{ record.activityId }}
                </div>
                <div class="text-xs text-slate-500">
                  开始时间：{{ formatTime(record.activityStartTime) }}
                </div>
                <div class="text-xs text-slate-500">
                  结束时间：{{ formatTime(record.activityEndTime) }}
                </div>
              </div>
            </template>
            <Button class="px-0" type="link">
              {{ getActivityDisplay(record) }}
            </Button>
          </Popover>

          <Popover
            v-else-if="column.dataIndex === 'questionBankName'"
            placement="topLeft"
          >
            <template #content>
              <div class="w-[280px] space-y-2">
                <div class="text-sm font-semibold text-slate-900">
                  {{ getQuestionBankDisplay(record) }}
                </div>
                <div class="text-xs text-slate-500">
                  题库 ID：{{ record.questionBankId }}
                </div>
                <div class="text-xs text-slate-500">
                  总题目数量：{{ record.questionBankQuestionCount ?? '-' }}
                </div>
                <div class="text-xs text-slate-500">
                  创建时间：{{ formatTime(record.questionBankCreateTime) }}
                </div>
                <div class="text-xs text-slate-500">
                  更新时间：{{ formatTime(record.questionBankUpdateTime) }}
                </div>
              </div>
            </template>
            <Button class="px-0" type="link">
              {{ getQuestionBankDisplay(record) }}
            </Button>
          </Popover>

          <Tag
            v-else-if="column.dataIndex === 'status'"
            :color="record.status === 'ENABLED' ? 'green' : 'default'"
          >
            {{ statusLabelMap[record.status] || record.status }}
          </Tag>

          <span v-else-if="column.dataIndex === 'answerRevealMode'">
            {{
              revealModeLabelMap[record.answerRevealMode] ||
              record.answerRevealMode
            }}
          </span>

          <span v-else-if="column.dataIndex === 'rewardRules'">
            {{ record.rewardRules?.length ?? 0 }} 条
          </span>

          <Button
            v-else-if="column.dataIndex === 'action'"
            type="link"
            @click="openEdit(record)"
          >
            编辑
          </Button>
        </template>
      </Table>
    </Card>

    <QuizActivityForm
      v-model:visible="visible"
      :initial-value="currentRecord"
      :locked-activity-id="lockedActivityId"
      @save="handleSave"
    />
  </Page>
</template>
