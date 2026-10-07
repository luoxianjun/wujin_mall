<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';

import { onMounted, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { formatDate } from '@vben/utils';

import { Descriptions, Image, Tag } from 'ant-design-vue';

import { getActivity } from '#/api/forum/activity';
import { DICT_TYPE, getDictLabel } from '#/utils';

defineOptions({ name: 'ForumActivityDetailView' });

const props = defineProps<{
  activityId?: number;
}>();


const detail = ref<ForumActivityApi.Activity>();
const loading = ref(false);
const ImagePreviewGroup = Image.PreviewGroup;

async function loadDetail(id?: number) {
  if (!id) return;
  loading.value = true;
  try {
    detail.value = await getActivity(id);
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.activityId,
  (id) => loadDetail(id),
  { immediate: true },
);

onMounted(() => {
  if (props.activityId) {
    loadDetail(props.activityId);
  }
});
</script>

<template>
  <Page auto-content-height>
    <a-card v-if="detail" bordered>
      <div class="mb-4">
        <div class="text-lg font-semibold">活动详情</div>
      </div>
      <Descriptions
        :loading="loading"
        bordered
        :column="2"
        :label-style="{ width: '120px' }"
        size="small"
      >
        <Descriptions.Item label="活动标题" :span="2">
          {{ detail.title }}
        </Descriptions.Item>
        <Descriptions.Item label="报名要求" :span="2">
          {{ detail.requirements }}
        </Descriptions.Item>
        <Descriptions.Item label="活动状态">
          {{ detail.statusName || detail.status || '-' }}
        </Descriptions.Item>
        <Descriptions.Item label="分类">
          {{
            getDictLabel(DICT_TYPE.FRUM_ACTIVITY_TYPE, detail.category) ||
            detail.categoryName ||
            detail.category ||
            '-'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="活动时间" :span="2">
          {{
            detail.startTime && detail.endTime
              ? `${formatDate(detail.startTime, 'YYYY-MM-DD HH:mm:ss')} ~ ${formatDate(detail.endTime, 'YYYY-MM-DD HH:mm:ss')}`
              : '-'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="报名时间" :span="2">
          {{
            detail.signUpStartTime && detail.signUpEndTime
              ? `${formatDate(detail.signUpStartTime, 'YYYY-MM-DD HH:mm:ss')} ~ ${formatDate(detail.signUpEndTime, 'YYYY-MM-DD HH:mm:ss')}`
              : '未限制'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="签到时间" :span="2">
          {{
            detail.checkInStartTime && detail.checkInEndTime
              ? `${formatDate(detail.checkInStartTime, 'YYYY-MM-DD HH:mm:ss')} ~ ${formatDate(detail.checkInEndTime, 'YYYY-MM-DD HH:mm:ss')}`
              : '未限制'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="签到方式">
          {{
            (() => {
              if (!detail.checkInType) return '-';
              const mapping: Record<number, string> = {
                1: '自助签到',
                2: '定位签到',
                3: '扫码签到',
              };
              return mapping[detail.checkInType] || detail.checkInType;
            })()
          }}
        </Descriptions.Item>
        <Descriptions.Item label="地点" :span="2">
          {{ detail.location }}
          <template v-if="detail.longitude && detail.latitude">
            （{{ detail.longitude }}, {{ detail.latitude }}）
          </template>
        </Descriptions.Item>
        <Descriptions.Item label="签到距离">
          {{ detail.checkInDistance ?? 100 }} 米
        </Descriptions.Item>
        <Descriptions.Item v-if="detail.showParticipantCount !== false" label="人数限制">
          {{
            detail.maxParticipants && detail.maxParticipants > 0
              ? detail.maxParticipants
              : '不限'
          }}
        </Descriptions.Item>
        <Descriptions.Item v-if="detail.showParticipantCount !== false" label="报名情况">
          {{ detail.currentParticipants ?? 0 }}
        </Descriptions.Item>
        <Descriptions.Item label="审核要求">
          {{ detail.needApproval ? '需要审核' : '无需审核' }}
        </Descriptions.Item>
        <Descriptions.Item label="奖励积分">
          <template v-if="detail.needPoint">
            奖励 {{ detail.pointAmount ?? 0 }} 积分
          </template>
          <template v-else>无奖励积分</template>
        </Descriptions.Item>
        <Descriptions.Item label="热点">
          {{ detail.hot ? '是' : '否' }}
        </Descriptions.Item>
        <Descriptions.Item label="校内可见">
          {{ detail.schoolOnly ? '仅本校' : '不限学校' }}
        </Descriptions.Item>
        <Descriptions.Item label="浏览/点赞">
          {{ detail.viewCount ?? 0 }} / {{ detail.likeCount ?? 0 }}
        </Descriptions.Item>
        <Descriptions.Item label="活动管理员">
          <div v-if="detail.adminMemberUids?.length" class="flex flex-wrap gap-1">
            <Tag v-for="(uid, index) in detail.adminMemberUids" :key="index">
              {{ uid }}
            </Tag>
          </div>
          <span v-else>-</span>
        </Descriptions.Item>
        <Descriptions.Item label="发布人">
          {{ detail.nickname || detail.uid || detail.userId || '-' }}
        </Descriptions.Item>
        <Descriptions.Item label="报名/签到" :span="2">
          {{ detail.signedUp ? '已报名' : '未报名' }} /
          {{ detail.checkedIn ? '已签到' : '未签到' }}
        </Descriptions.Item>
        <Descriptions.Item label="封面" :span="2">
          <ImagePreviewGroup>
            <Image
              v-if="detail.coverImage"
              :src="detail.coverImage"
              :preview="{ src: detail.coverImage }"
              :width="120"
              :height="120"
              class="rounded object-cover"
            />
            <span v-else>-</span>
          </ImagePreviewGroup>
        </Descriptions.Item>

        <Descriptions.Item label="活动描述" :span="2">
          <div
            v-dompurify-html="detail.description"
            class="prose max-w-none"
          ></div>
        </Descriptions.Item>
        <Descriptions.Item label="创建时间" :span="2">
          {{
            detail.createTime
              ? formatDate(detail.createTime, 'YYYY-MM-DD HH:mm:ss')
              : '-'
          }}
        </Descriptions.Item>
      </Descriptions>
    </a-card>
  </Page>
</template>
