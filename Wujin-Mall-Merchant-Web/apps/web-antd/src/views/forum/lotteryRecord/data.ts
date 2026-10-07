import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDate } from '@vben/utils';

/** 奖品类型选项 */
export const prizeTypeOptions = [
  { label: '积分', value: 0 },
  { label: '优惠券', value: 1 },
  { label: '实物', value: 2 },
  { label: '虚拟物品', value: 3 },
  { label: '谢谢参与', value: 4 },
];

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'lotteryActivityId',
      label: '抽奖活动ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入活动ID',
        allowClear: true,
        class: 'w-full',
      },
    },
    {
      fieldName: 'userId',
      label: '用户ID',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入用户ID',
        allowClear: true,
        class: 'w-full',
      },
    },
    {
      fieldName: 'won',
      label: '是否中奖',
      component: 'Select',
      componentProps: {
        placeholder: '全部',
        allowClear: true,
        options: [
          { label: '中奖', value: true },
          { label: '未中奖', value: false },
        ],
      },
    },
    {
      fieldName: 'delivered',
      label: '发放状态',
      component: 'Select',
      componentProps: {
        placeholder: '全部',
        allowClear: true,
        options: [
          { label: '已发放', value: true },
          { label: '未发放', value: false },
        ],
      },
    },
  ];
}

/** 列表字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'activityName', title: '抽奖活动', minWidth: 160, showOverflow: 'tooltip' },
    { field: 'userNickname', title: '用户', minWidth: 150, showOverflow: 'tooltip' },
    { field: 'prizeName', title: '奖品名称', minWidth: 150 },
    {
      field: 'prizeType',
      title: '奖品类型',
      minWidth: 110,
      formatter: ({ row }) =>
        prizeTypeOptions.find((i) => i.value === row.prizeType)?.label || '-',
    },
    {
      field: 'won',
      title: '是否中奖',
      minWidth: 90,
      formatter: ({ row }) => (row.won ? '✅ 中奖' : '❌ 未中奖'),
    },
    {
      field: 'drawTime',
      title: '抽奖时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.drawTime ? formatDate(row.drawTime, 'YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      field: 'delivered',
      title: '发放状态',
      minWidth: 100,
      formatter: ({ row }) => (row.delivered ? '已发放' : '未发放'),
    },
    {
      field: 'deliveryAddress',
      title: '收货地址',
      minWidth: 200,
      showOverflow: true,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
