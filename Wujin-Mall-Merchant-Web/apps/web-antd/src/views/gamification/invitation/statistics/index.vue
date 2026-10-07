<script lang="ts" setup>
import type { InvitationApi } from '#/api/gamification/invitation';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Card, Col, Row, Statistic } from 'ant-design-vue';

import { getInvitationStatistics } from '#/api/gamification/invitation';

defineOptions({ name: 'InvitationStatistics' });

const loading = ref(false);
const statistics = ref<InvitationApi.InvitationStatistics>({
  totalInvitations: 0,
  successfulInvitations: 0,
  pendingInvitations: 0,
  failedInvitations: 0,
  todayInvitations: 0,
  todayRewardPoints: 0,
  totalRewardPoints: 0,
  activeInviters: 0,
  successRate: 0,
});

const successRateValue = computed(() => statistics.value.successRate ?? 0);

async function loadStatistics() {
  loading.value = true;
  try {
    const res = await getInvitationStatistics();
    statistics.value = {
      totalInvitations: res.totalInvitations ?? 0,
      successfulInvitations: res.successfulInvitations ?? 0,
      pendingInvitations: res.pendingInvitations ?? 0,
      failedInvitations: res.failedInvitations ?? 0,
      todayInvitations: res.todayInvitations ?? 0,
      todayRewardPoints: res.todayRewardPoints ?? 0,
      totalRewardPoints: res.totalRewardPoints ?? 0,
      activeInviters: res.activeInviters ?? 0,
      successRate: res.successRate ?? 0,
    };
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadStatistics();
});
</script>

<template>
  <Page
    auto-content-height
    content-class="flex flex-col gap-4"
    description="查看邀请转化、奖励发放和整体完成情况。"
    title="邀请统计"
  >
    <Card :loading="loading" title="邀请概览">
      <Row :gutter="[16, 16]">
        <Col :span="6">
          <Card>
            <Statistic :value="statistics.totalInvitations" title="总邀请数" />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.successfulInvitations"
              title="成功邀请数"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.pendingInvitations"
              title="待完成邀请数"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.failedInvitations"
              title="失效邀请数"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.todayInvitations"
              title="今日邀请数"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.todayRewardPoints"
              title="今日奖励积分"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic
              :value="statistics.totalRewardPoints"
              title="累计奖励积分"
            />
          </Card>
        </Col>
        <Col :span="6">
          <Card>
            <Statistic :value="statistics.activeInviters" title="活跃邀请人" />
          </Card>
        </Col>
      </Row>
    </Card>

    <Card :loading="loading" title="转化情况">
      <Row :gutter="[16, 16]">
        <Col :span="8">
          <Card>
            <Statistic
              :precision="2"
              :value="successRateValue"
              suffix="%"
              title="成功率"
            />
          </Card>
        </Col>
      </Row>
    </Card>
  </Page>
</template>
