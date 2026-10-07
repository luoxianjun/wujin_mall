import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDate } from '@vben/utils';

import { requestClient } from '#/api/request';

/** 投票类型选项 */
export const voteTypeOptions = [
  { label: '单选', value: 0 },
  { label: '多选', value: 1 },
  { label: '排序', value: 2 },
];

/** 状态选项 */
export const statusOptions = [
  { label: '启用', value: 0 },
  { label: '禁用', value: 1 },
];

/** 审核状态选项 */
export const auditStatusOptions = [
  { label: '待审核', value: 0 },
  { label: '通过', value: 1 },
  { label: '拒绝', value: 2 },
];

/** 获取投票分类的活动列表 */
export async function getVoteActivityOptions() {
  const list = await requestClient.get<Array<{ id: number; title: string }>>(
    '/forum/activity/simple-list',
    { params: { category: 13 } },
  );
  return (list || []).map((item) => ({
    label: item.title,
    value: item.id,
  }));
}

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'voteType',
      label: '投票类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        allowClear: true,
        options: voteTypeOptions,
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
      field: 'activityId',
      title: '活动ID',
      minWidth: 100,
    },
    {
      field: 'voteType',
      title: '投票类型',
      minWidth: 100,
      formatter: ({ row }) =>
        voteTypeOptions.find((i) => i.value === row.voteType)?.label || '-',
    },
    {
      field: 'endTime',
      title: '截止时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.endTime ? formatDate(row.endTime, 'YYYY-MM-DD HH:mm') : '-',
    },
    {
      field: 'voterCount',
      title: '参与人数',
      minWidth: 100,
    },
    {
      field: 'anonymous',
      title: '匿名投票',
      minWidth: 100,
      formatter: ({ row }) => (row.anonymous ? '是' : '否'),
    },
    {
      field: 'showRealtimeResult',
      title: '实时结果',
      minWidth: 100,
      formatter: ({ row }) => (row.showRealtimeResult ? '显示' : '隐藏'),
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
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 投票活动表单 */
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
        placeholder: '请选择投票类型的活动',
        showSearch: true,
        filterOption: (input: string, option: any) =>
          (option?.label ?? '').toLowerCase().includes(input.toLowerCase()),
      },
      rules: 'required',
    },
    {
      fieldName: 'voteType',
      label: '投票类型',
      component: 'Select',
      componentProps: {
        options: voteTypeOptions,
        placeholder: '请选择投票类型',
      },
      rules: 'required',
      defaultValue: 0,
    },
    {
      fieldName: 'maxChoices',
      label: '最多可选数',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1, placeholder: '多选时最多可选数量' },
      dependencies: {
        triggerFields: ['voteType'],
        show: (values) => values.voteType === 1,
      },
      defaultValue: 3,
    },
    {
      fieldName: 'endTime',
      label: '截止时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        valueFormat: 'x',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '投票截止时间',
      },
    },
    {
      fieldName: 'anonymous',
      label: '匿名投票',
      component: 'Switch',
      defaultValue: true,
    },
    {
      fieldName: 'showRealtimeResult',
      label: '显示实时结果',
      component: 'Switch',
      defaultValue: true,
    },
    {
      fieldName: 'allowUserAddOption',
      label: '允许用户添加选项',
      component: 'Switch',
      defaultValue: false,
    },
    {
      fieldName: 'requireRealName',
      label: '要求实名参与',
      component: 'Switch',
      defaultValue: false,
    },
    {
      fieldName: 'maxOptions',
      label: '选项数量上限',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 2, max: 50 },
      defaultValue: 10,
    },
    {
      fieldName: 'minParticipants',
      label: '最低参与人数',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0, placeholder: '0=不限制' },
      defaultValue: 0,
    },
    {
      fieldName: 'allowComment',
      label: '允许评论',
      component: 'Switch',
      defaultValue: true,
    },
    {
      fieldName: 'votesPerUser',
      label: '每人票数',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      defaultValue: 1,
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
