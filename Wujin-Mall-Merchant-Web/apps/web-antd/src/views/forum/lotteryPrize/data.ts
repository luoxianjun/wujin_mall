import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { Image } from 'ant-design-vue';

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
      fieldName: 'name',
      label: '奖品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入奖品名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '奖品类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        allowClear: true,
        options: prizeTypeOptions,
      },
    },
  ];
}

/** 列表字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', minWidth: 80 },
    { field: 'name', title: '奖品名称', minWidth: 150 },
    {
      field: 'imageUrl',
      title: '奖品图片',
      minWidth: 100,
      slots: {
        default: ({ row }) => {
          return row.imageUrl
            ? h(Image, {
                src: row.imageUrl,
                width: 60,
                height: 60,
                style: { objectFit: 'cover', borderRadius: '6px' },
              })
            : '-';
        },
      },
    },
    {
      field: 'type',
      title: '奖品类型',
      minWidth: 110,
      formatter: ({ row }) =>
        prizeTypeOptions.find((i) => i.value === row.type)?.label || '-',
    },
    { field: 'value', title: '积分数量', minWidth: 100 },
    { field: 'totalStock', title: '总库存', minWidth: 90 },
    { field: 'remainingStock', title: '剩余库存', minWidth: 100 },
    { field: 'sortOrder', title: '排序', minWidth: 70 },
    {
      field: 'requireAddress',
      title: '需要地址',
      minWidth: 100,
      formatter: ({ row }) => (row.requireAddress ? '是' : '否'),
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

/** 奖品表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'name',
      label: '奖品名称',
      component: 'Input',
      componentProps: { placeholder: '请输入奖品名称' },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '奖品类型',
      component: 'Select',
      componentProps: {
        options: prizeTypeOptions,
        placeholder: '请选择奖品类型',
      },
      rules: 'required',
    },
    {
      fieldName: 'value',
      label: '积分数量',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        min: 0,
        placeholder: '积分类型时填写',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => values.type === 0,
      },
    },
    {
      fieldName: 'imageUrl',
      label: '奖品图片',
      component: 'ImageUpload',
      componentProps: {
        maxNumber: 1,
        multiple: false,
        helpText: '支持 jpg/png，建议尺寸 200x200',
      },
    },
    {
      fieldName: 'totalStock',
      label: '总库存',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      rules: 'required',
    },
    {
      fieldName: 'sortOrder',
      label: '排序',
      component: 'InputNumber',
      componentProps: { class: 'w-full', min: 0 },
      defaultValue: 0,
    },
    {
      fieldName: 'requireAddress',
      label: '需要地址',
      component: 'Switch',
      defaultValue: false,
    },
  ];
}
