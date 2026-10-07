<script lang="ts" setup>
import type { QuizApi } from '#/api/gamification/quiz';

import { computed } from 'vue';

import {
  Button,
  Input,
  InputNumber,
  Select,
  Space,
  Table,
} from 'ant-design-vue';

const props = defineProps<{
  modelValue: QuizApi.RewardRule[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: QuizApi.RewardRule[]): void;
}>();

const rewardTypeOptions = [
  { label: '积分奖励', value: 'POINTS' },
  { label: '实物奖励', value: 'PHYSICAL' },
] as const;

const rows = computed(() => props.modelValue ?? []);

function updateRow(index: number, patch: Partial<QuizApi.RewardRule>) {
  const next = [...rows.value];
  next[index] = { ...next[index], ...patch };
  emit('update:modelValue', next);
}

function addRow() {
  emit('update:modelValue', [
    ...rows.value,
    {
      rankStart: rows.value.length + 1,
      rankEnd: rows.value.length + 1,
      rewardType: 'POINTS',
      pointAmount: 0,
      rewardName: '',
    },
  ]);
}

function removeRow(index: number) {
  emit(
    'update:modelValue',
    rows.value.filter((_, currentIndex) => currentIndex !== index),
  );
}

const columns = [
  { title: '名次起始', dataIndex: 'rankStart', width: 120 },
  { title: '名次结束', dataIndex: 'rankEnd', width: 120 },
  { title: '奖励类型', dataIndex: 'rewardType', width: 140 },
  { title: '积分数量', dataIndex: 'pointAmount', width: 140 },
  { title: '奖励名称', dataIndex: 'rewardName', width: 320 },
  { title: '操作', dataIndex: 'action', width: 100 },
];
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex items-center justify-between">
      <div class="text-sm text-gray-500">
        配置名次区间、奖励类型、积分数量和奖励名称。
      </div>
      <Button type="dashed" @click="addRow">新增规则</Button>
    </div>

    <Table
      :columns="columns"
      :data-source="rows"
      :pagination="false"
      :row-key="(_, index) => index"
      :scroll="{ x: 960 }"
      size="small"
    >
      <template #bodyCell="{ column, index, record }">
        <InputNumber
          v-if="column.dataIndex === 'rankStart'"
          :min="1"
          :value="record.rankStart"
          class="w-full"
          @update:value="
            (value) => updateRow(index, { rankStart: Number(value ?? 1) })
          "
        />
        <InputNumber
          v-else-if="column.dataIndex === 'rankEnd'"
          :min="1"
          :value="record.rankEnd"
          class="w-full"
          @update:value="
            (value) => updateRow(index, { rankEnd: Number(value ?? 1) })
          "
        />
        <Select
          v-else-if="column.dataIndex === 'rewardType'"
          :options="rewardTypeOptions"
          :value="record.rewardType"
          class="w-full"
          @update:value="
            (value) =>
              updateRow(index, {
                rewardType: value as QuizApi.RewardRule['rewardType'],
              })
          "
        />
        <InputNumber
          v-else-if="column.dataIndex === 'pointAmount'"
          :disabled="record.rewardType === 'PHYSICAL'"
          :min="0"
          :value="record.pointAmount"
          class="w-full"
          @update:value="
            (value) => updateRow(index, { pointAmount: Number(value ?? 0) })
          "
        />
        <Input
          v-else-if="column.dataIndex === 'rewardName'"
          :value="record.rewardName"
          class="min-w-[260px]"
          @update:value="(value) => updateRow(index, { rewardName: value })"
        />
        <Space v-else-if="column.dataIndex === 'action'">
          <Button danger type="link" @click="removeRow(index)">删除</Button>
        </Space>
      </template>
    </Table>
  </div>
</template>
