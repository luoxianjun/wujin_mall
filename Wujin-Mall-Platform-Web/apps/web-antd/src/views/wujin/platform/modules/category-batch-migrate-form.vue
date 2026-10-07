<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  batchMigrateCategory,
  type WujinPlatformApi,
} from '#/api/wujin/platform';

import { useCategoryBatchMigrateFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformCategoryBatchMigrateForm' });

const emit = defineEmits<{ success: [] }>();

const selectedCategoryIds = ref<number[]>([]);

const [BatchMigrateForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'vertical',
  schema: useCategoryBatchMigrateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const values =
      await formApi.getValues<WujinPlatformApi.CategoryBatchMigrateRequest>();
    if (
      values.displayDepth !== undefined &&
      values.targetLevel !== undefined &&
      values.displayDepth < values.targetLevel
    ) {
      message.warning('搜索展示到层级不能小于目标分类层级');
      return;
    }
    await batchMigrateCategory({
      ...values,
      ids: selectedCategoryIds.value,
    });
    message.success('类目批量迁移已提交');
    emit('success');
    modalApi.close();
  },
  onOpenChange(open) {
    if (!open) {
      return;
    }
    const data = modalApi.getData<{
      selectedCategoryIds?: number[];
    }>();
    selectedCategoryIds.value = data.selectedCategoryIds ?? [];
    formApi.setValues({
      displayDepth: 2,
      healthStatus: 'HEALTHY',
      targetLane: 'PRODUCT',
      targetLevel: 1,
      targetParentId: 0,
    });
  },
  title: '批量迁移类目',
});
</script>

<template>
  <Modal>
    <BatchMigrateForm />
  </Modal>
</template>
