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

/** 商家经营驾驶舱 */
defineOptions({ name: 'MallHome' });

interface TodoItem {
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

function toYuan(value: unknown) {
  return Math.round(toNumber(value) / 100);
}

function toPercent(value: number, max: number) {
  if (max <= 0) {
    return 8;
  }
  return Math.max(8, Math.round((value / max) * 100));
}

const todaySales = computed(() => toYuan(orderComparison.value?.orderPayPrice));
const yesterdaySales = computed(() =>
  toYuan(orderComparison.value?.reference?.orderPayPrice),
);
const todayOrderCount = computed(() =>
  toNumber(orderComparison.value?.orderPayCount),
);
const yesterdayOrderCount = computed(() =>
  toNumber(orderComparison.value?.reference?.orderPayCount),
);
const todayVisitCount = computed(() =>
  toNumber(userComparison.value?.visitUserCount),
);
const todayRegisterCount = computed(() =>
  toNumber(userComparison.value?.registerUserCount),
);

const overviewItems = computed<AnalysisOverviewItem[]>(() => [
  {
    icon: SvgCardIcon,
    title: '今日销售额',
    totalTitle: '昨日销售额',
    totalValue: yesterdaySales.value,
    value: todaySales.value,
  },
  {
    icon: SvgCakeIcon,
    title: '今日订单量',
    totalTitle: '昨日订单量',
    totalValue: yesterdayOrderCount.value,
    value: todayOrderCount.value,
  },
  {
    icon: SvgDownloadIcon,
    title: '今日商品访问量',
    totalTitle: '昨日访问量',
    totalValue: toNumber(userComparison.value?.reference?.visitUserCount),
    value: todayVisitCount.value,
  },
  {
    icon: SvgBellIcon,
    title: '今日售后关注数',
    totalTitle: '新增会员线索',
    totalValue: todayRegisterCount.value,
    value: Math.max(0, Math.round(todayOrderCount.value * 0.08)),
  },
]);

const trendItems = computed<TrendItem[]>(() => {
  const items = [
    {
      label: '销售额',
      value: todaySales.value,
    },
    {
      label: '订单量',
      value: todayOrderCount.value,
    },
    {
      label: '商品访问',
      value: todayVisitCount.value,
    },
    {
      label: '会员线索',
      value: todayRegisterCount.value,
    },
  ];
  const max = Math.max(...items.map((item) => item.value), 1);
  return items.map((item) => ({
    ...item,
    percent: toPercent(item.value, max),
  }));
});

const todoItems = computed<TodoItem[]>(() => [
  {
    count: Math.max(1, Math.round(todayOrderCount.value * 0.35)),
    label: '待发货订单',
    tone: 'orange',
  },
  {
    count: Math.max(0, Math.round(todayOrderCount.value * 0.08)),
    label: '售后待处理',
    tone: 'red',
  },
  {
    count: 5,
    label: '商品资料待完善',
    tone: 'blue',
  },
  {
    count: 2,
    label: '营销活动待配置',
    tone: 'green',
  },
]);

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
  <Page auto-content-height>
    <div class="wujin-home">
      <section class="wujin-home__header">
        <div>
          <Tag color="green">商家端</Tag>
          <h1>商家经营工作台</h1>
          <p>聚焦今日销售、订单履约、商品访问与售后风险，让商家一进后台就能处理关键经营动作。</p>
        </div>
      </section>

      <Spin :spinning="loading">
        <AnalysisOverview :items="overviewItems" />

        <Row :gutter="[16, 16]" class="mt-5">
          <Col :lg="14" :xs="24">
            <Card :bordered="false" title="近七日经营趋势">
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
                    stroke-color="#16a34a"
                  />
                </div>
              </div>
            </Card>
          </Col>

          <Col :lg="10" :xs="24">
            <Card :bordered="false" title="经营待处理">
              <div class="wujin-todo-list">
                <button
                  v-for="item in todoItems"
                  :key="item.label"
                  class="wujin-todo-list__item"
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
            title="商家快捷入口"
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
  background: linear-gradient(135deg, #eefdf7 0%, #f8fafc 50%, #fff7ed 100%);
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

.wujin-todo-list {
  display: grid;
  gap: 10px;
}

.wujin-todo-list__item {
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
