<script lang="ts" setup>
import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Alert, Descriptions, message, Progress } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { submitRelation, updateRelationSubmission } from '#/api/wujin/merchant';
import { $t } from '#/locales';

import {
  auditRouteOptions,
  auditStatusOptions,
  optionLabel,
  useRelationSubmitFormSchema,
} from '../data';

defineOptions({ name: 'WujinMerchantRelationSubmitForm' });

type RelationSubmitFormValues = WujinMerchantApi.RelationSubmitRequest & {
  auditRoute?: string;
  auditStatus?: number;
  customRelationsJson?: string;
  id?: number;
  missingItems?: string[] | string;
  remark?: string;
  selectedEntityId?: number | string;
  selectedRelationRemark?: string;
  selectedRelationType?: string;
  selectedRequiredFlag?: boolean;
};
type CustomRelation = NonNullable<
  WujinMerchantApi.RelationSubmitRequest['customRelations']
>[number];

const emit = defineEmits(['success']);
const submitResult = ref<WujinMerchantApi.RelationSubmitResult>();
const formData = ref<RelationSubmitFormValues>();

const defaultSubmitForm: RelationSubmitFormValues = {
  certificationCount: 0,
  customRelationsJson: '[]',
  hasApplicationDescription: true,
  selectedRelationType: 'REQUIRES_MATERIAL',
  selectedRequiredFlag: false,
};

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useRelationSubmitFormSchema(),
  showDefaultActions: false,
});

const resultAuditStatusLabel = computed(() =>
  optionLabel(auditStatusOptions, submitResult.value?.auditStatus),
);

const resultAuditRouteLabel = computed(() =>
  optionLabel(auditRouteOptions, submitResult.value?.auditRoute),
);

const resultAlertType = computed(() => {
  if (submitResult.value?.auditRoute === 'AUTO_APPROVE') {
    return 'success';
  }
  if (submitResult.value?.auditRoute === 'BLOCK') {
    return 'error';
  }
  return 'warning';
});

const resultDescription = computed(
  () =>
    submitResult.value?.auditReason ??
    '系统已生成关系申报，请关注后续审核状态。',
);

const isEditMode = computed(() => Boolean(formData.value?.id));

const modalTitle = computed(() =>
  isEditMode.value ? '编辑关系申报' : '提交关系申报',
);

function parseCustomRelations(
  customRelationsJson?: string,
): WujinMerchantApi.RelationSubmitRequest['customRelations'] {
  const raw = customRelationsJson?.trim();
  if (!raw) {
    return [];
  }
  const parsed = JSON.parse(raw);
  if (!Array.isArray(parsed)) {
    throw new Error('customRelationsJson must be an array');
  }
  return parsed;
}

function buildSelectedRelation(
  values: RelationSubmitFormValues,
): CustomRelation | undefined {
  if (!values.selectedEntityId) {
    return undefined;
  }
  const entityId = Number(values.selectedEntityId);
  if (Number.isNaN(entityId)) {
    return undefined;
  }
  return {
    entityId,
    relationType: values.selectedRelationType ?? 'REQUIRES_MATERIAL',
    requiredFlag: values.selectedRequiredFlag ?? false,
    remark: values.selectedRelationRemark?.trim() || undefined,
  };
}

function buildCustomRelations(
  values: RelationSubmitFormValues,
): WujinMerchantApi.RelationSubmitRequest['customRelations'] {
  const customRelations = parseCustomRelations(values.customRelationsJson);
  const selectedRelation = buildSelectedRelation(values);
  return selectedRelation
    ? [...(customRelations ?? []), selectedRelation]
    : customRelations;
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const values = (await formApi.getValues()) as RelationSubmitFormValues;
    try {
      buildCustomRelations(values);
    } catch {
      message.error('自定义关系需填写 JSON 数组');
      return;
    }
    modalApi.lock();
    try {
      if (isEditMode.value) {
        await updateRelationSubmission({
          auditRoute: values.auditRoute,
          auditStatus: values.auditStatus,
          certificationCount: values.certificationCount,
          hasApplicationDescription: values.hasApplicationDescription,
          id: formData.value?.id,
          merchantId: values.merchantId,
          productCategoryId: values.productCategoryId,
          productId: values.productId,
          productName: values.productName,
          remark: values.remark,
          templateId: values.templateId,
        });
        await modalApi.close?.();
      } else {
        const result = await submitRelation({
          certificationCount: values.certificationCount,
          customRelations: buildCustomRelations(values),
          hasApplicationDescription: values.hasApplicationDescription,
          merchantId: values.merchantId,
          productCategoryId: values.productCategoryId,
          productId: values.productId,
          productName: values.productName,
          templateId: values.templateId,
        });
        submitResult.value = result;
      }
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      submitResult.value = undefined;
      formData.value = undefined;
      await formApi.resetForm();
      return;
    }
    submitResult.value = undefined;
    const data = {
      ...defaultSubmitForm,
      ...(modalApi.getData<RelationSubmitFormValues>() ?? {}),
    };
    formData.value = data;
    await formApi.setValues(data);
  },
});
</script>

<template>
  <Modal class="w-3/5" :title="modalTitle">
    <div class="wujin-submit-form">
      <Form class="mx-4" />
      <div v-if="submitResult" class="wujin-submit-result">
        <Alert
          :description="resultDescription"
          message="提交结果"
          show-icon
          :type="resultAlertType"
        />
        <Descriptions bordered size="small" :column="2">
          <Descriptions.Item label="申报单ID">
            {{ submitResult.submissionId ?? '-' }}
          </Descriptions.Item>
          <Descriptions.Item label="审核状态">
            {{ resultAuditStatusLabel }}
          </Descriptions.Item>
          <Descriptions.Item label="审核路线">
            {{ resultAuditRouteLabel }}
          </Descriptions.Item>
          <Descriptions.Item label="模板带出">
            {{ submitResult.copiedTemplateItemCount ?? 0 }} 项
          </Descriptions.Item>
          <Descriptions.Item label="优化建议" :span="2">
            {{ submitResult.completenessSuggestion || '暂无优化建议' }}
          </Descriptions.Item>
        </Descriptions>
        <div class="wujin-submit-score">
          <span>完善度</span>
          <Progress
            :percent="submitResult.completenessScore ?? 0"
            size="small"
          />
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.wujin-submit-form {
  display: grid;
  gap: 12px;
}

.wujin-submit-result {
  display: grid;
  gap: 12px;
  margin: 0 16px 8px;
}

.wujin-submit-score {
  display: grid;
  gap: 6px;
}
</style>
