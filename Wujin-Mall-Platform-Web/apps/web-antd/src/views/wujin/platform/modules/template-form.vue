<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createIndustryTemplate,
  updateIndustryTemplate,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useTemplateEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformTemplateForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.IndustryTemplate>();

const defaultTemplate: WujinPlatformApi.IndustryTemplate = {
  productLane: 'PRODUCT',
  status: 0,
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['行业模板'])
    : $t('ui.actionTitle.create', ['行业模板']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useTemplateEditFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values =
      (await formApi.getValues()) as WujinPlatformApi.IndustryTemplate;
    try {
      await (formData.value?.id
        ? updateIndustryTemplate(values)
        : createIndustryTemplate(values));
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
    const data = {
      ...defaultTemplate,
      ...(modalApi.getData<WujinPlatformApi.IndustryTemplate>() ?? {}),
    };
    formData.value = data;
    await formApi.setValues(data);
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
