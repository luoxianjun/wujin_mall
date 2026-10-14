<script lang="ts" setup>
import type { CategoryEditorForm } from '../tree';

import { computed } from 'vue';

import { Button, Input, InputNumber, Select, Switch } from 'ant-design-vue';

import {
  CATEGORY_HEALTH_OPTIONS,
  CATEGORY_LANE_OPTIONS,
  MAX_CATEGORY_LEVEL,
} from '../tree';

const props = defineProps<{
  codeInherited: boolean;
  mode: 'create' | 'edit';
  parentCode?: string;
  saving: boolean;
  showLane: boolean;
}>();

const emit = defineEmits<{ cancel: []; save: [] }>();

const form = defineModel<CategoryEditorForm>('form', { required: true });

const codePrefix = computed(() =>
  props.codeInherited && props.parentCode ? `${props.parentCode}.` : '',
);
</script>

<template>
  <div class="category-inline-editor" @click.stop>
    <div class="editor-field">
      <span class="editor-label">类目名称</span>
      <Input
        v-model:value="form.name"
        size="small"
        placeholder="请输入类目名称"
        :maxlength="64"
        @press-enter="emit('save')"
      />
    </div>
    <div class="editor-field">
      <span class="editor-label">类目编码</span>
      <Input
        v-model:value="form.codeSuffix"
        size="small"
        :addon-before="codePrefix || undefined"
        :placeholder="codePrefix ? '例如 CAR' : '例如 P'"
        :maxlength="64"
      />
    </div>
    <div v-if="showLane" class="editor-field">
      <span class="editor-label">泳道</span>
      <Select
        v-model:value="form.lane"
        size="small"
        :options="CATEGORY_LANE_OPTIONS"
      />
    </div>
    <div class="editor-row">
      <div class="editor-field">
        <span class="editor-label">搜索展示层级</span>
        <InputNumber
          v-model:value="form.displayDepth"
          class="w-full"
          size="small"
          :min="1"
          :max="MAX_CATEGORY_LEVEL"
        />
      </div>
      <div class="editor-field">
        <span class="editor-label">健康状态</span>
        <Select
          v-model:value="form.healthStatus"
          size="small"
          allow-clear
          :options="CATEGORY_HEALTH_OPTIONS"
        />
      </div>
    </div>
    <div class="editor-field editor-field--inline">
      <span class="editor-label">状态</span>
      <Switch
        v-model:checked="form.enabled"
        size="small"
        checked-children="开启"
        un-checked-children="关闭"
      />
    </div>
    <div class="editor-field">
      <span class="editor-label">说明</span>
      <Input.TextArea
        v-model:value="form.description"
        :rows="2"
        :maxlength="255"
      />
    </div>
    <div class="editor-actions">
      <Button size="small" @click="emit('cancel')">取消</Button>
      <Button
        size="small"
        type="primary"
        :loading="saving"
        @click="emit('save')"
      >
        {{ mode === 'create' ? '新增' : '保存' }}
      </Button>
    </div>
  </div>
</template>

<style scoped>
.category-inline-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  margin: 4px 0;
  background: hsl(var(--background));
  border: 1px solid hsl(var(--primary));
  border-radius: 6px;
}

.editor-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.editor-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.editor-field--inline {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.editor-label {
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}

.editor-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
