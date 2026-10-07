<template>
  <web-view :src="url" @message="onMessage"></web-view>
</template>

<script>
export default {
  data() {
    return {
      url: '',
    };
  },
  onLoad(options) {
    if (options.url) {
      this.url = decodeURIComponent(options.url);
    }
  },
  methods: {
    onMessage(e) {
      console.log('[WebView] 收到 H5 消息:', e.detail);
    },
  },
};
</script>
