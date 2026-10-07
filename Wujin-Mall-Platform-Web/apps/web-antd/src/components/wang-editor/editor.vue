<script lang="ts" setup>
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor';
import type { PropType } from 'vue';

import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue';

import { usePreferences } from '@vben/preferences';

import { Editor, Toolbar } from '@wangeditor/editor-for-vue';

import { useUpload } from '#/components/upload/use-upload';

import '@wangeditor/editor/dist/css/style.css';

defineOptions({ name: 'WangEditor' });

const props = defineProps({
  /** v-model:value 绑定值（兼容 Vben Form 的 baseModelPropName: 'value'） */
  value: {
    type: String,
    default: '',
  },
  /** v-model 绑定值 */
  modelValue: {
    type: String,
    default: '',
  },
  /** Vben Form 传递的更新函数 */
  'onUpdate:value': {
    type: Function as PropType<(val: string) => void>,
    default: undefined,
  },
  'onUpdate:modelValue': {
    type: Function as PropType<(val: string) => void>,
    default: undefined,
  },
  height: {
    type: [Number, String] as PropType<number | string>,
    default: 400,
  },
  placeholder: {
    type: String,
    default: '请输入内容...',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits<{
  (e: 'change', value: string): void;
  (e: 'update:value', value: string): void;
  (e: 'update:modelValue', value: string): void;
}>();

// 编辑器实例
const editorRef = shallowRef<IDomEditor>();

// 内容值
const valueHtml = ref(props.value || props.modelValue);

// 主题
const { isDark } = usePreferences();
const mode = computed(() => (isDark.value ? 'default' : 'default'));

// 高度
const editorHeight = computed(() => {
  const h = props.height;
  if (typeof h === 'number') {
    return `${h}px`;
  }
  return h;
});

// 监听外部传入的 value 变化
watch(
  () => props.value,
  (val) => {
    if (val !== undefined && val !== valueHtml.value) {
      valueHtml.value = val;
    }
  },
);

// 监听外部传入的 modelValue 变化
watch(
  () => props.modelValue,
  (val) => {
    if (val !== undefined && val !== valueHtml.value) {
      valueHtml.value = val;
    }
  },
);

// 工具栏配置
const toolbarConfig: Partial<IToolbarConfig> = {};

// 编辑器配置
const editorConfig = computed<Partial<IEditorConfig>>(() => ({
  placeholder: props.placeholder,
  readOnly: props.disabled,
  MENU_CONF: {
    uploadImage: {
      // 自定义上传图片
      async customUpload(file: File, insertFn: (url: string) => void) {
        try {
          const { httpRequest } = useUpload();
          const url = await httpRequest(file);
          insertFn(url);
        } catch (error) {
          console.error('上传图片失败:', error);
        }
      },
    },
    uploadVideo: {
      // 自定义上传视频
      async customUpload(file: File, insertFn: (url: string) => void) {
        try {
          const { httpRequest } = useUpload();
          const url = await httpRequest(file);
          insertFn(url);
        } catch (error) {
          console.error('上传视频失败:', error);
        }
      },
    },
  },
}));

// 编辑器创建完成
function handleCreated(editor: IDomEditor) {
  editorRef.value = editor;
}

// 内容变化
function handleChange(editor: IDomEditor) {
  const html = editor.getHtml();
  valueHtml.value = html;
  // 触发两种 v-model 事件
  emit('update:value', html);
  emit('update:modelValue', html);
  emit('change', html);
  // 调用 Vben Form 传递的更新函数
  if (typeof props['onUpdate:value'] === 'function') {
    props['onUpdate:value'](html);
  }
  if (typeof props['onUpdate:modelValue'] === 'function') {
    props['onUpdate:modelValue'](html);
  }
}

// 组件销毁时，销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value;
  if (editor) {
    editor.destroy();
  }
});

// 监听禁用状态
watch(
  () => props.disabled,
  (disabled) => {
    const editor = editorRef.value;
    if (editor) {
      if (disabled) {
        editor.disable();
      } else {
        editor.enable();
      }
    }
  },
);
</script>

<template>
  <div class="wang-editor-container" :style="{ height: editorHeight }">
    <Toolbar
      :editor="editorRef"
      :default-config="toolbarConfig"
      :mode="mode"
      class="wang-editor-toolbar"
    />
    <Editor
      v-model="valueHtml"
      :default-config="editorConfig"
      :mode="mode"
      class="wang-editor-content"
      @on-created="handleCreated"
      @on-change="handleChange"
    />
  </div>
</template>

<style scoped>
.wang-editor-container {
  display: flex;
  flex-direction: column;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.wang-editor-toolbar {
  border-bottom: 1px solid #d9d9d9;
}

.wang-editor-content {
  flex: 1;
  overflow-y: auto;
}

:deep(.w-e-text-container) {
  height: 100% !important;
}
</style>

