<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createSearchRuleConfig,
  updateSearchRuleConfig,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useSearchRuleEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformSearchRuleForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.SearchRuleConfig>();

const defaultSearchRule: WujinPlatformApi.SearchRuleConfig = {
  lane: 'PRODUCT',
  ruleType: 'GRANULARITY_LIMIT',
  ruleValue: '2',
  status: 0,
  weight: 100,
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['搜索规则'])
    : $t('ui.actionTitle.create', ['搜索规则']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useSearchRuleEditFormSchema(),
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
      (await formApi.getValues()) as WujinPlatformApi.SearchRuleConfig;
    try {
      await (formData.value?.id
        ? updateSearchRuleConfig(values)
        : createSearchRuleConfig(values));
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
      ...defaultSearchRule,
      ...(modalApi.getData<WujinPlatformApi.SearchRuleConfig>() ?? {}),
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
