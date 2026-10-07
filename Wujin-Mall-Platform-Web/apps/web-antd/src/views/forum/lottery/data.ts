import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDate } from '@vben/utils';

import { requestClient } from '#/api/request';

/** 抽奖类型选项 */
export const lotteryTypeOptions = [
  { label: '定时开奖', value: 0 },
  { label: '即时摇一摇', value: 1 },
];

/** 费用类型选项 */
export const costTypeOptions = [
  { label: '免费', value: 0 },
  { label: '积分', value: 1 },
];

/** 参与条件选项 */
export const participationOptions = [
  { label: '所有人', value: 0 },
  { label: '已报名', value: 1 },
  { label: '受邀人群', value: 2 },
];

/** 状态选项 */
export const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 },
];

/** 奖品类型选项 */
export const prizeTypeOptions = [
  { label: '积分', value: 0 },
  { label: '优惠券', value: 1 },
  { label: '实物', value: 2 },
  { label: '虚拟物品', value: 3 },
  { label: '谢谢参与', value: 4 },
];

/** 获取抽奖分类的活动列表 */
export async function getLotteryActivityOptions() {
  const list = await requestClient.get<Array<{ id: number; title: string }>>(
    '/forum/activity/simple-list',
    { params: { category: 11 } },
  );
  return (list || []).map((item) => ({
    label: item.title,
    value: item.id,
  }));
}

/** 获取全部奖品列表（用于活动表单选择） */
export async function getPrizeSimpleList() {
  const list = await requestClient.get<
    Array<{
      id: number;
      name: string;
      type: number;
      totalStock: number;
      remainingStock: number;
    }>
  >('/gamification/lottery/prize/simple-list');
  return (list || []).map((item) => ({
    label: `${item.name} (${prizeTypeOptions.find((p) => p.value === item.type)?.label || ''}, 库存:${item.remainingStock ?? item.totalStock})`,
    value: item.id,
  }));
}

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'type',
      label: '抽奖类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        allowClear: true,
        options: lotteryTypeOptions,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: statusOptions,
      },
    },
  ];
}

/** 列表字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    {
      field: 'type',
      title: '抽奖类型',
      minWidth: 120,
      formatter: ({ row }) =>
        lotteryTypeOptions.find((i) => i.value === row.type)?.label || '-',
    },
    {
      field: 'costType',
      title: '费用',
      minWidth: 100,
      formatter: ({ row }) => {
        if (row.costType === 1) return `${row.costAmount || 0} 积分`;
        return '免费';
      },
    },
    {
      field: 'maxDrawsPerDay',
      title: '每日上限',
      minWidth: 100,
      formatter: ({ row }) => row.maxDrawsPerDay || '不限',
    },
    {
      field: 'maxDrawsTotal',
      title: '总上限',
      minWidth: 100,
      formatter: ({ row }) => row.maxDrawsTotal || '不限',
    },
    {
      field: 'guaranteeDraws',
      title: '保底次数',
      minWidth: 100,
      formatter: ({ row }) => row.guaranteeDraws || '无',
    },
    {
      field: 'participationCondition',
      title: '参与条件',
      minWidth: 120,
      formatter: ({ row }) =>
        participationOptions.find((i) => i.value === row.participationCondition)
          ?.label || '-',
    },
    {
      field: 'drawTime',
      title: '开奖时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.drawTime ? formatDate(row.drawTime, 'YYYY-MM-DD HH:mm') : '-',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      formatter: ({ row }) =>
        statusOptions.find((i) => i.value === row.status)?.label || '-',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 抽奖活动表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'activityId',
      label: '关联活动',
      component: 'Select',
      componentProps: {
        class: 'w-full',
        placeholder: '请选择抽奖类型的活动',
        showSearch: true,
        filterOption: (input: string, option: any) =>
          (option?.label ?? '').toLowerCase().includes(input.toLowerCase()),
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '抽奖类型',
      component: 'Select',
      componentProps: {
        options: lotteryTypeOptions,
        placeholder: '请选择抽奖类型',
      },
      rules: 'required',
    },
    {
      fieldName: 'drawTime',
      label: '开奖时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'x',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '定时开奖时间',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 0,
      },
    },
    {
      fieldName: 'costType',
      label: '费用类型',
      component: 'Select',
      componentProps: {
        options: costTypeOptions,
        placeholder: '请选择费用类型',
      },
      defaultValue: 0,
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 1,
      },
    },
    {
      fieldName: 'costAmount',
      label: '积分消耗',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '每次消耗积分' },
      dependencies: {
        triggerFields: ['costType', 'type'],
        show: (values) => values.type === 1 && values.costType === 1,
      },
    },
    {
      fieldName: 'maxDrawsPerDay',
      label: '每日上限',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '0=不限' },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 1,
      },
    },
    {
      fieldName: 'maxDrawsTotal',
      label: '总上限',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '0=不限' },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 1,
      },
    },
    {
      fieldName: 'guaranteeDraws',
      label: '保底次数',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        min: 0,
        placeholder: '0=不保底，连续N次未中奖则保底中奖',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 1,
      },
    },
    {
      fieldName: 'participationCondition',
      label: '参与条件',
      component: 'Select',
      componentProps: {
        options: participationOptions,
        placeholder: '请选择参与条件',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: { options: statusOptions },
      defaultValue: 0,
    },
  ];
}
