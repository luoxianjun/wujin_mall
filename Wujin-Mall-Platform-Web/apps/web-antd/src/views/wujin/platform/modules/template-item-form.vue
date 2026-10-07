<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createIndustryTemplateItem,
  updateIndustryTemplateItem,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useTemplateItemEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformTemplateItemForm' });

type TemplateItemFormData = WujinPlatformApi.IndustryTemplateItem & {
  currentTemplateId?: number;
};

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.IndustryTemplateItem>();
const currentTemplateId = ref<number>();

const defaultTemplateItem: WujinPlatformApi.IndustryTemplateItem = {
  relationType: 'REQUIRES_MATERIAL',
  requiredFlag: true,
  sort: 0,
  weight: 100,
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['模板项'])
    : $t('ui.actionTitle.create', ['模板项']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useTemplateItemEditFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    if (!currentTemplateId.value) {
      message.warning('请选择行业模板');
      return;
    }
    modalApi.lock();
    const values = {
      ...((await formApi.getValues()) as WujinPlatformApi.IndustryTemplateItem),
      templateId: currentTemplateId.value,
    };
    try {
      await (formData.value?.id
        ? updateIndustryTemplateItem(values)
        : createIndustryTemplateItem(values));
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
      currentTemplateId.value = undefined;
      await formApi.resetForm();
      return;
    }
    const modalData = modalApi.getData<TemplateItemFormData>() ?? {};
    currentTemplateId.value = modalData.currentTemplateId ?? modalData.templateId;
    const data = {
      ...defaultTemplateItem,
      ...modalData,
      templateId: currentTemplateId.value,
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
