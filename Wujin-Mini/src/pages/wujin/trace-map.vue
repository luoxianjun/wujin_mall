<template>
  <view class="trace-map-entry">
    <button class="trace-map-entry__button" @tap="openFullscreenTrace">
      <AppIcon name="open_in_new" variant="light" size="28rpx" />
      <text>查看全屏图谱</text>
    </button>
  </view>
</template>

<script setup>
import { onLoad } from "@dcloudio/uni-app";

defineOptions({ name: "WujinMiniTraceMap" });

function openFullscreenTrace() {
  const query = getCurrentQueryString();
  uni.navigateTo({
    url: `/pages/wujin/trace-fullscreen${query ? `?${query}` : ""}`,
  });
}

function getCurrentQueryString() {
  try {
    const pages = typeof getCurrentPages === "function" ? getCurrentPages() : [];
    const current = pages.at(-1);
    const options = current?.options || {};
    return Object.entries(options)
      .filter(([, value]) => value !== undefined && value !== null && value !== "")
      .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
      .join("&");
  } catch (error) {
    return "";
  }
}

onLoad(() => {
  openFullscreenTrace();
});
</script>

<style lang="scss" scoped>
.trace-map-entry {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
}

.trace-map-entry__button {
  display: flex;
  align-items: center;
  gap: 10rpx;
  height: 76rpx;
  margin: 0;
  padding: 0 22rpx;
  border: none;
  border-radius: 16rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 76rpx;
}

.trace-map-entry__button::after {
  border: none;
}
</style>
