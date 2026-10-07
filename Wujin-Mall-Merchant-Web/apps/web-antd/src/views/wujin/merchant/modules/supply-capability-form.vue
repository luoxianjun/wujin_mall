<script lang="ts" setup>
import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createSupplyCapability,
  updateSupplyCapability,
} from '#/api/wujin/merchant';
import { $t } from '#/locales';

import { useSupplyCapabilityFormSchema } from '../data';

defineOptions({ name: 'WujinMerchantSupplyCapabilityForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinMerchantApi.SupplyCapability>();

const defaultSupplyCapabilityForm: WujinMerchantApi.SupplyCapability = {
  deliveryDays: 0,
  lane: 'MATERIAL',
  minOrderQuantity: 0,
  serviceArea: '',
  stockCount: 0,
  supplyStatus: 0,
};

const getTitle = computed(() =>
  formData.value?.id ? '编辑供应能力' : '新增供应能力',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 110,
  },
  layout: 'horizontal',
  schema: useSupplyCapabilityFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values = (await formApi.getValues()) as WujinMerchantApi.SupplyCapability;
    try {
      if (values.id) {
        await updateSupplyCapability(values);
      } else {
        await createSupplyCapability(values);
      }
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
      ...defaultSupplyCapabilityForm,
      ...(modalApi.getData<WujinMerchantApi.SupplyCapability>() ?? {}),
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
