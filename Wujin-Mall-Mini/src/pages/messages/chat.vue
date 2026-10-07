<template>
  <view class="chat-page">
    <view class="chat-header">
      <view class="back-btn" @tap="goBack">
        <AppIcon name="arrow_back" variant="dark" size="36rpx" />
      </view>
      <view class="chat-title-wrap">
        <text class="chat-title">{{ title }}</text>
        <text class="chat-subtitle">{{ channelTypeLabel }}</text>
      </view>
      <view class="header-spacer"></view>
    </view>

    <scroll-view
      class="message-scroll"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
    >
      <view class="message-list">
        <view v-if="!messages.length" class="empty-chat">
          <text class="empty-title">开始供应沟通</text>
          <text class="empty-desc">可以发送规格、数量、交期、认证要求等采购信息。</text>
        </view>

        <view
          v-for="message in messages"
          :id="message.domId"
          :key="message.key"
          class="message-row"
          :class="{ mine: message.mine }"
        >
          <view class="message-bubble">
            <text class="message-text">{{ message.text }}</text>
            <text class="message-time">{{ message.time }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="chat-input-bar">
      <input
        v-model="inputText"
        class="chat-input"
        confirm-type="send"
        placeholder="输入采购需求或报价沟通内容"
        @confirm="sendMessage"
      />
      <button class="send-btn" :disabled="!canSend" :class="{ disabled: !canSend }" @tap="sendMessage">
        发送
      </button>
    </view>
  </view>
</template>

<script>
import {
  wukongImClient,
  WUKONG_CHANNEL_TYPE_PERSON,
  WUKONG_CHANNEL_TYPE_GROUP,
} from "@/utils/wukong-im";
import { getUserProfile, getAccessToken } from "@/utils/session";
import { showToast } from "@/utils/toast";

function formatMessageText(message = {}) {
  const content = message.content || {};
  if (typeof content === "string") {
    return content;
  }
  return content.text || content.content || content.displayText || content.conversationDigest || "[消息]";
}

function formatMessageTime(timestamp) {
  const date = timestamp
    ? new Date(Number(timestamp) * (String(timestamp).length <= 10 ? 1000 : 1))
    : new Date();
  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

function getProfileUserId(profile = {}) {
  return profile.userId || profile.id || profile.uid || profile.memberUserId || profile.memberId || "";
}

export default {
  data() {
    return {
      channelId: "",
      channelType: WUKONG_CHANNEL_TYPE_PERSON,
      title: "供应沟通",
      lastMsgSeq: 0,
      messages: [],
      inputText: "",
      scrollIntoView: "",
      currentUserId: "",
    };
  },
  computed: {
    canSend() {
      return !!this.inputText.trim() && !!this.channelId;
    },
    channelTypeLabel() {
      return Number(this.channelType) === WUKONG_CHANNEL_TYPE_GROUP ? "供应群聊" : "供应商私聊";
    },
  },
  onLoad(options = {}) {
    this.channelId = options.channelId ? decodeURIComponent(options.channelId) : "";
    this.channelType = Number(options.channelType || WUKONG_CHANNEL_TYPE_PERSON);
    this.title = options.title ? decodeURIComponent(options.title) : "供应沟通";
    this.lastMsgSeq = Number(options.lastMsgSeq || 0);
    const profile = getUserProfile() || {};
    this.currentUserId = String(getProfileUserId(profile));
    wukongImClient.addListener("message", this.handleIncomingMessage);
    this.initChat();
  },
  onUnload() {
    wukongImClient.removeListener("message", this.handleIncomingMessage);
  },
  methods: {
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
      } else {
        uni.navigateTo({ url: "/pages/messages/index" });
      }
    },
    async initChat() {
      if (!getAccessToken()) {
        uni.navigateTo({ url: "/pages/users/login?redirect=/pages/messages/index" });
        return;
      }
      if (!this.channelId) {
        showToast("缺少会话对象");
        return;
      }
      await wukongImClient.init();
      await this.loadMessages();
      await wukongImClient.clearUnread(this.channelId, this.channelType, this.lastMsgSeq);
    },
    async loadMessages() {
      const remoteMessages = await wukongImClient.syncMessages(this.channelId, this.channelType, {
        limit: 30,
      });
      this.messages = remoteMessages.map((message, index) => this.formatMessage(message, index));
      this.scrollToBottom();
    },
    formatMessage(message = {}, index = 0) {
      const fromUID = message.fromUID || "";
      const mine = fromUID && String(fromUID) === String(this.currentUserId);
      return {
        key: message.clientMsgNo || message.messageID || `${Date.now()}_${index}`,
        domId: `msg_${index}_${message.messageSeq || message.clientSeq || Date.now()}`,
        text: formatMessageText(message),
        time: formatMessageTime(message.timestamp),
        mine,
      };
    },
    handleIncomingMessage(message) {
      const channel = message?.channel;
      const sameChannel =
        String(channel?.channelID || message?.channelID || "") === String(this.channelId) &&
        Number(channel?.channelType || message?.channelType || this.channelType) === Number(this.channelType);
      if (!sameChannel) {
        return;
      }
      this.messages.push(this.formatMessage(message, this.messages.length));
      this.scrollToBottom();
      wukongImClient.clearUnread(this.channelId, this.channelType, message.messageSeq);
    },
    async sendMessage() {
      const text = this.inputText.trim();
      if (!text) {
        return;
      }
      if (!this.channelId) {
        showToast("缺少会话对象");
        return;
      }
      this.inputText = "";
      try {
        const sent = await wukongImClient.sendTextMessage(text, this.channelId, this.channelType);
        this.messages.push(this.formatMessage(sent, this.messages.length));
        this.scrollToBottom();
      } catch (error) {
        console.error("WukongIM send failed", error);
        showToast(error?.message || "发送失败，请稍后再试");
        this.inputText = text;
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const last = this.messages[this.messages.length - 1];
        if (last) {
          this.scrollIntoView = last.domId;
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.chat-page {
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  background: #f7f8fa;
  color: #111111;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 68rpx 28rpx 24rpx;
  background: #ffd21e;
}

.back-btn,
.header-spacer {
  display: flex;
  width: 68rpx;
  height: 68rpx;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
}

.back-btn {
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.62);
}

.chat-title-wrap {
  display: grid;
  min-width: 0;
  flex: 1;
  gap: 4rpx;
  text-align: center;
}

.chat-title {
  overflow: hidden;
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-subtitle {
  color: rgba(17, 17, 17, 0.66);
  font-size: 22rpx;
  font-weight: 700;
}

.message-scroll {
  flex: 1;
  min-height: 0;
}

.message-list {
  display: grid;
  gap: 18rpx;
  padding: 26rpx 28rpx 34rpx;
}

.empty-chat {
  display: grid;
  gap: 10rpx;
  margin-top: 120rpx;
  padding: 34rpx;
  border: 1rpx dashed #cbd1d9;
  border-radius: 20rpx;
  background: #ffffff;
  text-align: center;
}

.empty-title {
  color: #111111;
  font-size: 32rpx;
  font-weight: 900;
}

.empty-desc {
  color: #5d6673;
  font-size: 25rpx;
  line-height: 1.55;
}

.message-row {
  display: flex;
  justify-content: flex-start;
}

.message-row.mine {
  justify-content: flex-end;
}

.message-bubble {
  display: grid;
  max-width: 74%;
  gap: 8rpx;
  padding: 18rpx 20rpx;
  border: 1rpx solid #eceff3;
  border-radius: 6rpx 20rpx 20rpx 20rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(17, 17, 17, 0.05);
}

.message-row.mine .message-bubble {
  border-color: #111111;
  border-radius: 20rpx 6rpx 20rpx 20rpx;
  background: #111111;
  color: #ffd21e;
}

.message-text {
  font-size: 28rpx;
  line-height: 1.55;
  word-break: break-word;
}

.message-time {
  color: #8b95a1;
  font-size: 20rpx;
}

.message-row.mine .message-time {
  color: rgba(255, 210, 30, 0.7);
}

.chat-input-bar {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #eceff3;
  background: #ffffff;
}

.chat-input {
  flex: 1;
  height: 76rpx;
  min-width: 0;
  padding: 0 22rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 16rpx;
  background: #f7f8fa;
  color: #111111;
  font-size: 27rpx;
  box-sizing: border-box;
}

.send-btn {
  width: 132rpx;
  height: 76rpx;
  margin: 0;
  border-radius: 16rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 27rpx;
  line-height: 76rpx;
  font-weight: 900;
}

.send-btn::after {
  border: none;
}

.send-btn.disabled {
  opacity: 0.42;
}
</style>
