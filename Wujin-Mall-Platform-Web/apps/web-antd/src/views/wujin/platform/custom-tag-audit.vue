<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { Page, useVbenModal } from '@vben/common-ui';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getProductCustomTagList } from '#/api/wujin/platform';

import { useCustomTagColumns, useCustomTagFormSchema } from './data';
import CustomTagReviewForm from './modules/custom-tag-review-form.vue';

defineOptions({ name: 'WujinPlatformCustomTagAudit' });

const [ReviewModal, reviewModalApi] = useVbenModal({
  connectedComponent: CustomTagReviewForm,
  destroyOnClose: true,
});

function onRefresh() {
  gridApi.query();
}

function handleReview(row: WujinPlatformApi.ProductCustomTag) {
  reviewModalApi.setData(row).open();
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useCustomTagFormSchema(),
  },
  gridOptions: {
    columns: useCustomTagColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params, formValues) =>
          await getProductCustomTagList(formValues),
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<WujinPlatformApi.ProductCustomTag>,
});
</script>

<template>
  <Page auto-content-height>
    <ReviewModal @success="onRefresh" />
    <Grid table-title="商品自定义标签审核">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '审核',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['wujin:product-custom-tag:review'],
              ifShow: row.auditStatus === 10,
              onClick: handleReview.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
