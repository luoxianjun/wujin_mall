<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createCategoryMapping,
  updateCategoryMapping,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useMappingEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformMappingForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.CategoryMapping>();

const defaultMapping: WujinPlatformApi.CategoryMapping = {
  confidence: 80,
  mappingType: 'REQUIRES_MATERIAL',
  sourceLane: 'PRODUCT',
  status: 0,
  targetLane: 'MATERIAL',
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['跨泳道映射'])
    : $t('ui.actionTitle.create', ['跨泳道映射']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useMappingEditFormSchema(),
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
      (await formApi.getValues()) as WujinPlatformApi.CategoryMapping;
    try {
      await (formData.value?.id
        ? updateCategoryMapping(values)
        : createCategoryMapping(values));
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
      ...defaultMapping,
      ...(modalApi.getData<WujinPlatformApi.CategoryMapping>() ?? {}),
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
