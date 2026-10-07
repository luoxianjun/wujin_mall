import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InvitationApi } from '#/api/gamification/invitation';

import { h } from 'vue';

import { Tag } from 'ant-design-vue';

import { getRangePickerDefaultProps } from '#/utils';

type InvitationDetailFormValues = {
  inviteeNickname?: string;
  inviterNickname?: string;
  registerTime?: [string, string] | string[];
  status?: number;
};

type PaginationParams = {
  currentPage: number;
  pageSize: number;
};

const statusMap: Record<number, { color: string; text: string }> = {
  1: { color: 'orange', text: '待完成' },
  2: { color: 'green', text: '已完成' },
  3: { color: 'red', text: '已失效' },
};

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'inviterNickname',
      label: '邀请人',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入邀请人昵称',
      },
    },
    {
      fieldName: 'inviteeNickname',
      label: '被邀请人',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入被邀请人昵称',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: '待完成', value: 1 },
          { label: '已完成', value: 2 },
          { label: '已失效', value: 3 },
        ],
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'registerTime',
      label: '注册时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: 'ID', width: 80 },
    { field: 'inviterNickname', title: '邀请人', width: 150 },
    { field: 'inviteeNickname', title: '被邀请人', width: 150 },
    { field: 'invitationCode', title: '邀请码', width: 140 },
    {
      field: 'status',
      title: '状态',
      width: 120,
      slots: {
        default: ({ row }: { row: InvitationApi.InvitationDetail }) => {
          const status = statusMap[row.status ?? 0] || {
            color: 'default',
            text: '未知',
          };
          return h(Tag, { color: status.color }, () => status.text);
        },
      },
    },
    {
      field: 'inviterRewardPoints',
      title: '邀请人奖励',
      width: 140,
    },
    {
      field: 'inviteeRewardPoints',
      title: '被邀请人奖励',
      width: 140,
    },
    {
      field: 'registerTime',
      title: '注册时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'verifiedTime',
      title: '认证完成时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  ];
}

export function buildInvitationDetailPageParams(
  page: PaginationParams,
  formValues: InvitationDetailFormValues = {},
): InvitationApi.InvitationDetailPageReq {
  const [beginTime, endTime] = formValues.registerTime || [];

  return {
    pageNo: page.currentPage,
    pageSize: page.pageSize,
    inviterNickname: formValues.inviterNickname,
    inviteeNickname: formValues.inviteeNickname,
    status: formValues.status,
    beginTime,
    endTime,
  };
}
