<script lang="ts" setup>
import type { MemberUserApi } from '#/api/member/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { changeForumPoint } from '#/api/forum/point-record';
import { getUser } from '#/api/member/user';
import { $t } from '#/locales';

import { usePointFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MemberUserApi.User>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: usePointFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const values = await formApi.getValues();
    try {
      // 计算实际变动积分
      const point = (values.changePoint || 0) * (values.changeType || 1);
      await changeForumPoint({
        userId: formData.value?.id as number,
        point,
        reason: values.reason,
      });
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<MemberUserApi.User>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getUser(data.id as number);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-1/3" :title="$t('ui.actionTitle.edit', ['用户积分'])">
    <Form class="mx-4" />
  </Modal>
</template>
