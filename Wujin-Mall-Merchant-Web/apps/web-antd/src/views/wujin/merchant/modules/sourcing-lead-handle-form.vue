<script lang="ts" setup>
import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { handleSourcingLead } from '#/api/wujin/merchant';
import { $t } from '#/locales';

import { useSourcingLeadHandleFormSchema } from '../data';

defineOptions({ name: 'WujinMerchantSourcingLeadHandleForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinMerchantApi.SourcingLeadHandleRequest>();

const getTitle = computed(() =>
  formData.value?.leadId
    ? `多阶段跟进寻源线索 #${formData.value.leadId}`
    : '多阶段跟进寻源线索',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useSourcingLeadHandleFormSchema(),
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
      (await formApi.getValues()) as WujinMerchantApi.SourcingLeadHandleRequest;
    try {
      await handleSourcingLead(values);
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
      followStage: 'FIRST_CONTACT',
      handleAction: 'CONTACTED',
      nextFollowTime: undefined,
      quotedAmount: undefined,
      winProbability: undefined,
      ...(modalApi.getData<WujinMerchantApi.SourcingLeadHandleRequest>() ?? {}),
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
