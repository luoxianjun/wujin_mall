<script lang="ts" setup>
import type {
  AnalysisOverviewItem,
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
} from '@vben/common-ui';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  AnalysisOverview,
  Page,
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

import { getUserCountComparison } from '#/api/mall/statistics/member';
import { getOrderComparison } from '#/api/mall/statistics/trade';

/** 平台运营驾驶舱 */
defineOptions({ name: 'MallHome' });

interface AttentionItem {
  count: number;
  label: string;
  tone: 'blue' | 'green' | 'orange' | 'red';
}

interface TrendItem {
  label: string;
  percent: number;
  value: number;
}

const loading = ref(true);
const orderComparison = ref();
const userComparison = ref();

const getOrder = async () => {
  orderComparison.value = await getOrderComparison();
};

const getUserCount = async () => {
  userComparison.value = await getUserCountComparison();
};

onMounted(async () => {
  loading.value = true;
  await Promise.all([getOrder(), getUserCount()]);
  loading.value = false;
});

function toNumber(value: unknown) {
  const result = Number(value ?? 0);
  return Number.isFinite(result) ? result : 0;
}

function toPercent(value: number, max: number) {
  if (max <= 0) {
    return 8;
  }
  return Math.max(8, Math.round((value / max) * 100));
}

const todayMemberCount = computed(() =>
  toNumber(userComparison.value?.registerUserCount),
);
const totalMemberCount = computed(() =>
  toNumber(userComparison.value?.reference?.registerUserCount),
);
const todayVisitCount = computed(() =>
  toNumber(userComparison.value?.visitUserCount),
);
const todayInteractionCount = computed(() =>
  toNumber(orderComparison.value?.orderPayCount),
);

const overviewItems = computed<AnalysisOverviewItem[]>(() => [
  {
    icon: SvgCardIcon,
    title: '今日新增会员数',
    totalTitle: '累计会员基数',
    totalValue: totalMemberCount.value,
    value: todayMemberCount.value,
  },
  {
    icon: SvgCakeIcon,
    title: '今日内容访问量',
    totalTitle: '昨日访问量',
    totalValue: toNumber(userComparison.value?.reference?.visitUserCount),
    value: todayVisitCount.value,
  },
  {
    icon: SvgDownloadIcon,
    title: '今日活动转化数',
    totalTitle: '昨日转化数',
    totalValue: toNumber(orderComparison.value?.reference?.orderPayCount),
    value: todayInteractionCount.value,
  },
  {
    icon: SvgBellIcon,
    title: '今日平台交互数',
    totalTitle: '昨日交互数',
    totalValue: toNumber(orderComparison.value?.reference?.orderPayCount),
    value: todayInteractionCount.value + todayVisitCount.value,
  },
]);

const trendItems = computed<TrendItem[]>(() => {
  const items = [
    {
      label: '会员增长',
      value: todayMemberCount.value,
    },
    {
      label: '内容访问',
      value: todayVisitCount.value,
    },
    {
      label: '活动转化',
      value: todayInteractionCount.value,
    },
    {
      label: '平台交互',
      value: todayInteractionCount.value + todayVisitCount.value,
    },
  ];
  const max = Math.max(...items.map((item) => item.value), 1);
  return items.map((item) => ({
    ...item,
    percent: toPercent(item.value, max),
  }));
});

const attentionItems = computed<AttentionItem[]>(() => [
  {
    count: 3,
    label: '关系审核待处理',
    tone: 'orange',
  },
  {
    count: 2,
    label: '平台类目需拆分',
    tone: 'red',
  },
  {
    count: 6,
    label: '搜索规则待调优',
    tone: 'blue',
  },
  {
    count: 4,
    label: '寻源线索待分发',
    tone: 'green',
  },
]);

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
  <Page auto-content-height>
    <div class="wujin-home">
      <section class="wujin-home__header">
        <div>
          <Tag color="blue">平台端</Tag>
          <h1>平台运营总览</h1>
          <p>聚合会员增长、内容访问、活动转化与平台交互，辅助运营人员快速判断今日健康度。</p>
        </div>
      </section>

      <Spin :spinning="loading">
        <AnalysisOverview :items="overviewItems" />

        <Row :gutter="[16, 16]" class="mt-5">
          <Col :lg="14" :xs="24">
            <Card :bordered="false" title="内容与活动趋势">
              <div class="wujin-trend-list">
                <div
                  v-for="item in trendItems"
                  :key="item.label"
                  class="wujin-trend-list__item"
                >
                  <div class="wujin-trend-list__meta">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </div>
                  <Progress
                    :percent="item.percent"
                    :show-info="false"
                    stroke-color="#2563eb"
                  />
                </div>
              </div>
            </Card>
          </Col>

          <Col :lg="10" :xs="24">
            <Card :bordered="false" title="平台待关注事项">
              <div class="wujin-attention-list">
                <button
                  v-for="item in attentionItems"
                  :key="item.label"
                  class="wujin-attention-list__item"
                  type="button"
                >
                  <span>{{ item.label }}</span>
                  <Tag :color="item.tone">{{ item.count }}</Tag>
                </button>
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
  </Page>
</template>

<style scoped>
.wujin-home {
  display: grid;
  gap: 16px;
}

.wujin-home__header {
  padding: 18px 20px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: linear-gradient(135deg, #eef6ff 0%, #f8fafc 48%, #eefdf7 100%);
}

.wujin-home__header h1 {
  margin: 10px 0 6px;
  color: #111827;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
}

.wujin-home__header p {
  max-width: 760px;
  margin: 0;
  color: #475569;
  line-height: 22px;
}

.wujin-trend-list {
  display: grid;
  gap: 14px;
}

.wujin-trend-list__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 5px;
  color: #475569;
}

.wujin-trend-list__meta strong {
  color: #111827;
  font-size: 18px;
}

.wujin-attention-list {
  display: grid;
  gap: 10px;
}

.wujin-attention-list__item {
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

@media (max-width: 768px) {
  .wujin-home__header {
    padding: 16px;
  }

  .wujin-home__header h1 {
    font-size: 21px;
    line-height: 28px;
  }
}
</style>
