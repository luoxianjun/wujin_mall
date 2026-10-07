<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createCategory, updateCategory } from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useCategoryEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformCategoryForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.Category>();

const defaultCategory: WujinPlatformApi.Category = {
  displayDepth: 2,
  healthStatus: 'HEALTHY',
  lane: 'PRODUCT',
  level: 1,
  parentId: 0,
  sort: 0,
  status: 0,
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['平台类目'])
    : $t('ui.actionTitle.create', ['平台类目']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useCategoryEditFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values = (await formApi.getValues()) as WujinPlatformApi.Category;
    if (
      values.displayDepth !== undefined &&
      values.level !== undefined &&
      values.displayDepth < values.level
    ) {
      message.warning('搜索展示到层级不能小于当前分类层级');
      modalApi.unlock();
      return;
    }
    try {
      await (formData.value?.id ? updateCategory(values) : createCategory(values));
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
      ...defaultCategory,
      ...(modalApi.getData<WujinPlatformApi.Category>() ?? {}),
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
