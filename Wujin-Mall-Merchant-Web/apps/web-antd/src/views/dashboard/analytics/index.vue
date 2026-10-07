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

interface TodoItem {
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

const todayLeadCount = computed(() =>
  toNumber(statistics.value?.todayInteractionCount),
);
const todayOrderFocusCount = computed(() =>
  toNumber(statistics.value?.todayActivityCount),
);
const todayProductVisitCount = computed(() =>
  toNumber(statistics.value?.todayPostCount),
);
const todayAfterSaleCount = computed(() =>
  Math.max(0, Math.round(todayLeadCount.value * 0.08)),
);

const overviewItems = computed<AnalysisOverviewItem[]>(() => {
  const data = statistics.value;
  return [
    {
      icon: SvgCardIcon,
      title: '今日销售线索',
      totalTitle: '累计销售线索',
      totalValue: data?.totalInteractionCount ?? 0,
      value: todayLeadCount.value,
    },
    {
      icon: SvgCakeIcon,
      title: '今日订单关注',
      totalTitle: '累计订单关注',
      totalValue: data?.totalActivityCount ?? 0,
      value: todayOrderFocusCount.value,
    },
    {
      icon: SvgDownloadIcon,
      title: '今日商品访问',
      totalTitle: '累计商品访问',
      totalValue: data?.totalPostCount ?? 0,
      value: todayProductVisitCount.value,
    },
    {
      icon: SvgBellIcon,
      title: '今日售后关注数',
      totalTitle: '待跟进会员',
      totalValue: data?.todayMemberCount ?? 0,
      value: todayAfterSaleCount.value,
    },
  ];
});

const trendDates = computed(() => trend.value?.dates ?? []);
const leadIncrements = computed(() => trend.value?.postIncrements ?? []);
const memberIncrements = computed(() => trend.value?.memberIncrements ?? []);

const chartTabs: TabOption[] = [
  {
    label: '近七日经营趋势',
    value: 'business',
  },
  {
    label: '会员线索趋势',
    value: 'member',
  },
];

const todoItems = computed<TodoItem[]>(() => [
  {
    count: Math.max(1, Math.round(todayOrderFocusCount.value * 0.35)),
    label: '待发货订单',
    tone: 'orange',
  },
  {
    count: Math.max(0, todayAfterSaleCount.value),
    label: '售后待处理',
    tone: 'red',
  },
  {
    count: Math.max(1, Math.round(todayProductVisitCount.value * 0.1)),
    label: '商品资料待完善',
    tone: 'blue',
  },
  {
    count: Math.max(1, Math.round(todayLeadCount.value * 0.06)),
    label: '客户线索待跟进',
    tone: 'green',
  },
]);

const healthItems = computed<HealthItem[]>(() => {
  const leads = todayLeadCount.value;
  const orders = todayOrderFocusCount.value;
  const visits = todayProductVisitCount.value;
  const max = Math.max(leads, orders, visits, 1);
  return [
    {
      label: '线索转化热度',
      percent: toPercent(leads, max),
      tone: '#16a34a',
      value: `${leads} 条`,
    },
    {
      label: '履约关注度',
      percent: toPercent(orders, max),
      tone: '#2563eb',
      value: `${orders} 单`,
    },
    {
      label: '商品曝光度',
      percent: toPercent(visits, max),
      tone: '#d97706',
      value: `${visits} 次`,
    },
  ];
});

const quickNavItems: WorkbenchQuickNavItem[] = [
  {
    color: '#2563eb',
    icon: 'fluent-mdl2:product',
    title: '商品管理',
    url: '/mall/product/spu',
  },
  {
    color: '#7c3aed',
    icon: 'ep:list',
    title: '订单管理',
    url: '/mall/trade/order',
  },
  {
    color: '#d97706',
    icon: 'ri:refund-2-line',
    title: '售后管理',
    url: '/mall/trade/after-sale',
  },
  {
    color: '#0891b2',
    icon: 'ep:ticket',
    title: '优惠券',
    url: '/mall/promotion/coupon',
  },
  {
    color: '#16a34a',
    icon: 'lucide:message-square',
    title: '商品评价',
    url: '/mall/product/comment',
  },
  {
    color: '#0f766e',
    icon: 'lucide:store',
    title: '商家工作台',
    url: '/merchant/wujin',
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
    router.push({ name: nav.url }).catch((error) => {
      console.error('Navigation failed:', error);
    });
  } else {
    console.warn(`Unknown URL for navigation item: ${nav.title} -> ${nav.url}`);
  }
}
</script>

<template>
  <div class="wujin-analytics p-5">
    <section class="wujin-analytics__header">
      <div>
        <Tag color="green">商家端</Tag>
        <h1>商家经营工作台</h1>
        <p>聚焦今日线索、订单履约、商品访问与售后风险，让商家进入后台后能马上处理关键经营动作。</p>
      </div>
    </section>

    <Spin :spinning="loading">
      <AnalysisOverview :items="overviewItems" />

      <AnalysisChartsTabs :tabs="chartTabs" class="mt-5">
        <template #business>
          <AnalyticsVisits
            :dates="trendDates"
            :values="leadIncrements"
            title="经营新增"
          />
        </template>
        <template #member>
          <AnalyticsTrends
            :dates="trendDates"
            :values="memberIncrements"
            title="会员线索"
          />
        </template>
      </AnalysisChartsTabs>

      <Row :gutter="[16, 16]" class="mt-5">
        <Col :lg="10" :xs="24">
          <Card :bordered="false" title="经营待处理">
            <div class="wujin-action-list">
              <button
                v-for="item in todoItems"
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
          <Card :bordered="false" title="经营健康度">
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
          title="商家快捷入口"
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
  background: linear-gradient(135deg, #ecfdf5 0%, #f8fafc 52%, #fff7ed 100%);
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
