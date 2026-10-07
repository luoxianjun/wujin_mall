import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { formatDate } from '@vben/utils';

import { Image, Switch } from 'ant-design-vue';

import { updateBannerStatus } from '#/api/forum/banner';
import { DICT_TYPE, getDictLabel, getDictOptions } from '#/utils';

/** Banner 状态选项 */
export const bannerStatusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 },
];

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'title',
      label: '标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标题',
        allowClear: true,
      },
    },
    {
      fieldName: 'targetType',
      label: '跳转类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择跳转类型',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.FORUM_BANNER_TARGET_TYPE, 'number'),
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: bannerStatusOptions,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onRefresh: () => void,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: 'ID',
      minWidth: 80,
    },
    {
      field: 'imageUrl',
      title: '图片',
      minWidth: 120,
      slots: {
        default: ({ row }) => {
          return row.imageUrl
            ? h(Image, {
                src: row.imageUrl,
                width: 80,
                height: 40,
                style: { objectFit: 'cover', borderRadius: '4px' },
              })
            : '-';
        },
      },
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'targetType',
      title: '跳转类型',
      minWidth: 120,
      formatter: ({ row }) =>
        getDictLabel(DICT_TYPE.FORUM_BANNER_TARGET_TYPE, row.targetType) || '-',
    },
    {
      field: 'target',
      title: '跳转目标',
      minWidth: 180,
      showOverflow: 'tooltip',
      slots: {
        default: ({ row }) => {
          // 外部链接使用 targetUrl，其他使用 targetId
          if (row.targetType === 4) {
            return row.targetUrl || '-';
          }
          return row.targetId || '-';
        },
      },
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      slots: {
        default: ({ row }) => {
          return h(Switch, {
            checked: row.status === 1,
            checkedChildren: '启用',
            unCheckedChildren: '禁用',
            onChange: async (checked: boolean) => {
              await updateBannerStatus(row.id, checked ? 1 : 0);
              row.status = checked ? 1 : 0;
              onRefresh();
            },
          });
        },
      },
    },
    {
      field: 'startTime',
      title: '生效时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.startTime ? formatDate(row.startTime, 'YYYY-MM-DD HH:mm') : '-',
    },
    {
      field: 'endTime',
      title: '失效时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.endTime ? formatDate(row.endTime, 'YYYY-MM-DD HH:mm') : '-',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 表单配置 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'title',
      label: '标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 Banner 标题',
      },
      rules: 'required',
    },
    {
      fieldName: 'imageUrl',
      label: '图片',
      component: 'ImageUpload',
      componentProps: {
        maxNumber: 1,
        multiple: false,
        helpText: '支持 jpg/png，建议尺寸 750x300',
      },
      rules: 'required',
    },
    {
      fieldName: 'targetType',
      label: '跳转类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择跳转类型',
        options: getDictOptions(DICT_TYPE.FORUM_BANNER_TARGET_TYPE, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'targetId',
      label: '目标ID',
      component: 'Input',
      componentProps: {
        placeholder: '请输入帖子ID/活动ID/用户ID',
      },
      dependencies: {
        triggerFields: ['targetType'],
        show: (values) =>
          values.targetType && [1, 2, 3].includes(values.targetType),
        rules: (values) => {
          if ([1, 2, 3].includes(values.targetType)) {
            return 'required';
          }
          return undefined;
        },
      },
    },
    {
      fieldName: 'targetUrl',
      label: '跳转链接',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部链接地址，如 https://www.example.com',
      },
      dependencies: {
        triggerFields: ['targetType'],
        show: (values) => values.targetType === 4,
        rules: (values) => {
          if (values.targetType === 4) {
            return 'required';
          }
          return undefined;
        },
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        class: 'w-full',
        min: 0,
        placeholder: '排序值越大越靠前',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Switch',
      componentProps: {
        checkedChildren: '启用',
        unCheckedChildren: '禁用',
        checkedValue: 1,
        unCheckedValue: 0,
        style: {
          width: '80px',
        },
      },
      defaultValue: 1,
    },
    {
      fieldName: 'startTime',
      label: '生效时间',
      component: 'DatePicker',
      componentProps: {
        showTime: {
          format: 'HH:mm',
        },
        valueFormat: 'YYYY-MM-DD HH:mm',
        placeholder: '不填表示立即生效',
      },
    },
    {
      fieldName: 'endTime',
      label: '失效时间',
      component: 'DatePicker',
      componentProps: {
        showTime: {
          format: 'HH:mm',
        },
        valueFormat: 'YYYY-MM-DD HH:mm',
        placeholder: '不填表示永久有效',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 2,
      },
    },
  ];
}
