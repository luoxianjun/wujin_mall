import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { Tag } from 'ant-design-vue';

import {
  getChainEntityList,
  getIndustryTemplateList,
} from '#/api/wujin/platform';
import { getRangePickerDefaultProps } from '#/utils';

export const laneOptions = [
  { label: '成品', value: 'PRODUCT' },
  { label: '加工', value: 'PROCESS' },
  { label: '原材料', value: 'MATERIAL' },
];

export const healthStatusOptions = [
  { label: '健康', value: 'HEALTHY' },
  { label: '需拆分', value: 'NEEDS_SPLIT' },
  { label: '无绑定', value: 'UNBOUND' },
];

export const monitorMetricOptions = [
  { label: '搜索满意度', value: 'SEARCH_SATISFACTION' },
  { label: '制造链查看率', value: 'CHAIN_VIEW_RATE' },
  { label: '分类准确率', value: 'CLASSIFICATION_ACCURACY' },
  { label: '平均响应时间', value: 'AVG_RESPONSE_TIME' },
  { label: '关系审核通过率', value: 'RELATION_AUDIT_PASS_RATE' },
  { label: '高风险提示次数', value: 'HIGH_RISK_WARNING_COUNT' },
];

export const statusOptions = [
  { label: '开启', value: 0 },
  { label: '关闭', value: 1 },
];

export const sourcingLeadStatusOptions = [
  { label: '已提交', value: 'SUBMITTED' },
  { label: '已分配', value: 'ASSIGNED' },
  { label: '已联系', value: 'CONTACTED' },
  { label: '已报价', value: 'QUOTED' },
  { label: '已成交', value: 'CONVERTED' },
  { label: '未成交', value: 'LOST' },
  { label: '已关闭', value: 'CLOSED' },
];

export const sourcingDispatchStatusOptions = [
  { label: '待分发', value: 'PENDING' },
  { label: '已分发', value: 'DISPATCHED' },
];

export const auditActionOptions = [
  { label: '阻断', value: 'BLOCK' },
  { label: '建议归并', value: 'SUGGEST_MERGE' },
  { label: '人工审核', value: 'MANUAL_REVIEW' },
  { label: '通过', value: 'APPROVE' },
  { label: '驳回', value: 'REJECT' },
];

export const relationTypeOptions = [
  { label: '需要原材料', value: 'REQUIRES_MATERIAL' },
  { label: '需要加工工艺', value: 'REQUIRES_PROCESS' },
  { label: '替代材料', value: 'ALTERNATIVE_MATERIAL' },
  { label: '风险约束', value: 'RISK_CONSTRAINT' },
];

export const searchRuleTypeOptions = [
  { label: '调整展示层级', value: 'GRANULARITY_LIMIT' },
  { label: '命中关键词后切换频道', value: 'LANE_OVERRIDE' },
  { label: '命中条件后提示风险', value: 'RISK_WARNING' },
  { label: '搜索词词典', value: 'INTENT_DICT' },
  { label: '商品别名', value: 'ENTITY_ALIAS' },
  { label: '搜索加权', value: 'WEIGHT' },
];

export const attributeValueTypeOptions = [
  { label: '文本', value: 'TEXT' },
  { label: '数字', value: 'NUMBER' },
  { label: '单选', value: 'ENUM' },
  { label: '多选', value: 'MULTI_ENUM' },
  { label: '是/否', value: 'BOOLEAN' },
];

export const attributeLaneOptions = [
  { label: '三泳道通用', value: '' },
  ...laneOptions,
];

export const customTagAuditStatusOptions = [
  { label: '待审核', value: 10 },
  { label: '审核通过', value: 30 },
  { label: '已驳回', value: 40 },
];

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

export function searchRuleTypeLabel(value?: string) {
  return optionLabel(searchRuleTypeOptions, value);
}

function hiddenIdField(): VbenFormSchema {
  return hiddenField('id');
}

function hiddenField(fieldName: string): VbenFormSchema {
  return {
    component: 'Input',
    fieldName,
    dependencies: { triggerFields: [''], show: () => false },
  };
}

export function useCategoryFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '类目名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '输入类目名称',
      },
    },
    {
      fieldName: 'lane',
      label: '泳道',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: laneOptions,
        placeholder: '全部泳道',
      },
    },
    {
      fieldName: 'healthStatus',
      label: '健康状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: healthStatusOptions,
        placeholder: '全部状态',
      },
    },
  ];
}

export function useCategoryEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'parentId',
      label: '父级ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      rules: 'required',
    },
    {
      fieldName: 'lane',
      label: '泳道',
      component: 'Select',
      componentProps: { options: laneOptions, placeholder: '请选择泳道' },
      rules: 'required',
    },
    {
      fieldName: 'code',
      label: '类目编码',
      component: 'Input',
      componentProps: { placeholder: '例如 P-TIRE-CAR' },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '类目名称',
      component: 'Input',
      componentProps: { placeholder: '请输入类目名称' },
      rules: 'required',
    },
    {
      fieldName: 'level',
      label: '层级',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: statusOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'displayDepth',
      label: '搜索展示到层级',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      help: '由平台运营设置。搜索命中该分类时，最多展示到此层级；仅有一个下级时不会自动展开。',
    },
    {
      fieldName: 'healthStatus',
      label: '健康状态',
      component: 'Select',
      componentProps: { allowClear: true, options: healthStatusOptions },
    },
    {
      fieldName: 'description',
      label: '说明',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useCategoryBatchMigrateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'targetParentId',
      label: '目标父级ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      rules: 'required',
    },
    {
      fieldName: 'targetLane',
      label: '目标泳道',
      component: 'Select',
      componentProps: { options: laneOptions, placeholder: '请选择目标泳道' },
      rules: 'required',
    },
    {
      fieldName: 'targetLevel',
      label: '目标层级',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'displayDepth',
      label: '搜索展示到层级',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      help: '批量设置分类在搜索结果中允许展开到的最深层级。',
    },
    {
      fieldName: 'healthStatus',
      label: '健康状态',
      component: 'Select',
      componentProps: { allowClear: true, options: healthStatusOptions },
    },
  ];
}

export function useCategoryColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 48 },
    {
      title: '拖拽',
      width: 70,
      slots: { default: 'categoryDragHandle' },
    },
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'lane',
      title: '泳道',
      minWidth: 100,
      slots: {
        default: ({ row }) => tag(laneOptions, row.lane),
      },
    },
    { field: 'code', title: '类目编码', minWidth: 160 },
    { field: 'name', title: '类目名称', minWidth: 180, treeNode: true },
    { field: 'parentId', title: '父级ID', minWidth: 100 },
    { field: 'level', title: '层级', minWidth: 80 },
    { field: 'displayDepth', title: '搜索展示层级', minWidth: 130 },
    {
      field: 'healthStatus',
      title: '健康状态',
      minWidth: 120,
      slots: {
        default: ({ row }) =>
          tag(healthStatusOptions, row.healthStatus, 'green'),
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
      formatter: ({ row }) => optionLabel(statusOptions, row.status),
    },
    { field: 'description', title: '说明', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'categoryActions' },
    },
  ];
}

export function useMappingFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'sourceLane',
      label: '来源泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'targetLane',
      label: '目标泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'mappingType',
      label: '映射类型',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: 'REQUIRES_MATERIAL' },
    },
  ];
}

export function useMappingEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'sourceCategoryId',
      label: '来源类目ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'sourceLane',
      label: '来源泳道',
      component: 'Select',
      componentProps: { options: laneOptions },
      rules: 'required',
    },
    {
      fieldName: 'targetCategoryId',
      label: '目标类目ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'targetLane',
      label: '目标泳道',
      component: 'Select',
      componentProps: { options: laneOptions },
      rules: 'required',
    },
    {
      fieldName: 'mappingType',
      label: '映射类型',
      component: 'Input',
      componentProps: { placeholder: '例如 REQUIRES_MATERIAL' },
      rules: 'required',
    },
    {
      fieldName: 'confidence',
      label: '置信度',
      component: 'InputNumber',
      componentProps: { class: 'w-full', max: 100, min: 0 },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: statusOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'riskNote',
      label: '风险说明',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useMappingColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'sourceCategoryId', title: '来源类目ID', minWidth: 120 },
    {
      field: 'sourceLane',
      title: '来源泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.sourceLane) },
    },
    { field: 'targetCategoryId', title: '目标类目ID', minWidth: 120 },
    {
      field: 'targetLane',
      title: '目标泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.targetLane) },
    },
    { field: 'mappingType', title: '映射类型', minWidth: 170 },
    { field: 'confidence', title: '置信度', minWidth: 100 },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
      formatter: ({ row }) => optionLabel(statusOptions, row.status),
    },
    { field: 'riskNote', title: '风险说明', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'mappingActions' },
    },
  ];
}

export function useTemplateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '输入模板名称' },
    },
    {
      fieldName: 'industryCode',
      label: '行业编码',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: { allowClear: true, options: statusOptions },
    },
  ];
}

export function useTemplateEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'templateCode',
      label: '模板编码',
      component: 'Input',
      componentProps: {
        placeholder: '例如 TPL.POWER_TOOL',
      },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: {
        placeholder: '例如 电动工具产业链模板',
      },
      rules: 'required',
    },
    {
      fieldName: 'industryCode',
      label: '行业编码',
      component: 'Input',
      componentProps: {
        placeholder: '例如 POWER_TOOL',
      },
      rules: 'required',
    },
    {
      fieldName: 'productLane',
      label: '适用泳道',
      component: 'Select',
      componentProps: { options: laneOptions },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: statusOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useTemplateColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'templateCode', title: '模板编码', minWidth: 170 },
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
      formatter: ({ row }) => optionLabel(statusOptions, row.status),
    },
    { field: 'remark', title: '备注', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'templateActions' },
    },
  ];
}

async function getChainEntitySelectOptions() {
  const entities = await getChainEntityList();
  return entities.map((entity) => ({
    ...entity,
    displayName: `${entity.name ?? '未命名实体'} #${entity.id ?? '-'}`,
  }));
}

export function useTemplateItemFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'entityId',
      label: '产业链实体',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: getChainEntitySelectOptions,
        fieldNames: { label: 'displayName', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '按实体名称或编号筛选',
        showSearch: true,
      },
    },
    {
      fieldName: 'relationType',
      label: '关系类型',
      component: 'Select',
      componentProps: { allowClear: true, options: relationTypeOptions },
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必需',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '必需', value: true },
          { label: '可选', value: false },
        ],
      },
    },
  ];
}

export function useTemplateItemEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    hiddenField('templateId'),
    {
      fieldName: 'entityId',
      label: '链路实体',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: getChainEntitySelectOptions,
        fieldNames: { label: 'displayName', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '请选择产业链实体',
        showSearch: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'relationType',
      label: '关系类型',
      component: 'Select',
      componentProps: { options: relationTypeOptions },
      rules: 'required',
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必需',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '必需', value: true },
          { label: '可选', value: false },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'weight',
      label: '权重',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useTemplateItemBatchMigrateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'targetTemplateId',
      label: '目标模板',
      component: 'ApiSelect',
      componentProps: {
        allowClear: true,
        api: () => getIndustryTemplateList({ status: 0 }),
        fieldNames: { label: 'name', value: 'id' },
        optionFilterProp: 'label',
        placeholder: '请选择目标行业模板',
        showSearch: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'relationType',
      label: '覆盖关系类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: relationTypeOptions,
        placeholder: '不选择则保留原关系类型',
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '覆盖是否必需',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '必需', value: true },
          { label: '可选', value: false },
        ],
        placeholder: '不选择则保留原必需状态',
      },
    },
    {
      fieldName: 'weight',
      label: '覆盖权重',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        min: 0,
        placeholder: '不填写则保留原权重',
      },
    },
  ];
}

export function useTemplateItemColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 48 },
    {
      title: '拖拽',
      width: 70,
      slots: { default: 'templateItemDragHandle' },
    },
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'entityId',
      title: '产业链实体',
      minWidth: 220,
      slots: { default: 'templateItemEntity' },
    },
    {
      field: 'relationType',
      title: '关系类型',
      minWidth: 160,
      formatter: ({ row }) =>
        optionLabel(relationTypeOptions, row.relationType),
    },
    {
      field: 'requiredFlag',
      title: '是否必需',
      minWidth: 100,
      formatter: ({ row }) => (row.requiredFlag ? '必需' : '可选'),
    },
    { field: 'sort', title: '展示顺序', minWidth: 100 },
    { field: 'weight', title: '匹配权重', minWidth: 100 },
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
      slots: { default: 'templateItemActions' },
    },
  ];
}

export function useAuditFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'submissionId',
      label: '申报单ID',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'action',
      label: '审核动作',
      component: 'Select',
      componentProps: { allowClear: true, options: auditActionOptions },
    },
  ];
}

export function useAuditReviewFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'submissionId',
      label: '申报单ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', disabled: true, min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'auditorId',
      label: '审核人ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'action',
      label: '审核动作',
      component: 'Select',
      componentProps: { options: auditActionOptions },
      rules: 'required',
    },
    {
      fieldName: 'comment',
      label: '审核意见',
      component: 'Textarea',
      componentProps: { rows: 4 },
    },
  ];
}

export function useAuditColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'submissionId', title: '申报单ID', minWidth: 120 },
    { field: 'auditorId', title: '审核人ID', minWidth: 120 },
    {
      field: 'action',
      title: '审核动作',
      minWidth: 120,
      slots: {
        default: ({ row }) => tag(auditActionOptions, row.action, 'orange'),
      },
    },
    { field: 'reason', title: '原因', minWidth: 190 },
    { field: 'comment', title: '审核意见', minWidth: 240 },
    {
      field: 'effectiveFlag',
      title: '生效入网',
      minWidth: 100,
      formatter: ({ row }) => (row.effectiveFlag ? '是' : '否'),
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'auditActions' },
    },
  ];
}

export function useSearchRuleFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'ruleType',
      label: '规则类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: searchRuleTypeOptions,
        placeholder: '全部规则类型',
      },
    },
    {
      fieldName: 'lane',
      label: '泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'industryCode',
      label: '行业编码',
      component: 'Input',
      componentProps: { allowClear: true },
    },
  ];
}

export function useSearchRuleEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'ruleType',
      label: '规则类型',
      component: 'Select',
      componentProps: {
        options: searchRuleTypeOptions,
        placeholder: '请选择搜索调优方式',
      },
      rules: 'required',
    },
    {
      fieldName: 'lane',
      label: '泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'industryCode',
      label: '行业编码',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '为空表示全局规则' },
    },
    {
      fieldName: 'ruleValue',
      label: '规则内容',
      component: 'Textarea',
      componentProps: {
        placeholder:
          '展示层级填 1、2 或 3；切换频道例如 lane=MATERIAL;when=keyword contains 橡胶；风险提示例如 text=跨行业不可互换;when=industry == 医疗器械',
        rows: 4,
      },
      rules: 'required',
    },
    {
      fieldName: 'weight',
      label: '权重',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: statusOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3 },
    },
  ];
}

export function useSearchRuleColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'ruleType',
      title: '规则类型',
      minWidth: 180,
      formatter: ({ row }) => searchRuleTypeLabel(row.ruleType),
    },
    {
      field: 'lane',
      title: '泳道',
      minWidth: 100,
      slots: { default: ({ row }) => tag(laneOptions, row.lane) },
    },
    { field: 'industryCode', title: '行业编码', minWidth: 130 },
    { field: 'ruleValue', title: '规则值', minWidth: 220 },
    { field: 'weight', title: '权重', minWidth: 80 },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
      formatter: ({ row }) => optionLabel(statusOptions, row.status),
    },
    { field: 'remark', title: '备注', minWidth: 180 },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'searchRuleActions' },
    },
  ];
}

export function useSearchLogFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      label: '关键词',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'resultLane',
      label: '结果泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'highRiskWarningTriggered',
      label: '风险提示',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '已触发', value: true },
          { label: '未触发', value: false },
        ],
      },
    },
    {
      fieldName: 'createTimeRange',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

export function useSearchLogColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'keyword', title: '关键词', minWidth: 140 },
    { field: 'intent', title: '搜索意图', minWidth: 120 },
    {
      field: 'resultLane',
      title: '结果泳道',
      minWidth: 110,
      slots: { default: ({ row }) => tag(laneOptions, row.resultLane) },
    },
    { field: 'industryCode', title: '行业编码', minWidth: 130 },
    {
      field: 'chainViewed',
      title: '查看制造链',
      minWidth: 110,
      formatter: ({ row }) => (row.chainViewed ? '是' : '否'),
    },
    {
      field: 'highRiskWarningTriggered',
      title: '风险提示',
      minWidth: 100,
      formatter: ({ row }) => (row.highRiskWarningTriggered ? '是' : '否'),
    },
    { field: 'satisfactionScore', title: '满意度', minWidth: 90 },
    { field: 'responseTimeMillis', title: '响应耗时(ms)', minWidth: 130 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
  ];
}

export function useSourcingLeadFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      label: '关键词',
      component: 'Input',
      componentProps: { allowClear: true },
    },
    {
      fieldName: 'lane',
      label: '泳道',
      component: 'Select',
      componentProps: { allowClear: true, options: laneOptions },
    },
    {
      fieldName: 'leadStatus',
      label: '线索状态',
      component: 'Select',
      componentProps: { allowClear: true, options: sourcingLeadStatusOptions },
    },
    {
      fieldName: 'dispatchStatus',
      label: '分发状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: sourcingDispatchStatusOptions,
      },
    },
  ];
}

export function useSourcingLeadDispatchFormSchema(): VbenFormSchema[] {
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
      fieldName: 'dispatchRemark',
      label: '分发备注',
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
    { field: 'industry', title: '行业', minWidth: 110 },
    { field: 'supplierName', title: '候选供应商', minWidth: 190 },
    { field: 'merchantId', title: '分发商家ID', minWidth: 120 },
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
      field: 'dispatchStatus',
      title: '分发状态',
      minWidth: 110,
      slots: {
        default: ({ row }) =>
          tag(sourcingDispatchStatusOptions, row.dispatchStatus, 'blue'),
      },
    },
    { field: 'requirement', title: '需求说明', minWidth: 240 },
    { field: 'handleRemark', title: '商家处理', minWidth: 220 },
    {
      field: 'quotedAmount',
      title: '报价金额(元)',
      minWidth: 120,
      formatter: ({ row }) =>
        row.quotedAmount === null || row.quotedAmount === undefined
          ? '-'
          : (Number(row.quotedAmount) / 100).toFixed(2),
    },
    {
      field: 'winProbability',
      title: '预计转化率',
      minWidth: 110,
      formatter: ({ row }) =>
        row.winProbability === null || row.winProbability === undefined
          ? '-'
          : `${row.winProbability}%`,
    },
    {
      field: 'dispatchTime',
      title: '分发时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
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

export function useMonitorSnapshotFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'metric',
      label: '指标',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: monitorMetricOptions,
      },
    },
    {
      fieldName: 'alertFlag',
      label: '告警',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '已告警', value: true },
          { label: '正常', value: false },
        ],
      },
    },
  ];
}

export function useMonitorSnapshotColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'metric',
      title: '指标',
      minWidth: 180,
      formatter: ({ row }) => optionLabel(monitorMetricOptions, row.metric),
    },
    { field: 'metricValue', title: '指标值', minWidth: 100 },
    { field: 'thresholdValue', title: '阈值', minWidth: 100 },
    {
      field: 'auditAction',
      title: '审核动作',
      minWidth: 120,
      formatter: ({ row }) => optionLabel(auditActionOptions, row.auditAction),
    },
    {
      field: 'alertFlag',
      title: '告警',
      minWidth: 90,
      formatter: ({ row }) => (row.alertFlag ? '是' : '否'),
    },
    { field: 'remark', title: '备注', minWidth: 220 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
  ];
}

export function useAttributeDictionaryFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '属性名称',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '输入属性名称' },
    },
    {
      fieldName: 'lane',
      label: '适用泳道',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: laneOptions,
        placeholder: '全部泳道',
      },
    },
    {
      fieldName: 'valueType',
      label: '值类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: attributeValueTypeOptions,
        placeholder: '全部类型',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: statusOptions,
        placeholder: '全部状态',
      },
    },
  ];
}

export function useAttributeDictionaryEditFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'groupName',
      label: '属性分组',
      component: 'Input',
      componentProps: { placeholder: '如：成品属性、原材料属性' },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '属性名称',
      component: 'Input',
      componentProps: { maxlength: 64, placeholder: '如：规格型号' },
      rules: 'required',
    },
    {
      fieldName: 'code',
      label: '属性编码',
      component: 'Input',
      componentProps: {
        maxlength: 64,
        placeholder: '大写字母、数字和下划线，如 FINISHED_PRODUCT_SPEC',
      },
      help: '保存时自动转为大写，同一编码只能存在一个属性',
      rules: 'required',
    },
    {
      fieldName: 'lane',
      label: '适用泳道',
      component: 'Select',
      componentProps: { options: attributeLaneOptions },
      help: '三泳道通用的属性会出现在所有泳道的商品发布中',
    },
    {
      fieldName: 'valueType',
      label: '值类型',
      component: 'Select',
      componentProps: { options: attributeValueTypeOptions },
      rules: 'required',
    },
    {
      fieldName: 'valueOptions',
      label: '可选值',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        placeholder: '输入后回车添加，单选/多选类型必填',
        tokenSeparators: [',', '，'],
      },
      dependencies: {
        triggerFields: ['valueType'],
        show: (values) =>
          ['ENUM', 'MULTI_ENUM', 'TEXT'].includes(values.valueType),
        rules: (values) =>
          ['ENUM', 'MULTI_ENUM'].includes(values.valueType) ? 'required' : null,
      },
    },
    {
      fieldName: 'unit',
      label: '单位',
      component: 'Input',
      componentProps: { maxlength: 32, placeholder: '如：mm、kg，可不填' },
    },
    {
      fieldName: 'requiredFlag',
      label: '发布必填',
      component: 'Switch',
      componentProps: { checkedChildren: '必填', unCheckedChildren: '可选' },
    },
    {
      fieldName: 'searchableFlag',
      label: '搜索筛选',
      component: 'Switch',
      componentProps: { checkedChildren: '参与', unCheckedChildren: '不参与' },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: statusOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '填写说明',
      component: 'Textarea',
      componentProps: {
        maxlength: 512,
        rows: 2,
        placeholder: '商家填写时看到的说明',
      },
    },
  ];
}

export function useAttributeDictionaryColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'groupName', title: '属性分组', minWidth: 120 },
    { field: 'name', title: '属性名称', minWidth: 130 },
    { field: 'code', title: '属性编码', minWidth: 200 },
    {
      field: 'lane',
      title: '适用泳道',
      minWidth: 110,
      slots: {
        default: ({ row }) =>
          row.lane ? tag(laneOptions, row.lane, 'geekblue') : '三泳道通用',
      },
    },
    {
      field: 'valueType',
      title: '值类型',
      minWidth: 90,
      formatter: ({ row }) =>
        optionLabel(attributeValueTypeOptions, row.valueType),
    },
    {
      field: 'valueOptions',
      title: '可选值',
      minWidth: 220,
      formatter: ({ row }) =>
        row.valueOptions?.length ? row.valueOptions.join('、') : '-',
    },
    { field: 'unit', title: '单位', minWidth: 70 },
    {
      field: 'requiredFlag',
      title: '必填',
      minWidth: 80,
      slots: {
        default: ({ row }) =>
          h(Tag, { color: row.requiredFlag ? 'red' : 'default' }, () =>
            row.requiredFlag ? '必填' : '可选',
          ),
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      slots: {
        default: ({ row }) =>
          tag(
            statusOptions,
            row.status,
            row.status === 0 ? 'green' : 'default',
          ),
      },
    },
    { field: 'sort', title: '排序', minWidth: 70 },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

export function useCustomTagFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'auditStatus',
      label: '审核状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: customTagAuditStatusOptions,
        placeholder: '全部状态',
      },
    },
    {
      fieldName: 'tagName',
      label: '标签',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '输入标签名称' },
    },
    {
      fieldName: 'productName',
      label: '商品名称',
      component: 'Input',
      componentProps: { allowClear: true, placeholder: '输入商品名称' },
    },
    {
      fieldName: 'merchantId',
      label: '商家ID',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1, placeholder: '商家ID' },
    },
  ];
}

export function useCustomTagReviewFormSchema(): VbenFormSchema[] {
  return [
    hiddenIdField(),
    {
      fieldName: 'action',
      label: '审核结果',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        optionType: 'button',
        options: [
          { label: '通过', value: 'APPROVE' },
          { label: '驳回', value: 'REJECT' },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'comment',
      label: '审核意见',
      component: 'Textarea',
      componentProps: {
        maxlength: 512,
        rows: 3,
        placeholder: '驳回时必须填写原因',
      },
      dependencies: {
        triggerFields: ['action'],
        rules: (values) => (values.action === 'REJECT' ? 'required' : null),
      },
    },
  ];
}

export function useCustomTagColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'tagName',
      title: '标签',
      minWidth: 120,
      slots: {
        default: ({ row }) => h(Tag, { color: 'blue' }, () => row.tagName),
      },
    },
    { field: 'productName', title: '商品名称', minWidth: 180 },
    { field: 'productId', title: '商品ID', minWidth: 90 },
    { field: 'merchantId', title: '商家ID', minWidth: 90 },
    { field: 'reviewNote', title: '商家说明', minWidth: 200 },
    {
      field: 'auditStatus',
      title: '审核状态',
      minWidth: 100,
      slots: {
        default: ({ row }) =>
          tag(
            customTagAuditStatusOptions,
            row.auditStatus,
            { 10: 'orange', 30: 'green', 40: 'red' }[
              row.auditStatus as number
            ] ?? 'blue',
          ),
      },
    },
    { field: 'auditComment', title: '审核意见', minWidth: 180 },
    {
      field: 'auditTime',
      title: '审核时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '提交时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
