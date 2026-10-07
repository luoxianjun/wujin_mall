const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const test = require('node:test');

const webRoot = path.resolve(__dirname, '..');

function read(relativePath) {
  return fs.readFileSync(path.resolve(webRoot, relativePath), 'utf8');
}

test('wujin merchant web runtime config uses the Wujin domain API', () => {
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

  assert.match(sitEnv, /^VITE_BASE=\/merchant\//m);
  assert.match(sitEnv, /^VITE_BASE_URL=https:\/\/wj\.halcyonz\.com$/m);
  assert.match(
    sitEnv,
    /^VITE_GLOB_API_URL=https:\/\/wj\.halcyonz\.com\/admin-api$/m,
  );
  assert.match(prodEnv, /^VITE_BASE=\/merchant\//m);
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

test('wujin merchant API client wraps relation submission endpoints', () => {
  const api = read('apps/web-antd/src/api/wujin/merchant.ts');

  [
    '/wujin/merchant-relation-submission/list',
    '/wujin/merchant-relation-submission/create',
    '/wujin/merchant-relation-submission/update',
    '/wujin/merchant-relation-submit/submit',
    '/wujin/merchant-relation-item/list',
    '/wujin/merchant-relation-item/create',
    '/wujin/chain-entity/list',
    '/wujin/industry-template/list',
    '/wujin/merchant-sourcing-lead/list',
    '/wujin/merchant-sourcing-lead/handle',
    '/wujin/merchant-supply-capability/list',
    '/wujin/merchant-supply-capability/create',
    '/wujin/merchant-supply-capability/update',
  ].forEach((endpoint) => assert.match(api, new RegExp(endpoint)));

  assert.match(api, /export namespace WujinMerchantApi/);
  assert.match(api, /export interface ChainEntity/);
  assert.match(api, /export function getRelationSubmissionList/);
  assert.match(api, /export function getChainEntityList/);
  assert.match(api, /export function submitRelation/);
  assert.match(api, /export function getRelationItemList/);
  assert.match(api, /export interface SourcingLead/);
  assert.match(api, /export function getSourcingLeadList/);
  assert.match(api, /export function handleSourcingLead/);
  assert.match(api, /export interface SupplyCapability/);
  assert.match(api, /export function getSupplyCapabilityList/);
  assert.match(api, /export function createSupplyCapability/);
  assert.match(api, /export function updateSupplyCapability/);
});

test('wujin merchant route exposes a merchant backend menu', () => {
  const route = read('apps/web-antd/src/router/routes/modules/wujin.ts');

  assert.match(route, /name:\s*'WujinMerchant'/);
  assert.match(route, /title:\s*'五金商家后台'/);
  assert.match(route, /path:\s*'\/merchant\/wujin'/);
  assert.match(route, /hideInMenu:\s*true/);
  assert.match(
    route,
    /component:\s*\(\) => import\('#\/views\/wujin\/merchant\/index\.vue'\)/,
  );
});

test('mall quick entry routes are registered for merchant dashboard paths', () => {
  const route = read('apps/web-antd/src/router/routes/modules/mall.ts');

  [
    ['ProductSpu', '/mall/product/spu', '#/views/mall/product/spu/index.vue'],
    ['TradeOrder', '/mall/trade/order', '#/views/mall/trade/order/index.vue'],
    [
      'TradeAfterSale',
      '/mall/trade/after-sale',
      '#/views/mall/trade/afterSale/index.vue',
    ],
    [
      'PromotionCoupon',
      '/mall/promotion/coupon',
      '#/views/mall/promotion/coupon/index.vue',
    ],
    [
      'ProductComment',
      '/mall/product/comment',
      '#/views/mall/product/comment/index.vue',
    ],
  ].forEach(([name, path, component]) => {
    assert.match(route, new RegExp(`name:\\s*'${name}'`));
    assert.match(route, new RegExp(`path:\\s*'${path}'`));
    assert.match(
      route,
      new RegExp(
        `component:\\s*\\(\\) => import\\('${component.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}'\\)`,
      ),
    );
  });
});

test('wujin merchant page avoids querying grids before vxe mount', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.match(
    page,
    /onMounted\(\(\) => \{[\s\S]*loadBusinessSourceData\(\);[\s\S]*\}\);/,
  );
  assert.doesNotMatch(page, /onMounted\(\(\) => \{[\s\S]*GridApi\.query\(\);/);
});

test('wujin merchant grids auto load after vxe mount', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.match(page, /proxyConfig:\s*\{[\s\S]*autoLoad:\s*true/);
});

test('wujin merchant grids use stable non-paged vxe loading', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.doesNotMatch(page, /height:\s*'auto'/);
  assert.doesNotMatch(page, /height:\s*520/);
  assert.match(page, /height:\s*680/);
  assert.doesNotMatch(page, /listAsGridResult/);
  assert.match(page, /return list \?\? \[\];/);
});

test('wujin merchant dashboard degrades per business source request', () => {
  const api = read('apps/web-antd/src/api/wujin/merchant.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const request = read('apps/web-antd/src/api/request.ts');
  const requestTypes = read(
    'packages/effects/request/src/request-client/types.ts',
  );

  assert.match(api, /type ListRequestOptions/);
  assert.match(api, /silentErrorMessage\?: boolean/);
  assert.match(request, /error\?\.config\?\.silentErrorMessage/);
  assert.match(requestTypes, /silentErrorMessage\?: boolean/);

  assert.match(page, /Promise\.allSettled/);
  assert.match(
    page,
    /getRelationSubmissionList\(undefined, \{\s*silentErrorMessage:\s*true/,
  );
  assert.match(
    page,
    /getSourcingLeadList\(undefined, \{\s*silentErrorMessage:\s*true/,
  );
  assert.match(
    page,
    /getSupplyCapabilityList\(undefined, \{\s*silentErrorMessage:\s*true/,
  );
  assert.doesNotMatch(
    page,
    /message\.warning\('经营数据本地聚合暂不可用，请稍后刷新。'\)/,
  );
  assert.match(page, /部分经营数据本地聚合暂不可用，请稍后刷新。/);
});

test('wujin merchant page follows Vben style and focuses on relation declaration', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const relationSubmitSchema = data.slice(
    data.indexOf('export function useRelationSubmitFormSchema'),
    data.indexOf('export function useProductPublishBaseSchema'),
  );

  assert.match(
    page,
    /defineOptions\(\{\s*name:\s*'WujinMerchantManage'\s*\}\)/,
  );
  assert.match(page, /useVbenVxeGrid/);
  assert.match(page, /useVbenModal/);
  assert.match(page, /TableAction/);
  assert.match(page, /关系申报/);
  assert.match(page, /申报明细/);
  assert.match(page, /行业模板/);
  assert.match(page, /寻源线索/);
  assert.match(page, /供应能力/);
  assert.match(page, /RelationSubmitFormModal/);
  assert.doesNotMatch(page, /SupplyCapabilityFormModal/);
  assert.match(page, /getSourcingLeadList/);
  assert.match(page, /getSupplyCapabilityList/);
  assert.match(page, /handleContactSourcingLead/);
  assert.doesNotMatch(page, /handleCreateSupplyCapability/);
  assert.doesNotMatch(page, /handleEditSupplyCapability/);
  assert.match(page, /handleCreateRelationSubmit/);
  assert.match(page, /refreshSubmissionGrid/);
  assert.match(page, /新增申报/);
  assert.doesNotMatch(page, /新增供应能力/);
  assert.doesNotMatch(page, /Tabs\.TabPane key="supplyCapability"/);
  assert.match(data, /auditStatusOptions/);
  assert.match(data, /supplyStatusOptions/);
  assert.match(data, /sourcingLeadStatusOptions/);
  assert.match(data, /sourcingDispatchStatusOptions/);
  assert.match(data, /useSupplyCapabilityColumns/);
  assert.match(data, /useSupplyCapabilityFormSchema/);
  assert.match(data, /useSourcingLeadColumns/);
  assert.match(data, /sourcingLeadActions/);
  assert.match(data, /supplyCapabilityActions/);
  assert.match(data, /completenessScore/);
  assert.match(data, /REQUIRES_MATERIAL/);
  assert.match(data, /REQUIRES_PROCESS/);
  assert.match(data, /useRelationSubmitFormSchema/);
  assert.match(data, /getChainEntityList/);
  assert.match(data, /component:\s*'ApiSelect'/);
  assert.match(
    data,
    /fieldNames:\s*\{\s*label:\s*'name',\s*value:\s*'id'\s*\}/,
  );
  assert.match(data, /selectedEntityId/);
  assert.match(data, /selectedRelationType/);
  assert.match(data, /selectedRequiredFlag/);
  assert.match(data, /selectedRelationRemark/);
  assert.doesNotMatch(
    relationSubmitSchema,
    /fieldName:\s*'customRelationsJson'/,
  );
  assert.doesNotMatch(data, /DEPENDS_ON_PROCESS/);
});

test('wujin merchant dashboard restores the real product list as the primary tab', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.match(data, /useProductListFormSchema/);
  assert.match(data, /useProductListColumns/);
  assert.match(data, /fieldName:\s*'categoryId'/);
  assert.match(data, /fieldName:\s*'tabType'/);
  assert.match(data, /title:\s*'商品名称'/);
  assert.match(data, /title:\s*'商品图片'/);
  assert.match(data, /title:\s*'销售价'/);
  assert.match(data, /title:\s*'库存'/);
  assert.match(data, /title:\s*'销售状态'/);
  assert.match(data, /formatter:\s*'formatDateTime'/);

  assert.match(page, /const activeTab = ref\('product'\)/);
  assert.match(page, /getSpuPage/);
  assert.match(page, /ProductGrid/);
  assert.match(page, /Tabs\.TabPane key="product" tab="商品列表"/);
  assert.match(page, /handleProductPublishSuccess/);
  assert.match(page, /productGridApi\.query\(\)/);
  assert.match(page, /label:\s*'发布商品'/);
});

test('wujin merchant product publish owns supply capability settings', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );

  assert.match(data, /useProductPublishSupplySchema/);
  assert.match(data, /fieldName:\s*'supplyEntityId'/);
  assert.match(data, /选择这个商品实际出售的内容/);
  assert.match(data, /fieldName:\s*'productStock'/);
  assert.match(data, /fieldName:\s*'supplyMinOrderQuantity'/);
  assert.match(data, /fieldName:\s*'supplyDeliveryDays'/);
  assert.match(data, /fieldName:\s*'supplyServiceArea'/);
  assert.match(data, /fieldName:\s*'supplyRemark'/);
  assert.match(data, /无需另行设置/);

  assert.match(wizard, /SupplyForm/);
  assert.match(wizard, /supplyFormApi/);
  assert.match(wizard, /supplyEntityId:\s*values\.supplyEntityId/);
  assert.match(
    wizard,
    /supplyMinOrderQuantity:\s*values\.supplyMinOrderQuantity/,
  );
  assert.match(wizard, /supplyDeliveryDays:\s*values\.supplyDeliveryDays/);
  assert.match(wizard, /supplyServiceArea:\s*values\.supplyServiceArea/);
  assert.match(wizard, /supplyRemark:\s*values\.supplyRemark/);
  assert.match(wizard, /<Descriptions\.Item label="认证数量" :span="2">/);

  assert.doesNotMatch(page, /SupplyCapabilityFormModal/);
  assert.doesNotMatch(page, /Tabs\.TabPane key="supplyCapability"/);
  assert.doesNotMatch(page, /新增供应能力/);
});

test('wujin merchant relation submit modal posts orchestration request', () => {
  const form = read(
    'apps/web-antd/src/views/wujin/merchant/modules/relation-submit-form.vue',
  );

  assert.match(
    form,
    /defineOptions\(\{\s*name:\s*'WujinMerchantRelationSubmitForm'\s*\}\)/,
  );
  assert.match(form, /useRelationSubmitFormSchema/);
  assert.match(form, /submitRelation/);
  assert.match(form, /parseCustomRelations/);
  assert.match(form, /buildSelectedRelation/);
  assert.match(form, /buildCustomRelations/);
  assert.match(form, /customRelations:\s*buildCustomRelations\(values\)/);
  assert.match(form, /certificationCount:\s*0/);
  assert.match(form, /hasApplicationDescription:\s*true/);
  assert.match(form, /emit\('success'\)/);
});

test('wujin merchant sourcing lead handle modal posts handle request', () => {
  const form = read(
    'apps/web-antd/src/views/wujin/merchant/modules/sourcing-lead-handle-form.vue',
  );

  assert.match(
    form,
    /defineOptions\(\{\s*name:\s*'WujinMerchantSourcingLeadHandleForm'\s*\}\)/,
  );
  assert.match(form, /useSourcingLeadHandleFormSchema/);
  assert.match(form, /handleSourcingLead/);
  assert.match(form, /handleAction:\s*'CONTACTED'/);
  assert.match(form, /emit\('success'\)/);
});

test('wujin merchant relation submit modal displays orchestration result', () => {
  const form = read(
    'apps/web-antd/src/views/wujin/merchant/modules/relation-submit-form.vue',
  );

  assert.match(form, /submitResult/);
  assert.match(form, /const result = await submitRelation/);
  assert.match(form, /submitResult\.value = result/);
  assert.match(form, /resultAlertType/);
  assert.match(form, /resultAuditStatusLabel/);
  assert.match(form, /resultAuditRouteLabel/);
  assert.match(form, /Descriptions/);
  assert.match(form, /Progress/);
  assert.match(form, /提交结果/);
  assert.match(form, /审核状态/);
  assert.match(form, /审核路线/);
  assert.match(form, /完善度/);
  assert.match(form, /优化建议/);
  assert.match(form, /copiedTemplateItemCount/);
  assert.match(form, /completenessSuggestion/);
  assert.doesNotMatch(form, /await modalApi\.close\(\)/);
});

test('wujin merchant page exposes product publish wizard entry', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.match(page, /ProductPublishWizardModal/);
  assert.match(page, /productPublishWizardModalApi/);
  assert.match(page, /handleCreateProductPublish/);
  assert.match(page, /发布商品/);
  assert.match(page, /auth:\s*\['wujin:merchant-product:create'\]/);
  assert.match(page, /@success="refreshSubmissionGrid"/);
});

test('wujin merchant product publish wizard guides base category chain and audit steps', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );
  const publishSchema = data.slice(
    data.indexOf('export function useProductPublishBaseSchema'),
    data.indexOf('export function useProductPublishCategorySchema'),
  );
  const publishChainSchema = data.slice(
    data.indexOf('export function useProductPublishChainSchema'),
    data.indexOf('function useSelectedRelationSchema'),
  );
  const publishProductIdField = publishSchema.slice(
    publishSchema.indexOf("fieldName: 'productId'"),
    publishSchema.indexOf("fieldName: 'productName'"),
  );

  assert.match(data, /productPublishSteps/);
  assert.match(data, /基础信息/);
  assert.match(data, /上架类目/);
  assert.match(data, /供应能力/);
  assert.match(data, /让买家更容易搜到/);
  assert.match(data, /审核上架/);
  assert.match(data, /useProductPublishBaseSchema/);
  assert.match(data, /useProductPublishCategorySchema/);
  assert.match(data, /useProductPublishSupplySchema/);
  assert.match(data, /useProductPublishChainSchema/);
  assert.match(data, /productLane/);
  assert.match(data, /productCategoryId/);
  assert.match(data, /productBrandId/);
  assert.match(data, /productPicUrl/);
  assert.match(data, /productPrice/);
  assert.match(data, /productStock/);
  assert.equal(publishProductIdField, '');
  assert.match(data, /templateId/);
  assert.match(data, /selectedEntityId/);
  assert.doesNotMatch(publishChainSchema, /fieldName:\s*'customRelationsJson'/);

  assert.match(
    wizard,
    /defineOptions\(\{\s*name:\s*'WujinMerchantProductPublishWizard'\s*\}\)/,
  );
  assert.match(wizard, /Steps/);
  assert.match(wizard, /currentStep/);
  assert.match(wizard, /productPublishSteps/);
  assert.match(wizard, /goPrevStep/);
  assert.match(wizard, /goNextStep/);
  assert.match(wizard, /buildSubmitRequest/);
  assert.doesNotMatch(wizard, /merchantId:\s*values\.merchantId/);
  assert.doesNotMatch(wizard, /productId:\s*values\.productId/);
  assert.match(wizard, /productBrandId:\s*values\.productBrandId/);
  assert.match(wizard, /productPicUrl:\s*values\.productPicUrl/);
  assert.match(wizard, /productPrice:\s*values\.productPrice/);
  assert.match(wizard, /productStock:\s*values\.productStock/);
  assert.match(wizard, /buildCustomRelations/);
  assert.match(wizard, /submitRelation/);
  assert.match(wizard, /submitResult/);
  assert.match(wizard, /商品发布/);
  assert.match(wizard, /审核上架/);
  assert.match(wizard, /submitResult\.productId/);
  assert.match(wizard, /提交审核/);
  assert.match(wizard, /完善度/);
  assert.match(wizard, /优化建议/);
  assert.match(wizard, /emit\('success'\)/);
});

test('wujin merchant product publish supports searchable material process selection and custom additions', () => {
  const api = read('apps/web-antd/src/api/wujin/merchant.ts');
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );
  const publishSteps = data.slice(
    data.indexOf('export const productPublishSteps'),
    data.indexOf('export function optionLabel'),
  );
  const selectedRelationSchema = data.slice(
    data.indexOf('function useSelectedRelationSchema'),
    data.indexOf('export function useItemFormSchema'),
  );
  const materialProcessOptions = data.slice(
    data.indexOf('async function getMaterialProcessChainEntityOptions'),
    data.indexOf('function useSelectedRelationSchema'),
  );

  assert.match(api, /entityName\?:\s*string/);
  assert.match(publishSteps, /让买家更容易搜到/);
  assert.doesNotMatch(publishSteps, /产业链角色/);

  assert.match(selectedRelationSchema, /label:\s*'主要原材料\/加工工艺'/);
  assert.match(selectedRelationSchema, /getMaterialProcessChainEntityOptions/);
  assert.match(materialProcessOptions, /lane:\s*'MATERIAL'/);
  assert.match(materialProcessOptions, /lane:\s*'PROCESS'/);
  assert.match(
    selectedRelationSchema,
    /placeholder:\s*'搜索选择，如天然橡胶、热处理、镀锌'/,
  );
  assert.doesNotMatch(selectedRelationSchema, /label:\s*'产业链实体'/);
  assert.doesNotMatch(selectedRelationSchema, /请选择产业链实体/);

  assert.match(wizard, /customRelationDraft/);
  assert.match(wizard, /customRelationDrafts/);
  assert.match(wizard, /addCustomRelationDraft/);
  assert.match(wizard, /removeCustomRelationDraft/);
  assert.match(
    wizard,
    /function buildCustomRelationDraft\(row: CustomRelation\)/,
  );
  assert.match(wizard, /const entityName = row\.entityName\?\.trim\(\)/);
  assert.match(wizard, /v-model:value="customRelationDraft\.entityName"/);
  assert.match(wizard, /系统里搜不到的资料/);
  assert.match(wizard, /添加自定义项/);
});

test('wujin merchant product publish uses selectors instead of raw technical ids', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const publishBaseSchema = data.slice(
    data.indexOf('export function useProductPublishBaseSchema'),
    data.indexOf('export function useProductPublishCategorySchema'),
  );
  const publishCategorySchema = data.slice(
    data.indexOf('export function useProductPublishCategorySchema'),
    data.indexOf('export function useProductPublishAttributeSchema'),
  );
  const publishBrandField = publishBaseSchema.slice(
    publishBaseSchema.indexOf("fieldName: 'productBrandId'"),
    publishBaseSchema.indexOf("fieldName: 'productPicUrl'"),
  );

  assert.doesNotMatch(publishBaseSchema, /fieldName:\s*'merchantId'/);
  assert.doesNotMatch(publishBaseSchema, /label:\s*'商家ID'/);
  assert.doesNotMatch(publishBaseSchema, /fieldName:\s*'productId'/);
  assert.doesNotMatch(publishBaseSchema, /label:\s*'商品ID'/);
  assert.doesNotMatch(publishBaseSchema, /label:\s*'品牌ID'/);
  assert.match(publishBaseSchema, /label:\s*'商品品牌'/);
  assert.match(publishBaseSchema, /fieldName:\s*'productBrandId'/);
  assert.match(publishBaseSchema, /component:\s*'ApiSelect'/);
  assert.match(publishBaseSchema, /getSimpleBrandList/);
  assert.doesNotMatch(publishBrandField, /rules:\s*'required'/);

  assert.doesNotMatch(publishCategorySchema, /label:\s*'上架类目ID'/);
  assert.doesNotMatch(publishCategorySchema, /label:\s*'行业模板ID'/);
  assert.match(publishCategorySchema, /label:\s*'上架类目'/);
  assert.match(publishCategorySchema, /label:\s*'行业模板'/);
  assert.match(publishCategorySchema, /component:\s*'ApiTreeSelect'/);
  assert.match(publishCategorySchema, /getProductCategoryTree/);
  assert.match(publishCategorySchema, /getIndustryTemplateList/);
});

test('wujin merchant dashboard shows local business metric cards', () => {
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');

  assert.match(page, /businessMetricCards/);
  assert.match(page, /businessSourceSummary/);
  assert.match(page, /relationSubmissions\.value/);
  assert.match(page, /sourcingLeads\.value/);
  assert.match(page, /supplyCapabilities\.value/);
  assert.match(page, /经营数据/);
  assert.match(page, /申报总数/);
  assert.match(page, /待审核申报/);
  assert.match(page, /可供能力/);
  assert.match(page, /待跟进线索/);
  assert.match(page, /本地聚合/);
});

test('wujin merchant product publish includes standard attributes and custom tag review entry', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );

  assert.match(data, /商品关键参数/);
  assert.match(data, /买家常搜词/);
  assert.match(data, /useProductPublishAttributeSchema/);
  assert.doesNotMatch(data, /fieldName:\s*'standardAttributesJson'/);
  assert.doesNotMatch(data, /fieldName:\s*'customTagsJson'/);
  assert.match(data, /customTagReviewNote/);
  assert.match(data, /提交平台审核/);
  assert.match(data, /填写买家会关注的材质、规格、牌号、用途/);
  assert.match(data, /买家常搜词会随商品发布进入平台审核/);

  assert.match(wizard, /AttributeForm/);
  assert.match(wizard, /attributeFormApi/);
  assert.match(wizard, /standardAttributeRows/);
  assert.match(wizard, /customTagValues/);
  assert.match(wizard, /addStandardAttribute/);
  assert.match(wizard, /removeStandardAttribute/);
  assert.match(wizard, /standardAttributes:\s*buildStandardAttributes\(\)/);
  assert.match(wizard, /customTags:\s*buildCustomTags\(\)/);
  assert.match(wizard, /customTagReviewNote:\s*values\.customTagReviewNote/);
  assert.doesNotMatch(wizard, /standardAttributesJson/);
  assert.doesNotMatch(wizard, /customTagsJson/);
  assert.doesNotMatch(wizard, /parseJsonArrayField/);
  assert.match(wizard, /商品关键参数/);
  assert.match(wizard, /买家常搜词/);
});

test('wujin merchant search data copy is written for business users', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );
  const supplySchema = data.slice(
    data.indexOf('export function useSupplyCapabilityFormSchema'),
    data.indexOf('export function useSupplyCapabilityFilterSchema'),
  );

  assert.match(data, /让买家更容易搜到/);
  assert.match(data, /主要原材料\/加工工艺/);
  assert.match(data, /这个资料的作用/);
  assert.match(data, /商品关键参数/);
  assert.match(data, /买家常搜词/);
  assert.match(data, /供应内容/);
  assert.match(data, /供应类型/);
  assert.match(data, /适用行业/);
  assert.doesNotMatch(supplySchema, /label:\s*'产业链实体'/);
  assert.doesNotMatch(supplySchema, /label:\s*'供应泳道'/);
  assert.doesNotMatch(supplySchema, /label:\s*'行业上下文'/);

  assert.match(wizard, /商品关键参数/);
  assert.match(wizard, /买家常搜词/);
  assert.doesNotMatch(wizard, /自定义原材料\/加工工艺/);
});

test('wujin merchant product publish builds json arrays from structured controls', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const wizard = read(
    'apps/web-antd/src/views/wujin/merchant/modules/product-publish-wizard.vue',
  );
  const chainSchema = data.slice(
    data.indexOf('export function useProductPublishChainSchema'),
    data.indexOf('function useSelectedRelationSchema'),
  );

  assert.doesNotMatch(chainSchema, /component:\s*'Textarea'/);
  assert.doesNotMatch(wizard, /JSON 数组/);
  assert.match(wizard, /relationRows/);
  assert.match(wizard, /addRelationRow/);
  assert.match(wizard, /removeRelationRow/);
  assert.match(wizard, /customRelations:\s*buildCustomRelations\(\)/);
  assert.match(wizard, /v-model:value="row\.entityId"/);
  assert.match(wizard, /v-model:value="row\.relationType"/);
  assert.match(wizard, /v-model:checked="row\.requiredFlag"/);
  assert.match(wizard, /v-model:value="customTagValues"/);
});

test('wujin merchant relation declaration supports edit modal and template import placeholder', () => {
  const api = read('apps/web-antd/src/api/wujin/merchant.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const form = read(
    'apps/web-antd/src/views/wujin/merchant/modules/relation-submit-form.vue',
  );

  assert.match(api, /RelationSubmissionUpdateRequest/);
  assert.match(api, /updateRelationSubmission/);

  assert.match(page, /handleEditRelationSubmit/);
  assert.match(page, /handleImportRelationTemplate/);
  assert.match(page, /编辑申报/);
  assert.match(page, /模板导入/);
  assert.match(page, /message\.info\('模板导入后端接口暂未提供/);
  assert.match(page, /submissionActions/);

  assert.match(form, /isEditMode/);
  assert.match(form, /updateRelationSubmission/);
  assert.match(form, /modalTitle/);
  assert.match(form, /编辑关系申报/);
  assert.match(form, /提交关系申报/);
});

test('wujin merchant completeness explanation modal uses backend score suggestion and missing items', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const modal = read(
    'apps/web-antd/src/views/wujin/merchant/modules/completeness-explain-modal.vue',
  );

  assert.match(data, /completenessSuggestion/);
  assert.match(data, /missingItems/);
  assert.match(data, /auditReason/);
  assert.match(page, /CompletenessExplainModal/);
  assert.match(page, /handleExplainCompleteness/);
  assert.match(page, /完善度解释/);
  assert.match(page, /completenessActions/);
  assert.match(modal, /WujinMerchantCompletenessExplainModal/);
  assert.match(modal, /completenessScore/);
  assert.match(modal, /completenessSuggestion/);
  assert.match(modal, /missingItems/);
  assert.match(modal, /auditReason/);
  assert.match(modal, /后端未返回缺失项/);
});

test('wujin merchant sourcing leads expose multi-stage follow-up and report skeleton', () => {
  const data = read('apps/web-antd/src/views/wujin/merchant/data.ts');
  const page = read('apps/web-antd/src/views/wujin/merchant/index.vue');
  const form = read(
    'apps/web-antd/src/views/wujin/merchant/modules/sourcing-lead-handle-form.vue',
  );

  assert.match(data, /sourcingLeadStageOptions/);
  assert.match(data, /followStage/);
  assert.match(data, /nextFollowTime/);
  assert.match(data, /quotedAmount/);
  assert.match(data, /winProbability/);
  assert.match(data, /CONVERTED/);
  assert.match(data, /LOST/);

  assert.match(page, /leadReportCards/);
  assert.match(page, /线索转化报表/);
  assert.match(page, /已报价/);
  assert.match(page, /已转化/);
  assert.match(page, /转化率/);
  assert.match(page, /待继续跟进/);

  assert.match(form, /followStage/);
  assert.match(form, /nextFollowTime/);
  assert.match(form, /quotedAmount/);
  assert.match(form, /winProbability/);
  assert.match(form, /多阶段跟进/);
});
