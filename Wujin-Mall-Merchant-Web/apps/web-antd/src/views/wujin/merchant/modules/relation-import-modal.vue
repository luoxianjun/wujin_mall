<script lang="ts" setup>
import type { FileType } from 'ant-design-vue/es/upload/interface';

import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import {
  Alert,
  Button,
  InputNumber,
  message,
  Select,
  Table,
  Tag,
  Upload,
} from 'ant-design-vue';

import {
  downloadRelationImportTemplate,
  getIndustryTemplateList,
  importRelations,
  previewRelationImportFile,
} from '#/api/wujin/merchant';

import { optionLabel, relationTypeOptions } from '../data';

defineOptions({ name: 'WujinMerchantRelationImportModal' });

const emit = defineEmits(['success']);

const templateOptions = ref<Array<{ label: string; value: number }>>([]);
const templates = ref<WujinMerchantApi.IndustryTemplate[]>([]);
const templateId = ref<number>();
const defaultProductCategoryId = ref<number>();
const uploadFile = ref<File>();
const fileList = ref<any[]>([]);
const preview = ref<WujinMerchantApi.ImportPreview>();
const importResult = ref<WujinMerchantApi.ImportResult>();

const previewColumns = [
  { dataIndex: 'rowNo', title: '行号', width: 64 },
  { dataIndex: 'productName', title: '商品名称', width: 180 },
  { dataIndex: 'productId', title: '商品ID', width: 90 },
  { dataIndex: 'entityName', title: '产业链实体', width: 140 },
  { dataIndex: 'relationType', title: '关系类型', width: 120 },
  { dataIndex: 'stockCount', title: '库存', width: 80 },
  { dataIndex: 'result', title: '校验结果' },
];

const previewRows = computed(() => [
  ...(preview.value?.invalidRows ?? []),
  ...(preview.value?.validRows ?? []),
]);

const matchedCount = computed(
  () =>
    (preview.value?.validRows ?? []).filter((row) => row.templateMatched)
      .length,
);

const selectedTemplate = computed(() =>
  templates.value.find((item) => item.id === templateId.value),
);

const confirmText = computed(() => {
  if (importResult.value) {
    return '完成';
  }
  if (preview.value?.validCount) {
    return `确认导入 ${preview.value.validCount} 行`;
  }
  return '上传并校验';
});

async function loadTemplates() {
  templates.value = await getIndustryTemplateList({ status: 0 }).catch(
    () => [],
  );
  templateOptions.value = templates.value
    .filter((item) => item.id)
    .map((item) => ({
      label: `${item.name || item.templateCode}（${item.industryCode || '通用'}）`,
      value: item.id!,
    }));
}

function resetState() {
  templateId.value = undefined;
  defaultProductCategoryId.value = undefined;
  uploadFile.value = undefined;
  fileList.value = [];
  preview.value = undefined;
  importResult.value = undefined;
}

function beforeUpload(file: FileType) {
  uploadFile.value = file as File;
  fileList.value = [file];
  preview.value = undefined;
  importResult.value = undefined;
  return false;
}

function handleRemoveFile() {
  uploadFile.value = undefined;
  fileList.value = [];
  preview.value = undefined;
}

async function handleDownloadTemplate() {
  const data = await downloadRelationImportTemplate();
  downloadFileFromBlobPart({
    fileName: '五金商家关系导入模板.xls',
    source: data,
  });
}

function toImportRow(
  row: WujinMerchantApi.ImportPreviewRow,
): WujinMerchantApi.ImportRow {
  return {
    deliveryDays: row.deliveryDays,
    entityId: row.entityId,
    entityName: row.entityName,
    merchantId: row.merchantId,
    minOrderQuantity: row.minOrderQuantity,
    productCategoryId: row.productCategoryId,
    productId: row.productId,
    productName: row.productName,
    relationType: row.relationType,
    remark: row.remark,
    serviceArea: row.serviceArea,
    stockCount: row.stockCount,
  };
}

async function runPreview() {
  if (!templateId.value) {
    message.error('请选择行业模板');
    return;
  }
  if (!uploadFile.value) {
    message.error('请先选择要导入的 Excel 文件');
    return;
  }
  preview.value = await previewRelationImportFile({
    defaultProductCategoryId: defaultProductCategoryId.value,
    file: uploadFile.value,
    industryCode: selectedTemplate.value?.industryCode,
    templateId: templateId.value,
  });
  if (!preview.value.totalCount) {
    message.warning('文件中没有可导入的数据行');
  }
}

async function runImport() {
  importResult.value = await importRelations({
    defaultProductCategoryId: defaultProductCategoryId.value,
    industryCode: selectedTemplate.value?.industryCode,
    rows: (preview.value?.validRows ?? []).map((row) => toImportRow(row)),
    templateId: templateId.value,
  });
  emit('success');
  message.success(
    `已导入 ${importResult.value.importedCount ?? 0} 行，${importResult.value.pendingReviewCount ?? 0} 行待平台审核`,
  );
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (importResult.value) {
      await modalApi.close();
      return;
    }
    modalApi.lock();
    try {
      await (preview.value?.validCount ? runImport() : runPreview());
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    resetState();
    if (isOpen) {
      await loadTemplates();
    }
  },
});
</script>

<template>
  <Modal class="w-3/5" :confirm-text="confirmText" title="关系模板导入">
    <div class="wujin-relation-import mx-4">
      <Alert
        class="mb-3"
        message="按模板填写商品与原材料/加工工艺关系"
        description="与所选行业模板一致的关系导入后直接生效并开启供应能力；模板外的关系会进入平台审核，审核通过后生效。未填写商家编号的行归属当前登录商家。"
        show-icon
        type="info"
      />
      <div class="wujin-relation-import__fields">
        <label>
          <span>行业模板</span>
          <Select
            v-model:value="templateId"
            :options="templateOptions"
            placeholder="选择行业模板"
            show-search
            option-filter-prop="label"
          />
        </label>
        <label>
          <span>默认成品分类编号</span>
          <InputNumber
            v-model:value="defaultProductCategoryId"
            class="w-full"
            :min="1"
            placeholder="行内未填写分类时使用"
          />
        </label>
        <label>
          <span>Excel 文件</span>
          <Upload
            :before-upload="beforeUpload"
            :file-list="fileList"
            :max-count="1"
            accept=".xls,.xlsx"
            @remove="handleRemoveFile"
          >
            <Button>选择 Excel 文件</Button>
          </Upload>
        </label>
      </div>

      <template v-if="preview">
        <div class="wujin-relation-import__summary">
          <Tag color="blue">共 {{ preview.totalCount ?? 0 }} 行</Tag>
          <Tag color="green">可导入 {{ preview.validCount ?? 0 }} 行</Tag>
          <Tag color="cyan">模板内直接生效 {{ matchedCount }} 行</Tag>
          <Tag color="orange">
            需平台审核 {{ (preview.validCount ?? 0) - matchedCount }} 行
          </Tag>
          <Tag :color="preview.invalidCount ? 'red' : 'default'">
            需修正 {{ preview.invalidCount ?? 0 }} 行
          </Tag>
        </div>
        <Table
          :columns="previewColumns"
          :data-source="previewRows"
          :pagination="{ pageSize: 8, hideOnSinglePage: true }"
          :row-key="
            (row: WujinMerchantApi.ImportPreviewRow) => `row-${row.rowNo}`
          "
          :scroll="{ x: 860 }"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'relationType'">
              {{ optionLabel(relationTypeOptions, record.relationType) }}
            </template>
            <template v-else-if="column.dataIndex === 'result'">
              <template v-if="record.errors?.length">
                <Tag v-for="error in record.errors" :key="error" color="red">
                  {{ error }}
                </Tag>
              </template>
              <Tag v-else-if="record.templateMatched" color="green">
                模板内关系，导入即生效
              </Tag>
              <Tag v-else color="orange">模板外关系，导入后待审核</Tag>
            </template>
          </template>
        </Table>
      </template>

      <Alert
        v-if="importResult"
        class="mt-3"
        :message="`导入完成：${importResult.importedCount ?? 0} 行`"
        :description="`直接生效 ${importResult.effectiveCount ?? 0} 行，待平台审核 ${importResult.pendingReviewCount ?? 0} 行，跳过 ${importResult.skippedCount ?? 0} 行。`"
        show-icon
        type="success"
      />
    </div>
    <template #prepend-footer>
      <div class="flex flex-auto items-center">
        <Button @click="handleDownloadTemplate">下载导入模板</Button>
      </div>
    </template>
  </Modal>
</template>

<style scoped>
.wujin-relation-import__fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.wujin-relation-import__fields label span {
  display: block;
  margin-bottom: 4px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-relation-import__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .wujin-relation-import__fields {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
