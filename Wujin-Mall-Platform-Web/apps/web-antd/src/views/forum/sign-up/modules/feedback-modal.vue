<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { feedbackSignUp } from '#/api/forum/activity';

const emit = defineEmits(['success']);
const signUp = ref<ForumActivityApi.SignUp>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 96,
  },
  layout: 'horizontal',
  schema: [
    {
      fieldName: 'signUpId',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'feedback',
      label: '点评内容',
      component: 'Textarea',
      componentProps: {
        rows: 4,
        placeholder: '请输入点评内容',
        showCount: true,
        maxlength: 500,
      },
      rules: 'required',
    },
  ],
  showDefaultActions: false,
});

const title = computed(() =>
  signUp.value?.nickname ? `点评：${signUp.value.nickname}` : '报名点评',
);

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = await formApi.getValues();
    try {
      await feedbackSignUp({
        signUpId: Number(values.signUpId),
        feedback: values.feedback,
      });
      await modalApi.close();
      emit('success');
      message.success('点评成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      signUp.value = undefined;
      return;
    }
    const data = modalApi.getData<ForumActivityApi.SignUp>();
    signUp.value = data;
    await formApi.setValues({
      signUpId: data?.id,
      feedback: data?.feedback,
    });
  },
});
</script>

<template>
  <Modal :title="title" class="w-1/3">
    <Form />
  </Modal>
</template>
