import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/forum/sign-up',
    component: () => import('#/views/forum/sign-up/index.vue'),
    name: 'ForumActivitySignUp',
    meta: {
      title: '娲诲姩鎶ュ悕绠＄悊',
      icon: 'lucide:list',
      hideInMenu: true,
      requiresAuth: true,
    },
  },
  {
    path: '/forum/post/detail',
    component: () => import('#/views/forum/post/detail.vue'),
    name: 'ForumPostDetail',
    meta: {
      title: '甯栧瓙璇︽儏',
      icon: 'lucide:file-text',
      hideInMenu: true,
      requiresAuth: true,
    },
  },
  {
    path: '/forum/activity/detail/:id?',
    component: () => import('#/views/forum/activity/detail.vue'),
    name: 'ForumActivityDetail',
    meta: {
      title: '娲诲姩璇︽儏',
      icon: 'lucide:file-text',
      hideInMenu: true,
      requiresAuth: true,
    },
  },
];

export default routes;
