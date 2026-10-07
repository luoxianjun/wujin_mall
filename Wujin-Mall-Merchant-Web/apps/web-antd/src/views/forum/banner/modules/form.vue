<script lang="ts" setup>
import type { ForumBannerApi } from '#/api/forum/banner';

import { computed, nextTick, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { createBanner, getBanner, updateBanner } from '#/api/forum/banner';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'ForumBannerForm' });

const emit = defineEmits(['success']);
const formData = ref<ForumBannerApi.Banner>();

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['Banner'])
    : $t('ui.actionTitle.create', ['Banner']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 100,
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
    const values = (await formApi.getValues()) as ForumBannerApi.Banner;
    try {
      await (formData.value?.id
        ? updateBanner(values as ForumBannerApi.BannerUpdateReq)
        : createBanner(values as ForumBannerApi.BannerCreateReq));
      message.success($t('ui.actionMessage.operationSuccess'));
      await modalApi.close();
      // 等待 modal 完全关闭后再触发 success 事件
      await nextTick();
      emit('success');
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
    const data = modalApi.getData<ForumBannerApi.Banner>();
    if (!data?.id) return;
    modalApi.lock();
    try {
      formData.value = await getBanner(data.id);
      const values = { ...formData.value };
      if (typeof values.startTime === 'number') {
        values.startTime = dayjs(values.startTime).format('YYYY-MM-DD HH:mm');
      }
      if (typeof values.endTime === 'number') {
        values.endTime = dayjs(values.endTime).format('YYYY-MM-DD HH:mm');
      }
      await formApi.setValues(values);
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
