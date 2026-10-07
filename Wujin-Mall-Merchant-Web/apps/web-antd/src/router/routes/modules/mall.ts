import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/mall/product/spu',
    name: 'ProductSpu',
    component: () => import('#/views/mall/product/spu/index.vue'),
    meta: {
      title: '商品列表',
      icon: 'lucide:shopping-bag',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/product/comment',
    name: 'ProductComment',
    component: () => import('#/views/mall/product/comment/index.vue'),
    meta: {
      title: '商品评价',
      icon: 'lucide:message-square',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/trade/order',
    name: 'TradeOrder',
    component: () => import('#/views/mall/trade/order/index.vue'),
    meta: {
      title: '订单管理',
      icon: 'lucide:list',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/trade/after-sale',
    name: 'TradeAfterSale',
    component: () => import('#/views/mall/trade/afterSale/index.vue'),
    meta: {
      title: '售后管理',
      icon: 'ri:refund-2-line',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/promotion/coupon',
    name: 'PromotionCoupon',
    component: () => import('#/views/mall/promotion/coupon/index.vue'),
    meta: {
      title: '优惠券',
      icon: 'ep:ticket',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/product/spu/add',
    name: 'ProductSpuAdd',
    component: () => import('#/views/mall/product/spu/modules/form.vue'),
    meta: {
      title: '商品添加',
      activeMenu: '/mall/product/spu',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/product/spu/edit/:id',
    name: 'ProductSpuEdit',
    component: () => import('#/views/mall/product/spu/modules/form.vue'),
    meta: {
      title: '商品编辑',
      activeMenu: '/mall/product/spu',
      hideInMenu: true,
    },
  },
  {
    path: '/mall/product/spu/detail/:id',
    name: 'ProductSpuDetail',
    component: () => import('#/views/mall/product/spu/modules/detail.vue'),
    meta: {
      title: '商品详情',
      activeMenu: '/mall/product/spu',
      hideInMenu: true,
    },
  },
];

export default routes;
