<template>
  <view class="wujin-page profile-page">
    <view class="wujin-hero profile-hero" :style="heroStyle">
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">我的</text>
        <view class="hero-actions">
          <button class="hero-action" @tap="handleMessagesOpen">
            <AppIcon name="notifications_none" variant="dark" size="34rpx" />
            <view v-if="unreadCount > 0" class="hero-badge">
              {{ unreadCount > 99 ? "99+" : unreadCount }}
            </view>
          </button>
        </view>
      </view>
      <text class="wujin-hero__title">{{ displayName }}</text>
      <text class="wujin-hero__subtitle">{{ loggedIn ? "欢迎回来，继续你的五金寻源" : "登录后可保存寻源线索与沟通记录" }}</text>
      <button v-if="!loggedIn" class="wujin-button login-button" @tap="handleLogin">
        登录 / 注册
      </button>
    </view>

    <view class="wujin-content profile-content">
      <view class="wujin-card entry-card">
        <view class="entry-row" @tap="handleLeadsOpen">
          <view class="entry-left">
            <AppIcon name="assignment" variant="dark" size="36rpx" />
            <text class="entry-label">我的寻源线索</text>
          </view>
          <AppIcon name="expand_more" variant="muted" size="30rpx" class="entry-arrow" />
        </view>
        <view class="entry-row" @tap="handleMessagesOpen">
          <view class="entry-left">
            <AppIcon name="notifications_none" variant="dark" size="36rpx" />
            <text class="entry-label">消息沟通</text>
          </view>
          <AppIcon name="expand_more" variant="muted" size="30rpx" class="entry-arrow" />
        </view>
        <view class="entry-row" @tap="handleTraceOpen">
          <view class="entry-left">
            <AppIcon name="visibility" variant="dark" size="36rpx" />
            <text class="entry-label">溯源图谱</text>
          </view>
          <AppIcon name="expand_more" variant="muted" size="30rpx" class="entry-arrow" />
        </view>
      </view>

      <view class="wujin-card placeholder-card">
        <text class="wujin-section-title">更多功能</text>
        <text class="wujin-muted">收货地址、企业资质、采购偏好等功能开发中。</text>
      </view>
    </view>

    <BottomNavBar active="profile" :unread-count="unreadCount" />
  </view>
</template>

<script>
import BottomNavBar from "@/components/BottomNavBar.vue";
import statusBarMixin from "@/mixins/statusBar";
import { getAccessToken, getUserProfile } from "@/utils/session";

export default {
  name: "WujinMiniProfile",
  components: { BottomNavBar },
  mixins: [statusBarMixin],
  data() {
    return {
      unreadCount: 0,
      userProfile: null,
      _unreadHandler: null,
    };
  },
  computed: {
    loggedIn() {
      return Boolean(getAccessToken());
    },
    displayName() {
      if (!this.loggedIn) {
        return "未登录";
      }
      return this.userProfile?.nickname || this.userProfile?.name || "五金用户";
    },
    heroStyle() {
      return { paddingTop: `calc(${this.statusBarHeight}px + 24rpx)` };
    },
  },
  onShow() {
    this.userProfile = getUserProfile();
    this.unreadCount = getApp()?.globalData?.unreadCount || 0;
  },
  onLoad() {
    this._unreadHandler = (count) => {
      this.unreadCount = count;
    };
    uni.$on("updateUnreadCount", this._unreadHandler);
  },
  onUnload() {
    if (this._unreadHandler) {
      uni.$off("updateUnreadCount", this._unreadHandler);
      this._unreadHandler = null;
    }
  },
  methods: {
    handleLogin() {
      uni.navigateTo({ url: "/pages/users/login" });
    },
    handleLeadsOpen() {
      uni.navigateTo({ url: "/pages/wujin/lead-progress" });
    },
    handleMessagesOpen() {
      uni.navigateTo({ url: "/pages/messages/index" });
    },
    handleTraceOpen() {
      uni.navigateTo({ url: "/pages/wujin/trace-map" });
    },
  },
};
</script>

<style lang="scss" scoped>
.profile-hero {
  padding-bottom: 40rpx;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.hero-action {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  margin: 0;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.46);
}

.hero-action::after {
  border: none;
}

.hero-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  display: flex;
  min-width: 30rpx;
  height: 30rpx;
  align-items: center;
  justify-content: center;
  padding: 0 7rpx;
  border: 2rpx solid #ffd21e;
  border-radius: 999rpx;
  background: #d92d20;
  color: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  box-sizing: border-box;
}

.login-button {
  position: relative;
  z-index: 1;
  width: 240rpx;
  margin-top: 28rpx;
}

.profile-content {
  margin-top: -28rpx;
}

.entry-card {
  position: relative;
  z-index: 2;
  gap: 0;
  padding: 8rpx 24rpx;
}

.entry-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f2f5;
}

.entry-row:last-child {
  border-bottom: none;
}

.entry-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.entry-label {
  color: #111111;
  font-size: 30rpx;
  font-weight: 600;
}

.entry-arrow {
  transform: rotate(-90deg);
}
</style>
