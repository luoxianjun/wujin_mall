<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumPointRecordApi } from '#/api/forum/point-record';
import type { MemberUserApi } from '#/api/member/user';

import { useVbenModal } from '@vben/common-ui';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getForumPointRecordPage } from '#/api/forum/point-record';
import { getUser } from '#/api/member/user';
import { DICT_TYPE, getDictOptions, getRangePickerDefaultProps } from '#/utils';
import { useGridColumns } from '#/views/member/point/record/data';
import PointForm from '../modules/point-form.vue';

const props = defineProps<{
  userId: number;
}>();

const [PointFormModal, pointFormModalApi] = useVbenModal({
  connectedComponent: PointForm,
  destroyOnClose: true,
});

/** 刷新表格数据 */
function onRefresh() {
  gridApi.query();
}

/** 修改积分 */
async function handleUpdatePoint() {
  const user = await getUser(props.userId);
  pointFormModalApi.setData(user).open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: [
      {
        fieldName: 'bizType',
        label: '业务类型',
        component: 'Select',
        componentProps: {
          allowClear: true,
          options: getDictOptions(DICT_TYPE.MEMBER_POINT_BIZ_TYPE, 'number'),
        },
      },
      {
        fieldName: 'title',
        label: '积分标题',
        component: 'Input',
      },
      {
        fieldName: 'createDate',
        label: '获得时间',
        component: 'RangePicker',
        componentProps: {
          ...getRangePickerDefaultProps(),
          allowClear: true,
        },
      },
    ],
  },
  gridOptions: {
    columns: useGridColumns(),
    keepSource: true,
    pagerConfig: {
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          const { createDate, ...rest } = formValues;
          const [startDate, endDate] = createDate || [];
          return await getForumPointRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            startDate,
            endDate,
            ...rest,
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
  } as VxeTableGridOptions<ForumPointRecordApi.PointRecord>,
  separator: false,
});
</script>

<template>
  <div>
    <PointFormModal @success="onRefresh" />
    <Grid>
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '修改积分',
              type: 'primary',
              icon: 'lucide:edit',
              auth: ['forum:point-record:change'],
              onClick: handleUpdatePoint,
            },
          ]"
        />
      </template>
    </Grid>
  </div>
</template>
