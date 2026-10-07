const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

test('wujin platform web runtime config uses the Wujin domain API', () => {
  const sitEnv = read('apps/web-antd/.env.sit');
  const prodEnv = read('apps/web-antd/.env.production');
  const devEnv = read('apps/web-antd/.env.development');
  const viteConfig = read('apps/web-antd/vite.config.mts');

  [sitEnv, prodEnv, devEnv, viteConfig].forEach((source) => {
    assert.doesNotMatch(source, /39\.107\.248\.167/);
    assert.doesNotMatch(source, /forum\.gbastu\.com/);
    assert.doesNotMatch(source, /localhost:16610/);
    assert.doesNotMatch(source, /http:\/\/wj\.halcyonz\.com/);
  });

  assert.match(sitEnv, /^VITE_BASE=\/platform\//m);
  assert.match(sitEnv, /^VITE_BASE_URL=https:\/\/wj\.halcyonz\.com$/m);
  assert.match(
    sitEnv,
    /^VITE_GLOB_API_URL=https:\/\/wj\.halcyonz\.com\/admin-api$/m,
  );
  assert.match(prodEnv, /^VITE_BASE=\/platform\//m);
  assert.match(prodEnv, /^VITE_BASE_URL=https:\/\/wj\.halcyonz\.com$/m);
  assert.match(
    prodEnv,
    /^VITE_GLOB_API_URL=https:\/\/wj\.halcyonz\.com\/admin-api$/m,
  );
  assert.match(devEnv, /^VITE_BASE_URL=https:\/\/wj\.halcyonz\.com$/m);
  assert.match(
    viteConfig,
    /target:\s*'https:\/\/wj\.halcyonz\.com\/admin-api'/,
  );
});

test('wujin platform API client wraps completed admin endpoints', () => {
  const api = read('apps/web-antd/src/api/wujin/platform.ts');

  [
    '/wujin/category/list',
    '/wujin/category/create',
    '/wujin/category/batch-migrate',
    '/wujin/category-mapping/list',
    '/wujin/chain-entity/list',
    '/wujin/industry-template/list',
    '/wujin/industry-template/create',
    '/wujin/industry-template/update',
    '/wujin/industry-template-item/list',
    '/wujin/industry-template-item/create',
    '/wujin/industry-template-item/update',
    '/wujin/industry-template-item/batch-migrate',
    '/wujin/industry-template-item/delete',
    '/wujin/industry-template-item/get',
    '/wujin/relation-audit-record/list',
    '/wujin/relation-audit-review/review',
    '/wujin/search-rule-config/list',
    '/wujin/search-rule-config/create',
    '/wujin/search-rule-config/update',
    '/wujin/search-behavior-log/list',
    '/wujin/monitor-snapshot/list',
    '/wujin/monitor-dashboard/summary',
    '/wujin/sourcing-lead/list',
    '/wujin/sourcing-lead/dispatch',
    '/wujin/sourcing-lead/auto-dispatch',
  ].forEach((endpoint) => assert.match(api, new RegExp(endpoint)));

  assert.match(api, /export namespace WujinPlatformApi/);
  assert.match(api, /export function getCategoryList/);
  assert.match(api, /export interface CategoryBatchMigrateRequest/);
  assert.match(api, /export function batchMigrateCategory/);
  assert.match(api, /export interface ChainEntity/);
  assert.match(api, /export function getChainEntityList/);
  assert.match(api, /export function createIndustryTemplate/);
  assert.match(api, /export function updateIndustryTemplate/);
  assert.match(api, /export function reviewRelationSubmission/);
  assert.match(api, /export function getIndustryTemplateItemList/);
  assert.match(api, /export function createIndustryTemplateItem/);
  assert.match(api, /export function updateIndustryTemplateItem/);
  assert.match(api, /export interface IndustryTemplateItemBatchMigrateRequest/);
  assert.match(api, /export function batchMigrateIndustryTemplateItem/);
  assert.match(api, /export function deleteIndustryTemplateItem/);
  assert.match(api, /export function getIndustryTemplateItem/);
  assert.match(api, /export function createSearchRuleConfig/);
  assert.match(api, /export function updateSearchRuleConfig/);
  assert.match(api, /export function getMonitorSnapshotList/);
  assert.match(api, /export function getMonitorDashboardSummary/);
  assert.match(api, /export interface SourcingLead/);
  assert.match(api, /export function getSourcingLeadList/);
  assert.match(api, /export function dispatchSourcingLead/);
  assert.match(api, /export function autoDispatchSourcingLead/);
});

test('wujin platform route exposes an operations backend menu', () => {
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');

  assert.match(route, /name:\s*'WujinPlatform'/);
  assert.match(route, /title:\s*'五金运营后台'/);
  assert.match(route, /path:\s*'\/wujin\/platform'/);
  assert.match(
    route,
    /component:\s*\(\) => import\('#\/views\/wujin\/platform\/index\.vue'\)/,
  );
});

test('wujin platform page follows Vben admin table style and covers core operations', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');

  assert.match(
    page,
    /defineOptions\(\{\s*name:\s*'WujinPlatformManage'\s*\}\)/,
  );
  assert.match(page, /useVbenVxeGrid/);
  assert.match(page, /useVbenModal/);
  assert.match(page, /TableAction/);
  assert.match(page, /平台类目/);
  assert.match(page, /跨泳道映射/);
  assert.match(page, /行业模板/);
  assert.match(page, /关系审核/);
  assert.match(page, /运营看板/);
  assert.match(page, /搜索监控/);
  assert.match(page, /寻源线索/);
  assert.match(page, /getMonitorDashboardSummary/);
  assert.match(page, /getSourcingLeadList/);
  assert.match(page, /handleDispatchSourcingLead/);
  assert.match(page, /autoDispatchSourcingLead/);
  assert.match(page, /handleAutoDispatchSourcingLead/);
  assert.match(page, /自动分发/);
  assert.match(page, /搜索样本/);
  assert.match(page, /制造链查看率/);
  assert.match(page, /分类准确率/);
  assert.match(page, /审核通过率/);
  assert.match(page, /高风险预警/);
  assert.match(page, /告警指标/);
  assert.match(page, /CategoryFormModal/);
  assert.match(page, /CategoryBatchMigrateModal/);
  assert.match(page, /MappingFormModal/);
  assert.match(page, /TemplateFormModal/);
  assert.match(page, /TemplateItemManageModal/);
  assert.match(page, /SearchRuleFormModal/);
  assert.match(page, /AuditReviewFormModal/);
  assert.match(page, /handleCreateCategory/);
  assert.match(page, /handleBatchMigrateCategory/);
  assert.match(page, /selectedCategoryIds/);
  assert.match(page, /categoryBatchMigrateModalApi/);
  assert.match(page, /handleEditCategory/);
  assert.match(page, /handleCreateMapping/);
  assert.match(page, /handleEditMapping/);
  assert.match(page, /handleCreateTemplate/);
  assert.match(page, /handleEditTemplate/);
  assert.match(page, /handleManageTemplateItems/);
  assert.match(page, /新增模板/);
  assert.match(page, /配置关系/);
  assert.match(page, /handleCreateSearchRule/);
  assert.match(page, /handleEditSearchRule/);
  assert.match(page, /handleReviewAudit/);
  assert.match(data, /laneOptions/);
  assert.match(data, /PRODUCT/);
  assert.match(data, /PROCESS/);
  assert.match(data, /MATERIAL/);
  assert.match(data, /healthStatusOptions/);
  assert.match(data, /monitorMetricOptions/);
  assert.match(data, /sourcingLeadStatusOptions/);
  assert.match(data, /sourcingDispatchStatusOptions/);
  assert.match(data, /useSourcingLeadColumns/);
  assert.match(data, /sourcingLeadActions/);
  assert.match(data, /useCategoryEditFormSchema/);
  assert.match(data, /categoryDragHandle/);
  assert.match(data, /useMappingEditFormSchema/);
  assert.match(data, /useTemplateItemColumns/);
  assert.match(data, /useTemplateItemFormSchema/);
  assert.match(data, /useTemplateItemEditFormSchema/);
  assert.match(data, /templateItemDragHandle/);
  assert.match(data, /getChainEntityList/);
  assert.match(data, /component:\s*'ApiSelect'/);
  assert.match(
    data,
    /fieldNames:\s*\{\s*label:\s*'displayName',\s*value:\s*'id'\s*\}/,
  );
  assert.match(data, /showSearch:\s*true/);
  assert.match(data, /optionFilterProp:\s*'label'/);
  assert.match(data, /useSearchRuleEditFormSchema/);
  assert.match(data, /useAuditReviewFormSchema/);
});

test('wujin platform industry template items can be configured from template rows', () => {
  const manageModal = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-item-manage-modal.vue',
  );
  const designerPanel = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-item-designer-panel.vue',
  );
  const itemForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-item-form.vue',
  );

  assert.match(
    manageModal,
    /defineOptions\(\{\s*name:\s*'WujinPlatformTemplateItemManage'\s*\}\)/,
  );
  assert.match(manageModal, /useVbenVxeGrid/);
  assert.match(manageModal, /TemplateItemFormModal/);
  assert.match(manageModal, /TemplateItemBatchMigrateModal/);
  assert.match(manageModal, /TemplateItemDesignerPanel/);
  assert.match(manageModal, /getIndustryTemplateItemList/);
  assert.match(manageModal, /deleteIndustryTemplateItem/);
  assert.match(manageModal, /updateIndustryTemplateItem/);
  assert.match(manageModal, /batch-migrate-form\.vue/);
  assert.match(manageModal, /useSortable/);
  assert.match(manageModal, /templateItems/);
  assert.match(manageModal, /selectedTemplateItemIds/);
  assert.match(manageModal, /templateItems\.value = list/);
  assert.doesNotMatch(manageModal, /listAsGridResult/);
  assert.match(manageModal, /return list;/);
  assert.match(manageModal, /templateId:\s*currentTemplate\.value\?\.id/);
  assert.match(manageModal, /isTemplateItemSorting/);
  assert.match(manageModal, /sortableInstance/);
  assert.match(manageModal, /handleCreateTemplateItem/);
  assert.match(manageModal, /handleEditTemplateItem/);
  assert.match(manageModal, /handleDeleteTemplateItem/);
  assert.match(manageModal, /handleTemplateItemSelectionChange/);
  assert.match(manageModal, /handleBatchMigrateTemplateItem/);
  assert.match(manageModal, /handleTemplateItemSortStart/);
  assert.match(manageModal, /handleTemplateItemSortSubmit/);
  assert.match(manageModal, /handleTemplateItemSortCancel/);
  assert.match(manageModal, /新增关系项/);
  assert.match(manageModal, /批量迁移/);
  assert.match(manageModal, /至少选择一个关系项/);
  assert.match(manageModal, /拖拽排序/);
  assert.match(manageModal, /保存排序/);
  assert.match(manageModal, /取消排序/);
  assert.match(manageModal, /编辑/);
  assert.match(manageModal, /删除/);
  assert.match(manageModal, /popConfirm/);
  assert.match(manageModal, /wujin:industry-template-item:delete/);
  assert.match(manageModal, /wujin:industry-template-item:update/);
  assert.match(manageModal, /wujin-template-item-drag-handle/);
  assert.match(manageModal, /sort:\s*index\s*\+\s*1/);
  assert.match(manageModal, /refreshTemplateItemGrid/);
  assert.match(manageModal, /useTemplateItemColumns/);
  assert.match(manageModal, /useTemplateItemFormSchema/);
  assert.match(manageModal, /templateItemDragHandle/);
  assert.match(manageModal, /<TemplateItemDesignerPanel/);
  assert.match(manageModal, /:entities="chainEntities"/);
  assert.match(manageModal, /templateItemEntity/);
  assert.match(manageModal, /关系项明细/);
  assert.match(manageModal, /搜索范围为当前模板/);
  assert.match(
    manageModal,
    /selectedTemplateItemIds:\s*selectedTemplateItemIds\.value/,
  );

  assert.match(
    designerPanel,
    /defineOptions\(\{\s*name:\s*'WujinPlatformTemplateItemDesignerPanel'\s*\}\)/,
  );
  assert.match(designerPanel, /templateDesignerGroups/);
  assert.match(designerPanel, /templateDesignerStats/);
  assert.match(designerPanel, /requiredTemplateItems/);
  assert.match(designerPanel, /optionalTemplateItems/);
  assert.match(designerPanel, /weightTotal/);
  assert.match(designerPanel, /optionLabel\(relationTypeOptions/);
  assert.match(designerPanel, /模板关系概览/);
  assert.match(designerPanel, /必需项/);
  assert.match(designerPanel, /可选项/);
  assert.match(designerPanel, /匹配权重合计/);
  assert.match(designerPanel, /chainEntityName/);
  assert.match(designerPanel, /展示顺序/);
  assert.doesNotMatch(designerPanel, /<Progress/);
  assert.match(designerPanel, /Tag/);
  assert.match(designerPanel, /Empty/);

  assert.match(
    itemForm,
    /defineOptions\(\{\s*name:\s*'WujinPlatformTemplateItemForm'\s*\}\)/,
  );
  assert.match(itemForm, /useTemplateItemEditFormSchema/);
  assert.match(itemForm, /createIndustryTemplateItem/);
  assert.match(itemForm, /updateIndustryTemplateItem/);
  assert.match(itemForm, /templateId:\s*currentTemplateId\.value/);
  assert.match(itemForm, /relationType:\s*'REQUIRES_MATERIAL'/);
  assert.match(itemForm, /requiredFlag:\s*true/);
  assert.match(itemForm, /weight:\s*100/);
});

test('wujin platform page wires modal forms to create and update config records', () => {
  const categoryForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/category-form.vue',
  );
  const categoryBatchMigrateForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/category-batch-migrate-form.vue',
  );
  const templateItemBatchMigrateForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-item-batch-migrate-form.vue',
  );
  const mappingForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/mapping-form.vue',
  );
  const templateForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/template-form.vue',
  );
  const searchRuleForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/search-rule-form.vue',
  );
  const auditReviewForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/audit-review-form.vue',
  );
  const sourcingLeadDispatchForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/sourcing-lead-dispatch-form.vue',
  );

  assert.match(categoryForm, /useCategoryEditFormSchema/);
  assert.match(categoryForm, /createCategory/);
  assert.match(categoryForm, /updateCategory/);
  assert.match(
    categoryBatchMigrateForm,
    /defineOptions\(\{\s*name:\s*'WujinPlatformCategoryBatchMigrateForm'\s*\}\)/,
  );
  assert.match(categoryBatchMigrateForm, /useCategoryBatchMigrateFormSchema/);
  assert.match(categoryBatchMigrateForm, /batchMigrateCategory/);
  assert.match(categoryBatchMigrateForm, /selectedCategoryIds/);
  assert.match(categoryBatchMigrateForm, /ids:\s*selectedCategoryIds\.value/);
  assert.match(
    templateItemBatchMigrateForm,
    /defineOptions\(\{\s*name:\s*'WujinPlatformTemplateItemBatchMigrateForm'\s*\}\)/,
  );
  assert.match(
    templateItemBatchMigrateForm,
    /useTemplateItemBatchMigrateFormSchema/,
  );
  assert.match(
    templateItemBatchMigrateForm,
    /batchMigrateIndustryTemplateItem/,
  );
  assert.match(templateItemBatchMigrateForm, /selectedTemplateItemIds/);
  assert.match(
    templateItemBatchMigrateForm,
    /ids:\s*selectedTemplateItemIds\.value/,
  );
  assert.match(mappingForm, /useMappingEditFormSchema/);
  assert.match(mappingForm, /createCategoryMapping/);
  assert.match(mappingForm, /updateCategoryMapping/);
  assert.match(templateForm, /useTemplateEditFormSchema/);
  assert.match(templateForm, /createIndustryTemplate/);
  assert.match(templateForm, /updateIndustryTemplate/);
  assert.match(searchRuleForm, /useSearchRuleEditFormSchema/);
  assert.match(searchRuleForm, /createSearchRuleConfig/);
  assert.match(searchRuleForm, /updateSearchRuleConfig/);
  assert.match(auditReviewForm, /useAuditReviewFormSchema/);
  assert.match(auditReviewForm, /reviewRelationSubmission/);
  assert.match(sourcingLeadDispatchForm, /useSourcingLeadDispatchFormSchema/);
  assert.match(sourcingLeadDispatchForm, /dispatchSourcingLead/);
});

test('wujin platform lets operators configure category search display depth safely', () => {
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');
  const categoryForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/category-form.vue',
  );
  const categoryBatchMigrateForm = read(
    'apps/web-antd/src/views/wujin/platform/modules/category-batch-migrate-form.vue',
  );

  assert.match(data, /label:\s*'搜索展示到层级'/);
  assert.match(data, /由平台运营设置。搜索命中该分类时，最多展示到此层级/);
  assert.match(data, /仅有一个下级时不会自动展开/);
  assert.match(data, /title:\s*'搜索展示层级'/);
  assert.match(categoryForm, /values\.displayDepth\s*<\s*values\.level/);
  assert.match(categoryForm, /搜索展示到层级不能小于当前分类层级/);
  assert.match(
    categoryBatchMigrateForm,
    /values\.displayDepth\s*<\s*values\.targetLevel/,
  );
  assert.match(
    categoryBatchMigrateForm,
    /搜索展示到层级不能小于目标分类层级/,
  );
});

test('wujin platform search rule form uses business labels instead of raw rule editing', () => {
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');
  const form = read(
    'apps/web-antd/src/views/wujin/platform/modules/search-rule-form.vue',
  );
  const editSchema = data.slice(
    data.indexOf('export function useSearchRuleEditFormSchema'),
    data.indexOf('export function useSearchRuleColumns'),
  );

  assert.match(data, /searchRuleTypeOptions/);
  assert.match(data, /调整展示层级/);
  assert.match(data, /命中关键词后切换频道/);
  assert.match(data, /命中条件后提示风险/);
  assert.match(editSchema, /component:\s*'Select'/);
  assert.match(editSchema, /options:\s*searchRuleTypeOptions/);
  assert.match(editSchema, /展示层级填 1、2 或 3/);
  assert.match(editSchema, /例如 lane=MATERIAL;when=keyword contains 橡胶/);
  assert.match(editSchema, /例如 text=跨行业不可互换/);
  assert.match(data, /searchRuleTypeLabel/);
  assert.match(
    data,
    /formatter:\s*\(\{\s*row\s*\}\)\s*=>\s*searchRuleTypeLabel\(row\.ruleType\)/,
  );
  assert.match(form, /ruleValue:\s*'2'/);
  assert.match(form, /weight:\s*100/);
});

test('wujin platform category grid supports tree maintenance actions', () => {
  const page = read('apps/web-antd/src/views/wujin/platform/index.vue');
  const data = read('apps/web-antd/src/views/wujin/platform/data.ts');

  assert.match(page, /isCategoryTreeExpanded/);
  assert.match(page, /isCategorySorting/);
  assert.match(page, /categoryItems/);
  assert.match(page, /selectedCategoryIds/);
  assert.match(page, /categorySortableInstance/);
  assert.match(page, /useSortable/);
  assert.match(page, /updateCategory/);
  assert.match(page, /toggleCategoryTreeExpand/);
  assert.match(page, /setAllTreeExpand/);
  assert.match(page, /treeConfig/);
  assert.match(page, /parentField:\s*'parentId'/);
  assert.match(page, /rowField:\s*'id'/);
  assert.match(page, /transform:\s*true/);
  assert.match(page, /handleAppendCategory/);
  assert.match(page, /handleCategorySortStart/);
  assert.match(page, /handleCategorySortSubmit/);
  assert.match(page, /handleCategorySortCancel/);
  assert.match(page, /handleCategorySelectionChange/);
  assert.match(page, /handleBatchMigrateCategory/);
  assert.match(page, /parentId:\s*row\.id/);
  assert.match(page, /level:\s*\(row\.level\s*\?\?\s*0\)\s*\+\s*1/);
  assert.match(page, /新增下级/);
  assert.match(page, /展开类目/);
  assert.match(page, /收起类目/);
  assert.match(page, /拖拽排序/);
  assert.match(page, /保存排序/);
  assert.match(page, /取消排序/);
  assert.match(page, /批量迁移/);
  assert.match(page, /至少选择一个类目/);
  assert.match(page, /wujin:category:update/);
  assert.match(page, /wujin-category-drag-handle/);
  assert.match(page, /sort:\s*index\s*\+\s*1/);
  assert.match(data, /type:\s*'checkbox'/);
  assert.match(data, /treeNode:\s*true/);
  assert.match(data, /categoryDragHandle/);
  assert.match(data, /useCategoryBatchMigrateFormSchema/);
  assert.match(data, /targetParentId/);
  assert.match(data, /targetLane/);
  assert.match(data, /targetLevel/);
});
