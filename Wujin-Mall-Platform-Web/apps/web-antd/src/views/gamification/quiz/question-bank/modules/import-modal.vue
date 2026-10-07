<script lang="ts" setup>
import { ref } from 'vue';

import { Alert, Button, message, Modal, Upload } from 'ant-design-vue';

defineProps<{
  visible: boolean;
}>();

const emit = defineEmits<{
  (e: 'downloadTemplate'): void;
  (e: 'import', file: File): void;
  (e: 'update:visible', value: boolean): void;
}>();

const fileList = ref<any[]>([]);

const columnsHint = [
  '题库名称(bankName)',
  '题目类型(questionType)',
  '题干(content)',
  '图片地址(imageUrl)',
  '分值(score)',
  '选项A(optionA)',
  '选项B(optionB)',
  '选项C(optionC)',
  '选项D(optionD)',
  '正确答案(correctAnswers)',
  '答案解析(explanation)',
];

function closeModal() {
  fileList.value = [];
  emit('update:visible', false);
}

function beforeUpload(file: File) {
  fileList.value = [{ originFileObj: file, uid: file.name, name: file.name }];
  return false;
}

function handleImport() {
  const file = fileList.value[0]?.originFileObj;
  if (!file) {
    message.error('请先选择 Excel 文件');
    return;
  }
  emit('import', file);
}
</script>

<template>
  <Modal
    :open="visible"
    title="导入 Excel"
    @cancel="closeModal"
    @ok="handleImport"
  >
    <div class="flex flex-col gap-4">
      <Alert message="模板列说明" type="info">
        <template #description>
          <div class="flex flex-col gap-2 text-sm leading-6">
            <Button size="small" type="link" @click="emit('downloadTemplate')">
              下载导入模板
            </Button>
            <div>{{ columnsHint.join(', ') }}</div>
          </div>
        </template>
      </Alert>

      <Upload
        :before-upload="beforeUpload"
        :file-list="fileList"
        accept=".xls,.xlsx"
        max-count="1"
      >
        <Button>选择文件</Button>
      </Upload>
    </div>
  </Modal>
</template>
