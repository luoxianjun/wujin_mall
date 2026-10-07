<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumSignRecordApi } from '#/api/forum/sign-record';

import { h } from 'vue';

import { Tag } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getForumSignRecordPage } from '#/api/forum/sign-record';
import { getRangePickerDefaultProps } from '#/utils';

const props = defineProps<{
  userId: number;
}>();

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'dateRange',
        label: '签到日期',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          allowClear: true,
        },
      },
    ],
  },
  gridOptions: {
    columns: [
      {
        field: 'id',
        title: '编号',
      },
      {
        field: 'signDate',
        title: '签到日期',
        formatter: 'formatDate',
      },
      {
        field: 'continuousDays',
        title: '连续签到天数',
      },
      {
        field: 'point',
        title: '获得积分',
        slots: {
          default: ({ row }) => {
            const point = row.point || 0;
            return h(
              Tag,
              {
                class: 'mr-5px',
                color: point > 0 ? 'blue' : 'red',
              },
              () => (point > 0 ? `+${point}` : point),
            );
          },
        },
      },
      {
        field: 'remark',
        title: '备注',
      },
      {
        field: 'createTime',
        title: '创建时间',
        formatter: 'formatDateTime',
      },
    ],
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const [startDate, endDate] = formValues.dateRange || [];
          return await getForumSignRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            startDate,
            endDate,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<ForumSignRecordApi.SignRecord>,
  separator: false,
});
</script>

<template>
  <Grid />
</template>
