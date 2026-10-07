<script lang="ts" setup>
import type { QuizApi } from '#/api/gamification/quiz';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart, formatDateTime } from '@vben/utils';

import {
  Button,
  Card,
  Input,
  message,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  createQuestionBank,
  downloadQuestionBankImportTemplate,
  getQuestionBank,
  getQuestionBankPage,
  importQuestionBankQuestions,
  updateQuestionBank,
} from '#/api/gamification/quiz';

import ImportModal from './modules/import-modal.vue';
import QuestionEditor from './modules/question-editor.vue';

defineOptions({ name: 'QuizQuestionBank' });

const loading = ref(false);
const visible = ref(false);
const importVisible = ref(false);
const readOnly = ref(false);
const records = ref<QuizApi.QuestionBank[]>([]);
const currentRecord = ref<null | Partial<QuizApi.QuestionBank>>(null);
const filters = reactive({
  name: '',
});

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '题库名称', dataIndex: 'name' },
  { title: '状态', dataIndex: 'enabled', width: 120 },
  { title: '题目数量', dataIndex: 'questionCount', width: 140 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  { title: '操作', dataIndex: 'action', width: 220, fixed: 'right' as const },
];

const preparedRecords = computed(() =>
  records.value.map((item) => ({
    ...item,
    questionCount: item.questionCount ?? item.questions?.length ?? 0,
  })),
);

async function loadRecords() {
  loading.value = true;
  try {
    const response = await getQuestionBankPage({
      enabled: undefined,
      name: filters.name || undefined,
      pageNo: 1,
      pageSize: 100,
    });
    records.value = response.list;
  } catch (error: any) {
    records.value = [];
    message.error(error?.message || '加载题库列表失败');
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  currentRecord.value = null;
  readOnly.value = false;
  visible.value = true;
}

async function openEdit(record: QuizApi.QuestionBank, readonly = false) {
  readOnly.value = readonly;
  currentRecord.value = await getQuestionBank(record.id as number);
  visible.value = true;
}

async function handleSave(payload: QuizApi.QuestionBank) {
  try {
    if (payload.id) {
      await updateQuestionBank(payload);
      message.success('题库已更新');
    } else {
      await createQuestionBank(payload);
      message.success('题库已创建');
    }
    visible.value = false;
    await loadRecords();
  } catch (error: any) {
    message.error(error?.message || '保存题库失败');
  }
}

async function handleImport(file: File) {
  try {
    const result = await importQuestionBankQuestions(file);
    message.success(
      `导入成功 ${result.successCount} 条，失败 ${result.failureCount} 条`,
    );
    importVisible.value = false;
    await loadRecords();
  } catch (error: any) {
    message.error(error?.message || '导入失败');
  }
}

async function handleDownloadTemplate() {
  try {
    const data = await downloadQuestionBankImportTemplate();
    const blob =
      data instanceof Blob
        ? data
        : new Blob([data], {
            type: 'application/vnd.ms-excel',
          });

    if (blob.size === 0) {
      throw new Error('下载内容为空');
    }

    if (
      blob.type.includes('application/json') ||
      blob.type.includes('text/plain')
    ) {
      const text = await blob.text();
      try {
        const parsed = JSON.parse(text);
        throw new Error(parsed?.msg || parsed?.message || '下载导入模板失败');
      } catch (error: any) {
        throw new Error(error?.message || '下载导入模板失败');
      }
    }

    downloadFileFromBlobPart({
      fileName: '答题题库导入模板.xls',
      source: blob,
    });
    message.success('导入模板下载成功');
  } catch (error: any) {
    message.error(error?.message || '下载导入模板失败');
  }
}

onMounted(() => {
  void loadRecords();
});
</script>

<template>
  <Page
    auto-content-height
    content-class="flex flex-col gap-4"
    description="管理可复用题库，并支持通过 Excel 批量导入题目。"
    title="题库管理"
  >
    <Card>
      <div class="mb-4 flex flex-wrap items-center gap-3">
        <Input
          v-model:value="filters.name"
          class="w-[240px]"
          placeholder="按题库名称搜索"
        />
        <Space>
          <Button @click="loadRecords">查询</Button>
          <Button type="primary" @click="openCreate">新建题库</Button>
          <Button @click="importVisible = true">导入 Excel</Button>
          <Button @click="handleDownloadTemplate">下载导入模板</Button>
        </Space>
      </div>

      <Table
        :columns="columns"
        :data-source="preparedRecords"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <Tag
            v-if="column.dataIndex === 'enabled'"
            :color="record.enabled ? 'green' : 'default'"
          >
            {{ record.enabled ? '启用' : '停用' }}
          </Tag>
          <span v-else-if="column.dataIndex === 'updateTime'">
            {{ record.updateTime ? formatDateTime(record.updateTime) : '-' }}
          </span>
          <Space v-else-if="column.dataIndex === 'action'">
            <Button type="link" @click="openEdit(record)">编辑</Button>
            <Button type="link" @click="openEdit(record, true)">
              查看题目
            </Button>
            <Button type="link" @click="importVisible = true">
              导入 Excel
            </Button>
          </Space>
        </template>
      </Table>
    </Card>

    <QuestionEditor
      v-model:visible="visible"
      :initial-value="currentRecord"
      :read-only="readOnly"
      @save="handleSave"
    />
    <ImportModal
      v-model:visible="importVisible"
      @download-template="handleDownloadTemplate"
      @import="handleImport"
    />
  </Page>
</template>
