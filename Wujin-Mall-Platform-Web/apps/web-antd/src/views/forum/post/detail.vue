<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { Page } from '@vben/common-ui';

import PostDetailView from './modules/detail-view.vue';

defineOptions({ name: 'ForumPostDetailPage' });

const route = useRoute();
const postId = ref<number>();

onMounted(() => {
  const id = Number(route.query.id || route.params.id);
  if (Number.isFinite(id)) {
    postId.value = id;
  }
});
</script>

<template>
  <Page auto-content-height>
    <PostDetailView :post-id="postId" />
  </Page>
</template>
