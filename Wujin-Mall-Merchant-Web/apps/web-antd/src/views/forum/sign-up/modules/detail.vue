<script lang="ts" setup>
import type { ForumActivityApi } from '#/api/forum/activity';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { Descriptions, Image } from 'ant-design-vue';

import { formatDateTime } from '@vben/utils';

defineOptions({ name: 'ForumSignUpDetail' });

const detail = ref<ForumActivityApi.SignUp>();

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  onOpenChange(isOpen) {
    if (!isOpen) {
      detail.value = undefined;
      return;
    }
    detail.value = drawerApi.getData<ForumActivityApi.SignUp>();
  },
});

const formattedCreateTime = computed(() =>
  detail.value?.createTime ? formatDateTime(detail.value.createTime) : '-',
);
const formattedCheckInTime = computed(() =>
  detail.value?.checkInTime ? formatDateTime(detail.value.checkInTime) : '-',
);
</script>

<template>
  <Drawer title="报名详情" width="42%">
    <Descriptions v-if="detail" bordered :column="2" size="small">
      <Descriptions.Item label="报名ID">
        {{ detail.id }}
      </Descriptions.Item>
      <Descriptions.Item label="活动ID">
        {{ detail.activityId }}
      </Descriptions.Item>
      <Descriptions.Item label="活动标题" :span="2">
        {{ detail.activityTitle }}
      </Descriptions.Item>
      <Descriptions.Item label="用户UID">
        {{ detail.uid || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="昵称">
        {{ detail.nickname || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="头像" :span="2">
        <Image
          v-if="detail.avatar"
          :src="detail.avatar"
          width="80"
          height="80"
          class="rounded"
        />
        <span v-else>-</span>
      </Descriptions.Item>
      <Descriptions.Item label="审核状态">
        {{ detail.approvalStatusName || detail.approvalStatus || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="审核备注">
        {{ detail.approvalRemark || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="点评内容" :span="2">
        {{ detail.feedback || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="签到状态">
        {{ detail.checkedIn ? '已签到' : '未签到' }}
      </Descriptions.Item>
      <Descriptions.Item label="签到时间">
        {{ formattedCheckInTime }}
      </Descriptions.Item>
      <Descriptions.Item label="报名备注" :span="2">
        {{ detail.remark || '-' }}
      </Descriptions.Item>
      <Descriptions.Item label="报名时间" :span="2">
        {{ formattedCreateTime }}
      </Descriptions.Item>
    </Descriptions>
  </Drawer>
</template>
