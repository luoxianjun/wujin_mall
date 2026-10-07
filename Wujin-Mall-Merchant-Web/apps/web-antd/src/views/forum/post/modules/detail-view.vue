<script lang="ts" setup>
import type { ForumPostApi } from '#/api/forum/post';

import { onMounted, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';

import { Descriptions, Image, Tag } from 'ant-design-vue';

import { formatDate } from '@vben/utils';

import { getPost } from '#/api/forum/post';
import { DICT_TYPE, getDictLabel } from '#/utils';

defineOptions({ name: 'ForumPostDetailView' });

const props = defineProps<{
  postId?: number;
}>();

const detail = ref<ForumPostApi.Post>();
const loading = ref(false);

async function loadDetail(id?: number) {
  if (!id) return;
  loading.value = true;
  try {
    detail.value = await getPost(id);
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.postId,
  (id) => loadDetail(id),
  { immediate: true },
);

onMounted(() => {
  if (props.postId) {
    loadDetail(props.postId);
  }
});
</script>

<template>
  <Page auto-content-height>
    <a-card v-if="detail" bordered>
      <div class="mb-4">
        <div class="text-lg font-semibold">帖子详情</div>
      </div>
      <Descriptions
        :loading="loading"
        bordered
        :column="2"
        size="small"
      >
        <Descriptions.Item label="帖子ID">
          {{ detail.id }}
        </Descriptions.Item>
        <Descriptions.Item label="状态">
          {{
            detail.status === 0
              ? '待审核'
              : detail.status === 1
                ? '已通过'
                : detail.status === 2
                  ? '已驳回'
                  : detail.status
          }}
        </Descriptions.Item>
        <Descriptions.Item label="标题" :span="2">
          {{ detail.title }}
        </Descriptions.Item>
        <Descriptions.Item label="话题">
          <div v-if="detail.categories?.length" class="flex flex-wrap gap-1">
            <Tag v-for="categoryId in detail.categories" :key="categoryId">
              {{ getDictLabel(DICT_TYPE.FRUM_TOPIC_TYPE, categoryId) || categoryId }}
            </Tag>
          </div>
          <span v-else>-</span>
        </Descriptions.Item>
        <Descriptions.Item label="用户">
          {{ detail.nickname || detail.uid || detail.userId || '-' }}
        </Descriptions.Item>
        <Descriptions.Item label="匿名">
          {{ detail.anonymous ? '是' : '否' }}
        </Descriptions.Item>
        <Descriptions.Item label="校内可见">
          {{ detail.schoolOnly ? '仅本校' : '不限' }}
        </Descriptions.Item>
        <Descriptions.Item label="置顶">
          {{ detail.isTop ? '是' : '否' }}
        </Descriptions.Item>
        <Descriptions.Item label="点赞/评论/浏览" :span="2">
          {{ detail.likeCount ?? 0 }} / {{ detail.commentCount ?? 0 }} /
          {{ detail.viewCount ?? 0 }}
        </Descriptions.Item>
        <Descriptions.Item label="最近评论时间" :span="2">
          {{
            detail.latestCommentTime
              ? formatDate(detail.latestCommentTime, 'YYYY-MM-DD HH:mm:ss')
              : '-'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="创建时间" :span="2">
          {{
            detail.createTime
              ? formatDate(detail.createTime, 'YYYY-MM-DD HH:mm:ss')
              : '-'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="图片" :span="2">
          <div v-if="detail.imageUrls?.length" class="flex flex-wrap gap-2">
            <Image
              v-for="img in detail.imageUrls"
              :key="img"
              :src="img"
              width="120"
              height="120"
              class="rounded object-cover"
              :preview="{ src: img }"
            />
          </div>
          <span v-else>-</span>
        </Descriptions.Item>
        <Descriptions.Item label="内容" :span="2">
          <div
            v-dompurify-html="detail.content"
            class="prose max-w-none whitespace-pre-wrap"
          />
        </Descriptions.Item>
      </Descriptions>
    </a-card>
  </Page>
</template>
