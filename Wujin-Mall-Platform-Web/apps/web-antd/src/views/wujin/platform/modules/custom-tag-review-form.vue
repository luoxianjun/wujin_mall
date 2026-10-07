<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Descriptions, message, Tag } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { reviewProductCustomTag } from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useCustomTagReviewFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformCustomTagReviewForm' });

const emit = defineEmits(['success']);
const currentTag = ref<WujinPlatformApi.ProductCustomTag>();

const getTitle = computed(() =>
  currentTag.value?.tagName
    ? `审核自定义标签「${currentTag.value.tagName}」`
    : '审核自定义标签',
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useCustomTagReviewFormSchema(),
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
      (await formApi.getValues()) as WujinPlatformApi.ProductCustomTagReviewRequest;
    try {
      await reviewProductCustomTag(values);
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      currentTag.value = undefined;
      await formApi.resetForm();
      return;
    }
    const row = modalApi.getData<WujinPlatformApi.ProductCustomTag>();
    currentTag.value = row;
    await formApi.setValues({ action: 'APPROVE', comment: '', id: row?.id });
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Descriptions
      v-if="currentTag"
      :column="2"
      bordered
      class="mx-4 mb-4"
      size="small"
    >
      <Descriptions.Item label="标签">
        <Tag color="blue">{{ currentTag.tagName }}</Tag>
      </Descriptions.Item>
      <Descriptions.Item label="商家ID">
        {{ currentTag.merchantId }}
      </Descriptions.Item>
      <Descriptions.Item label="商品" :span="2">
        {{ currentTag.productName || '-' }}（ID {{ currentTag.productId }}）
      </Descriptions.Item>
      <Descriptions.Item label="商家说明" :span="2">
        {{ currentTag.reviewNote || '商家未填写说明' }}
      </Descriptions.Item>
    </Descriptions>
    <Form class="mx-4" />
  </Modal>
</template>
