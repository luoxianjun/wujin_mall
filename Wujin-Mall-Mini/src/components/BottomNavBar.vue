<template>
  <view class="bottom-nav">
    <view
      v-for="item in navItems"
      :key="item.key"
      class="nav-item"
      :class="{ active: active === item.key }"
      @tap="handleNav(item)"
    >
      <view class="nav-icon-shell">
        <AppIcon
          class="nav-icon"
          :name="item.icon"
          :variant="active === item.key ? 'dark' : 'muted'"
          size="42rpx"
        />
        <view
          v-if="item.key === 'profile' && unreadCount > 0"
          class="tab-badge"
        >
          {{ unreadCount > 99 ? "99+" : unreadCount }}
        </view>
      </view>
      <text class="nav-label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script>
const NAV_ITEMS = [
  {
    key: "home",
    label: "五金",
    icon: "search",
    path: "/pages/wujin/search",
  },
  {
    key: "sourcing",
    label: "寻源",
    icon: "assignment",
    path: "/pages/wujin/sourcing",
  },
  {
    key: "trace",
    label: "溯源",
    icon: "visibility",
    path: "/pages/wujin/trace-map",
  },
  {
    key: "profile",
    label: "我的",
    icon: "person",
    path: "/pages/wujin/profile",
  },
];

export default {
  name: "BottomNavBar",
  props: {
    active: {
      type: String,
      default: "home",
    },
    unreadCount: {
      type: Number,
      default: 0,
    },
  },
  emits: ["tab-change"],
  data() {
    return {
      navItems: NAV_ITEMS,
    };
  },
  methods: {
    handleNav(item) {
      if (this.active === item.key) {
        return;
      }
      this.$emit("tab-change", item.key);
      // tab 级页面使用 reLaunch，避免 navigateTo 反复压栈导致超过 10 层栈上限
      uni.reLaunch({
        url: item.path,
        fail: () => {
          uni.navigateTo({ url: item.path });
        },
      });
    },
  },
};
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 999;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  height: calc(130rpx + env(safe-area-inset-bottom));
  padding: 16rpx 22rpx env(safe-area-inset-bottom);
  border-top: 1rpx solid #eceff3;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 -16rpx 36rpx rgba(17, 17, 17, 0.08);
  box-sizing: border-box;
}

.nav-item {
  position: relative;
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  color: #5d6673;
}

.nav-item.active {
  color: #111111;
}

.nav-icon-shell {
  position: relative;
  display: flex;
  width: 58rpx;
  height: 48rpx;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
}

.nav-item.active .nav-icon-shell {
  background: #ffd21e;
}

.nav-label {
  font-size: 22rpx;
  line-height: 30rpx;
  font-weight: 700;
}

.tab-badge {
  position: absolute;
  top: -10rpx;
  right: -18rpx;
  display: flex;
  min-width: 30rpx;
  height: 30rpx;
  align-items: center;
  justify-content: center;
  padding: 0 7rpx;
  border: 2rpx solid #ffffff;
  border-radius: 999rpx;
  background: #d92d20;
  color: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  box-sizing: border-box;
}
</style>
