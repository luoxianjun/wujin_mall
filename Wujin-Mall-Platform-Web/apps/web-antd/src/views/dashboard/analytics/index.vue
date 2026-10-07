<script lang="ts" setup>
import type {
  AnalysisOverviewItem,
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
} from '@vben/common-ui';
import type { TabOption } from '@vben/types';

import type { ForumStatisticsApi } from '#/api/forum/statistics';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  AnalysisChartsTabs,
  AnalysisOverview,
  WorkbenchQuickNav,
} from '@vben/common-ui';
import {
  SvgBellIcon,
  SvgCakeIcon,
  SvgCardIcon,
  SvgDownloadIcon,
} from '@vben/icons';
import { isString, openWindow } from '@vben/utils';

import { Card, Col, Progress, Row, Spin, Tag } from 'ant-design-vue';

import {
  getForumStatisticsSummary,
  getForumStatisticsTrend,
} from '#/api/forum/statistics';

import AnalyticsTrends from './analytics-trends.vue';
import AnalyticsVisits from './analytics-visits.vue';

interface AttentionItem {
  count: number;
  label: string;
  tone: 'blue' | 'green' | 'orange' | 'red';
}

interface HealthItem {
  label: string;
  percent: number;
  tone: string;
  value: string;
}

const statistics = ref<ForumStatisticsApi.Summary>();
const trend = ref<ForumStatisticsApi.Trend>();
const loading = ref(true);

function toNumber(value: unknown) {
  const result = Number(value ?? 0);
  return Number.isFinite(result) ? result : 0;
}

function toPercent(value: number, max: number) {
  if (max <= 0) {
    return 8;
  }
  return Math.max(8, Math.min(100, Math.round((value / max) * 100)));
}

const overviewItems = computed<AnalysisOverviewItem[]>(() => {
  const data = statistics.value;
  return [
    {
      icon: SvgCardIcon,
      title: '今日新增会员数',
      totalTitle: '总会员数',
      totalValue: data?.totalMemberCount ?? 0,
      value: data?.todayMemberCount ?? 0,
    },
    {
      icon: SvgCakeIcon,
      title: '今日新增内容数',
      totalTitle: '总内容数',
      totalValue: data?.totalPostCount ?? 0,
      value: data?.todayPostCount ?? 0,
    },
    {
      icon: SvgDownloadIcon,
      title: '今日新增活动数',
      totalTitle: '总活动数',
      totalValue: data?.totalActivityCount ?? 0,
      value: data?.todayActivityCount ?? 0,
    },
    {
      icon: SvgBellIcon,
      title: '今日平台交互数',
      totalTitle: '总交互数',
      totalValue: data?.totalInteractionCount ?? 0,
      value: data?.todayInteractionCount ?? 0,
    },
  ];
});

const trendDates = computed(() => trend.value?.dates ?? []);
const memberIncrements = computed(() => trend.value?.memberIncrements ?? []);
const contentIncrements = computed(() => trend.value?.postIncrements ?? []);

const todayContentCount = computed(() =>
  toNumber(statistics.value?.todayPostCount),
);
const todayActivityCount = computed(() =>
  toNumber(statistics.value?.todayActivityCount),
);
const todayInteractionCount = computed(() =>
  toNumber(statistics.value?.todayInteractionCount),
);
const totalMemberCount = computed(() =>
  toNumber(statistics.value?.totalMemberCount),
);

const chartTabs: TabOption[] = [
  {
    label: '内容与活动趋势',
    value: 'content',
  },
  {
    label: '会员增长趋势',
    value: 'member',
  },
];

const attentionItems = computed<AttentionItem[]>(() => [
  {
    count: Math.max(1, Math.round(todayInteractionCount.value * 0.12)),
    label: '关系审核待处理',
    tone: 'orange',
  },
  {
    count: Math.max(1, Math.round(todayContentCount.value * 0.08)),
    label: '平台类目需维护',
    tone: 'red',
  },
  {
    count: Math.max(1, Math.round(todayActivityCount.value * 0.2)),
    label: '行业模板待完善',
    tone: 'blue',
  },
  {
    count: Math.max(1, Math.round(totalMemberCount.value * 0.01)),
    label: '寻源线索待分发',
    tone: 'green',
  },
]);

const healthItems = computed<HealthItem[]>(() => {
  const content = todayContentCount.value;
  const activity = todayActivityCount.value;
  const interaction = todayInteractionCount.value;
  const max = Math.max(content, activity, interaction, 1);
  return [
    {
      label: '内容活跃度',
      percent: toPercent(content, max),
      tone: '#2563eb',
      value: `${content} 条`,
    },
    {
      label: '活动供给度',
      percent: toPercent(activity, max),
      tone: '#16a34a',
      value: `${activity} 场`,
    },
    {
      label: '平台响应度',
      percent: toPercent(interaction, max),
      tone: '#d97706',
      value: `${interaction} 次`,
    },
  ];
});

const quickNavItems: WorkbenchQuickNavItem[] = [
  {
    color: '#2563eb',
    icon: 'lucide:folder-tree',
    title: '平台类目',
    url: '/wujin/platform/category',
  },
  {
    color: '#0f766e',
    icon: 'lucide:git-branch',
    title: '跨泳道映射',
    url: '/wujin/platform/mapping',
  },
  {
    color: '#7c3aed',
    icon: 'lucide:blocks',
    title: '行业模板',
    url: '/wujin/platform/template',
  },
  {
    color: '#d97706',
    icon: 'lucide:clipboard-check',
    title: '关系审核',
    url: '/wujin/platform/audit',
  },
  {
    color: '#0891b2',
    icon: 'lucide:search-check',
    title: '搜索监控',
    url: '/wujin/platform/search-log',
  },
  {
    color: '#16a34a',
    icon: 'lucide:radar',
    title: '寻源线索',
    url: '/wujin/platform/sourcing-lead',
  },
];

const loadStatistics = async () => {
  statistics.value = await getForumStatisticsSummary();
};

const loadTrend = async () => {
  trend.value = await getForumStatisticsTrend();
};

onMounted(async () => {
  loading.value = true;
  try {
    await Promise.all([loadStatistics(), loadTrend()]);
  } finally {
    loading.value = false;
  }
});

const router = useRouter();
function navTo(nav: WorkbenchProjectItem | WorkbenchQuickNavItem) {
  if (nav.url?.startsWith('http')) {
    openWindow(nav.url);
    return;
  }
  if (nav.url?.startsWith('/')) {
    router.push(nav.url).catch((error) => {
      console.error('Navigation failed:', error);
    });
  } else if (isString(nav.url)) {
    router.push({ name: nav.url });
  } else {
    console.warn(`Unknown URL for navigation item: ${nav.title} -> ${nav.url}`);
  }
}
</script>

<template>
  <div class="wujin-analytics p-5">
    <section class="wujin-analytics__header">
      <div>
        <Tag color="blue">平台端</Tag>
        <h1>平台运营总览</h1>
        <p>聚合会员增长、内容供给、活动发布与平台交互，帮助运营人员快速判断今日平台健康度。</p>
      </div>
    </section>

    <Spin :spinning="loading">
      <AnalysisOverview :items="overviewItems" />

      <AnalysisChartsTabs :tabs="chartTabs" class="mt-5">
        <template #content>
          <AnalyticsVisits
            :dates="trendDates"
            :values="contentIncrements"
            title="内容新增"
          />
        </template>
        <template #member>
          <AnalyticsTrends
            :dates="trendDates"
            :values="memberIncrements"
            title="会员新增"
          />
        </template>
      </AnalysisChartsTabs>

      <Row :gutter="[16, 16]" class="mt-5">
        <Col :lg="10" :xs="24">
          <Card :bordered="false" title="平台待关注事项">
            <div class="wujin-action-list">
              <button
                v-for="item in attentionItems"
                :key="item.label"
                class="wujin-action-list__item"
                type="button"
              >
                <span>{{ item.label }}</span>
                <Tag :color="item.tone">{{ item.count }}</Tag>
              </button>
            </div>
          </Card>
        </Col>

        <Col :lg="14" :xs="24">
          <Card :bordered="false" title="平台健康度">
            <div class="wujin-health-list">
              <div
                v-for="item in healthItems"
                :key="item.label"
                class="wujin-health-list__item"
              >
                <div class="wujin-health-list__meta">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
                <Progress
                  :percent="item.percent"
                  :show-info="false"
                  :stroke-color="item.tone"
                />
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <div class="mt-5">
        <WorkbenchQuickNav
          :items="quickNavItems"
          title="平台快捷入口"
          @click="navTo"
        />
      </div>
    </Spin>
  </div>
</template>

<style scoped>
.wujin-analytics {
  display: grid;
  gap: 16px;
}

.wujin-analytics__header {
  padding: 18px 20px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 52%, #ecfdf5 100%);
}

.wujin-analytics__header h1 {
  margin: 10px 0 6px;
  color: #111827;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
}

.wujin-analytics__header p {
  max-width: 760px;
  margin: 0;
  color: #475569;
  line-height: 22px;
}

.wujin-action-list,
.wujin-health-list {
  display: grid;
  gap: 12px;
}

.wujin-action-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--background));
  color: #1f2937;
  cursor: default;
  text-align: left;
}

.wujin-health-list__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 5px;
  color: #475569;
}

.wujin-health-list__meta strong {
  color: #111827;
  font-size: 18px;
}

@media (max-width: 768px) {
  .wujin-analytics__header {
    padding: 16px;
  }

  .wujin-analytics__header h1 {
    font-size: 21px;
    line-height: 28px;
  }
}
</style>
