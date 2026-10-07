<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';

import type { ForumPostApi } from '#/api/forum/post';

import { ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { reviewPost } from '#/api/forum/post';

const emit = defineEmits<{
  success: [];
}>();

const current = ref<ForumPostApi.Post>();

const schema: VbenFormSchema[] = [
  {
    fieldName: 'flag',
    label: '审核结果',
    component: 'RadioGroup',
    defaultValue: true,
    rules: 'required',
    componentProps: {
      options: [
        { label: '通过', value: true },
        { label: '不通过', value: false },
      ],
    },
  },
  {
    fieldName: 'reviewRemark',
    label: '审核结论',
    component: 'Textarea',
    componentProps: {
      placeholder: '请输入审核结论',
      rows: 4,
      maxlength: 500,
      showCount: true,
    },
  },
];

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema,
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  title: '帖子复审',
  async onConfirm() {
    const values = await formApi.getValues();
    // 如果选择不通过，校验审核结论不能为空
    if (values.flag === false && !values.reviewRemark?.trim()) {
      message.error('审核不通过时，审核结论不能为空');
      return;
    }
    // 验证其他必填字段
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      await reviewPost(
        current.value?.id as number,
        values.flag as boolean,
        values.reviewRemark as string | undefined,
      );
      message.success('审核成功');
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      current.value = undefined;
      await formApi.resetForm();
      return;
    }
    current.value = modalApi.getData<ForumPostApi.Post>();
    // 回显当前状态和审核结论
    if (current.value) {
      // 根据 status 回显 flag: 1-已通过(true), 2-已驳回(false), 0-待审核(默认true)
      const flag = current.value.status !== 2;
      await formApi.setValues({
        flag,
        reviewRemark: current.value.reviewResult || '',
      });
    }
  },
});
</script>

<template>
  <Modal class="w-[500px]">
    <Form />
  </Modal>
</template>
