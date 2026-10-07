<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { reviewRelationSubmission } from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useAuditReviewFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformAuditReviewForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.RelationAuditReviewRequest>();

const getTitle = computed(() =>
  formData.value?.submissionId
    ? `审核申报单 #${formData.value.submissionId}`
    : '审核关系申报',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useAuditReviewFormSchema(),
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
      (await formApi.getValues()) as WujinPlatformApi.RelationAuditReviewRequest;
    try {
      await reviewRelationSubmission(values);
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
      action: 'APPROVE',
      ...(modalApi.getData<WujinPlatformApi.RelationAuditReviewRequest>() ?? {}),
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
