<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSpuApi } from '#/api/mall/product/spu';
import type { Ref } from 'vue';

import { computed, onMounted, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { Card, Col, message, Row, Statistic, Tabs } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getSpuPage } from '#/api/mall/product/spu';
import {
  getSupplyCapabilityList,
  getSourcingLeadList,
  getIndustryTemplateList,
  getRelationItemList,
  getRelationSubmissionList,
} from '#/api/wujin/merchant';

import {
  useItemColumns,
  useItemFormSchema,
  useProductListColumns,
  useProductListFormSchema,
  useSubmissionColumns,
  useSubmissionFormSchema,
  useSourcingLeadColumns,
  useSourcingLeadFormSchema,
  useTemplateColumns,
  useTemplateFormSchema,
} from './data';
import CompletenessExplainModal from './modules/completeness-explain-modal.vue';
import ProductPublishWizard from './modules/product-publish-wizard.vue';
import RelationSubmitForm from './modules/relation-submit-form.vue';
import SourcingLeadHandleForm from './modules/sourcing-lead-handle-form.vue';

defineOptions({ name: 'WujinMerchantManage' });

type MetricCard = {
  description?: string;
  suffix?: string;
  title: string;
  value: number;
};

const activeTab = ref('product');
const relationSubmissions = ref<any[]>([]);
const sourcingLeads = ref<any[]>([]);
const supplyCapabilities = ref<any[]>([]);

const [RelationSubmitFormModal, relationSubmitFormModalApi] = useVbenModal({
  connectedComponent: RelationSubmitForm,
  destroyOnClose: true,
});

const [ProductPublishWizardModal, productPublishWizardModalApi] = useVbenModal({
  connectedComponent: ProductPublishWizard,
  destroyOnClose: true,
});

const [SourcingLeadHandleFormModal, sourcingLeadHandleFormModalApi] =
  useVbenModal({
    connectedComponent: SourcingLeadHandleForm,
    destroyOnClose: true,
  });

const [CompletenessExplainModalView, completenessExplainModalApi] =
  useVbenModal({
    connectedComponent: CompletenessExplainModal,
    destroyOnClose: true,
  });

function createListGridOptions<T>(
  columns: VxeTableGridOptions<T>['columns'],
  queryApi: (params: Record<string, any>) => Promise<T[]>,
  cacheRef?: Ref<T[]>,
): VxeTableGridOptions<T> {
  return {
    columns,
    height: 680,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      autoLoad: true,
      ajax: {
        query: async (_params, formValues) => {
          const list = await queryApi(formValues);
          if (cacheRef) {
            cacheRef.value = list ?? [];
          }
          return list ?? [];
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  };
}

const [ProductGrid, productGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useProductListFormSchema(),
  },
  gridOptions: {
    columns: useProductListColumns(),
    height: 680,
    cellConfig: {
      height: 80,
    },
    pagerConfig: {
      enabled: true,
    },
    proxyConfig: {
      autoLoad: true,
      ajax: {
        query: async ({ page }, formValues) =>
          getSpuPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MallSpuApi.Spu>,
});

const [SubmissionGrid, submissionGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSubmissionFormSchema(),
  },
  gridOptions: createListGridOptions(
    useSubmissionColumns(),
    getRelationSubmissionList,
    relationSubmissions,
  ) as VxeTableGridOptions,
});

const [ItemGrid] = useVbenVxeGrid({
  formOptions: {
    schema: useItemFormSchema(),
  },
  gridOptions: createListGridOptions(
    useItemColumns(),
    getRelationItemList,
  ) as VxeTableGridOptions,
});

const [TemplateGrid] = useVbenVxeGrid({
  formOptions: {
    schema: useTemplateFormSchema(),
  },
  gridOptions: createListGridOptions(
    useTemplateColumns(),
    getIndustryTemplateList,
  ) as VxeTableGridOptions,
});

const [SourcingLeadGrid, sourcingLeadGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSourcingLeadFormSchema(),
  },
  gridOptions: createListGridOptions(
    useSourcingLeadColumns(),
    getSourcingLeadList,
    sourcingLeads,
  ) as VxeTableGridOptions,
});

const tableTitle = computed(() => {
  const titles: Record<string, string> = {
    item: '申报明细',
    product: '商品列表',
    submission: '关系申报',
    sourcingLead: '寻源线索',
    template: '行业模板',
  };
  return titles[activeTab.value] ?? '商家关系申报';
});

const businessMetricCards = computed<MetricCard[]>(() => {
  const submissions = relationSubmissions.value;
  const leads = sourcingLeads.value;
  const capabilities = supplyCapabilities.value;
  const pendingSubmissions = submissions.filter((item) =>
    [0, 10, 40].includes(Number(item.auditStatus)),
  ).length;
  const activeCapabilities = capabilities.filter(
    (item) => Number(item.supplyStatus) === 0,
  ).length;
  const pendingLeads = leads.filter(
    (item) =>
      !['CLOSED', 'CONVERTED', 'LOST'].includes(String(item.leadStatus ?? '')),
  ).length;

  return [
    {
      description: '关系申报列表',
      title: '申报总数',
      value: submissions.length,
    },
    {
      description: '草稿、待审核、驳回补充',
      title: '待审核申报',
      value: pendingSubmissions,
    },
    {
      description: '供应能力启用中',
      title: '可供能力',
      value: activeCapabilities,
    },
    {
      description: '未关闭、未转化、未流失',
      title: '待跟进线索',
      value: pendingLeads,
    },
  ];
});

const businessSourceSummary = computed(
  () =>
    `本地聚合：关系申报 ${relationSubmissions.value.length} 条，供应能力 ${supplyCapabilities.value.length} 条，寻源线索 ${sourcingLeads.value.length} 条。`,
);

const leadReportCards = computed<MetricCard[]>(() => {
  const leads = sourcingLeads.value;
  const quoted = leads.filter((item) =>
    ['QUOTED', 'CONVERTED'].includes(String(item.leadStatus ?? '')),
  ).length;
  const converted = leads.filter(
    (item) => String(item.leadStatus ?? '') === 'CONVERTED',
  ).length;
  const pending = leads.filter(
    (item) =>
      !['CLOSED', 'CONVERTED', 'LOST'].includes(String(item.leadStatus ?? '')),
  ).length;
  const conversionRate = leads.length
    ? Math.round((converted / leads.length) * 100)
    : 0;

  return [
    { title: '已报价', value: quoted },
    { title: '已转化', value: converted },
    { suffix: '%', title: '转化率', value: conversionRate },
    { title: '待继续跟进', value: pending },
  ];
});

async function loadBusinessSourceData() {
  const [submissions, leads, capabilities] = await Promise.allSettled([
    getRelationSubmissionList(undefined, { silentErrorMessage: true }),
    getSourcingLeadList(undefined, { silentErrorMessage: true }),
    getSupplyCapabilityList(undefined, { silentErrorMessage: true }),
  ]);

  let hasUnavailableSource = false;

  if (submissions.status === 'fulfilled') {
    relationSubmissions.value = submissions.value ?? [];
  } else {
    hasUnavailableSource = true;
  }

  if (leads.status === 'fulfilled') {
    sourcingLeads.value = leads.value ?? [];
  } else {
    hasUnavailableSource = true;
  }

  if (capabilities.status === 'fulfilled') {
    supplyCapabilities.value = capabilities.value ?? [];
  } else {
    hasUnavailableSource = true;
  }

  if (hasUnavailableSource) {
    message.warning('部分经营数据本地聚合暂不可用，请稍后刷新。');
  }
}

function refreshSubmissionGrid() {
  submissionGridApi.query();
  void loadBusinessSourceData();
}

function handleProductPublishSuccess() {
  productGridApi.query();
  refreshSubmissionGrid();
}

function refreshSourcingLeadGrid() {
  sourcingLeadGridApi.query();
  void loadBusinessSourceData();
}

function handleCreateRelationSubmit() {
  relationSubmitFormModalApi
    .setData({
      certificationCount: 0,
      customRelationsJson: '[]',
      hasApplicationDescription: true,
    })
    .open();
}

function handleEditRelationSubmit(row: any) {
  relationSubmitFormModalApi
    .setData({
      ...row,
      customRelationsJson: row.customRelationsJson ?? '[]',
      hasApplicationDescription: row.hasApplicationDescription ?? true,
    })
    .open();
}

function handleImportRelationTemplate() {
  message.info('模板导入后端接口暂未提供，当前仅预留入口。');
}

function handleExplainCompleteness(row: any) {
  completenessExplainModalApi.setData(row).open();
}

function handleCreateProductPublish() {
  productPublishWizardModalApi
    .setData({
      certificationCount: 0,
      hasApplicationDescription: true,
      productLane: 'PRODUCT',
    })
    .open();
}

function handleContactSourcingLead(row: any) {
  sourcingLeadHandleFormModalApi
    .setData({
      followStage: row.followStage,
      handleAction: row.leadStatus === 'QUOTED' ? 'QUOTED' : 'CONTACTED',
      handleRemark: row.handleRemark,
      leadId: row.id,
      merchantId: row.merchantId,
      nextFollowTime: row.nextFollowTime,
      quotedAmount: row.quotedAmount,
      winProbability: row.winProbability,
    })
    .open();
}

onMounted(() => {
  void loadBusinessSourceData();
});
</script>

<template>
  <Page auto-content-height>
    <RelationSubmitFormModal @success="refreshSubmissionGrid" />
    <ProductPublishWizardModal @success="handleProductPublishSuccess" />
    <SourcingLeadHandleFormModal @success="refreshSourcingLeadGrid" />
    <CompletenessExplainModalView />

    <section class="wujin-merchant-dashboard">
      <div class="wujin-merchant-dashboard__header">
        <span>经营数据</span>
        <span>{{ businessSourceSummary }}</span>
      </div>
      <Row :gutter="[12, 12]">
        <Col
          v-for="card in businessMetricCards"
          :key="card.title"
          :lg="6"
          :sm="12"
          :xs="24"
        >
          <Card size="small">
            <Statistic
              :title="card.title"
              :value="card.value"
              :suffix="card.suffix"
            />
            <div class="wujin-merchant-dashboard__desc">
              {{ card.description }}
            </div>
          </Card>
        </Col>
      </Row>
    </section>

    <Tabs v-model:active-key="activeTab" class="wujin-merchant-tabs">
      <Tabs.TabPane key="product" tab="商品列表">
        <ProductGrid :table-title="tableTitle">
          <template #toolbar-tools>
            <TableAction
              :actions="[
                {
                  label: '发布商品',
                  type: 'primary',
                  icon: ACTION_ICON.ADD,
                  auth: ['wujin:merchant-product:create'],
                  onClick: handleCreateProductPublish,
                },
              ]"
            />
          </template>
        </ProductGrid>
      </Tabs.TabPane>
      <Tabs.TabPane key="submission" tab="关系申报">
        <SubmissionGrid :table-title="tableTitle">
          <template #toolbar-tools>
            <TableAction
              :actions="[
                {
                  label: '新增申报',
                  icon: ACTION_ICON.ADD,
                  auth: ['wujin:merchant-relation-submit:create'],
                  onClick: handleCreateRelationSubmit,
                },
                {
                  label: '模板导入',
                  icon: ACTION_ICON.ADD,
                  auth: ['wujin:merchant-relation-submit:create'],
                  onClick: handleImportRelationTemplate,
                },
              ]"
            />
          </template>
          <template #completenessActions="{ row }">
            <TableAction
              :actions="[
                {
                  label: `${row.completenessScore ?? 0}% 完善度解释`,
                  type: 'link',
                  icon: ACTION_ICON.VIEW,
                  auth: ['wujin:merchant-relation-submit:query'],
                  onClick: handleExplainCompleteness.bind(null, row),
                },
              ]"
            />
          </template>
          <template #submissionActions="{ row }">
            <TableAction
              :actions="[
                {
                  label: '编辑申报',
                  type: 'link',
                  icon: ACTION_ICON.EDIT,
                  auth: ['wujin:merchant-relation-submit:update'],
                  onClick: handleEditRelationSubmit.bind(null, row),
                },
              ]"
            />
          </template>
        </SubmissionGrid>
      </Tabs.TabPane>
      <Tabs.TabPane key="item" tab="申报明细">
        <ItemGrid :table-title="tableTitle" />
      </Tabs.TabPane>
      <Tabs.TabPane key="template" tab="行业模板">
        <TemplateGrid :table-title="tableTitle" />
      </Tabs.TabPane>
      <Tabs.TabPane key="sourcingLead" tab="寻源线索">
        <section class="wujin-lead-report">
          <div class="wujin-lead-report__title">线索转化报表</div>
          <Row :gutter="[12, 12]">
            <Col
              v-for="card in leadReportCards"
              :key="card.title"
              :lg="6"
              :sm="12"
              :xs="24"
            >
              <Card size="small">
                <Statistic
                  :title="card.title"
                  :value="card.value"
                  :suffix="card.suffix"
                />
              </Card>
            </Col>
          </Row>
        </section>
        <SourcingLeadGrid :table-title="tableTitle">
          <template #sourcingLeadActions="{ row }">
            <TableAction
              :actions="[
                {
                  label: '处理',
                  type: 'link',
                  icon: ACTION_ICON.EDIT,
                  auth: ['wujin:merchant-sourcing-lead:update'],
                  onClick: handleContactSourcingLead.bind(null, row),
                },
              ]"
            />
          </template>
        </SourcingLeadGrid>
      </Tabs.TabPane>
    </Tabs>
  </Page>
</template>

<style scoped>
.wujin-merchant-dashboard,
.wujin-lead-report {
  display: grid;
  gap: 12px;
  margin-bottom: 12px;
}

.wujin-merchant-dashboard__header,
.wujin-lead-report__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.wujin-merchant-dashboard__header span:last-child {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 400;
}

.wujin-merchant-dashboard__desc {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-merchant-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 12px;
}
</style>
