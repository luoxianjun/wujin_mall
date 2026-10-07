import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    name: 'WujinPlatform',
    path: '/wujin',
    redirect: '/wujin/platform/dashboard',
    meta: {
      hideInMenu: true,
      icon: 'lucide:factory',
      order: 10,
      title: '五金运营后台',
    },
    children: [
      {
        name: 'WujinPlatformConfig',
        path: '/wujin/platform',
        redirect: '/wujin/platform/dashboard',
        meta: {
          icon: 'lucide:sliders-horizontal',
          title: '平台运营配置',
        },
        children: [
          {
            name: 'WujinPlatformDashboard',
            path: '/wujin/platform/dashboard',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:layout-dashboard',
              title: '运营看板',
            },
          },
          {
            name: 'WujinPlatformCategory',
            path: '/wujin/platform/category',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:folder-tree',
              title: '平台类目',
            },
          },
          {
            name: 'WujinPlatformMapping',
            path: '/wujin/platform/mapping',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:git-branch',
              title: '跨泳道映射',
            },
          },
          {
            name: 'WujinPlatformTemplate',
            path: '/wujin/platform/template',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:blocks',
              title: '行业模板',
            },
          },
          {
            name: 'WujinPlatformAudit',
            path: '/wujin/platform/audit',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:clipboard-check',
              title: '关系审核',
            },
          },
          {
            name: 'WujinPlatformSearchRule',
            path: '/wujin/platform/search-rule',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:sliders-horizontal',
              title: '搜索规则',
            },
          },
          {
            name: 'WujinPlatformSearchLog',
            path: '/wujin/platform/search-log',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:search-check',
              title: '搜索监控',
            },
          },
          {
            name: 'WujinPlatformSourcingLead',
            path: '/wujin/platform/sourcing-lead',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:radar',
              title: '寻源线索',
            },
          },
          {
            name: 'WujinPlatformMonitor',
            path: '/wujin/platform/monitor',
            component: () => import('#/views/wujin/platform/index.vue'),
            meta: {
              icon: 'lucide:activity',
              title: '监控快照',
            },
          },
        ],
      },
      {
        name: 'WujinPlatformAttributeDictionary',
        path: '/wujin/platform/attribute-dictionary',
        component: () =>
          import('#/views/wujin/platform/attribute-dictionary.vue'),
        meta: {
          icon: 'lucide:list-tree',
          title: '平台属性字典',
        },
      },
      {
        name: 'WujinPlatformCustomTagAudit',
        path: '/wujin/platform/custom-tag',
        component: () => import('#/views/wujin/platform/custom-tag-audit.vue'),
        meta: {
          icon: 'lucide:tags',
          title: '标签审核',
        },
      },
    ],
  },
];

export default routes;
