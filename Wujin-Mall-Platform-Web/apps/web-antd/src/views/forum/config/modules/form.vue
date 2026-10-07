<script lang="ts" setup>
import type { ForumConfigApi } from '#/api/forum/config';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { getConfig, saveConfig } from '#/api/forum/config';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'ForumConfigForm' });

const emit = defineEmits(['success']);
const formData = ref<ForumConfigApi.Config & { key?: string; value?: string }>();

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['配置'])
    : $t('ui.actionTitle.create', ['配置']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = (await formApi.getValues()) as ForumConfigApi.Config & {
      key: string;
      value: string;
    };
    try {
      await saveConfig({
        key: values.key,
        value: values.value || '',
        name: values.name,
        type: values.type,
        remark: values.remark,
      });
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      formData.value = undefined;
      await formApi.resetForm();
      return;
    }
    const data = modalApi.getData<
      ForumConfigApi.Config & { key?: string; value?: string }
    >();
    // 新增时，data 为 null 或没有 configKey，直接设置表单数据
    if (!data?.configKey) {
      formData.value = data || undefined;
      if (formData.value) {
        await formApi.setValues(formData.value);
      }
      return;
    }
    // 编辑时，从服务器获取最新配置数据
    modalApi.lock();
    try {
      const config = await getConfig(data.configKey);
      formData.value = {
        ...config,
        key: config?.configKey,
        value: config?.configValue,
      };
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-[800px]" :title="getTitle">
    <Form />
  </Modal>
</template>
