<script lang="ts" setup>
import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Alert,
  Button,
  Descriptions,
  Input,
  message,
  Progress,
  Select,
  Steps,
  Switch,
} from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getChainEntityList, submitRelation } from '#/api/wujin/merchant';
import { $t } from '#/locales';

import {
  auditRouteOptions,
  auditStatusOptions,
  laneOptions,
  optionLabel,
  productPublishStructuredHelp,
  productPublishSteps,
  relationTypeOptions,
  useProductPublishAttributeSchema,
  useProductPublishBaseSchema,
  useProductPublishCategorySchema,
  useProductPublishChainSchema,
  useProductPublishSupplySchema,
} from '../data';

defineOptions({ name: 'WujinMerchantProductPublishWizard' });

type ProductPublishFormValues = WujinMerchantApi.RelationSubmitRequest & {
  productLane?: WujinMerchantApi.WujinLane;
  customTagReviewNote?: string;
  customTagReviewRequired?: boolean;
};
type CustomRelation = NonNullable<
  WujinMerchantApi.RelationSubmitRequest['customRelations']
>[number];
type StandardAttribute = NonNullable<
  WujinMerchantApi.RelationSubmitRequest['standardAttributes']
>[number];

type ChainEntityOption = { label: string; value: number };

const emit = defineEmits(['success']);
const currentStep = ref(0);
const publishPreview = ref<ProductPublishFormValues>({});
const submitResult = ref<WujinMerchantApi.RelationSubmitResult>();
const chainEntityOptions = ref<ChainEntityOption[]>([]);
const customTagValues = ref<string[]>([]);
const customRelationDraft = ref<CustomRelation>(createRelationRow());
const customRelationDrafts = ref<CustomRelation[]>([]);
const relationRows = ref<CustomRelation[]>([createRelationRow()]);
const standardAttributeRows = ref<StandardAttribute[]>([
  createStandardAttributeRow(),
]);

const defaultBaseValues: ProductPublishFormValues = {
  hasApplicationDescription: true,
  productLane: 'PRODUCT',
};
const defaultChainValues: ProductPublishFormValues = {
  certificationCount: 0,
};
const defaultSupplyValues: ProductPublishFormValues = {
  productStock: 0,
  supplyDeliveryDays: 1,
  supplyMinOrderQuantity: 1,
};
const defaultAttributeValues: ProductPublishFormValues = {
  customTagReviewRequired: true,
};

function createFormOptions(
  schema: ReturnType<typeof useProductPublishBaseSchema>,
) {
  return {
    commonConfig: {
      componentProps: { class: 'w-full' },
      formItemClass: 'col-span-2',
      labelWidth: 120,
    },
    layout: 'horizontal' as const,
    schema,
    showDefaultActions: false,
  };
}

const [BaseForm, baseFormApi] = useVbenForm(
  createFormOptions(useProductPublishBaseSchema()),
);
const [CategoryForm, categoryFormApi] = useVbenForm(
  createFormOptions(useProductPublishCategorySchema()),
);
const [SupplyForm, supplyFormApi] = useVbenForm(
  createFormOptions(useProductPublishSupplySchema()),
);
const [ChainForm, chainFormApi] = useVbenForm(
  createFormOptions(useProductPublishChainSchema()),
);
const [AttributeForm, attributeFormApi] = useVbenForm(
  createFormOptions(useProductPublishAttributeSchema()),
);

const isAuditStep = computed(
  () => currentStep.value === productPublishSteps.length - 1,
);

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
    submitResult.value?.auditReason ?? '商品发布资料已进入三泳道关系审核流程。',
);

function buildCustomRelationDraft(row: CustomRelation) {
  const entityName = row.entityName?.trim();
  if (!entityName) {
    return undefined;
  }
  return {
    entityName,
    relationType: row.relationType ?? 'REQUIRES_MATERIAL',
    requiredFlag: row.requiredFlag ?? false,
    remark: row.remark?.trim() || undefined,
  };
}

function buildCustomRelations(): WujinMerchantApi.RelationSubmitRequest['customRelations'] {
  const selectedRelations = relationRows.value
    .map((row) => {
      const entityId = Number(row.entityId);
      if (!row.entityId || Number.isNaN(entityId)) {
        return undefined;
      }
      return {
        entityId,
        relationType: row.relationType ?? 'REQUIRES_MATERIAL',
        requiredFlag: row.requiredFlag ?? false,
        remark: row.remark?.trim() || undefined,
      };
    })
    .filter(
      Boolean,
    ) as WujinMerchantApi.RelationSubmitRequest['customRelations'];
  const addedCustomRelations = customRelationDrafts.value
    .map(buildCustomRelationDraft)
    .filter(
      Boolean,
    ) as WujinMerchantApi.RelationSubmitRequest['customRelations'];
  const pendingCustomRelation = buildCustomRelationDraft(
    customRelationDraft.value,
  );
  return [
    ...(selectedRelations ?? []),
    ...(addedCustomRelations ?? []),
    ...(pendingCustomRelation ? [pendingCustomRelation] : []),
  ];
}

function buildStandardAttributes(): WujinMerchantApi.RelationSubmitRequest['standardAttributes'] {
  return standardAttributeRows.value
    .map((row) => ({
      name: row.name?.trim(),
      value: row.value?.trim(),
    }))
    .filter((row) => row.name && row.value);
}

function buildCustomTags() {
  return customTagValues.value
    .map((tag) => tag.trim())
    .filter((tag, index, tags) => tag && tags.indexOf(tag) === index);
}

async function loadChainEntityOptions() {
  if (chainEntityOptions.value.length > 0) {
    return;
  }
  const [materials, processes] = await Promise.all([
    getChainEntityList({ lane: 'MATERIAL', status: 0 }),
    getChainEntityList({ lane: 'PROCESS', status: 0 }),
  ]);
  const entities = [...(materials ?? []), ...(processes ?? [])];
  chainEntityOptions.value = (entities ?? [])
    .filter((entity) => entity.id)
    .map((entity) => ({
      label: `${entity.name || entity.entityCode || '未命名实体'} #${entity.id}`,
      value: entity.id!,
    }));
}

function createRelationRow(): CustomRelation {
  return {
    relationType: 'REQUIRES_MATERIAL',
    requiredFlag: false,
  };
}

function createStandardAttributeRow(): StandardAttribute {
  return {};
}

function addRelationRow() {
  relationRows.value.push(createRelationRow());
}

function removeRelationRow(index: number) {
  relationRows.value.splice(index, 1);
  if (relationRows.value.length === 0) {
    addRelationRow();
  }
}

function addStandardAttribute() {
  standardAttributeRows.value.push(createStandardAttributeRow());
}

function addCustomRelationDraft() {
  const customRelation = buildCustomRelationDraft(customRelationDraft.value);
  if (!customRelation) {
    message.error('请先填写买家可能搜索的材料、工艺或俗称');
    return;
  }
  customRelationDrafts.value.push(customRelation);
  customRelationDraft.value = createRelationRow();
}

function removeCustomRelationDraft(index: number) {
  customRelationDrafts.value.splice(index, 1);
}

function removeStandardAttribute(index: number) {
  standardAttributeRows.value.splice(index, 1);
  if (standardAttributeRows.value.length === 0) {
    addStandardAttribute();
  }
}

function resetStructuredFields(data: ProductPublishFormValues = {}) {
  const selectedRelations =
    data.customRelations?.filter((row) => row.entityId) ?? [];
  const customRelations =
    data.customRelations?.filter((row) => !row.entityId && row.entityName) ??
    [];
  relationRows.value =
    selectedRelations.length > 0
      ? selectedRelations.map((row) => ({
          entityId: row.entityId,
          relationType: row.relationType ?? 'REQUIRES_MATERIAL',
          requiredFlag: row.requiredFlag ?? false,
          remark: row.remark,
        }))
      : [createRelationRow()];
  standardAttributeRows.value =
    data.standardAttributes && data.standardAttributes.length > 0
      ? data.standardAttributes.map((row) => ({
          name: row.name,
          value: row.value,
        }))
      : [createStandardAttributeRow()];
  customRelationDraft.value = createRelationRow();
  customRelationDrafts.value = customRelations.map((row) => ({
    entityName: row.entityName,
    relationType: row.relationType ?? 'REQUIRES_MATERIAL',
    requiredFlag: row.requiredFlag ?? false,
    remark: row.remark,
  }));
  customTagValues.value = [...(data.customTags ?? [])];
}

function hasIncompleteStandardAttribute() {
  return standardAttributeRows.value.some(
    (row) => Boolean(row.name?.trim()) !== Boolean(row.value?.trim()),
  );
}

function relationEntityLabel(relation: CustomRelation) {
  if (relation.entityName) {
    return relation.entityName;
  }
  return (
    chainEntityOptions.value.find(
      (option) => option.value === relation.entityId,
    )?.label ?? (relation.entityId ? `实体 #${relation.entityId}` : '未选择')
  );
}

function formatRelations(relations?: CustomRelation[]) {
  if (!relations || relations.length === 0) {
    return '-';
  }
  return relations
    .map((relation) => {
      const type = optionLabel(relationTypeOptions, relation.relationType);
      const required = relation.requiredFlag ? '必填' : '选填';
      const remark = relation.remark ? `，${relation.remark}` : '';
      return `${relationEntityLabel(relation)}（${type}，${required}${remark}）`;
    })
    .join('；');
}

function formatStandardAttributes(attributes?: StandardAttribute[]) {
  if (!attributes || attributes.length === 0) {
    return '-';
  }
  return attributes
    .map((attribute) => `${attribute.name}: ${attribute.value}`)
    .join('；');
}

function formatTags(tags?: string[]) {
  return tags && tags.length > 0 ? tags.join('、') : '-';
}

async function collectFormValues(): Promise<ProductPublishFormValues> {
  return {
    ...((await baseFormApi.getValues()) as ProductPublishFormValues),
    ...((await categoryFormApi.getValues()) as ProductPublishFormValues),
    ...((await supplyFormApi.getValues()) as ProductPublishFormValues),
    ...((await chainFormApi.getValues()) as ProductPublishFormValues),
    ...((await attributeFormApi.getValues()) as ProductPublishFormValues),
  };
}

async function buildPublishPreview(): Promise<ProductPublishFormValues> {
  return {
    ...(await collectFormValues()),
    customRelations: buildCustomRelations(),
    customTags: buildCustomTags(),
    standardAttributes: buildStandardAttributes(),
  };
}

async function buildSubmitRequest(): Promise<WujinMerchantApi.RelationSubmitRequest> {
  const values = await collectFormValues();
  return {
    certificationCount: values.certificationCount ?? 0,
    customRelations: buildCustomRelations(),
    hasApplicationDescription: values.hasApplicationDescription ?? true,
    productBrandId: values.productBrandId,
    productCategoryId: values.productCategoryId,
    productCostPrice: values.productCostPrice,
    productMarketPrice: values.productMarketPrice,
    productName: values.productName,
    productPicUrl: values.productPicUrl,
    productPrice: values.productPrice,
    productStock: values.productStock,
    supplyEntityId: values.supplyEntityId,
    supplyDeliveryDays: values.supplyDeliveryDays,
    supplyMinOrderQuantity: values.supplyMinOrderQuantity,
    supplyRemark: values.supplyRemark,
    supplyServiceArea: values.supplyServiceArea,
    standardAttributes: buildStandardAttributes(),
    customTags: buildCustomTags(),
    customTagReviewNote: values.customTagReviewNote,
    customTagReviewRequired: values.customTagReviewRequired ?? true,
    templateId: values.templateId,
  };
}

function updateModalAction() {
  modalApi.setState({
    confirmText: isAuditStep.value ? '提交审核' : '下一步',
    showConfirmButton: !submitResult.value,
  });
}

async function validateCurrentStep() {
  if (currentStep.value === 0) {
    return (await baseFormApi.validate()).valid;
  }
  if (currentStep.value === 1) {
    return (await categoryFormApi.validate()).valid;
  }
  if (currentStep.value === 2) {
    return (await supplyFormApi.validate()).valid;
  }
  if (currentStep.value === 3) {
    const { valid } = await chainFormApi.validate();
    if (!valid) {
      return false;
    }
    const attributeValid = await attributeFormApi.validate();
    if (!attributeValid.valid) {
      return false;
    }
    if (hasIncompleteStandardAttribute()) {
      message.error('商品关键参数需同时填写参数名和参数值');
      return false;
    }
  }
  return true;
}

async function goNextStep() {
  if (!(await validateCurrentStep())) {
    return;
  }
  publishPreview.value = await buildPublishPreview();
  if (currentStep.value < productPublishSteps.length - 1) {
    currentStep.value += 1;
    updateModalAction();
  }
}

function goPrevStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1;
    submitResult.value = undefined;
    updateModalAction();
  }
}

async function submitProductPublish() {
  modalApi.lock();
  try {
    const result = await submitRelation(await buildSubmitRequest());
    submitResult.value = result;
    publishPreview.value = await buildPublishPreview();
    updateModalAction();
    emit('success');
    message.success($t('ui.actionMessage.operationSuccess'));
  } finally {
    modalApi.unlock();
  }
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (!isAuditStep.value) {
      await goNextStep();
      return;
    }
    await submitProductPublish();
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      currentStep.value = 0;
      publishPreview.value = {};
      submitResult.value = undefined;
      await baseFormApi.resetForm();
      await categoryFormApi.resetForm();
      await supplyFormApi.resetForm();
      await chainFormApi.resetForm();
      await attributeFormApi.resetForm();
      resetStructuredFields();
      return;
    }
    currentStep.value = 0;
    publishPreview.value = {};
    submitResult.value = undefined;
    await loadChainEntityOptions();
    const data = modalApi.getData<ProductPublishFormValues>() ?? {};
    resetStructuredFields(data);
    await baseFormApi.setValues({
      ...defaultBaseValues,
      ...data,
    });
    await categoryFormApi.setValues(data);
    await supplyFormApi.setValues({
      ...defaultSupplyValues,
      ...data,
    });
    await chainFormApi.setValues({
      ...defaultChainValues,
      ...data,
    });
    await attributeFormApi.setValues({
      ...defaultAttributeValues,
      ...data,
    });
    updateModalAction();
  },
});
</script>

<template>
  <Modal class="w-3/5" title="商品发布">
    <template #prepend-footer>
      <Button v-if="currentStep > 0 && !submitResult" @click="goPrevStep">
        上一步
      </Button>
    </template>
    <div class="wujin-product-publish">
      <Steps :current="currentStep" :items="productPublishSteps" size="small" />

      <div class="wujin-product-publish__body">
        <BaseForm v-show="currentStep === 0" class="mx-4" />
        <CategoryForm v-show="currentStep === 1" class="mx-4" />
        <div
          v-show="currentStep === 2"
          class="wujin-product-publish__supply mx-4"
        >
          <Alert
            :message="productPublishStructuredHelp.supplyCapability"
            show-icon
            type="info"
          />
          <SupplyForm />
        </div>
        <div v-show="currentStep === 3" class="wujin-product-publish__chain">
          <ChainForm class="mx-4" />
          <div class="wujin-product-publish__section mx-4">
            <div class="wujin-product-publish__section-title">
              <span>主要原材料/加工工艺</span>
              <span>{{ productPublishStructuredHelp.chainRelations }}</span>
            </div>
            <div class="wujin-product-publish__relation-list">
              <div
                v-for="(row, index) in relationRows"
                :key="`relation-${index}`"
                class="wujin-product-publish__relation-row"
              >
                <Select
                  v-model:value="row.entityId"
                  allow-clear
                  show-search
                  :filter-option="true"
                  :options="chainEntityOptions"
                  placeholder="搜索选择，如天然橡胶、热处理、镀锌"
                />
                <Select
                  v-model:value="row.relationType"
                  :options="relationTypeOptions"
                  placeholder="关系类型"
                />
                <Switch
                  v-model:checked="row.requiredFlag"
                  checked-children="必填"
                  un-checked-children="选填"
                />
                <Input v-model:value="row.remark" placeholder="补充说明" />
                <Button type="link" @click="removeRelationRow(index)">
                  移除
                </Button>
              </div>
            </div>
            <Button type="dashed" @click="addRelationRow">添加现有项</Button>
          </div>

          <div class="wujin-product-publish__section mx-4">
            <div class="wujin-product-publish__section-title">
              <span>系统里搜不到的资料</span>
              <span>输入商户自己的叫法，平台审核后会沉淀为可搜索资料</span>
            </div>
            <div class="wujin-product-publish__relation-row">
              <Input
                v-model:value="customRelationDraft.entityName"
                placeholder="输入买家可能搜索的材料、工艺或俗称"
              />
              <Select
                v-model:value="customRelationDraft.relationType"
                :options="relationTypeOptions"
                placeholder="关系类型"
              />
              <Switch
                v-model:checked="customRelationDraft.requiredFlag"
                checked-children="必填"
                un-checked-children="选填"
              />
              <Input
                v-model:value="customRelationDraft.remark"
                placeholder="补充说明"
              />
              <Button type="primary" @click="addCustomRelationDraft">
                添加自定义项
              </Button>
            </div>
            <div
              v-if="customRelationDrafts.length > 0"
              class="wujin-product-publish__custom-list"
            >
              <div
                v-for="(row, index) in customRelationDrafts"
                :key="`custom-relation-${row.entityName}-${index}`"
                class="wujin-product-publish__custom-item"
              >
                <span>{{ row.entityName }}</span>
                <span>{{
                  optionLabel(relationTypeOptions, row.relationType)
                }}</span>
                <span>{{ row.requiredFlag ? '必填' : '选填' }}</span>
                <span>{{ row.remark || '-' }}</span>
                <Button type="link" @click="removeCustomRelationDraft(index)">
                  移除
                </Button>
              </div>
            </div>
          </div>

          <div class="wujin-product-publish__section mx-4">
            <div class="wujin-product-publish__section-title">
              <span>商品关键参数</span>
              <span>{{ productPublishStructuredHelp.standardAttributes }}</span>
            </div>
            <div
              v-for="(row, index) in standardAttributeRows"
              :key="`attribute-${index}`"
              class="wujin-product-publish__attribute-row"
            >
              <Input v-model:value="row.name" placeholder="参数名，如材质" />
              <Input
                v-model:value="row.value"
                placeholder="参数值，如304不锈钢"
              />
              <Button type="link" @click="removeStandardAttribute(index)">
                移除
              </Button>
            </div>
            <Button type="dashed" @click="addStandardAttribute">
              添加关键参数
            </Button>
          </div>

          <div class="wujin-product-publish__section mx-4">
            <div class="wujin-product-publish__section-title">
              <span>买家常搜词</span>
              <span>{{ productPublishStructuredHelp.customTags }}</span>
            </div>
            <Select
              v-model:value="customTagValues"
              mode="tags"
              placeholder="输入后回车，如耐腐蚀、小批量现货、GCr15"
            />
          </div>
          <AttributeForm class="mx-4" />
        </div>

        <div v-if="currentStep === 3" class="wujin-product-publish__review">
          <Alert
            description="确认后系统会提交商品产业链关系申报，并返回审核路线与关系完善度。"
            message="审核上架"
            show-icon
            type="info"
          />
          <Descriptions bordered size="small" :column="2">
            <Descriptions.Item label="商品名称">
              {{ publishPreview.productName || '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="商品泳道">
              {{ optionLabel(laneOptions, publishPreview.productLane) }}
            </Descriptions.Item>
            <Descriptions.Item label="商品品牌">
              {{ publishPreview.productBrandId ? '已选择' : '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="销售价">
              {{ publishPreview.productPrice ?? 0 }} 分
            </Descriptions.Item>
            <Descriptions.Item label="可供库存">
              {{ publishPreview.productStock ?? 0 }}
            </Descriptions.Item>
            <Descriptions.Item label="供应内容">
              {{ publishPreview.supplyEntityId ? '已选择' : '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="最小起订量">
              {{ publishPreview.supplyMinOrderQuantity ?? 1 }}
            </Descriptions.Item>
            <Descriptions.Item label="交付周期">
              {{ publishPreview.supplyDeliveryDays ?? 1 }} 天
            </Descriptions.Item>
            <Descriptions.Item label="服务区域">
              {{ publishPreview.supplyServiceArea || '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="供应说明" :span="2">
              {{ publishPreview.supplyRemark || '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="上架类目">
              {{ publishPreview.productCategoryId ? '已选择' : '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="行业模板">
              {{ publishPreview.templateId ? '已选择' : '-' }}
            </Descriptions.Item>
            <Descriptions.Item label="认证数量" :span="2">
              {{ publishPreview.certificationCount ?? 0 }}
            </Descriptions.Item>
            <Descriptions.Item label="主要原材料/加工工艺" :span="2">
              {{ formatRelations(publishPreview.customRelations) }}
            </Descriptions.Item>
            <Descriptions.Item label="商品关键参数" :span="2">
              {{ formatStandardAttributes(publishPreview.standardAttributes) }}
            </Descriptions.Item>
            <Descriptions.Item label="买家常搜词" :span="2">
              {{ formatTags(publishPreview.customTags) }}
            </Descriptions.Item>
          </Descriptions>

          <div v-if="submitResult" class="wujin-product-publish__result">
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
              <Descriptions.Item label="商品ID">
                {{ submitResult.productId ?? publishPreview.productId ?? '-' }}
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
            <div class="wujin-product-publish__score">
              <span>完善度</span>
              <Progress
                :percent="submitResult.completenessScore ?? 0"
                size="small"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.wujin-product-publish {
  display: grid;
  gap: 16px;
  padding: 4px 0 8px;
}

.wujin-product-publish__body {
  min-height: 280px;
}

.wujin-product-publish__supply {
  display: grid;
  gap: 16px;
}

.wujin-product-publish__review,
.wujin-product-publish__result {
  display: grid;
  gap: 12px;
  margin: 0 16px;
}

.wujin-product-publish__chain,
.wujin-product-publish__section,
.wujin-product-publish__relation-list,
.wujin-product-publish__custom-list {
  display: grid;
  gap: 10px;
}

.wujin-product-publish__section {
  padding-bottom: 12px;
  border-bottom: 1px solid hsl(214 18% 92%);
}

.wujin-product-publish__section-title {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: hsl(215 14% 42%);
}

.wujin-product-publish__section-title span:first-child {
  color: hsl(222 22% 18%);
  font-weight: 600;
}

.wujin-product-publish__relation-row {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) 150px 78px minmax(180px, 1fr) auto;
  gap: 8px;
  align-items: center;
}

.wujin-product-publish__attribute-row {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) minmax(220px, 1.3fr) auto;
  gap: 8px;
  align-items: center;
}

.wujin-product-publish__custom-item {
  display: grid;
  grid-template-columns: minmax(180px, 1.2fr) 130px 70px minmax(180px, 1fr) auto;
  gap: 8px;
  align-items: center;
  padding: 6px 8px;
  background: hsl(210 20% 98%);
  border: 1px solid hsl(214 18% 90%);
  border-radius: 6px;
}

.wujin-product-publish__score {
  display: grid;
  gap: 6px;
}
</style>
