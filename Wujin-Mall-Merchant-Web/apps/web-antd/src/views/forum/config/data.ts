import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { formatDate } from '@vben/utils';

import { Tag, Tooltip } from 'ant-design-vue';

/** 配置类型选项 */
export const configTypeOptions = [
  { label: '文本', value: 'text' },
  { label: '富文本', value: 'richtext' },
  { label: '图片', value: 'image' },
  { label: '链接', value: 'url' },
  { label: 'JSON', value: 'json' },
];

/** 配置类型标签颜色 */
const typeColorMap: Record<string, string> = {
  text: 'blue',
  richtext: 'green',
  image: 'purple',
  url: 'orange',
  json: 'cyan',
};

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '配置名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'configKey',
      label: '配置键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置键',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        allowClear: true,
        options: configTypeOptions,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: 'ID',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '配置名称',
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'configKey',
      title: '配置键',
      minWidth: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'type',
      title: '类型',
      minWidth: 100,
      slots: {
        default: ({ row }) => {
          const type = row.type || 'text';
          const option = configTypeOptions.find((o) => o.value === type);
          return h(
            Tag,
            { color: typeColorMap[type] || 'default' },
            () => option?.label || type,
          );
        },
      },
    },
    {
      field: 'configValue',
      title: '配置值',
      minWidth: 200,
      slots: {
        default: ({ row }) => {
          const value = row.configValue || '';
          const displayValue =
            value.length > 50 ? `${value.slice(0, 50)}...` : value;
          return h(
            Tooltip,
            { title: value.length > 50 ? value : undefined },
            () => (row.type === 'richtext' ? '[富文本内容]' : displayValue),
          );
        },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 160,
      showOverflow: 'tooltip',
    },
    {
      field: 'updateTime',
      title: '更新时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.updateTime ? formatDate(row.updateTime, 'YYYY-MM-DD HH:mm:ss') : '-',
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
      fieldName: 'name',
      label: '配置名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置名称，如：福利群内容',
      },
      rules: 'required',
    },
    {
      fieldName: 'key',
      label: '配置键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置键，如：forum.welfare.content',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择配置类型',
        options: configTypeOptions,
      },
      defaultValue: 'text',
      rules: 'required',
    },
    {
      fieldName: 'value',
      label: '配置值',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入配置值',
        rows: 4,
      },
      dependencies: {
        triggerFields: ['type', 'name', 'key'],
        show: (values) => {
          // 如果是富文本类型，不显示普通文本域
          if (values.type === 'richtext') return false;
          // 如果是福利群配置，不显示普通文本域（应该使用富文本编辑器）
          const name = values.name || '';
          const key = values.key || '';
          if (name.includes('福利群') || key.includes('welfare')) {
            return false;
          }
          return true;
        },
      },
    },
    {
      fieldName: 'value',
      label: '配置值',
      component: 'RichTextarea',
      componentProps: {
        placeholder: '请输入配置值',
        height: 400,
      },
      dependencies: {
        triggerFields: ['type', 'name', 'key'],
        show: (values) => {
          // 如果是富文本类型，显示富文本编辑器
          if (values.type === 'richtext') return true;
          // 如果是福利群配置，也显示富文本编辑器
          const name = values.name || '';
          const key = values.key || '';
          if (name.includes('福利群') || key.includes('welfare')) {
            return true;
          }
          return false;
        },
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注说明',
        rows: 2,
      },
    },
  ];
}
