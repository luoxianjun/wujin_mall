<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  batchMigrateIndustryTemplateItem,
  type WujinPlatformApi,
} from '#/api/wujin/platform';

import { useTemplateItemBatchMigrateFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformTemplateItemBatchMigrateForm' });

const emit = defineEmits<{ success: [] }>();

const selectedTemplateItemIds = ref<number[]>([]);

const [BatchMigrateForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'vertical',
  schema: useTemplateItemBatchMigrateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    if (selectedTemplateItemIds.value.length === 0) {
      message.warning('至少选择一个模板项');
      return;
    }
    const values =
      await formApi.getValues<WujinPlatformApi.IndustryTemplateItemBatchMigrateRequest>();
    await batchMigrateIndustryTemplateItem({
      ...values,
      ids: selectedTemplateItemIds.value,
    });
    message.success('模板项批量迁移已提交');
    emit('success');
    modalApi.close();
  },
  onOpenChange(open) {
    if (!open) {
      selectedTemplateItemIds.value = [];
      return;
    }
    const data = modalApi.getData<{
      selectedTemplateItemIds?: number[];
    }>();
    selectedTemplateItemIds.value = data.selectedTemplateItemIds ?? [];
    formApi.setValues({
      relationType: undefined,
      requiredFlag: undefined,
      targetTemplateId: undefined,
      weight: undefined,
    });
  },
  title: '批量迁移模板项',
});
</script>

<template>
  <Modal>
    <BatchMigrateForm />
  </Modal>
</template>
