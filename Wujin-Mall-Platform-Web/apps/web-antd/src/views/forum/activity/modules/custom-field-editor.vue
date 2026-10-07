<script lang="ts" setup>
import { ref, watch } from 'vue';
import {
  Button,
  Card,
  Input,
  Select,
  Switch,
  Space,
  Empty,
  Tag,
} from 'ant-design-vue';

import { customFieldTypeOptions } from '../data';

defineOptions({ name: 'CustomFieldEditor' });

export interface CustomField {
  key: string;
  type: 'checkbox' | 'date' | 'file' | 'input' | 'radio' | 'select' | 'textarea';
  required: boolean;
  options?: string[];
}

const props = defineProps<{
  modelValue?: CustomField[] | string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: CustomField[]];
}>();

// 内部字段列表
const fields = ref<CustomField[]>([]);

// 需要显示选项配置的类型
const typesWithOptions = ['select', 'radio', 'checkbox'];

// 解析 modelValue
function parseModelValue(value: CustomField[] | string | undefined): CustomField[] {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }
  return [];
}

// 初始化
watch(
  () => props.modelValue,
  (newVal) => {
    const parsed = parseModelValue(newVal);
    // 只在真正变化时更新，避免循环
    if (JSON.stringify(parsed) !== JSON.stringify(fields.value)) {
      fields.value = parsed.map((f) => ({
        key: f.key || '',
        type: f.type || 'input',
        required: f.required ?? false,
        options: f.options || [],
      }));
    }
  },
  { immediate: true },
);

// 当内部 fields 变化时，emit 出去
watch(
  fields,
  (newFields) => {
    emit('update:modelValue', newFields);
  },
  { deep: true },
);

// 添加新字段
function addField() {
  fields.value.push({
    key: '',
    type: 'input',
    required: false,
    options: [],
  });
}

// 删除字段
function removeField(index: number) {
  fields.value.splice(index, 1);
}

// 上移字段
function moveFieldUp(index: number) {
  if (index <= 0) return;
  const temp = fields.value[index];
  fields.value[index] = fields.value[index - 1]!;
  fields.value[index - 1] = temp!;
}

// 下移字段
function moveFieldDown(index: number) {
  if (index >= fields.value.length - 1) return;
  const temp = fields.value[index];
  fields.value[index] = fields.value[index + 1]!;
  fields.value[index + 1] = temp!;
}

// 更新字段名
function updateFieldKey(index: number, value: string) {
  fields.value[index]!.key = value;
}

// 更新字段类型
function updateFieldType(index: number, value: CustomField['type']) {
  fields.value[index]!.type = value;
  // 如果类型不需要选项，清空选项
  if (!typesWithOptions.includes(value)) {
    fields.value[index]!.options = [];
  }
}

// 更新是否必填
function updateFieldRequired(index: number, value: boolean) {
  fields.value[index]!.required = value;
}

// 更新选项 (逗号分隔字符串转数组)
function updateFieldOptions(index: number, value: string) {
  fields.value[index]!.options = value
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
}

// 获取选项显示字符串
function getOptionsString(field: CustomField): string {
  return field.options?.join(', ') || '';
}
</script>

<template>
  <div class="custom-field-editor">
    <!-- 空状态 -->
    <Empty
      v-if="fields.length === 0"
      description="暂无自定义字段"
      class="py-4"
    >
      <Button type="primary" @click="addField">
        + 添加字段
      </Button>
    </Empty>

    <!-- 字段列表 -->
    <div v-else class="field-list">
      <Card
        v-for="(field, index) in fields"
        :key="index"
        size="small"
        class="field-card mb-3"
      >
        <template #title>
          <div class="flex items-center gap-2">
            <Tag color="blue">字段 {{ index + 1 }}</Tag>
            <span v-if="field.key" class="text-sm text-gray-600">{{ field.key }}</span>
          </div>
        </template>
        <template #extra>
          <Space>
            <Button
              type="text"
              size="small"
              :disabled="index === 0"
              title="上移"
              @click="moveFieldUp(index)"
            >
              ↑
            </Button>
            <Button
              type="text"
              size="small"
              :disabled="index === fields.length - 1"
              title="下移"
              @click="moveFieldDown(index)"
            >
              ↓
            </Button>
            <Button
              type="text"
              size="small"
              danger
              title="删除"
              @click="removeField(index)"
            >
              ✕
            </Button>
          </Space>
        </template>

        <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
          <!-- 字段名 -->
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700">
              字段名 <span class="text-red-500">*</span>
            </label>
            <Input
              :value="field.key"
              placeholder="请输入字段名"
              @update:value="updateFieldKey(index, $event)"
            />
          </div>

          <!-- 字段类型 -->
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700">
              字段类型 <span class="text-red-500">*</span>
            </label>
            <Select
              :value="field.type"
              :options="customFieldTypeOptions"
              placeholder="请选择类型"
              class="w-full"
              @update:value="updateFieldType(index, $event)"
            />
          </div>

          <!-- 是否必填 -->
          <div>
            <label class="mb-1 block text-sm font-medium text-gray-700">
              是否必填
            </label>
            <div class="flex h-8 items-center">
              <Switch
                :checked="field.required"
                checked-children="是"
                un-checked-children="否"
                @update:checked="updateFieldRequired(index, $event)"
              />
            </div>
          </div>
        </div>

        <div v-if="field.type === 'file'" class="mt-3 text-sm text-gray-500">
          支持 jpg、png、pdf、doc、docx，报名后导出为文件链接。
        </div>

        <!-- 选项配置 (仅针对 select/radio/checkbox) -->
        <div
          v-if="typesWithOptions.includes(field.type)"
          class="mt-3"
        >
          <label class="mb-1 block text-sm font-medium text-gray-700">
            选项 <span class="text-gray-400">(用逗号分隔)</span>
          </label>
          <Input
            :value="getOptionsString(field)"
            placeholder="例如：选项1, 选项2, 选项3"
            @update:value="updateFieldOptions(index, $event)"
          />
        </div>
      </Card>

      <!-- 添加按钮 -->
      <Button type="dashed" block @click="addField">
        + 添加字段
      </Button>
    </div>
  </div>
</template>

<style scoped>
.custom-field-editor {
  width: 100%;
}

.field-card {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
}

.field-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}
</style>
