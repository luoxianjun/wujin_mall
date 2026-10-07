<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { sendBroadcast } from '#/api/forum/systemBroadcast';
import { $t } from '#/locales';

defineOptions({ name: 'SystemBroadcastSendForm' });

const emit = defineEmits(['success']);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: [
    {
      fieldName: 'title',
      label: '消息标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入消息标题（可选）',
      },
    },
    {
      fieldName: 'content',
      label: '消息内容',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入消息内容',
        rows: 6,
      },
      rules: 'required',
      formItemClass: 'col-span-2',
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    try {
      const values = await formApi.getValues();
      await sendBroadcast({
        title: values.title as string,
        content: values.content as string,
      });
      message.success('消息已开始发送');
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
  },
});
</script>

<template>
  <Modal class="w-3/5" title="发送系统消息">
    <Form />
  </Modal>
</template>
