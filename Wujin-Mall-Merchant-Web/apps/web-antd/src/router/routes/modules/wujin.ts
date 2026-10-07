import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    name: 'WujinMerchant',
    path: '/merchant/wujin',
    component: () => import('#/views/wujin/merchant/index.vue'),
    meta: {
      hideInMenu: true,
      icon: 'lucide:store',
      order: 10,
      title: '五金商家后台',
    },
  },
];

export default routes;
