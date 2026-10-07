import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDate } from '@vben/utils';

import { DICT_TYPE, getDictOptions } from '#/utils';

export const postStatusOptions = [
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已驳回', value: 2 },
];

export const orderByOptions = [
  { label: '最新', value: 1 },
  { label: '热度', value: 2 },
];

export interface PostGridFormSchemaOptions {
  getUserSelectProps?: () => Record<string, any>;
}

/** 列表的搜索表单 */
export function useGridFormSchema(
  options: PostGridFormSchemaOptions = {},
): VbenFormSchema[] {
  return [
    {
      fieldName: 'keyword',
      label: '关键字',
      component: 'Input',
      componentProps: {
        placeholder: '标题/内容模糊搜索',
        allowClear: true,
      },
    },
    {
      fieldName: 'category',
      label: '话题',
      component: 'Select',
      componentProps: {
        placeholder: '请选择话题',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.FRUM_TOPIC_TYPE, 'number'),
      },
    },
    {
      fieldName: 'status',
      label: '审核状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: postStatusOptions,
      },
    },
    {
      fieldName: 'userId',
      label: '用户',
      component: 'Select',
      componentProps: () => ({
        placeholder: '请选择用户',
        allowClear: true,
        showSearch: true,
        filterOption: false,
        ...options.getUserSelectProps?.(),
      }),
    },
    {
      fieldName: 'orderBy',
      label: '排序',
      component: 'Select',
      componentProps: {
        placeholder: '请选择排序',
        allowClear: true,
        options: orderByOptions,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: '帖子ID', minWidth: 90 },
    {
      field: 'uid',
      title: '用户UID',
      minWidth: 120,
      formatter: ({ row }) => row.uid || '-',
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'categoryName',
      title: '话题',
      minWidth: 170,
      slots: { default: 'topic' },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      formatter: ({ row }) => {
        const map: Record<number, string> = {
          0: '待审核',
          1: '已通过',
          2: '已驳回',
        };
        return map[row.status as number] ?? row.status ?? '-';
      },
    },
    {
      field: 'isTop',
      title: '置顶',
      minWidth: 80,
      formatter: ({ row }) => (row.isTop ? '是' : '否'),
    },
    {
      field: 'likeCount',
      title: '点赞',
      minWidth: 90,
    },
    {
      field: 'commentCount',
      title: '评论',
      minWidth: 90,
    },
    {
      field: 'viewCount',
      title: '浏览',
      minWidth: 90,
    },
    {
      field: 'latestCommentTime',
      title: '最近评论',
      minWidth: 170,
      formatter: 'formatDateTime',
    },
    {
      field: 'isAdminPost',
      title: '是否管理员帖子',
      minWidth: 130,
      formatter: ({ row }) => (row.isAdminPost ? '是' : '否'),
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 170,
      formatter: ({ row }) =>
        row.createTime ? formatDate(row.createTime, 'YYYY-MM-DD HH:mm:ss') : '',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
