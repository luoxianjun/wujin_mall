<template>
  <view class="messages-page wujin-page">
    <view class="wujin-hero messages-hero">
      <view class="wujin-hero__top">
        <text class="wujin-hero__tag">供应沟通</text>
        <text class="connect-state">{{ connectLabel }}</text>
      </view>
      <text class="wujin-hero__title">消息</text>
      <text class="wujin-hero__subtitle">
        供应商报价、线索响应和平台通知会在这里集中处理。
      </text>
    </view>

    <view class="wujin-content messages-content">
      <view class="wujin-card summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ unreadCount }}</text>
          <text class="summary-label">未读沟通</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ conversations.length }}</text>
          <text class="summary-label">会话</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">WK</text>
          <text class="summary-label">WukongIM</text>
        </view>
      </view>

      <view class="wujin-card notice-card" @tap="openSystemChat">
        <view class="notice-icon">
          <AppIcon name="notifications_active" variant="dark" size="38rpx" />
        </view>
        <view class="notice-copy">
          <text class="notice-title">平台寻源助手</text>
          <text class="notice-desc">接收线索匹配、系统通知和供应商分配结果</text>
        </view>
        <AppIcon name="chevron_right" variant="muted" size="28rpx" />
      </view>

      <view class="wujin-card conversation-card">
        <view class="wujin-section-head">
          <text class="wujin-section-title">供应商会话</text>
          <button class="refresh-btn" @tap="refreshConversations">刷新</button>
        </view>

        <view v-if="conversations.length" class="conversation-list">
          <view
            v-for="item in conversations"
            :key="item.key"
            class="conversation-row"
            @tap="openConversation(item)"
          >
            <view class="conversation-avatar">
              <text>{{ item.initial }}</text>
            </view>
            <view class="conversation-main">
              <view class="conversation-head">
                <text class="conversation-title">{{ item.title }}</text>
                <text class="conversation-time">{{ item.timeLabel }}</text>
              </view>
              <text class="conversation-digest">{{ item.digest }}</text>
            </view>
            <view v-if="item.unread" class="conversation-badge">
              {{ item.unread > 99 ? "99+" : item.unread }}
            </view>
          </view>
        </view>

        <view v-else class="wujin-empty">
          <text class="empty-title">还没有供应沟通</text>
          <text class="wujin-muted">
            提交寻源线索或打开供应能力详情后，可从供应商卡片进入沟通。
          </text>
          <button class="wujin-button wujin-button--yellow" @tap="goSourcing">
            去一键寻源
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getAccessToken } from "@/utils/session";
import {
  wukongImClient,
  WUKONG_CHANNEL_TYPE_PERSON,
} from "@/utils/wukong-im";
import { showToast } from "@/utils/toast";

function getMessageDigest(message) {
  const content = message?.content || message?.lastMessage?.content;
  if (!content) {
    return "暂无最新消息";
  }
  if (typeof content === "string") {
    return content;
  }
  return content.text || content.content || content.displayText || content.conversationDigest || "新消息";
}

function formatTime(timestamp) {
  if (!timestamp) {
    return "";
  }
  const date = new Date(Number(timestamp) * (String(timestamp).length <= 10 ? 1000 : 1));
  const now = new Date();
  if (date.toDateString() === now.toDateString()) {
    return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
  }
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

export default {
  data() {
    return {
      conversations: [],
      unreadCount: 0,
      isConnected: false,
    };
  },
  computed: {
    connectLabel() {
      if (!getAccessToken()) {
        return "未登录";
      }
      return this.isConnected ? "已连接" : "连接中";
    },
  },
  onLoad() {
    wukongImClient.addListener("conversation", this.handleConversationUpdate);
    wukongImClient.addListener("connect", this.handleConnectUpdate);
  },
  onShow() {
    this.refreshConversations();
  },
  onUnload() {
    wukongImClient.removeListener("conversation", this.handleConversationUpdate);
    wukongImClient.removeListener("connect", this.handleConnectUpdate);
  },
  methods: {
    handleConversationUpdate() {
      this.loadLocalConversations();
    },
    handleConnectUpdate(event) {
      this.isConnected = !!event?.isConnected;
    },
    async refreshConversations() {
      if (!getAccessToken()) {
        this.promptLogin();
        return;
      }
      try {
        await wukongImClient.init();
      } catch (error) {
        console.warn("WukongIM refresh failed", error);
      }
      this.loadLocalConversations();
    },
    loadLocalConversations() {
      const rawList = wukongImClient.getConversations();
      this.unreadCount = wukongImClient.getUnreadCount();
      this.conversations = rawList.map((item) => this.formatConversation(item));
      const app = getApp();
      if (app?.globalData) {
        app.globalData.imUnreadCount = this.unreadCount;
        app.globalData.unreadCount = this.unreadCount;
      }
      uni.$emit("updateUnreadCount", this.unreadCount);
    },
    formatConversation(item = {}) {
      const channel = item.channel || {};
      const channelId = channel.channelID || item.channelID || "";
      const channelType = channel.channelType || item.channelType || WUKONG_CHANNEL_TYPE_PERSON;
      const title =
        item.title ||
        item.channelInfo?.title ||
        item.extra?.title ||
        (channelType === 2 ? "供应商群聊" : `供应商 ${channelId || ""}`);
      const digest = getMessageDigest(item.lastMessage || item.recents?.[0] || item);
      return {
        key: `${channelId}_${channelType}`,
        channelId,
        channelType,
        title,
        digest,
        unread: item.unread || 0,
        lastMsgSeq: item.lastMsgSeq || item.lastMessage?.messageSeq || 0,
        timeLabel: formatTime(item.lastMsgTimestamp || item.timestamp || item.lastMessage?.timestamp),
        initial: String(title || "供").slice(0, 1),
      };
    },
    openConversation(item) {
      if (!item.channelId) {
        showToast("缺少会话信息");
        return;
      }
      uni.navigateTo({
        url: `/pages/messages/chat?channelId=${encodeURIComponent(item.channelId)}&channelType=${encodeURIComponent(item.channelType)}&title=${encodeURIComponent(item.title)}&lastMsgSeq=${encodeURIComponent(item.lastMsgSeq || "")}`,
      });
    },
    openSystemChat() {
      uni.navigateTo({
        url: "/pages/messages/chat?channelId=wujin-system&channelType=1&title=平台寻源助手",
      });
    },
    goSourcing() {
      uni.navigateTo({ url: "/pages/wujin/sourcing" });
    },
    promptLogin() {
      uni.showModal({
        title: "需要登录",
        content: "查看供应沟通需要先登录，是否立即登录？",
        confirmText: "去登录",
        cancelText: "取消",
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: "/pages/users/login?redirect=/pages/messages/index" });
          }
        },
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.messages-page {
  padding-bottom: 48rpx;
}

.messages-hero {
  padding-bottom: 54rpx;
}

.connect-state {
  font-size: 24rpx;
  font-weight: 800;
}

.messages-content {
  margin-top: -28rpx;
}

.summary-card {
  position: relative;
  z-index: 2;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.summary-item {
  display: grid;
  gap: 6rpx;
  padding: 16rpx 10rpx;
  border-radius: 16rpx;
  background: #f7f8fa;
  text-align: center;
}

.summary-value {
  font-size: 32rpx;
  font-weight: 900;
  color: #111111;
}

.summary-label {
  color: #5d6673;
  font-size: 22rpx;
}

.notice-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.notice-icon {
  display: flex;
  width: 74rpx;
  height: 74rpx;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 18rpx;
  background: #ffd21e;
}

.notice-copy {
  display: grid;
  flex: 1;
  gap: 6rpx;
}

.notice-title,
.empty-title {
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
}

.notice-desc {
  color: #5d6673;
  font-size: 24rpx;
  line-height: 1.45;
}

.refresh-btn {
  height: 54rpx;
  margin: 0;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #f7f8fa;
  color: #111111;
  font-size: 23rpx;
  line-height: 54rpx;
  font-weight: 800;
}

.refresh-btn::after {
  border: none;
}

.conversation-list {
  display: grid;
}

.conversation-row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #eceff3;
}

.conversation-row:last-child {
  border-bottom: none;
}

.conversation-avatar {
  display: flex;
  width: 80rpx;
  height: 80rpx;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 30rpx;
  font-weight: 900;
}

.conversation-main {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 8rpx;
}

.conversation-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.conversation-title {
  overflow: hidden;
  color: #111111;
  font-size: 29rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time,
.conversation-digest {
  color: #5d6673;
  font-size: 23rpx;
}

.conversation-digest {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-badge {
  display: flex;
  min-width: 34rpx;
  height: 34rpx;
  align-items: center;
  justify-content: center;
  padding: 0 8rpx;
  border-radius: 999rpx;
  background: #d92d20;
  color: #ffffff;
  font-size: 19rpx;
  font-weight: 900;
  box-sizing: border-box;
}
</style>
