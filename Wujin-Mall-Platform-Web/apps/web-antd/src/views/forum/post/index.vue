<script lang="ts" setup>
import type { SelectProps } from 'ant-design-vue';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ForumPostApi } from '#/api/forum/post';
import type { ForumUserProfileApi } from '#/api/forum/userProfile';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';

import { useDebounceFn } from '@vueuse/core';
import { message, Tag } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { deletePost, getPostPage, setPostTop } from '#/api/forum/post';
import { getForumUserProfilePage } from '#/api/forum/userProfile';

import { useGridColumns, useGridFormSchema } from './data';
import { DICT_TYPE, getDictLabel } from '#/utils';
import ReviewModal from './modules/review-modal.vue';

defineOptions({ name: 'ForumPostManage' });

const router = useRouter();
const [Review, reviewModalApi] = useVbenModal({
  connectedComponent: ReviewModal,
  destroyOnClose: true,
});

// 用户选择相关状态
const userOptions = ref<SelectProps['options']>([]);
const userSearch = ref('');
const loadingUsers = ref(false);

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

/** 查看详情 */
function handleDetail(row: ForumPostApi.Post) {
  router.push({
    name: 'ForumPostDetail',
    query: { id: row.id },
  });
}

/** 复审 */
function handleReview(row: ForumPostApi.Post) {
  reviewModalApi.setData(row).open();
}

/** 置顶/取消置顶 */
async function handleSetTop(row: ForumPostApi.Post) {
  const isTop = !row.isTop;
  const actionText = isTop ? '置顶' : '取消置顶';
  try {
    await confirm({
      content: `确认${actionText}帖子「${row.title}」吗？`,
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: `正在${actionText}中`,
    key: 'action_key_msg',
  });
  try {
    await setPostTop(row.id as number, isTop);
    message.success({ content: `${actionText}成功`, key: 'action_key_msg' });
    onRefresh();
  } finally {
    hide();
  }
}
/** 删除帖子 */
async function handleDelete(row: ForumPostApi.Post) {
  try {
    await confirm({
      content: `确认删除帖子「${row.title}」吗？`,
    });
  } catch {
    return;
  }
  const hide = message.loading({
    content: '正在删除中',
    key: 'action_key_msg',
  });
  try {
    await deletePost(row.id as number);
    message.success({ content: '删除成功', key: 'action_key_msg' });
    onRefresh();
  } finally {
    hide();
  }
}

/** 格式化用户选项 */
function formatUserOption(user: ForumUserProfileApi.AdminUserProfileRespVO) {
  let label = '-';
  if (user.nickname) {
    label = user.uid ? `${user.nickname} (UID: ${user.uid})` : user.nickname;
  } else if (user.uid) {
    label = `UID: ${user.uid}`;
  } else if (user.userId) {
    label = `ID: ${user.userId}`;
  }
  return {
    label,
    value: user.userId as number,
  };
}

/** 获取用户选项 */
async function fetchUserOptions(keyword?: string) {
  loadingUsers.value = true;
  try {
    const trimmedKeyword = keyword?.trim();
    // 判断是否为 UID 格式：以 U 开头才认为是 UID
    const isUidFormat = trimmedKeyword && trimmedKeyword.startsWith('U');
    const params: ForumUserProfileApi.AdminUserProfilePageReqVO = {
      pageNo: 1,
      pageSize: 20,
    };
    if (trimmedKeyword) {
      if (isUidFormat) {
        params.uid = trimmedKeyword;
      } else {
        // 纯数字或其他文本都使用昵称模糊搜索
        params.nickname = trimmedKeyword;
      }
    }
    const { list } = await getForumUserProfilePage(params);
    const nextOptions = (list ?? [])
      .filter((item) => item.userId !== undefined && item.userId !== null)
      .map((item) => formatUserOption(item));

    // 如果有搜索关键词，直接使用搜索结果；否则合并保留已存在的选项
    if (trimmedKeyword) {
      // 有搜索关键词时，替换选项列表
      userOptions.value = nextOptions;
    } else {
      // 没有搜索关键词时，合并保留已存在的选项
      const mergedOptions = [...nextOptions];
      const currentOptions = userOptions.value ?? [];
      for (const option of currentOptions) {
        if (
          option &&
          option.value !== undefined &&
          !mergedOptions.some((item) => item?.value === option.value)
        ) {
          mergedOptions.push(option as { label: string; value: number });
        }
      }
      userOptions.value = mergedOptions;
    }
  } finally {
    loadingUsers.value = false;
  }
}

/** 防抖搜索用户 */
const debouncedSearchUsers = useDebounceFn(async (keyword: string) => {
  await fetchUserOptions(keyword);
}, 300);

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema({
      getUserSelectProps: () => ({
        options: userOptions.value ?? [],
        loading: loadingUsers.value,
        onDropdownVisibleChange: (open: boolean) => {
          if (open && (userOptions.value?.length ?? 0) === 0) {
            void fetchUserOptions(userSearch.value);
          }
        },
        onSearch: (value: string) => {
          userSearch.value = value;
          void debouncedSearchUsers(value);
        },
      }),
    }),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getPostPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
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
  } as VxeTableGridOptions<ForumPostApi.Post>,
});
</script>

<template>
  <Page auto-content-height>
    <Review @success="onRefresh" />
    <Grid table-title="帖子管理">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '详情',
              type: 'link',
              icon: ACTION_ICON.VIEW,
              auth: ['forum:post:query'],
              onClick: handleDetail.bind(null, row),
            }, {
                    label: row.isTop ? '取消置顶' : '置顶',
                    type: 'link',
                    icon: 'ant-design:vertical-align-top-outlined',
                    auth: ['forum:post:update'],
                    onClick: handleSetTop.bind(null, row),
                  },
            {
              label: '复审',
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['forum:post:review'],
              onClick: handleReview.bind(null, row),
            },
            {
              label: '删除',
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['forum:post:delete'],
              onClick: handleDelete.bind(null, row),
            },
          ]"
        />
      </template>
      <template #topic="{ row }">
        <div v-if="row.categories?.length" class="flex flex-wrap gap-1">
          <Tag v-for="categoryId in row.categories" :key="categoryId">
            {{ getDictLabel(DICT_TYPE.FRUM_TOPIC_TYPE, categoryId) || categoryId }}
          </Tag>
        </div>
        <span v-else>-</span>
      </template>
    </Grid>
  </Page>
</template>
