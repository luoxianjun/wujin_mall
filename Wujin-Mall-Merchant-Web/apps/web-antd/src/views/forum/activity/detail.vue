<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { Button, Result } from 'ant-design-vue';

import ActivityDetailView from './modules/detail-view.vue';

defineOptions({ name: 'ForumActivityDetailPage' });

const route = useRoute();
const router = useRouter();

const activityId = computed(() => {
  const raw =
    route.params.id ??
    route.query.id ??
    route.query.activityId ??
    route.query.activity_id;
  const value = Array.isArray(raw) ? raw[0] : raw;
  const num = Number(value);
  return Number.isFinite(num) ? num : undefined;
});

function handleBack() {
  // Try to go back; fallback to activity list
  router.back();
  // If there is no history, push to the activity list route path
  setTimeout(() => {
    if (router.currentRoute.value.name === 'ForumActivityDetail') {
      router.push('/forum/activity');
    }
  }, 0);
}
</script>

<template>
  <Page auto-content-height>
    <template v-if="activityId">
      <ActivityDetailView :activity-id="activityId" />
    </template>
    <Result v-else status="warning" title="缺少活动ID">
      <template #extra>
        <Button type="primary" @click="handleBack">返回</Button>
      </template>
    </Result>
  </Page>
</template>
