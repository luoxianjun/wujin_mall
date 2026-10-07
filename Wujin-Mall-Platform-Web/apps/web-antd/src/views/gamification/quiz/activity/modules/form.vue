<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';
import type { QuizApi } from '#/api/gamification/quiz';

import { computed, reactive, ref, watch } from 'vue';

import {
  Alert,
  Button,
  Drawer,
  Form,
  FormItem,
  InputNumber,
  message,
  Select,
  Space,
  Switch,
} from 'ant-design-vue';

import { getActivity, getActivityPage } from '#/api/forum/activity';
import { getQuestionBank, getQuestionBankPage } from '#/api/gamification/quiz';

import RewardRuleEditor from './reward-rule-editor.vue';

const props = defineProps<{
  initialValue?: null | Partial<QuizApi.QuizActivity>;
  lockedActivityId?: number;
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: 'save', value: QuizApi.QuizActivity): void;
  (e: 'update:visible', value: boolean): void;
}>();

const OPTION_PAGE_SIZE = 100;

type SelectOption = {
  label: string;
  value: number;
};

const revealOptions = [
  { label: '每题作答后公布', value: 'PER_QUESTION' },
  { label: '提交后公布', value: 'AFTER_SUBMIT' },
  { label: '活动结束后公布', value: 'AFTER_ACTIVITY_END' },
  { label: '不公布', value: 'HIDDEN' },
] as const;

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已启用', value: 'ENABLED' },
  { label: '已停用', value: 'DISABLED' },
] as const;

const activityOptions = ref<SelectOption[]>([]);
const activityLoading = ref(false);
const bankOptions = ref<SelectOption[]>([]);
const questionBankLoading = ref(false);
const questionBankLoadError = ref('');
const formState = reactive<QuizApi.QuizActivity>({
  activityId: 0,
  answerRevealMode: 'AFTER_SUBMIT',
  durationSeconds: 600,
  leaderboardSize: 10,
  maxAttempts: 1,
  questionBankId: 0,
  questionCount: 10,
  randomOptionOrder: true,
  randomQuestionOrder: true,
  rewardRules: [],
  status: 'ENABLED',
});

const isEdit = computed(() => Boolean(props.initialValue?.id));
const isActivityLocked = computed(() => Boolean(props.lockedActivityId));

function filterSelectOption(input: string, option?: SelectOption) {
  return String(option?.label ?? '')
    .toLowerCase()
    .includes(input.toLowerCase());
}

function buildActivityLabel(activity: ForumActivityApi.Activity) {
  const title = activity.title?.trim() || `活动 #${activity.id}`;
  return `${title}${activity.id ? ` (ID:${activity.id})` : ''}`;
}

function buildQuestionBankLabel(questionBank: QuizApi.QuestionBank) {
  if (
    questionBank.questionCount === undefined ||
    questionBank.questionCount === null
  ) {
    return questionBank.name;
  }
  return `${questionBank.name} (${questionBank.questionCount} 题)`;
}

function ensureActivityOption(activity: ForumActivityApi.Activity) {
  if (!activity.id) {
    return;
  }
  if (!activityOptions.value.some((item) => item.value === activity.id)) {
    activityOptions.value = [
      ...activityOptions.value,
      {
        label: buildActivityLabel(activity),
        value: activity.id,
      },
    ];
  }
}

function ensureQuestionBankOption(questionBank: QuizApi.QuestionBank) {
  if (!questionBank.id) {
    return;
  }
  if (!bankOptions.value.some((item) => item.value === questionBank.id)) {
    bankOptions.value = [
      ...bankOptions.value,
      {
        label: buildQuestionBankLabel(questionBank),
        value: questionBank.id,
      },
    ];
  }
}

function resetForm() {
  Object.assign(formState, {
    id: props.initialValue?.id,
    activityId: Number(
      props.initialValue?.activityId ?? props.lockedActivityId ?? 0,
    ),
    answerRevealMode: props.initialValue?.answerRevealMode ?? 'AFTER_SUBMIT',
    durationSeconds: Number(props.initialValue?.durationSeconds ?? 600),
    leaderboardSize: Number(props.initialValue?.leaderboardSize ?? 10),
    maxAttempts: Number(props.initialValue?.maxAttempts ?? 1),
    questionBankId: Number(props.initialValue?.questionBankId ?? 0),
    questionCount: Number(props.initialValue?.questionCount ?? 10),
    randomOptionOrder: props.initialValue?.randomOptionOrder ?? true,
    randomQuestionOrder: props.initialValue?.randomQuestionOrder ?? true,
    rewardRules: [...(props.initialValue?.rewardRules ?? [])],
    status: props.initialValue?.status ?? 'ENABLED',
  });
}

async function loadActivityOptions() {
  activityLoading.value = true;
  try {
    if (isActivityLocked.value && props.lockedActivityId) {
      const activity = await getActivity(props.lockedActivityId);
      activityOptions.value = [
        {
          label: buildActivityLabel(activity),
          value: props.lockedActivityId,
        },
      ];
      return;
    }

    const response = await getActivityPage({
      pageNo: 1,
      pageSize: OPTION_PAGE_SIZE,
    });
    activityOptions.value = response.list
      .filter((item) => item.id)
      .map((item) => ({
        label: buildActivityLabel(item),
        value: item.id as number,
      }));

    if (
      formState.activityId &&
      !activityOptions.value.some((item) => item.value === formState.activityId)
    ) {
      const activity = await getActivity(formState.activityId);
      ensureActivityOption(activity);
    }
  } catch (error: any) {
    message.error(error?.message || '加载活动选项失败');
  } finally {
    activityLoading.value = false;
  }
}

async function loadQuestionBanks() {
  questionBankLoading.value = true;
  questionBankLoadError.value = '';
  try {
    const response = await getQuestionBankPage({
      pageNo: 1,
      pageSize: OPTION_PAGE_SIZE,
    });
    bankOptions.value = response.list
      .filter((item) => item.id)
      .map((item) => ({
        label: buildQuestionBankLabel(item),
        value: item.id as number,
      }));

    if (
      formState.questionBankId &&
      !bankOptions.value.some((item) => item.value === formState.questionBankId)
    ) {
      const questionBank = await getQuestionBank(formState.questionBankId);
      ensureQuestionBankOption(questionBank);
    }
  } catch (error: any) {
    bankOptions.value = [];
    questionBankLoadError.value =
      error?.message || '题库加载失败，请重试后再保存配置';
    message.error(questionBankLoadError.value);
  } finally {
    questionBankLoading.value = false;
  }
}

async function initializeOptions() {
  await Promise.all([loadActivityOptions(), loadQuestionBanks()]);
}

function closeDrawer() {
  emit('update:visible', false);
}

function handleSave() {
  if (!formState.activityId) {
    message.error('请选择活动');
    return;
  }
  if (!formState.questionBankId) {
    message.error('请选择题库');
    return;
  }
  emit('save', {
    ...formState,
    rewardRules: (formState.rewardRules ?? []).map((item) => ({
      ...item,
      pointAmount: Number(item.pointAmount ?? 0),
      rankEnd: Number(item.rankEnd ?? 1),
      rankStart: Number(item.rankStart ?? 1),
    })),
  });
}

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      return;
    }
    resetForm();
    void initializeOptions();
  },
  { immediate: true },
);

watch(
  () => props.initialValue,
  () => {
    resetForm();
  },
  { deep: true },
);
</script>

<template>
  <Drawer
    :open="visible"
    :title="isEdit ? '编辑答题配置' : '新建答题配置'"
    width="960"
    @close="closeDrawer"
  >
    <Form layout="vertical">
      <Alert
        v-if="questionBankLoadError"
        :message="questionBankLoadError"
        class="mb-4"
        show-icon
        type="error"
      />

      <div class="grid grid-cols-2 gap-4">
        <FormItem label="活动">
          <Select
            v-model:value="formState.activityId"
            :disabled="isActivityLocked"
            :filter-option="filterSelectOption"
            :loading="activityLoading"
            :options="activityOptions"
            class="w-full"
            option-filter-prop="label"
            placeholder="请选择活动"
            show-search
          />
        </FormItem>
        <FormItem label="题库">
          <Select
            v-model:value="formState.questionBankId"
            :filter-option="filterSelectOption"
            :loading="questionBankLoading"
            :not-found-content="
              questionBankLoading
                ? '题库加载中'
                : questionBankLoadError || '暂无可用题库'
            "
            :options="bankOptions"
            class="w-full"
            option-filter-prop="label"
            placeholder="请选择题库"
            show-search
          />
        </FormItem>
        <FormItem label="抽题数量">
          <InputNumber
            v-model:value="formState.questionCount"
            :min="1"
            class="w-full"
          />
        </FormItem>
        <FormItem label="最大作答次数">
          <InputNumber
            v-model:value="formState.maxAttempts"
            :min="1"
            class="w-full"
          />
        </FormItem>
        <FormItem label="答题时长(秒)">
          <InputNumber
            v-model:value="formState.durationSeconds"
            :min="1"
            class="w-full"
          />
        </FormItem>
        <FormItem label="排行榜人数">
          <InputNumber
            v-model:value="formState.leaderboardSize"
            :min="0"
            class="w-full"
          />
        </FormItem>
        <FormItem label="答案公布方式">
          <Select
            v-model:value="formState.answerRevealMode"
            :options="revealOptions"
          />
        </FormItem>
        <FormItem label="状态">
          <Select v-model:value="formState.status" :options="statusOptions" />
        </FormItem>
        <FormItem label="题目随机顺序">
          <Switch v-model:checked="formState.randomQuestionOrder" />
        </FormItem>
        <FormItem label="选项随机顺序">
          <Switch v-model:checked="formState.randomOptionOrder" />
        </FormItem>
      </div>

      <FormItem label="奖励规则">
        <RewardRuleEditor v-model="formState.rewardRules" />
      </FormItem>
    </Form>

    <template #footer>
      <Space>
        <Button @click="closeDrawer">取消</Button>
        <Button v-if="questionBankLoadError" @click="loadQuestionBanks">
          重试加载题库
        </Button>
        <Button type="primary" @click="handleSave">保存</Button>
      </Space>
    </template>
  </Drawer>
</template>
