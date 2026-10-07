<script lang="ts" setup>
import type { QuizApi } from '#/api/gamification/quiz';

import { computed, reactive, watch } from 'vue';

import {
  Button,
  Divider,
  Drawer,
  Form,
  FormItem,
  Input,
  InputNumber,
  Select,
  Space,
  Switch,
} from 'ant-design-vue';

import { ImageUpload } from '#/components/upload';

const props = defineProps<{
  initialValue?: null | Partial<QuizApi.QuestionBank>;
  readOnly?: boolean;
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: 'save', value: QuizApi.QuestionBank): void;
  (e: 'update:visible', value: boolean): void;
}>();

const questionTypeOptions = [
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' },
] as const;

const formState = reactive<QuizApi.QuestionBank>({
  description: '',
  enabled: true,
  name: '',
  questions: [],
});

const title = computed(() => {
  if (props.readOnly) {
    return '查看题目';
  }
  if (props.initialValue?.id) {
    return '编辑题库';
  }
  return '新建题库';
});

function createDefaultQuestion(
  type: QuizApi.Question['questionType'] = 'SINGLE_CHOICE',
): QuizApi.Question {
  if (type === 'TRUE_FALSE') {
    return {
      content: '',
      explanation: '',
      imageUrl: '',
      options: [
        { content: '正确', isCorrect: true, optionKey: 'TRUE', sort: 1 },
        { content: '错误', isCorrect: false, optionKey: 'FALSE', sort: 2 },
      ],
      questionType: type,
      score: 1,
      sort: 1,
    };
  }
  return {
    content: '',
    explanation: '',
    imageUrl: '',
    options: [
      { content: '', isCorrect: true, optionKey: 'A', sort: 1 },
      { content: '', isCorrect: false, optionKey: 'B', sort: 2 },
      { content: '', isCorrect: false, optionKey: 'C', sort: 3 },
      { content: '', isCorrect: false, optionKey: 'D', sort: 4 },
    ],
    questionType: type,
    score: 1,
    sort: 1,
  };
}

function resetForm() {
  Object.assign(formState, {
    description: props.initialValue?.description ?? '',
    enabled: props.initialValue?.enabled ?? true,
    id: props.initialValue?.id,
    name: props.initialValue?.name ?? '',
    questions:
      props.initialValue?.questions?.map((question, index) => ({
        ...question,
        options:
          question.options?.map((option, optionIndex) => ({
            ...option,
            sort: option.sort ?? optionIndex + 1,
          })) ?? [],
        sort: question.sort ?? index + 1,
      })) ?? [],
  });
}

function closeDrawer() {
  emit('update:visible', false);
}

function addQuestion() {
  formState.questions.push({
    ...createDefaultQuestion('SINGLE_CHOICE'),
    sort: formState.questions.length + 1,
  });
}

function removeQuestion(index: number) {
  formState.questions.splice(index, 1);
}

function handleQuestionTypeChange(
  index: number,
  type: QuizApi.Question['questionType'],
) {
  formState.questions[index] = {
    ...createDefaultQuestion(type),
    content: formState.questions[index].content,
    explanation: formState.questions[index].explanation,
    imageUrl: formState.questions[index].imageUrl,
    score: formState.questions[index].score,
    sort: formState.questions[index].sort,
  };
}

function handleSave() {
  emit('save', {
    ...formState,
    questions: formState.questions.map((question, questionIndex) => ({
      ...question,
      options: question.options.map((option, optionIndex) => ({
        ...option,
        sort: optionIndex + 1,
      })),
      sort: questionIndex + 1,
    })),
  });
}

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      resetForm();
    }
  },
  { immediate: true },
);
</script>

<template>
  <Drawer :open="visible" :title="title" width="880" @close="closeDrawer">
    <Form layout="vertical">
      <div class="grid grid-cols-2 gap-4">
        <FormItem label="题库名称">
          <Input v-model:value="formState.name" :disabled="readOnly" />
        </FormItem>
        <FormItem label="启用状态">
          <Switch v-model:checked="formState.enabled" :disabled="readOnly" />
        </FormItem>
      </div>
      <FormItem label="题库说明">
        <Input v-model:value="formState.description" :disabled="readOnly" />
      </FormItem>

      <div class="mb-4 flex items-center justify-between">
        <div class="text-sm text-gray-500">
          支持单选题、多选题、判断题，并配置题干、图片、分值、选项和解析。
        </div>
        <Button v-if="!readOnly" type="dashed" @click="addQuestion">
          新增题目
        </Button>
      </div>

      <div
        v-for="(question, questionIndex) in formState.questions"
        :key="questionIndex"
        class="mb-6 rounded-lg border border-gray-200 p-4"
      >
        <div class="mb-3 flex items-center justify-between">
          <div class="font-medium">题目 {{ questionIndex + 1 }}</div>
          <Button
            v-if="!readOnly"
            danger
            type="link"
            @click="removeQuestion(questionIndex)"
          >
            删除
          </Button>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <FormItem label="题型">
            <Select
              :disabled="readOnly"
              :options="questionTypeOptions"
              :value="question.questionType"
              @update:value="
                (value) =>
                  handleQuestionTypeChange(
                    questionIndex,
                    value as QuizApi.Question['questionType'],
                  )
              "
            />
          </FormItem>
          <FormItem label="分值">
            <InputNumber
              v-model:value="question.score"
              :disabled="readOnly"
              :min="0"
              class="w-full"
            />
          </FormItem>
        </div>
        <FormItem label="题干">
          <Input v-model:value="question.content" :disabled="readOnly" />
        </FormItem>
        <FormItem label="题目图片">
          <ImageUpload
            v-model:value="question.imageUrl"
            :disabled="readOnly"
            :show-description="false"
          />
        </FormItem>
        <FormItem label="答案解析">
          <Input v-model:value="question.explanation" :disabled="readOnly" />
        </FormItem>

        <Divider orientation="left">选项</Divider>
        <div
          v-for="(option, optionIndex) in question.options"
          :key="`${questionIndex}-${optionIndex}`"
          class="mb-3 grid grid-cols-[120px_1fr_120px] gap-3"
        >
          <Input :value="option.optionKey" disabled />
          <Input v-model:value="option.content" :disabled="readOnly" />
          <Select
            v-model:value="option.isCorrect"
            :disabled="readOnly"
            :options="[
              { label: '正确答案', value: true },
              { label: '错误答案', value: false },
            ]"
          />
        </div>
      </div>
    </Form>

    <template #footer>
      <Space>
        <Button v-if="!readOnly" type="dashed" @click="addQuestion">
          新增题目
        </Button>
        <Button @click="closeDrawer">关闭</Button>
        <Button v-if="!readOnly" type="primary" @click="handleSave">
          保存
        </Button>
      </Space>
    </template>
  </Drawer>
</template>
