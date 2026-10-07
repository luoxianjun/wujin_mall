<script>
import { getAccessToken } from "@/utils/session";
import { wukongImClient } from "@/utils/wukong-im";

export default {
  globalData: {
    unreadCount: 0,
    imUnreadCount: 0,
  },
  onLaunch() {
    this.bindWukongUnread();
    this.initWukongIM();
  },
  onShow() {
    this.initWukongIM();
  },
  methods: {
    getRuntimeGlobalData() {
      return this.globalData || getApp?.()?.globalData || null;
    },
    bindWukongUnread() {
      if (this._wukongUnreadBound) {
        return;
      }
      const update = () => {
        const globalData = this.getRuntimeGlobalData();
        if (!globalData) {
          return;
        }
        globalData.imUnreadCount = wukongImClient.getUnreadCount();
        globalData.unreadCount = globalData.imUnreadCount;
        uni.$emit("updateUnreadCount", globalData.unreadCount);
      };
      wukongImClient.addListener("conversation", update);
      wukongImClient.addListener("message", update);
      this._wukongUnreadBound = true;
    },
    async initWukongIM() {
      if (!getAccessToken()) {
        return;
      }
      try {
        await wukongImClient.init();
        const globalData = this.getRuntimeGlobalData();
        if (globalData) {
          globalData.imUnreadCount = wukongImClient.getUnreadCount();
          globalData.unreadCount = globalData.imUnreadCount;
          uni.$emit("updateUnreadCount", globalData.unreadCount);
        }
      } catch (error) {
        console.warn("[App] WukongIM init failed", error);
      }
    },
  },
};
</script>

<style lang="scss">
@import "@/styles/wujin-theme.scss";

/*每个页面公共css */
.material-icon {
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
  -moz-osx-font-smoothing: grayscale;
  font-feature-settings: "liga";
}
uni-page-body,
html,
body,
page {
  width: 100% !important;
  min-height: 100% !important;
  height: auto !important;
}

html,
body,
page {
  background: #f7f8fa;
}

uni-page-body {
  background: transparent;
  overflow-x: hidden;
}

.page,
.messages-page {
  overflow-x: hidden;
}

.messages-page > .tab-route-content {
  position: relative;
  width: 100%;
  box-sizing: border-box;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
