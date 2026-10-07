<script lang="ts" setup>
import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { requestClient } from '#/api/request';

import { useFormSchema } from './data';

const emit = defineEmits<{ success: [] }>();
const formData = ref<Record<string, any>>();

const getTitle = computed(() =>
  formData.value?.id ? '编辑奖品' : '新增奖品',
);

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  showDefaultActions: false,
  commonConfig: { componentProps: { class: 'w-full' } },
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      await formApi.resetForm();
      return;
    }
    const data = modalApi.getData<Record<string, any>>();
    if (!data?.id) return;
    formData.value = data;
    await formApi.setValues(data);
  },
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const values = await formApi.getValues();
      if (formData.value?.id) {
        await requestClient.put(
          '/gamification/lottery/prize/update',
          values,
        );
        message.success('更新成功');
      } else {
        await requestClient.post(
          '/gamification/lottery/prize/create',
          values,
        );
        message.success('创建成功');
      }
      emit('success');
      await modalApi.close();
    } catch (error: any) {
      message.error(error?.message || '操作失败');
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[600px]">
    <Form />
  </Modal>
</template>
