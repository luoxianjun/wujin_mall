<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createAttributeDictionary,
  updateAttributeDictionary,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import { useAttributeDictionaryEditFormSchema } from '../data';

defineOptions({ name: 'WujinPlatformAttributeDictionaryForm' });

const emit = defineEmits(['success']);
const formData = ref<WujinPlatformApi.AttributeDictionary>();

const defaultAttribute: WujinPlatformApi.AttributeDictionary = {
  groupName: '成品属性',
  lane: 'PRODUCT',
  requiredFlag: false,
  searchableFlag: false,
  sort: 0,
  status: 0,
  valueOptions: [],
  valueType: 'TEXT',
};

const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['平台属性'])
    : $t('ui.actionTitle.create', ['平台属性']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useAttributeDictionaryEditFormSchema(),
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
      (await formApi.getValues()) as WujinPlatformApi.AttributeDictionary;
    const data: WujinPlatformApi.AttributeDictionary = {
      ...values,
      // 空字符串表示三泳道通用，后端按 null 保存
      lane: values.lane || undefined,
      valueOptions: values.valueOptions ?? [],
    };
    try {
      await (formData.value?.id
        ? updateAttributeDictionary(data)
        : createAttributeDictionary(data));
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
    const row = modalApi.getData<WujinPlatformApi.AttributeDictionary>();
    const data = { ...defaultAttribute, ...row };
    formData.value = data;
    // 已有属性的空泳道在表单中显示为“三泳道通用”
    await formApi.setValues({
      ...data,
      lane: row?.id ? (row.lane ?? '') : defaultAttribute.lane,
    });
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
