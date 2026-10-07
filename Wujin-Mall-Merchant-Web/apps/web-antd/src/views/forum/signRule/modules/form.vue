<script lang="ts" setup>
import type { ForumSignRuleApi } from '#/api/forum/signRule';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import {
  createSignRule,
  getSignRule,
  updateSignRule,
} from '#/api/forum/signRule';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'ForumSignRuleForm' });

const emit = defineEmits(['success']);
const formData = ref<ForumSignRuleApi.SignRule>();

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['签到规则'])
    : $t('ui.actionTitle.create', ['签到规则']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 110,
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
    const values = (await formApi.getValues()) as ForumSignRuleApi.SignRule;
    try {
      await (formData.value?.id ? updateSignRule(values) : createSignRule(values));
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
    const data = modalApi.getData<ForumSignRuleApi.SignRule>();
    if (!data?.id) return;
    modalApi.lock();
    try {
      formData.value = await getSignRule(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-1/2" :title="getTitle">
    <Form />
  </Modal>
</template>
