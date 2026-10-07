import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

export const periodTypeOptions = [
  { label: '周', value: 1 },
  { label: '月', value: 2 },
];

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'periodType',
      label: '周期类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择周期类型',
        allowClear: true,
        options: periodTypeOptions,
      },
    },
  ];
}

/** 表单 schema */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'periodType',
      label: '周期类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择周期类型',
        options: periodTypeOptions,
      },
      rules: 'required',
    },
    {
      fieldName: 'minDays',
      label: '最小连续天数',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'maxDays',
      label: '最大连续天数',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 1 },
      rules: 'required',
    },
    {
      fieldName: 'points',
      label: '奖励积分',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      rules: 'required',
    },
  ];
}

/** 列表列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 70 },
    {
      field: 'periodType',
      title: '周期',
      minWidth: 80,
      formatter: ({ row }) => (row.periodType === 1 ? '周' : row.periodType === 2 ? '月' : row.periodType),
    },
    {
      field: 'range',
      title: '连续天数',
      minWidth: 140,
      formatter: ({ row }) => `${row.minDays ?? ''} ~ ${row.maxDays ?? ''} 天`,
    },
    { field: 'points', title: '奖励积分', minWidth: 100 },
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
