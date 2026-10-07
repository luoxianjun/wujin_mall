<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ActionItem } from '#/components/table-action/typing';

import { computed, nextTick, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';

import { useSortable } from '@vueuse/integrations/useSortable';
import {
  Alert,
  Card,
  Col,
  message,
  Radio,
  Row,
  Spin,
  Statistic,
} from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  autoDispatchSourcingLead,
  getCategoryList,
  getCategoryMappingList,
  getIndustryTemplateList,
  getMonitorDashboardSummary,
  getMonitorSnapshotList,
  getRelationAuditRecordList,
  getSearchBehaviorLogList,
  getSearchRuleConfigList,
  getSourcingLeadList,
  updateCategory,
} from '#/api/wujin/platform';

import {
  healthStatusOptions,
  laneOptions,
  monitorMetricOptions,
  optionLabel,
  useAuditColumns,
  useAuditFormSchema,
  useCategoryColumns,
  useCategoryFormSchema,
  useMappingColumns,
  useMappingFormSchema,
  useMonitorSnapshotColumns,
  useMonitorSnapshotFormSchema,
  useSearchLogColumns,
  useSearchLogFormSchema,
  useSearchRuleColumns,
  useSearchRuleFormSchema,
  useSourcingLeadColumns,
  useSourcingLeadFormSchema,
  useTemplateColumns,
  useTemplateFormSchema,
} from './data';
import AuditReviewForm from './modules/audit-review-form.vue';
import CategoryBatchMigrateForm from './modules/category-batch-migrate-form.vue';
import CategoryForm from './modules/category-form.vue';
import MappingForm from './modules/mapping-form.vue';
import SearchRuleForm from './modules/search-rule-form.vue';
import SourcingLeadDispatchForm from './modules/sourcing-lead-dispatch-form.vue';
import TemplateForm from './modules/template-form.vue';
import TemplateItemManage from './modules/template-item-manage-modal.vue';

defineOptions({ name: 'WujinPlatformManage' });

type PlatformSection =
  | 'audit'
  | 'category'
  | 'dashboard'
  | 'mapping'
  | 'monitor'
  | 'searchLog'
  | 'searchRule'
  | 'sourcingLead'
  | 'template';

const route = useRoute();

const sectionByRoutePath: Record<string, PlatformSection> = {
  audit: 'audit',
  category: 'category',
  dashboard: 'dashboard',
  mapping: 'mapping',
  monitor: 'monitor',
  'search-log': 'searchLog',
  'search-rule': 'searchRule',
  'sourcing-lead': 'sourcingLead',
  template: 'template',
};

const activeSection = computed<PlatformSection>(() => {
  const segments = String(route.path).split('/').filter(Boolean);
  const leafPath = segments[segments.length - 1] ?? '';
  return sectionByRoutePath[leafPath] ?? 'dashboard';
});

const dashboardLoading = ref(false);
const dashboardSummary = ref<WujinPlatformApi.MonitorDashboardSummary>();
const isCategoryTreeExpanded = ref(false);
const isCategorySorting = ref(false);
const categoryItems = ref<WujinPlatformApi.Category[]>([]);
const originalCategoryItems = ref<WujinPlatformApi.Category[]>([]);
const categorySortableInstance = ref<any>(null);
const selectedCategoryIds = ref<number[]>([]);

const categorySortableSelector =
  '.wujin-category-sort-area .vxe-table .vxe-table--body-wrapper:not(.fixed-right--wrapper) .vxe-table--body tbody';

const [CategoryFormModal, categoryFormModalApi] = useVbenModal({
  connectedComponent: CategoryForm,
  destroyOnClose: true,
});

const [CategoryBatchMigrateModal, categoryBatchMigrateModalApi] = useVbenModal({
  connectedComponent: CategoryBatchMigrateForm,
  destroyOnClose: true,
});

const [MappingFormModal, mappingFormModalApi] = useVbenModal({
  connectedComponent: MappingForm,
  destroyOnClose: true,
});

const [SearchRuleFormModal, searchRuleFormModalApi] = useVbenModal({
  connectedComponent: SearchRuleForm,
  destroyOnClose: true,
});

const [AuditReviewFormModal, auditReviewFormModalApi] = useVbenModal({
  connectedComponent: AuditReviewForm,
  destroyOnClose: true,
});

const [TemplateFormModal, templateFormModalApi] = useVbenModal({
  connectedComponent: TemplateForm,
  destroyOnClose: true,
});

const [TemplateItemManageModal, templateItemManageModalApi] = useVbenModal({
  connectedComponent: TemplateItemManage,
  destroyOnClose: true,
});

const [SourcingLeadDispatchFormModal, sourcingLeadDispatchFormModalApi] =
  useVbenModal({
    connectedComponent: SourcingLeadDispatchForm,
    destroyOnClose: true,
  });

interface DashboardMetric {
  label: string;
  precision?: number;
  source?: string;
  suffix?: string;
  value: number;
}

type CategoryLaneView = 'ALL' | WujinPlatformApi.WujinLane;
type SearchMonitorTimeRange = '30D' | '7D' | 'ALL';

interface CategoryHealthSummary {
  HEALTHY: number;
  NEEDS_SPLIT: number;
  UNBOUND: number;
  lanes: Record<WujinPlatformApi.WujinLane, CategoryLaneHealthRow>;
  total: number;
}

interface CategoryLaneHealthCell {
  label: string;
  percent: number;
  value: number;
}

interface CategoryLaneHealthRow {
  HEALTHY: number;
  NEEDS_SPLIT: number;
  UNBOUND: number;
  healthCells: CategoryLaneHealthCell[];
  label: string;
  lane: WujinPlatformApi.WujinLane;
  total: number;
}

interface SearchMonitorTrendItem {
  alertFlag?: boolean;
  createTime?: string;
  key: string;
  label: string;
  percent: number;
  value: number;
}

const categoryLaneView = ref<CategoryLaneView>('ALL');
const categoryLaneViewOptions: Array<{
  label: string;
  value: CategoryLaneView;
}> = [
  { label: '全部类目', value: 'ALL' },
  { label: '成品树', value: 'PRODUCT' },
  { label: '加工树', value: 'PROCESS' },
  { label: '原材料树', value: 'MATERIAL' },
];

const searchMonitorTimeRange = ref<SearchMonitorTimeRange>('7D');
const searchMonitorTimeRangeOptions: Array<{
  label: string;
  value: SearchMonitorTimeRange;
}> = [
  { label: '近7天', value: '7D' },
  { label: '近30天', value: '30D' },
  { label: '全部数据', value: 'ALL' },
];

const monitorSnapshots = ref<WujinPlatformApi.MonitorSnapshot[]>([]);
const monitorSnapshotLoading = ref(false);

function rateToPercent(value?: number) {
  return Number(((value ?? 0) * 100).toFixed(2));
}

function toBarPercent(value?: number) {
  return Math.min(100, Math.max(4, Math.abs(Number(value ?? 0))));
}

function createCategoryLaneHealthRow(
  lane: WujinPlatformApi.WujinLane,
  label: string,
): CategoryLaneHealthRow {
  return {
    HEALTHY: 0,
    NEEDS_SPLIT: 0,
    UNBOUND: 0,
    healthCells: [],
    label,
    lane,
    total: 0,
  };
}

function createCategoryHealthSummary(): CategoryHealthSummary {
  const lanes = laneOptions.reduce(
    (result, lane) => ({
      ...result,
      [lane.value]: createCategoryLaneHealthRow(
        lane.value as WujinPlatformApi.WujinLane,
        lane.label,
      ),
    }),
    {} as Record<WujinPlatformApi.WujinLane, CategoryLaneHealthRow>,
  );

  return {
    HEALTHY: 0,
    NEEDS_SPLIT: 0,
    UNBOUND: 0,
    lanes,
    total: 0,
  };
}

function buildCategoryHealthSummary(
  summary: CategoryHealthSummary,
  item: WujinPlatformApi.Category,
) {
  const healthStatus = item.healthStatus ?? 'UNBOUND';
  summary.total += 1;
  summary[healthStatus] += 1;

  if (item.lane && summary.lanes[item.lane]) {
    const lane = summary.lanes[item.lane];
    lane.total += 1;
    lane[healthStatus] += 1;
  }

  return summary;
}

function withHealthCells(row: CategoryLaneHealthRow): CategoryLaneHealthRow {
  return {
    ...row,
    healthCells: healthStatusOptions.map((status) => {
      const value = row[status.value as WujinPlatformApi.WujinHealthStatus];
      return {
        label: status.label,
        percent: row.total === 0 ? 0 : rateToPercent(value / row.total),
        value,
      };
    }),
  };
}

function filterMonitorSnapshotsByRange(
  snapshots: WujinPlatformApi.MonitorSnapshot[],
  range: SearchMonitorTimeRange,
) {
  if (range === 'ALL') {
    return snapshots;
  }

  const dayCount = range === '7D' ? 7 : 30;
  const startTime = Date.now() - dayCount * 24 * 60 * 60 * 1000;
  return snapshots.filter((snapshot) => {
    if (!snapshot.createTime) {
      return true;
    }
    const snapshotTime = new Date(snapshot.createTime).getTime();
    return Number.isNaN(snapshotTime) || snapshotTime >= startTime;
  });
}

async function loadDashboardSummary() {
  dashboardLoading.value = true;
  try {
    dashboardSummary.value = await getMonitorDashboardSummary();
  } catch (error) {
    console.error('Failed to load wujin monitor dashboard summary', error);
    dashboardSummary.value = { alerts: [] };
  } finally {
    dashboardLoading.value = false;
  }
}

async function loadMonitorSnapshots() {
  monitorSnapshotLoading.value = true;
  try {
    monitorSnapshots.value = await getMonitorSnapshotList();
  } catch (error) {
    console.error('Failed to load wujin monitor snapshots', error);
    monitorSnapshots.value = [];
  } finally {
    monitorSnapshotLoading.value = false;
  }
}

onMounted(() => {
  void loadDashboardSummary();
  void loadMonitorSnapshots();
});

function cloneCategoryItems(items: WujinPlatformApi.Category[]) {
  return items.map((item) => ({ ...item }));
}

function sortCategoryItems(items: WujinPlatformApi.Category[]) {
  return [...items].sort((left, right) => {
    const parentSort = (left.parentId ?? 0) - (right.parentId ?? 0);
    if (parentSort !== 0) {
      return parentSort;
    }
    const itemSort = (left.sort ?? 0) - (right.sort ?? 0);
    if (itemSort !== 0) {
      return itemSort;
    }
    return (left.id ?? 0) - (right.id ?? 0);
  });
}

function disableCategorySortable() {
  if (categorySortableInstance.value) {
    categorySortableInstance.value.option('disabled', true);
  }
}

function resetCategorySortState() {
  isCategorySorting.value = false;
  originalCategoryItems.value = [];
  disableCategorySortable();
  categorySortableInstance.value = null;
}

function resetCategorySelection() {
  selectedCategoryIds.value = [];
}

async function queryCategoryList(params: Record<string, any>) {
  const queryParams = {
    ...params,
    ...(categoryLaneView.value === 'ALL'
      ? {}
      : { lane: categoryLaneView.value }),
  };
  const list = sortCategoryItems(await getCategoryList(queryParams));
  categoryItems.value = list;
  resetCategorySelection();
  resetCategorySortState();
  return list;
}

async function queryMonitorSnapshotList(params: Record<string, any>) {
  const list = await getMonitorSnapshotList(params);
  monitorSnapshots.value = list;
  return list;
}

function createListGridOptions<T>(
  columns: VxeTableGridOptions<T>['columns'],
  queryApi: (params: Record<string, any>) => Promise<T[]>,
): VxeTableGridOptions<T> {
  return {
    columns,
    height: 520,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params, formValues) => await queryApi(formValues),
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

const [CategoryGrid, categoryGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useCategoryFormSchema(),
  },
  gridOptions: {
    ...createListGridOptions(useCategoryColumns(), queryCategoryList),
    height: 680,
    treeConfig: {
      parentField: 'parentId',
      reserve: true,
      rowField: 'id',
      transform: true,
    },
    checkboxConfig: {
      reserve: true,
    },
    events: {
      checkboxAll: handleCategorySelectionChange,
      checkboxChange: handleCategorySelectionChange,
    },
  } as VxeTableGridOptions,
});

const [MappingGrid, mappingGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useMappingFormSchema(),
  },
  gridOptions: createListGridOptions(
    useMappingColumns(),
    getCategoryMappingList,
  ) as VxeTableGridOptions,
});

const [TemplateGrid, templateGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useTemplateFormSchema(),
  },
  gridOptions: createListGridOptions(
    useTemplateColumns(),
    getIndustryTemplateList,
  ) as VxeTableGridOptions,
});

const [AuditGrid, auditGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useAuditFormSchema(),
  },
  gridOptions: createListGridOptions(
    useAuditColumns(),
    getRelationAuditRecordList,
  ) as VxeTableGridOptions,
});

const [SearchRuleGrid, searchRuleGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSearchRuleFormSchema(),
  },
  gridOptions: createListGridOptions(
    useSearchRuleColumns(),
    getSearchRuleConfigList,
  ) as VxeTableGridOptions,
});

const [SearchLogGrid] = useVbenVxeGrid({
  formOptions: {
    schema: useSearchLogFormSchema(),
  },
  gridOptions: createListGridOptions(
    useSearchLogColumns(),
    getSearchBehaviorLogList,
  ) as VxeTableGridOptions,
});

const [SourcingLeadGrid, sourcingLeadGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useSourcingLeadFormSchema(),
  },
  gridOptions: createListGridOptions(
    useSourcingLeadColumns(),
    getSourcingLeadList,
  ) as VxeTableGridOptions,
});

const [MonitorSnapshotGrid] = useVbenVxeGrid({
  formOptions: {
    schema: useMonitorSnapshotFormSchema(),
  },
  gridOptions: createListGridOptions(
    useMonitorSnapshotColumns(),
    queryMonitorSnapshotList,
  ) as VxeTableGridOptions,
});

const tableTitle = computed(() => {
  const titles: Record<string, string> = {
    audit: '关系审核',
    category: '平台类目',
    dashboard: '运营看板',
    mapping: '跨泳道映射',
    monitor: '监控快照',
    searchLog: '搜索监控',
    searchRule: '搜索规则',
    sourcingLead: '寻源线索',
    template: '行业模板',
  };
  return titles[activeSection.value] ?? '平台运营配置';
});

const dashboardMetrics = computed<DashboardMetric[]>(() => [
  {
    label: '搜索样本',
    suffix: '次',
    value: dashboardSummary.value?.searchSampleCount ?? 0,
  },
  {
    label: '审核样本',
    suffix: '条',
    value: dashboardSummary.value?.auditSampleCount ?? 0,
  },
  {
    label: '搜索满意度',
    precision: 2,
    value: dashboardSummary.value?.searchSatisfaction ?? 0,
  },
  {
    label: '制造链查看率',
    precision: 2,
    suffix: '%',
    value: rateToPercent(dashboardSummary.value?.chainViewRate),
  },
  {
    label: '分类准确率',
    precision: 2,
    suffix: '%',
    value: rateToPercent(dashboardSummary.value?.classificationAccuracy),
  },
  {
    label: '平均响应耗时',
    precision: 0,
    suffix: 'ms',
    value: dashboardSummary.value?.averageResponseTimeMillis ?? 0,
  },
  {
    label: '审核通过率',
    precision: 2,
    suffix: '%',
    value: rateToPercent(dashboardSummary.value?.relationAuditPassRate),
  },
  {
    label: '高风险预警',
    suffix: '次',
    value: dashboardSummary.value?.highRiskWarningCount ?? 0,
  },
]);

const dashboardAlerts = computed(() => dashboardSummary.value?.alerts ?? []);

const categoryHealthSummary = computed(() =>
  categoryItems.value.reduce(
    buildCategoryHealthSummary,
    createCategoryHealthSummary(),
  ),
);

const categoryHealthCards = computed(() => [
  {
    label: '总类目',
    value: categoryHealthSummary.value.total,
  },
  {
    label: '健康',
    value: categoryHealthSummary.value.HEALTHY,
  },
  {
    label: '需拆分',
    value: categoryHealthSummary.value.NEEDS_SPLIT,
  },
  {
    label: '无绑定',
    value: categoryHealthSummary.value.UNBOUND,
  },
]);

const categoryLaneHealthRows = computed(() =>
  laneOptions.map((lane) =>
    withHealthCells(
      categoryHealthSummary.value.lanes[
        lane.value as WujinPlatformApi.WujinLane
      ],
    ),
  ),
);

const searchMonitorFilteredSnapshots = computed(() =>
  filterMonitorSnapshotsByRange(
    monitorSnapshots.value,
    searchMonitorTimeRange.value,
  ),
);

const searchMonitorTrendItems = computed<SearchMonitorTrendItem[]>(() =>
  [...searchMonitorFilteredSnapshots.value]
    .sort((left, right) => {
      const leftTime = new Date(left.createTime ?? '').getTime();
      const rightTime = new Date(right.createTime ?? '').getTime();
      return (leftTime || 0) - (rightTime || 0);
    })
    .slice(-8)
    .map((snapshot, index) => {
      const value = Number(snapshot.metricValue ?? 0);
      return {
        alertFlag: snapshot.alertFlag,
        createTime: snapshot.createTime,
        key: `${snapshot.id ?? index}-${snapshot.metric ?? 'metric'}`,
        label: String(optionLabel(monitorMetricOptions, snapshot.metric)),
        percent: toBarPercent(value),
        value,
      };
    }),
);

const searchMonitorOperationCards = computed<DashboardMetric[]>(() => [
  {
    label: '搜索满意度',
    precision: 2,
    source: '运营看板',
    value: dashboardSummary.value?.searchSatisfaction ?? 0,
  },
  {
    label: '制造链查看率',
    precision: 2,
    source: '运营看板',
    suffix: '%',
    value: rateToPercent(dashboardSummary.value?.chainViewRate),
  },
  {
    label: '分类准确率',
    precision: 2,
    source: '运营看板',
    suffix: '%',
    value: rateToPercent(dashboardSummary.value?.classificationAccuracy),
  },
  {
    label: '平均响应耗时',
    precision: 0,
    source: '运营看板',
    suffix: 'ms',
    value: dashboardSummary.value?.averageResponseTimeMillis ?? 0,
  },
  {
    label: '高风险预警',
    source: '运营看板',
    suffix: '次',
    value: dashboardSummary.value?.highRiskWarningCount ?? 0,
  },
]);

const categoryToolbarActions = computed<ActionItem[]>(() => {
  const actions: ActionItem[] = [
    {
      label: '新增类目',
      type: 'primary',
      icon: ACTION_ICON.ADD,
      disabled: isCategorySorting.value,
      auth: ['wujin:category:create'],
      onClick: handleCreateCategory,
    },
    {
      label: isCategoryTreeExpanded.value ? '收起类目' : '展开类目',
      type: 'primary',
      disabled: isCategorySorting.value,
      onClick: toggleCategoryTreeExpand,
    },
    {
      label: '批量迁移',
      type: 'primary',
      disabled: isCategorySorting.value,
      auth: ['wujin:category:update'],
      onClick: handleBatchMigrateCategory,
    },
  ];

  if (isCategorySorting.value) {
    actions.push(
      {
        label: '保存排序',
        type: 'primary',
        auth: ['wujin:category:update'],
        onClick: handleCategorySortSubmit,
      },
      {
        label: '取消排序',
        onClick: handleCategorySortCancel,
      },
    );
  } else {
    actions.push({
      label: '拖拽排序',
      auth: ['wujin:category:update'],
      onClick: handleCategorySortStart,
    });
  }

  return actions;
});

function refreshCategoryGrid() {
  categoryGridApi.query();
}

function reloadCategoryTreeByLane() {
  isCategoryTreeExpanded.value = false;
  resetCategorySortState();
  refreshCategoryGrid();
}

function handleCategoryLaneViewChange() {
  reloadCategoryTreeByLane();
}

function toggleCategoryTreeExpand() {
  isCategoryTreeExpanded.value = !isCategoryTreeExpanded.value;
  categoryGridApi.grid.setAllTreeExpand(isCategoryTreeExpanded.value);
}

async function handleCategorySortStart() {
  if (categoryItems.value.length < 2) {
    message.warning('至少需要两个类目才能排序');
    return;
  }
  originalCategoryItems.value = cloneCategoryItems(categoryItems.value);
  isCategorySorting.value = true;
  isCategoryTreeExpanded.value = true;
  categoryGridApi.setGridOptions({
    data: categoryItems.value,
  });
  categoryGridApi.grid.setAllTreeExpand(true);
  await nextTick();

  if (categorySortableInstance.value) {
    categorySortableInstance.value.option('disabled', false);
    return;
  }

  categorySortableInstance.value = useSortable(
    categorySortableSelector,
    categoryItems.value,
    {
      animation: 150,
      disabled: false,
      draggable: '.vxe-body--row',
      handle: '.wujin-category-drag-handle',
      onEnd: ({ newDraggableIndex, oldDraggableIndex }) => {
        if (newDraggableIndex === oldDraggableIndex) {
          return;
        }
        const moved = categoryItems.value.splice(oldDraggableIndex ?? 0, 1)[0];
        if (!moved) {
          return;
        }
        categoryItems.value.splice(newDraggableIndex ?? 0, 0, moved);
        categoryGridApi.setGridOptions({
          data: categoryItems.value,
        });
      },
    },
  );
}

async function handleCategorySortSubmit() {
  if (!isCategorySorting.value) {
    return;
  }
  const hideLoading = message.loading({
    content: '正在保存类目排序',
    key: 'wujin_category_sort',
  });
  try {
    await Promise.all(
      categoryItems.value
        .map((item, index) => ({
          ...item,
          sort: index + 1,
        }))
        .filter((item) => item.id)
        .map((item) => updateCategory(item)),
    );
    message.success({
      content: '类目排序已保存',
      key: 'wujin_category_sort',
    });
    resetCategorySortState();
    refreshCategoryGrid();
  } finally {
    hideLoading();
  }
}

function handleCategorySortCancel() {
  categoryItems.value = cloneCategoryItems(originalCategoryItems.value);
  categoryGridApi.setGridOptions({
    data: categoryItems.value,
  });
  resetCategorySortState();
}

function handleCategorySelectionChange() {
  const selectedRows =
    categoryGridApi.grid.getCheckboxRecords?.() ??
    categoryGridApi.grid.getCheckboxReserveRecords?.() ??
    [];
  selectedCategoryIds.value = selectedRows
    .map((row: WujinPlatformApi.Category) => row.id)
    .filter((id: number | undefined): id is number => Boolean(id));
}

function handleBatchMigrateCategory() {
  handleCategorySelectionChange();
  if (selectedCategoryIds.value.length === 0) {
    message.warning('至少选择一个类目');
    return;
  }
  categoryBatchMigrateModalApi
    .setData({
      selectedCategoryIds: selectedCategoryIds.value,
    })
    .open();
}

function refreshMappingGrid() {
  mappingGridApi.query();
}

function refreshSearchRuleGrid() {
  searchRuleGridApi.query();
}

function refreshAuditGrid() {
  auditGridApi.query();
}

function refreshSourcingLeadGrid() {
  sourcingLeadGridApi.query();
}

function handleCreateCategory() {
  categoryFormModalApi
    .setData({
      displayDepth: 2,
      healthStatus: 'HEALTHY',
      lane: 'PRODUCT',
      level: 1,
      parentId: 0,
      sort: 0,
      status: 0,
    })
    .open();
}

function handleEditCategory(row: WujinPlatformApi.Category) {
  categoryFormModalApi.setData(row).open();
}

function handleAppendCategory(row: WujinPlatformApi.Category) {
  categoryFormModalApi
    .setData({
      displayDepth: row.displayDepth ?? 2,
      healthStatus: row.healthStatus ?? 'HEALTHY',
      lane: row.lane ?? 'PRODUCT',
      level: (row.level ?? 0) + 1,
      parentId: row.id,
      sort: 0,
      status: row.status ?? 0,
    })
    .open();
}

function handleCreateMapping() {
  mappingFormModalApi
    .setData({
      confidence: 80,
      mappingType: 'REQUIRES_MATERIAL',
      sourceLane: 'PRODUCT',
      status: 0,
      targetLane: 'MATERIAL',
    })
    .open();
}

function handleEditMapping(row: WujinPlatformApi.CategoryMapping) {
  mappingFormModalApi.setData(row).open();
}

function handleManageTemplateItems(row: WujinPlatformApi.IndustryTemplate) {
  templateItemManageModalApi.setData(row).open();
}

function refreshTemplateGrid() {
  templateGridApi.query();
}

function handleCreateTemplate() {
  templateFormModalApi
    .setData({
      productLane: 'PRODUCT',
      status: 0,
    })
    .open();
}

function handleEditTemplate(row: WujinPlatformApi.IndustryTemplate) {
  templateFormModalApi.setData(row).open();
}

function handleCreateSearchRule() {
  searchRuleFormModalApi
    .setData({
      lane: 'PRODUCT',
      ruleType: 'GRANULARITY_LIMIT',
      ruleValue: '2',
      status: 0,
      weight: 100,
    })
    .open();
}

function handleEditSearchRule(row: WujinPlatformApi.SearchRuleConfig) {
  searchRuleFormModalApi.setData(row).open();
}

function handleReviewAudit(row: WujinPlatformApi.RelationAuditRecord) {
  auditReviewFormModalApi
    .setData({
      action: 'APPROVE',
      auditorId: row.auditorId,
      comment: row.comment,
      submissionId: row.submissionId,
    })
    .open();
}

function handleDispatchSourcingLead(row: WujinPlatformApi.SourcingLead) {
  sourcingLeadDispatchFormModalApi
    .setData({
      dispatchRemark: row.dispatchRemark,
      leadId: row.id,
      merchantId: row.merchantId,
    })
    .open();
}

async function handleAutoDispatchSourcingLead(
  row: WujinPlatformApi.SourcingLead,
) {
  if (!row.id) {
    message.warning('线索ID不能为空');
    return;
  }
  await autoDispatchSourcingLead(row.id);
  message.success('已自动匹配分发');
  refreshSourcingLeadGrid();
}
</script>

<template>
  <Page auto-content-height>
    <CategoryFormModal @success="refreshCategoryGrid" />
    <CategoryBatchMigrateModal @success="refreshCategoryGrid" />
    <MappingFormModal @success="refreshMappingGrid" />
    <SearchRuleFormModal @success="refreshSearchRuleGrid" />
    <AuditReviewFormModal @success="refreshAuditGrid" />
    <TemplateFormModal @success="refreshTemplateGrid" />
    <TemplateItemManageModal />
    <SourcingLeadDispatchFormModal @success="refreshSourcingLeadGrid" />

    <template v-if="activeSection === 'dashboard'">
      <Spin :spinning="dashboardLoading">
        <Row :gutter="[12, 12]">
          <Col
            v-for="metric in dashboardMetrics"
            :key="metric.label"
            :lg="8"
            :sm="12"
            :xl="6"
            :xs="24"
          >
            <Card :bordered="false" class="wujin-dashboard-card">
              <Statistic
                :precision="metric.precision"
                :suffix="metric.suffix"
                :title="metric.label"
                :value="metric.value"
              />
            </Card>
          </Col>
        </Row>
        <Card
          :bordered="false"
          class="wujin-dashboard-alert-card"
          title="告警指标"
        >
          <div v-if="dashboardAlerts.length" class="wujin-dashboard-alerts">
            <Alert
              v-for="alert in dashboardAlerts"
              :key="`${alert.metric}-${alert.message}`"
              :description="alert.message"
              :message="alert.metric"
              show-icon
              type="warning"
            />
          </div>
          <div v-else class="wujin-dashboard-empty">暂无告警指标</div>
        </Card>
      </Spin>
    </template>
    <template v-else-if="activeSection === 'category'">
      <Card
        :bordered="false"
        class="wujin-category-health-panel"
        title="类目健康度总览"
      >
        <div class="wujin-category-health-cards">
          <div
            v-for="card in categoryHealthCards"
            :key="card.label"
            class="wujin-category-health-card"
          >
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
          </div>
        </div>
        <div class="wujin-category-lane-health">
          <div class="wujin-category-lane-health__title">按泳道健康分布</div>
          <div
            v-for="lane in categoryLaneHealthRows"
            :key="lane.lane"
            class="wujin-category-lane-health__row"
          >
            <div class="wujin-category-lane-health__label">
              <strong>{{ lane.label }}</strong>
              <span>{{ lane.total }} 个类目</span>
            </div>
            <div class="wujin-category-lane-health__cells">
              <div
                v-for="cell in lane.healthCells"
                :key="`${lane.lane}-${cell.label}`"
                class="wujin-category-lane-health__cell"
              >
                <span>{{ cell.label }} {{ cell.value }}</span>
                <div class="wujin-category-lane-health__bar">
                  <i :style="{ width: `${cell.percent}%` }"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Card>
      <div class="wujin-category-sort-area">
        <CategoryGrid :table-title="tableTitle">
          <template #toolbar-tools>
            <div class="wujin-category-toolbar">
              <div class="wujin-category-lane-view">
                <span class="wujin-category-lane-label">按泳道查看</span>
                <Radio.Group
                  v-model:value="categoryLaneView"
                  button-style="solid"
                  :disabled="isCategorySorting"
                  option-type="button"
                  :options="categoryLaneViewOptions"
                  size="small"
                  @change="handleCategoryLaneViewChange"
                />
              </div>
              <TableAction :actions="categoryToolbarActions" />
            </div>
          </template>
          <template #categoryDragHandle>
            <span
              :class="[
                'wujin-category-drag-handle icon-[ic--round-drag-indicator] text-lg',
                isCategorySorting
                  ? 'cursor-move text-gray-500'
                  : 'cursor-not-allowed text-gray-300',
              ]"
              :title="isCategorySorting ? '拖动排序' : '点击拖拽排序后可拖动'"
            ></span>
          </template>
          <template #categoryActions="{ row }">
            <TableAction
              :actions="[
                {
                  label: '新增下级',
                  type: 'link',
                  icon: ACTION_ICON.ADD,
                  disabled: isCategorySorting,
                  auth: ['wujin:category:create'],
                  onClick: handleAppendCategory.bind(null, row),
                },
                {
                  label: '编辑',
                  type: 'link',
                  icon: ACTION_ICON.EDIT,
                  disabled: isCategorySorting,
                  auth: ['wujin:category:update'],
                  onClick: handleEditCategory.bind(null, row),
                },
              ]"
            />
          </template>
        </CategoryGrid>
      </div>
    </template>
    <template v-else-if="activeSection === 'mapping'">
      <MappingGrid :table-title="tableTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增映射',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wujin:category-mapping:create'],
                onClick: handleCreateMapping,
              },
            ]"
          />
        </template>
        <template #mappingActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '编辑',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:category-mapping:update'],
                onClick: handleEditMapping.bind(null, row),
              },
            ]"
          />
        </template>
      </MappingGrid>
    </template>
    <template v-else-if="activeSection === 'template'">
      <TemplateGrid :table-title="tableTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增模板',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wujin:industry-template:create'],
                onClick: handleCreateTemplate,
              },
            ]"
          />
        </template>
        <template #templateActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '配置关系',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:industry-template-item:query'],
                onClick: handleManageTemplateItems.bind(null, row),
              },
              {
                label: '编辑',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:industry-template:update'],
                onClick: handleEditTemplate.bind(null, row),
              },
            ]"
          />
        </template>
      </TemplateGrid>
    </template>
    <template v-else-if="activeSection === 'audit'">
      <AuditGrid :table-title="tableTitle">
        <template #auditActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '审核',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:relation-audit-review:update'],
                onClick: handleReviewAudit.bind(null, row),
              },
            ]"
          />
        </template>
      </AuditGrid>
    </template>
    <template v-else-if="activeSection === 'searchRule'">
      <SearchRuleGrid :table-title="tableTitle">
        <template #toolbar-tools>
          <TableAction
            :actions="[
              {
                label: '新增规则',
                type: 'primary',
                icon: ACTION_ICON.ADD,
                auth: ['wujin:search-rule-config:create'],
                onClick: handleCreateSearchRule,
              },
            ]"
          />
        </template>
        <template #searchRuleActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '编辑',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:search-rule-config:update'],
                onClick: handleEditSearchRule.bind(null, row),
              },
            ]"
          />
        </template>
      </SearchRuleGrid>
    </template>
    <template v-else-if="activeSection === 'searchLog'">
      <Spin :spinning="dashboardLoading || monitorSnapshotLoading">
        <Card
          :bordered="false"
          class="wujin-search-monitor-panel"
          title="搜索监控趋势"
        >
          <template #extra>
            <Radio.Group
              v-model:value="searchMonitorTimeRange"
              button-style="solid"
              option-type="button"
              :options="searchMonitorTimeRangeOptions"
              size="small"
            />
          </template>
          <div class="wujin-search-monitor-hint">
            趋势数据来自监控快照，运营图表复用当前看板指标。
          </div>
          <div class="wujin-search-monitor-cards">
            <div
              v-for="card in searchMonitorOperationCards"
              :key="card.label"
              class="wujin-search-monitor-card"
            >
              <span>{{ card.label }}</span>
              <strong>
                {{ card.value
                }}<small v-if="card.suffix">{{ card.suffix }}</small>
              </strong>
              <em>{{ card.source }}</em>
            </div>
          </div>
          <div class="wujin-search-monitor-chart" aria-label="运营图表">
            <div class="wujin-search-monitor-chart__title">运营图表</div>
            <div
              v-if="searchMonitorTrendItems.length"
              class="wujin-search-monitor-trends"
            >
              <div
                v-for="item in searchMonitorTrendItems"
                :key="item.key"
                class="wujin-search-monitor-trend"
              >
                <div class="wujin-search-monitor-trend__meta">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
                <div class="wujin-search-monitor-trend__bar">
                  <i
                    :class="{ 'is-alert': item.alertFlag }"
                    :style="{ width: `${item.percent}%` }"
                  ></i>
                </div>
              </div>
            </div>
            <div v-else class="wujin-dashboard-empty">暂无趋势快照</div>
          </div>
        </Card>
      </Spin>
      <SearchLogGrid :table-title="tableTitle" />
    </template>
    <template v-else-if="activeSection === 'sourcingLead'">
      <SourcingLeadGrid :table-title="tableTitle">
        <template #sourcingLeadActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '分发',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:sourcing-lead:dispatch'],
                onClick: handleDispatchSourcingLead.bind(null, row),
              },
              {
                label: '自动分发',
                type: 'link',
                auth: ['wujin:sourcing-lead:dispatch'],
                disabled: row.dispatchStatus === 'DISPATCHED',
                onClick: handleAutoDispatchSourcingLead.bind(null, row),
              },
            ]"
          />
        </template>
      </SourcingLeadGrid>
    </template>
    <template v-else-if="activeSection === 'monitor'">
      <MonitorSnapshotGrid :table-title="tableTitle" />
    </template>
  </Page>
</template>

<style scoped>
.wujin-dashboard-card {
  min-height: 112px;
}

.wujin-dashboard-alert-card {
  margin-top: 12px;
}

.wujin-dashboard-alerts {
  display: grid;
  gap: 8px;
}

.wujin-dashboard-empty {
  color: hsl(var(--muted-foreground));
}

.wujin-category-health-panel,
.wujin-search-monitor-panel {
  margin-bottom: 12px;
}

.wujin-category-health-cards,
.wujin-search-monitor-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}

.wujin-category-health-card,
.wujin-search-monitor-card {
  min-width: 0;
  padding: 10px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--muted) / 28%);
}

.wujin-category-health-card span,
.wujin-search-monitor-card span {
  display: block;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-category-health-card strong,
.wujin-search-monitor-card strong {
  display: block;
  margin-top: 4px;
  font-size: 22px;
  line-height: 28px;
}

.wujin-search-monitor-card small {
  margin-left: 2px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-weight: 400;
}

.wujin-search-monitor-card em {
  display: block;
  margin-top: 4px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
  font-style: normal;
}

.wujin-category-lane-health,
.wujin-search-monitor-chart {
  margin-top: 12px;
}

.wujin-category-lane-health__title,
.wujin-search-monitor-chart__title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
}

.wujin-category-lane-health__row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 10px 0;
  border-top: 1px solid hsl(var(--border));
}

.wujin-category-lane-health__label span {
  display: block;
  margin-top: 2px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-category-lane-health__cells {
  display: grid;
  grid-template-columns: repeat(3, minmax(90px, 1fr));
  gap: 8px;
}

.wujin-category-lane-health__cell span,
.wujin-search-monitor-hint,
.wujin-search-monitor-trend__meta {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-category-lane-health__bar,
.wujin-search-monitor-trend__bar {
  height: 6px;
  margin-top: 5px;
  overflow: hidden;
  border-radius: 999px;
  background: hsl(var(--muted));
}

.wujin-category-lane-health__bar i,
.wujin-search-monitor-trend__bar i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: hsl(var(--primary));
}

.wujin-search-monitor-hint {
  margin-bottom: 10px;
}

.wujin-search-monitor-trends {
  display: grid;
  gap: 8px;
}

.wujin-search-monitor-trend__meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.wujin-search-monitor-trend__bar i.is-alert {
  background: #d97706;
}

.wujin-category-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.wujin-category-lane-view {
  display: flex;
  align-items: center;
  gap: 8px;
}

.wujin-category-lane-label {
  color: hsl(var(--muted-foreground));
  font-size: 13px;
}

@media (max-width: 768px) {
  .wujin-category-lane-health__row,
  .wujin-category-lane-health__cells {
    grid-template-columns: 1fr;
  }
}
</style>
