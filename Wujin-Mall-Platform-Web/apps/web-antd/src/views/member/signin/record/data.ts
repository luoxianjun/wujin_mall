import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { Tag } from 'ant-design-vue';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'uid',
      label: '用户UID',
      component: 'Input',
    },
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        style: { width: '100%' },
      },
    },
    {
      fieldName: 'dateRange',
      label: '签到日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
    },
    {
      field: 'userId',
      title: '用户编号',
    },
    {
      field: 'uid',
      title: '用户UID',
    },
    {
      field: 'nickname',
      title: '昵称',
    },
    {
      field: 'signDate',
      title: '签到日期',
      formatter: 'formatDate',
    },
    {
      field: 'point',
      title: '获得积分',
      slots: {
        default: ({ row }) => {
          return h(
            Tag,
            {
              class: 'mr-5px',
              color: row.point > 0 ? 'blue' : 'red',
            },
            () => (row.point > 0 ? `+${row.point}` : row.point),
          );
        },
      },
    },
    {
      field: 'continuousDays',
      title: '连续签到天数',
    },
    {
      field: 'remark',
      title: '备注',
    },
    {
      field: 'createTime',
      title: '签到时间',
      formatter: 'formatDateTime',
    },
  ];
}
