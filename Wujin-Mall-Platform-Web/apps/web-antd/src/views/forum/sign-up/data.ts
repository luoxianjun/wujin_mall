import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

export const approvalStatusOptions = [
  { label: '待审核', value: 0 },
  { label: '通过', value: 1 },
  { label: '拒绝', value: 2 },
  { label: '已取消', value: 3 },
];

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'activityId',
      label: '活动ID',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        disabled: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'approvalStatus',
      label: '审核状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '全部状态',
        options: approvalStatusOptions,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: '报名ID', minWidth: 90 },
    {
      field: 'activityTitle',
      title: '活动标题',
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    { field: 'uid', title: '用户UID', minWidth: 120 },
    { field: 'nickname', title: '昵称', minWidth: 120 },
    {
      field: 'approvalStatusName',
      title: '审核状态',
      minWidth: 110,
      formatter: ({ row }) => row.approvalStatusName || row.approvalStatus || '-',
    },
    {
      field: 'checkedIn',
      title: '签到',
      minWidth: 90,
      formatter: ({ row }) => (row.checkedIn ? '已签到' : '未签到'),
    },
    {
      field: 'checkInTime',
      title: '签到时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '报名时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
