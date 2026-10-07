<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { approveSignUp } from '#/api/forum/activity';

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
      fieldName: 'approvalStatus',
      label: '审核结果',
      component: 'Select',
      componentProps: {
        placeholder: '请选择审核结果',
        options: [
          { label: '通过', value: 1 },
          { label: '拒绝', value: 2 },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'approvalRemark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        placeholder: '可填写备注说明',
      },
    },
  ],
  showDefaultActions: false,
});

const title = computed(() =>
  signUp.value?.nickname ? `审核：${signUp.value.nickname}` : '审核报名',
);

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = await formApi.getValues();
    try {
      await approveSignUp({
        signUpId: Number(values.signUpId),
        approvalStatus: Number(values.approvalStatus),
        approvalRemark: values.approvalRemark,
      });
      await modalApi.close();
      emit('success');
      message.success('审核成功');
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
      approvalStatus:
        data?.approvalStatus && data.approvalStatus !== 0
          ? data.approvalStatus
          : undefined,
      approvalRemark:
        data?.approvalStatus && data.approvalStatus !== 0
          ? data.approvalRemark
          : undefined,
    });
  },
});
</script>

<template>
  <Modal :title="title" class="w-1/3">
    <Form />
  </Modal>
</template>
