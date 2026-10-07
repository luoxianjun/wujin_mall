<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InvitationApi } from '#/api/gamification/invitation';

import { Page } from '@vben/common-ui';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getInvitationDetailPage } from '#/api/gamification/invitation';

import {
  buildInvitationDetailPageParams,
  useGridColumns,
  useGridFormSchema,
} from './data';

defineOptions({ name: 'InvitationDetails' });

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getInvitationDetailPage(
            buildInvitationDetailPageParams(page, formValues),
          );
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
  } as VxeTableGridOptions<InvitationApi.InvitationDetail>,
});
</script>

<template>
  <Page
    auto-content-height
    content-class="flex flex-col gap-4"
    description="查看邀请关系、状态、奖励和认证完成时间。"
    title="邀请明细"
  >
    <Grid table-title="邀请明细列表" />
  </Page>
</template>
