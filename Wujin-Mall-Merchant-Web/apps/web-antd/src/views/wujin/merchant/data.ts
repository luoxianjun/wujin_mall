import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSpuApi } from '#/api/mall/product/spu';

import { h } from 'vue';

import { handleTree } from '@vben/utils';

import { Tag } from 'ant-design-vue';

import { getCategoryList } from '#/api/mall/product/category';
import { getSimpleBrandList } from '#/api/mall/product/brand';
import {
  getChainEntityList,
  getIndustryTemplateList,
} from '#/api/wujin/merchant';

export const laneOptions = [
  { label: '成品', value: 'PRODUCT' },
  { label: '加工', value: 'PROCESS' },
  { label: '原材料', value: 'MATERIAL' },
];

export const productStatusOptions = [
  { label: '回收站', value: -1 },
  { label: '仓库中', value: 0 },
  { label: '出售中', value: 1 },
];

export const productListTabOptions = [
  { label: '出售中', value: 0 },
  { label: '仓库中', value: 1 },
  { label: '已售罄', value: 2 },
  { label: '警戒库存', value: 3 },
  { label: '回收站', value: 4 },
];

export const auditStatusOptions = [
  { label: '草稿', value: 0 },
  { label: '待审核', value: 10 },
  { label: '自动通过', value: 20 },
  { label: '人工通过', value: 30 },
  { label: '驳回补充', value: 40 },
];

export const auditRouteOptions = [
  { label: '平台模板自动通过', value: 'AUTO_APPROVE' },
  { label: '平台人工审核', value: 'MANUAL_REVIEW' },
  { label: '阻断', value: 'BLOCK' },
];

export const relationTypeOptions = [
  { label: '需要原材料', value: 'REQUIRES_MATERIAL' },
  { label: '需要加工工艺', value: 'REQUIRES_PROCESS' },
  { label: '需要设备', value: 'REQUIRES_EQUIPMENT' },
  { label: '来源于', value: 'SOURCE_FROM' },
  { label: '规格参数', value: 'SPECIFICATION' },
];

export const templateStatusOptions = [
  { label: '已发布', value: 0 },
  { label: '停用', value: 1 },
];

export const supplyStatusOptions = [
  { label: '启用', value: 0 },
  { label: '停供', value: 1 },
];

export const sourcingLeadStatusOptions = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '已联系', value: 'CONTACTED' },
  { label: '已报价', value: 'QUOTED' },
  { label: '已转化', value: 'CONVERTED' },
  { label: '已流失', value: 'LOST' },
  { label: '已关闭', value: 'CLOSED' },
];

export const sourcingLeadStageOptions = [
  { label: '初次联系', value: 'FIRST_CONTACT' },
  { label: '需求确认', value: 'REQUIREMENT_CONFIRMED' },
  { label: '报价中', value: 'QUOTING' },
  { label: '样品/合同', value: 'SAMPLE_OR_CONTRACT' },
  { label: '已转化', value: 'CONVERTED' },
  { label: '已流失', value: 'LOST' },
];

export const sourcingDispatchStatusOptions = [
  { label: '待分发', value: 'PENDING' },
  { label: '已分发', value: 'DISPATCHED' },
];

export const productPublishSteps = [
  { title: '基础信息' },
  { title: '上架类目' },
  { title: '供应能力' },
  { title: '让买家更容易搜到' },
  { title: '审核上架' },
];

export const productPublishStructuredHelp = {
  chainRelations:
    '填写商品用到的主要原材料或加工工艺，买家搜索相关词时更容易找到你。',
  chainRelationsTitle: '主要原材料/加工工艺',
  customTags: '买家常搜词会随商品发布进入平台审核。',
  customTagsTitle: '买家常搜词',
  supplyCapability:
    '这些信息会保存为当前商品的供应能力，审核通过后自动参与采购寻源，无需另行设置。',
  standardAttributes:
    '填写买家会关注的材质、规格、牌号、用途，提交后随商品一起审核。',
  standardAttributesTitle: '商品关键参数',
};

export function optionLabel(
  options: Array<{ label: string; value: number | string }>,
  value?: number | string,
) {
  return options.find((item) => item.value === value)?.label ?? value ?? '-';
}

function tag(
  options: Array<{ label: string; value: number | string }>,
  value?: number | string,
  color = 'blue',
) {
  const label = optionLabel(options, value);
  return label === '-' ? '-' : h(Tag, { color }, () => label);
}

export function useProductListFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '商品名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '输入商品名称',
      },
    },
    {
      fieldName: 'categoryId',
      label: '商品分类',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: getProductCategoryTree,
        fieldNames: { children: 'children', label: 'name', value: 'id' },
        placeholder: '请选择商品分类',
        showSearch: true,
        treeDefaultExpandAll: true,
      },
    },
    {
      fieldName: 'tabType',
      label: '商品状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: productListTabOptions,
        placeholder: '全部状态',
      },
    },
  ];
}

export function useProductListColumns(): VxeTableGridOptions<MallSpuApi.Spu>['columns'] {
  return [
    { field: 'id', title: '商品编号', minWidth: 110 },
    { field: 'name', title: '商品名称', minWidth: 220 },
    {
      field: 'picUrl',
      title: '商品图片',
      width: 100,
      cellRender: { name: 'CellImage' },
    },
    {
      field: 'price',
      title: '销售价',
      minWidth: 110,
      formatter: 'formatAmount2',
    },
    { field: 'salesCount', title: '销量', minWidth: 90 },
    { field: 'stock', title: '库存', minWidth: 90 },
    {
      field: 'status',
      title: '销售状态',
      minWidth: 110,
      slots: {
        default: ({ row }) => {
          const color =
            row.status === 1
              ? 'green'
              : row.status === -1
                ? 'default'
                : 'orange';
          return tag(productStatusOptions, row.status, color);
        },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
  ];
}

export function useSubmissionFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '输入商品名称',
      },
    },
    {
      fieldName: 'auditStatus',
      label: '审核状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: auditStatusOptions,
      },
    },
    {
      fieldName: 'auditRoute',
      label: '审核路径',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: auditRouteOptions,
      },
    },
  ];
}

export function useSubmissionColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'productName', title: '商品名称', minWidth: 190 },
    { field: 'productId', title: '商品ID', minWidth: 100 },
    {
      field: 'productLane',
      title: '商品泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.productLane) },
    },
    { field: 'productCategoryId', title: '成品类目ID', minWidth: 120 },
    { field: 'templateId', title: '模板ID', minWidth: 100 },
    {
      field: 'auditStatus',
      title: '审核状态',
      minWidth: 120,
      slots: {
        default: ({ row }) =>
          tag(auditStatusOptions, row.auditStatus, 'orange'),
      },
    },
    {
      field: 'auditRoute',
      title: '审核路径',
      minWidth: 160,
      formatter: ({ row }) => optionLabel(auditRouteOptions, row.auditRoute),
    },
    {
      field: 'completenessScore',
      title: '关系完善度',
      minWidth: 130,
      slots: { default: 'completenessActions' },
    },
    { field: 'completenessSuggestion', title: '完善度建议', minWidth: 220 },
    { field: 'missingItems', title: '缺失项', minWidth: 180 },
    { field: 'auditReason', title: '审核说明', minWidth: 220 },
    { field: 'remark', title: '备注', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'submissionActions' },
    },
  ];
}

export function useRelationSubmitFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'productId',
      label: '商品ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: { placeholder: '请输入待申报商品名称' },
      rules: 'required',
    },
    {
      fieldName: 'productCategoryId',
      label: '成品类目ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
    },
    {
      fieldName: 'templateId',
      label: '行业模板ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'certificationCount',
      label: '认证数量',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'hasApplicationDescription',
      label: '应用说明',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '已填写', value: true },
          { label: '未填写', value: false },
        ],
      },
    },
    ...useSelectedRelationSchema(),
  ];
}

export function useProductPublishBaseSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: { placeholder: '请输入商品名称' },
      rules: 'required',
    },
    {
      fieldName: 'productBrandId',
      label: '商品品牌',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleBrandList,
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '请选择商品品牌',
        showSearch: true,
      },
    },
    {
      fieldName: 'productPicUrl',
      label: '商品主图',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商品主图 URL',
      },
    },
    {
      fieldName: 'productPrice',
      label: '销售价(分)',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'productMarketPrice',
      label: '市场价(分)',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'productCostPrice',
      label: '成本价(分)',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'productLane',
      label: '商品泳道',
      component: 'Select',
      componentProps: {
        options: laneOptions,
      },
      defaultValue: 'PRODUCT',
      rules: 'required',
    },
    {
      fieldName: 'hasApplicationDescription',
      label: '应用说明',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '已填写', value: true },
          { label: '未填写', value: false },
        ],
      },
      defaultValue: true,
    },
  ];
}

export function useProductPublishCategorySchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'productCategoryId',
      label: '上架类目',
      component: 'ApiTreeSelect',
      componentProps: {
        api: getProductCategoryTree,
        fieldNames: { children: 'children', label: 'name', value: 'id' },
        placeholder: '请选择商城商品类目',
        showSearch: true,
        treeDefaultExpandAll: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'templateId',
      label: '行业模板',
      component: 'ApiSelect',
      componentProps: {
        api: () =>
          getIndustryTemplateList({ productLane: 'PRODUCT', status: 0 }),
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '请选择行业关系模板',
        showSearch: true,
      },
      rules: 'required',
    },
  ];
}

export function useProductPublishSupplySchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'supplyEntityId',
      label: '供应内容',
      component: 'ApiSelect',
      componentProps: {
        api: () => getChainEntityList({ status: 0 }),
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '选择这个商品实际出售的内容，如轮胎或天然橡胶',
        showSearch: true,
      },
      help: '这里只填写商品实际出售的内容；生产所需材料在下一步填写。',
      rules: 'required',
    },
    {
      fieldName: 'productStock',
      label: '可供库存',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, precision: 0 },
      defaultValue: 0,
      rules: 'required',
    },
    {
      fieldName: 'supplyMinOrderQuantity',
      label: '最小起订量',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1, precision: 0 },
      defaultValue: 1,
      rules: 'required',
    },
    {
      fieldName: 'supplyDeliveryDays',
      label: '交付周期(天)',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, precision: 0 },
      defaultValue: 1,
      rules: 'required',
    },
    {
      fieldName: 'supplyServiceArea',
      label: '服务区域',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '例如：全国、华东地区、江浙沪',
      },
      rules: 'required',
    },
    {
      fieldName: 'supplyRemark',
      label: '供应说明',
      component: 'Textarea',
      componentProps: {
        allowClear: true,
        placeholder: '补充包装方式、发货条件、定制能力等信息',
        rows: 3,
      },
    },
  ];
}

async function getProductCategoryTree() {
  const categories = await getCategoryList({});
  return handleTree(categories, 'id', 'parentId', 'children');
}

export function useProductPublishAttributeSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'customTagReviewNote',
      label: '标签审核说明',
      component: 'Textarea',
      componentProps: {
        allowClear: true,
        placeholder: '说明自定义标签的依据，便于平台审核。',
        rows: 2,
      },
    },
    {
      fieldName: 'customTagReviewRequired',
      label: '标签审核入口',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '提交平台审核', value: true },
          { label: '暂不提交', value: false },
        ],
      },
      defaultValue: true,
    },
  ];
}

export function useProductPublishChainSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'certificationCount',
      label: '认证数量',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      defaultValue: 0,
    },
  ];
}

async function getMaterialProcessChainEntityOptions() {
  const [materials, processes] = await Promise.all([
    getChainEntityList({ lane: 'MATERIAL', status: 0 }),
    getChainEntityList({ lane: 'PROCESS', status: 0 }),
  ]);
  return [...materials, ...processes];
}

function useSelectedRelationSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'selectedEntityId',
      label: '主要原材料/加工工艺',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: getMaterialProcessChainEntityOptions,
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '搜索选择，如天然橡胶、热处理、镀锌',
        showSearch: true,
      },
    },
    {
      fieldName: 'selectedRelationType',
      label: '这个资料的作用',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: relationTypeOptions,
      },
      defaultValue: 'REQUIRES_MATERIAL',
    },
    {
      fieldName: 'selectedRequiredFlag',
      label: '是否必填',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '必填', value: true },
          { label: '选填', value: false },
        ],
      },
      defaultValue: false,
    },
    {
      fieldName: 'selectedRelationRemark',
      label: '补充说明',
      component: 'Textarea',
      componentProps: {
        allowClear: true,
        placeholder: '例如：轮胎主料、表面防腐处理、可替代材料',
        rows: 2,
      },
    },
  ];
}

export function useItemFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'submissionId',
      label: '申报单ID',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'relationType',
      label: '关系类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: relationTypeOptions,
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '必填项',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '必填', value: true },
          { label: '选填', value: false },
        ],
      },
    },
  ];
}

export function useItemColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'submissionId', title: '申报单ID', minWidth: 120 },
    { field: 'entityId', title: '产业链实体ID', minWidth: 130 },
    {
      field: 'relationType',
      title: '关系类型',
      minWidth: 150,
      slots: {
        default: ({ row }) =>
          tag(relationTypeOptions, row.relationType, 'green'),
      },
    },
    {
      field: 'fromTemplate',
      title: '模板带出',
      minWidth: 100,
      formatter: ({ row }) => (row.fromTemplate ? '是' : '否'),
    },
    {
      field: 'requiredFlag',
      title: '必填',
      minWidth: 90,
      formatter: ({ row }) => (row.requiredFlag ? '是' : '否'),
    },
    { field: 'remark', title: '说明', minWidth: 240 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
  ];
}

export function useTemplateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'industryCode',
      label: '行业编码',
      component: 'Input',
      componentProps: { allowClear: true },
    },
  ];
}

export function useTemplateColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'templateCode', title: '模板编码', minWidth: 160 },
    { field: 'name', title: '模板名称', minWidth: 200 },
    { field: 'industryCode', title: '行业编码', minWidth: 130 },
    {
      field: 'productLane',
      title: '适用泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.productLane) },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
      formatter: ({ row }) => optionLabel(templateStatusOptions, row.status),
    },
    { field: 'remark', title: '说明', minWidth: 240 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
  ];
}

export function useSupplyCapabilityFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'productId',
      label: '商品ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: { placeholder: '请输入商品名称' },
      rules: 'required',
    },
    {
      fieldName: 'entityId',
      label: '供应内容',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: () => getChainEntityList({ status: 0 }),
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '搜索选择你能供应的商品、原材料或工艺',
        showSearch: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'lane',
      label: '供应类型',
      component: 'Select',
      componentProps: { options: laneOptions },
      defaultValue: 'MATERIAL',
      rules: 'required',
    },
    {
      fieldName: 'industry',
      label: '适用行业',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '如：轮胎、轴承、紧固件',
      },
    },
    {
      fieldName: 'supplyStatus',
      label: '供应状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: supplyStatusOptions,
      },
      defaultValue: 0,
      rules: 'required',
    },
    {
      fieldName: 'stockCount',
      label: '库存数量',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'minOrderQuantity',
      label: '最小起订量',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'deliveryDays',
      label: '交付周期',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'serviceArea',
      label: '服务区域',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '如：华东' },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useSupplyCapabilityFilterSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
    },
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'supplyStatus',
      label: '供应状态',
      component: 'Select',
      componentProps: { allowClear: true, options: supplyStatusOptions },
    },
  ];
}

export function useSupplyCapabilityColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'merchantId', title: '商家ID', minWidth: 100 },
    { field: 'productName', title: '商品名称', minWidth: 180 },
    { field: 'productId', title: '商品ID', minWidth: 100 },
    { field: 'entityId', title: '产业链实体ID', minWidth: 130 },
    {
      field: 'lane',
      title: '供应泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.lane) },
    },
    { field: 'industry', title: '行业上下文', minWidth: 130 },
    {
      field: 'supplyStatus',
      title: '供应状态',
      minWidth: 100,
      slots: {
        default: ({ row }) =>
          tag(
            supplyStatusOptions,
            row.supplyStatus,
            row.supplyStatus === 0 ? 'green' : 'red',
          ),
      },
    },
    { field: 'stockCount', title: '库存', minWidth: 90 },
    { field: 'minOrderQuantity', title: '起订量', minWidth: 90 },
    { field: 'deliveryDays', title: '交付天数', minWidth: 100 },
    { field: 'serviceArea', title: '服务区域', minWidth: 120 },
    { field: 'remark', title: '备注', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'supplyCapabilityActions' },
    },
  ];
}

export function useSourcingLeadFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
    },
    {
      fieldName: 'keyword',
      label: '关键词',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'leadStatus',
      label: '线索状态',
      component: 'Select',
      componentProps: { allowClear: true, options: sourcingLeadStatusOptions },
    },
  ];
}

export function useSourcingLeadHandleFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'leadId',
      label: '线索ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', disabled: true, min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'handleAction',
      label: '处理动作',
      component: 'Select',
      componentProps: {
        options: [
          { label: '已联系', value: 'CONTACTED' },
          { label: '已报价', value: 'QUOTED' },
          { label: '已转化', value: 'CONVERTED' },
          { label: '已流失', value: 'LOST' },
          { label: '已关闭', value: 'CLOSED' },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'followStage',
      label: '跟进阶段',
      component: 'Select',
      componentProps: {
        options: sourcingLeadStageOptions,
        placeholder: '多阶段跟进',
      },
    },
    {
      fieldName: 'nextFollowTime',
      label: '下次跟进',
      component: 'DatePicker',
      componentProps: {
        class: 'w-full',
        format: 'YYYY-MM-DD HH:mm',
        showTime: true,
        // 后端 LocalDateTime 按毫秒时间戳反序列化
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'quotedAmount',
      label: '报价金额(分)',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'winProbability',
      label: '预计转化率',
      component: 'InputNumber',
      componentProps: { class: 'w-full', max: 100, min: 0 },
    },
    {
      fieldName: 'handleRemark',
      label: '处理备注',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useSourcingLeadColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'keyword', title: '关键词', minWidth: 140 },
    {
      field: 'lane',
      title: '泳道',
      minWidth: 100,
      slots: { default: ({ row }) => tag(laneOptions, row.lane) },
    },
    { field: 'sourceKeyword', title: '来源关键词', minWidth: 130 },
    { field: 'supplierName', title: '候选供应商', minWidth: 180 },
    { field: 'merchantId', title: '商家ID', minWidth: 100 },
    { field: 'contactName', title: '联系人', minWidth: 110 },
    { field: 'contactPhone', title: '联系方式', minWidth: 140 },
    {
      field: 'leadStatus',
      title: '线索状态',
      minWidth: 110,
      slots: {
        default: ({ row }) =>
          tag(sourcingLeadStatusOptions, row.leadStatus, 'orange'),
      },
    },
    {
      field: 'followStage',
      title: '跟进阶段',
      minWidth: 130,
      formatter: ({ row }) =>
        optionLabel(sourcingLeadStageOptions, row.followStage),
    },
    {
      field: 'nextFollowTime',
      title: '下次跟进',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    { field: 'quotedAmount', title: '报价金额(分)', minWidth: 120 },
    { field: 'winProbability', title: '预计转化率', minWidth: 120 },
    {
      field: 'dispatchStatus',
      title: '分发状态',
      minWidth: 110,
      slots: {
        default: ({ row }) =>
          tag(sourcingDispatchStatusOptions, row.dispatchStatus, 'blue'),
      },
    },
    { field: 'requirement', title: '需求说明', minWidth: 240 },
    { field: 'dispatchRemark', title: '平台分发备注', minWidth: 220 },
    { field: 'handleRemark', title: '处理备注', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'sourcingLeadActions' },
    },
  ];
}
