<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';

import { Avatar, Switch, Tag, message } from 'ant-design-vue';
import { h } from 'vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getForumUserProfilePage, setUserAdmin } from '#/api/forum/userProfile';

defineOptions({ name: 'ForumUserProfile' });

const { hasAccessByCodes } = useAccess();

const gridOptions: VxeTableGridOptions = {
  columns: [
    { type: 'seq', width: 60, title: '序号' },
    {
      field: 'avatar',
      title: '头像',
      width: 80,
      slots: {
        default: ({ row }) => h(Avatar, { src: row.avatar, size: 36 }),
      },
    },
    { field: 'userId', title: '用户 ID', width: 100 },
    { field: 'uid', title: 'UID', width: 120 },
    { field: 'nickname', title: '昵称', minWidth: 120 },
    { field: 'campus', title: '校区', minWidth: 100 },
    { field: 'point', title: '积分', width: 80 },
    { field: 'postCount', title: '发帖数', width: 90 },
    {
      field: 'schoolEmailVerified',
      title: '邮箱认证',
      width: 100,
      slots: {
        default: ({ row }) =>
          h(
            Tag,
            { color: row.schoolEmailVerified ? 'green' : 'default' },
            () => (row.schoolEmailVerified ? '已认证' : '未认证'),
          ),
      },
    },
    {
      field: 'isAdmin',
      title: '管理员',
      width: 110,
      slots: {
        default: ({ row }) =>
          h(Switch, {
            checked: row.isAdmin,
            checkedChildren: '是',
            unCheckedChildren: '否',
            disabled: !hasAccessByCodes(['forum:user-profile:update']),
            loading: row.loading,
            onChange: async (checked) => {
              const next = checked === true;
              row.loading = true;
              try {
                await setUserAdmin({ userId: row.userId, isAdmin: next });
                row.isAdmin = next;
                message.success(next ? '已设为管理员' : '已取消管理员');
              } catch {
                message.error('操作失败');
              } finally {
                row.loading = false;
              }
            },
          }),
      },
    },
    { field: 'createTime', title: '注册时间', width: 180 },
  ],
  height: 'auto',
  keepSource: true,
  pagerConfig: {
    enabled: true,
  },
  proxyConfig: {
    ajax: {
      query: async ({ page }) => {
        const res = await getForumUserProfilePage({
          pageNo: page.currentPage,
          pageSize: page.pageSize,
        });
        return { list: res.list || [], total: res.total || 0 };
      },
    },
  },
  toolbarConfig: {
    refresh: true,
  },
};

const [Grid] = useVbenVxeGrid({ gridOptions });
</script>

<template>
  <Page auto-content-height>
    <Grid />
  </Page>
</template>
