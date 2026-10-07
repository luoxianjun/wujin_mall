<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberUserApi } from '#/api/member/user';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { DocAlert, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message, Switch } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { setUserAdmin } from '#/api/forum/userProfile';
import { exportMemberUser, getUserPage } from '#/api/member/user';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import BalanceForm from './modules/balance-form.vue';
import Form from './modules/form.vue';
import LeavelForm from './modules/leavel-form.vue';
import PointForm from './modules/point-form.vue';

const router = useRouter();
const { hasAccessByCodes } = useAccess();

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [PointFormModal, pointFormModalApi] = useVbenModal({
  connectedComponent: PointForm,
  destroyOnClose: true,
});

const [BalanceFormModal] = useVbenModal({
  connectedComponent: BalanceForm,
  destroyOnClose: true,
});

const [LeavelFormModal] = useVbenModal({
  connectedComponent: LeavelForm,
  destroyOnClose: true,
});

/** 刷新表格数据 */
function onRefresh() {
  gridApi.query();
}

/** 设置选中 ID */
const checkedIds = ref<number[]>([]);
function setCheckedIds({ records }: { records: MemberUserApi.User[] }) {
  checkedIds.value = records.map((item) => item.id as number);
}

/** 发送优惠券 */
function handleSendCoupon() {
  formModalApi.setData(null).open();
}

/** 导出会员列表 */
const exporting = ref(false);
async function handleExport() {
  try {
    exporting.value = true;
    const formValues = await gridApi.formApi?.getValues();
    const data = await exportMemberUser(formValues);
    downloadFileFromBlobPart({
      fileName: '会员用户列表.xlsx',
      source: data,
    });
    message.success('导出成功');
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败');
  } finally {
    exporting.value = false;
  }
}

/** 编辑会员 */
function handleEdit(row: MemberUserApi.User) {
  formModalApi.setData(row).open();
}

/** 修改会员等级 */

/** 修改会员积分 */
function handleUpdatePoint(row: MemberUserApi.User) {
  pointFormModalApi.setData(row).open();
}

/** 修改会员余额 */

/** 查看会员详情 */
function handleViewDetail(row: MemberUserApi.User) {
  router.push({
    name: 'MemberUserDetail',
    query: {
      id: row.id,
    },
  });
}

/** 设置论坛管理员 */
async function handleSetAdmin(row: MemberUserApi.User, checked: boolean) {
  row.loading = true;
  try {
    await setUserAdmin({ userId: row.id as number, isAdmin: checked });
    row.isAdmin = checked;
    message.success(checked ? '已设为论坛管理员' : '已取消论坛管理员');
  } catch {
    message.error('操作失败');
  } finally {
    row.loading = false;
  }
}

// 表格实例
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    checkboxConfig: {
      highlight: true,
      labelField: 'checkbox',
    },
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getUserPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
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
  } as VxeTableGridOptions<MemberUserApi.User>,
  gridEvents: {
    checkboxAll: setCheckedIds,
    checkboxChange: setCheckedIds,
  },
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert
        title="会员用户、标签、分组"
        url="https://doc.iocoder.cn/member/user/"
      />
    </template>

    <FormModal @success="onRefresh" />
    <PointFormModal @success="onRefresh" />
    <BalanceFormModal @success="onRefresh" />
    <LeavelFormModal @success="onRefresh" />
    <Grid table-title="会员列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '发送优惠券',
              type: 'primary',
              icon: 'lucide:mouse-pointer-2',
              auth: ['promotion:coupon:send'],
              onClick: handleSendCoupon,
            },
            {
              label: '导出',
              type: 'primary',
              icon: 'lucide:download',
              auth: ['member:user:export'],
              loading: exporting,
              onClick: handleExport,
            },
          ]"
        />
      </template>

      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              onClick: handleViewDetail.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              auth: ['member:user:update'],
              onClick: handleEdit.bind(null, row),
            },
            // {
            //   label: '修改等级',
            //   type: 'link',
            //   auth: ['member:user:update-level'],
            //   onClick: handleUpdateLevel.bind(null, row),
            // },
            {
              label: '修改积分',
              type: 'link',
              auth: ['forum:point-record:change'],
              onClick: handleUpdatePoint.bind(null, row),
            },
            // {
            //   label: '修改余额',
            //   type: 'link',
            //   auth: ['pay:wallet:update-balance'],
            //   onClick: handleUpdateBalance.bind(null, row),
            // },
          ]"
        />
      </template>

      <template #isAdmin="{ row }">
        <Switch
          :checked="row.isAdmin"
          :disabled="!hasAccessByCodes(['forum:user-profile:update'])"
          checked-children="是"
          un-checked-children="否"
          :loading="row.loading"
          @change="(checked) => handleSetAdmin(row, checked === true)"
        />
      </template>
    </Grid>
  </Page>
</template>
