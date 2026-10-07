<template>
  <view class="wujin-back-bar" :style="barStyle">
    <view class="wujin-back-btn" @tap="handleBack">
      <AppIcon name="arrow_back" variant="dark" size="36rpx" />
    </view>
  </view>
</template>

<script>
import statusBarMixin from "@/mixins/statusBar";

export default {
  name: "WujinBackBar",
  mixins: [statusBarMixin],
  props: {
    // 无上级页面时的兜底跳转（如从分享链接直达详情）
    fallbackUrl: {
      type: String,
      default: "/pages/wujin/search",
    },
  },
  computed: {
    barStyle() {
      return { paddingTop: `${this.statusBarHeight}px` };
    },
  },
  methods: {
    handleBack() {
      const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
      if (pages.length > 1) {
        uni.navigateBack();
        return;
      }
      uni.reLaunch({ url: this.fallbackUrl });
    },
  },
};
</script>

<style lang="scss" scoped>
.wujin-back-bar {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  min-height: 64rpx;
}

.wujin-back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.46);
}
</style>
