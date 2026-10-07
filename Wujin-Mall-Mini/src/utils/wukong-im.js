import WKSDK, {
  MessageText,
  Channel,
  ChannelTypePerson,
  ChannelTypeGroup,
  Message,
  ConversationAction,
} from "wukongimjssdk";

import { getAccessToken, getUserProfile } from "@/utils/session";
import { resolveBaseUrl } from "@/utils/request";

const CONVERSATION_CACHE_KEY = "WUJIN_WK_CONVERSATIONS";
// ponytail: temporary kill switch until /im-ws is deployed; flip to true when chat resumes.
const IM_ENABLED = false;

function trimRightSlash(value = "") {
  return String(value).replace(/\/+$/, "");
}

function resolveImApiUrl() {
  const envUrl = import.meta.env?.VITE_IM_API_URL || import.meta.env?.VITE_WUJIN_IM_API_URL;
  if (envUrl) {
    return trimRightSlash(envUrl);
  }
  return trimRightSlash(resolveBaseUrl()).replace(/\/app-api$/, "/im-api");
}

function resolveImWsUrl() {
  const envUrl = import.meta.env?.VITE_IM_WS_URL || import.meta.env?.VITE_WUJIN_IM_WS_URL;
  if (envUrl) {
    return envUrl;
  }
  return trimRightSlash(resolveBaseUrl())
    .replace(/^https:/, "wss:")
    .replace(/^http:/, "ws:")
    .replace(/\/app-api$/, "/im-ws");
}

function getProfileUserId(profile = {}) {
  return (
    profile.userId ||
    profile.id ||
    profile.uid ||
    profile.memberUserId ||
    profile.memberId ||
    profile.mobile ||
    ""
  );
}

function decodeBase64Json(payload) {
  if (!payload) {
    return null;
  }
  try {
    const arrayBuffer = uni.base64ToArrayBuffer(payload);
    const bytes = new Uint8Array(arrayBuffer);
    let output = "";
    for (let index = 0; index < bytes.length; index += 1) {
      output += String.fromCharCode(bytes[index]);
    }
    return JSON.parse(decodeURIComponent(escape(output)));
  } catch (error) {
    try {
      const arrayBuffer = uni.base64ToArrayBuffer(payload);
      const bytes = new Uint8Array(arrayBuffer);
      let output = "";
      for (let index = 0; index < bytes.length; index += 1) {
        output += String.fromCharCode(bytes[index]);
      }
      return JSON.parse(output);
    } catch (innerError) {
      console.warn("[WukongIM] payload decode failed", error, innerError);
      return null;
    }
  }
}

function createMessageFromServer(item = {}) {
  const message = new Message();
  message.messageID = item.message_idstr || item.messageID || item.message_id || "";
  message.messageSeq = item.message_seq || item.messageSeq || 0;
  message.clientMsgNo = item.client_msg_no || item.clientMsgNo || "";
  message.fromUID = item.from_uid || item.fromUID || "";
  message.channelID = item.channel_id || item.channelID || "";
  message.channelType = item.channel_type || item.channelType || ChannelTypePerson;
  message.timestamp = item.timestamp || 0;
  const payload = decodeBase64Json(item.payload);
  if (payload?.type === 1) {
    message.content = new MessageText(payload.content || "");
  } else if (payload) {
    message.content = payload;
  }
  return message;
}

class WukongIMService {
  constructor() {
    this.isConnected = false;
    this.sdkListenersBound = false;
    this.listeners = [];
    this.conversationList = [];
    this.currentUserId = "";
    this.bindProviderCallbacks();
  }

  bindProviderCallbacks() {
    if (!IM_ENABLED) {
      return;
    }
    const sdk = WKSDK.shared();
    sdk.config.debug = false;
    if (!sdk.config.provider) {
      sdk.config.provider = {};
    }

    sdk.config.provider.syncConversationsCallback = async () => {
      const uid = sdk.config.uid;
      if (!uid) {
        return [];
      }
      try {
        const result = await this.request("/conversation/sync", "POST", {
          uid,
          version: 0,
          limit: 100,
        });
        const list = Array.isArray(result) ? result : result?.conversations || [];
        const conversations = list.map((item) => ({
          channel: new Channel(item.channel_id || item.channelID, item.channel_type || item.channelType),
          unread: item.unread || 0,
          lastMsgTimestamp: item.timestamp || item.lastMsgTimestamp || 0,
          lastMsgSeq: item.last_msg_seq || item.lastMsgSeq || 0,
          lastClientMsgNo: item.last_client_msg_no || item.lastClientMsgNo || "",
          version: item.version || 0,
          recents: item.recents || [],
          title: item.title || item.channel_name || "",
          avatar: item.avatar || "",
        }));
        if (conversations.length) {
          this.conversationList = conversations;
          this.saveConversationsToCache();
          this.notifyListeners("conversation", { action: "sync", conversations });
        }
        return conversations;
      } catch (error) {
        console.warn("[WukongIM] sync conversations failed", error);
        return [];
      }
    };

    sdk.config.provider.syncMessagesCallback = async (channel, options = {}) => {
      try {
        const result = await this.request("/channel/messagesync", "POST", {
          login_uid: sdk.config.uid,
          channel_id: channel.channelID,
          channel_type: channel.channelType,
          start_message_seq: options.startMessageSeq || 0,
          end_message_seq: options.endMessageSeq || 0,
          limit: options.limit || 20,
          pull_mode: options.pullMode || 1,
        });
        const messages = Array.isArray(result) ? result : result?.messages || [];
        return messages.map(createMessageFromServer);
      } catch (error) {
        console.warn("[WukongIM] sync messages failed", error);
        return [];
      }
    };
  }

  syncSessionConfig() {
    const token = getAccessToken();
    const profile = getUserProfile() || {};
    const userId = getProfileUserId(profile);
    if (!token || !userId) {
      return null;
    }
    const sdk = WKSDK.shared();
    this.currentUserId = String(userId);
    sdk.config.addr = resolveImWsUrl();
    sdk.config.uid = this.currentUserId;
    sdk.config.token = token;
    return { token, userId: this.currentUserId, profile };
  }

  async init() {
    if (!IM_ENABLED) {
      this.isConnected = false;
      this.conversationList = [];
      return false;
    }
    const session = this.syncSessionConfig();
    if (!session) {
      return false;
    }
    this.loadConversationsFromCache();
    const sdk = WKSDK.shared();
    if (!this.sdkListenersBound) {
      sdk.connectManager.addConnectStatusListener((status) => {
        this.isConnected = status === 1;
        this.notifyListeners("connect", { status, isConnected: this.isConnected });
      });
      sdk.chatManager.addMessageListener((message) => {
        this.notifyListeners("message", message);
      });
      sdk.conversationManager.addConversationListener((conversation, action) => {
        this.updateConversationList(conversation, action);
        this.notifyListeners("conversation", { conversation, action });
      });
      this.sdkListenersBound = true;
    }
    sdk.connectManager.connect();
    try {
      await sdk.conversationManager.sync({});
    } catch (error) {
      console.warn("[WukongIM] conversation sync failed", error);
    }
    return true;
  }

  connect() {
    if (!IM_ENABLED) {
      this.isConnected = false;
      return false;
    }
    const session = this.syncSessionConfig();
    if (!session) {
      return false;
    }
    WKSDK.shared().connectManager.connect();
    return true;
  }

  disconnect() {
    try {
      WKSDK.shared().connectManager.disconnect();
    } catch (error) {
      console.warn("[WukongIM] disconnect failed", error);
    }
    this.isConnected = false;
  }

  async sendTextMessage(text, channelId, channelType = ChannelTypePerson) {
    if (!IM_ENABLED) {
      throw new Error("聊天功能暂未开放");
    }
    const session = this.syncSessionConfig();
    if (!session) {
      throw new Error("请先登录后再发送消息");
    }
    if (!channelId) {
      throw new Error("缺少会话对象");
    }
    const message = await WKSDK.shared().chatManager.send(
      new MessageText(text),
      new Channel(String(channelId), Number(channelType) || ChannelTypePerson),
    );
    if (message) {
      message.fromUID = session.userId;
      this.notifyListeners("message", message);
    }
    return message;
  }

  async syncMessages(channelId, channelType = ChannelTypePerson, options = {}) {
    if (!IM_ENABLED) {
      return [];
    }
    if (!channelId) {
      return [];
    }
    const session = this.syncSessionConfig();
    if (!session) {
      return [];
    }
    const channel = new Channel(String(channelId), Number(channelType) || ChannelTypePerson);
    try {
      const result = await WKSDK.shared().chatManager.syncMessages(channel, {
        limit: 30,
        ...options,
      });
      return Array.isArray(result) ? result : [];
    } catch (error) {
      console.warn("[WukongIM] sync messages failed", error);
      return [];
    }
  }

  getConversations() {
    if (!IM_ENABLED) {
      return [];
    }
    return [...this.conversationList].sort(
      (a, b) => (b.lastMsgTimestamp || 0) - (a.lastMsgTimestamp || 0),
    );
  }

  getUnreadCount() {
    if (!IM_ENABLED) {
      return 0;
    }
    return this.conversationList.reduce((sum, item) => sum + (item.unread || 0), 0);
  }

  async clearUnread(channelId, channelType, messageSeq) {
    if (!IM_ENABLED) {
      return;
    }
    const conversation = this.conversationList.find(
      (item) =>
        String(item.channel?.channelID) === String(channelId) &&
        Number(item.channel?.channelType) === Number(channelType),
    );
    if (conversation) {
      conversation.unread = 0;
      this.saveConversationsToCache();
      this.notifyListeners("conversation", {
        conversation,
        action: ConversationAction.update,
      });
    }
    const session = this.syncSessionConfig();
    if (!session) {
      return;
    }
    const seq = messageSeq || conversation?.lastMsgSeq || 0;
    if (!seq) {
      return;
    }
    try {
      await this.request("/conversations/clearUnread", "POST", {
        uid: session.userId,
        channel_id: channelId,
        channel_type: Number(channelType) || ChannelTypePerson,
        message_seq: seq,
      });
    } catch (error) {
      console.warn("[WukongIM] clear unread failed", error);
    }
  }

  updateConversationList(conversation, action) {
    if (!conversation?.channel) {
      return;
    }
    const index = this.conversationList.findIndex(
      (item) =>
        String(item.channel?.channelID) === String(conversation.channel.channelID) &&
        Number(item.channel?.channelType) === Number(conversation.channel.channelType),
    );
    if (action === ConversationAction.remove) {
      if (index >= 0) {
        this.conversationList.splice(index, 1);
      }
    } else if (index >= 0) {
      this.conversationList.splice(index, 1, conversation);
    } else {
      this.conversationList.push(conversation);
    }
    this.saveConversationsToCache();
  }

  loadConversationsFromCache() {
    const session = this.syncSessionConfig();
    if (!session) {
      return;
    }
    const cached = uni.getStorageSync(`${CONVERSATION_CACHE_KEY}_${session.userId}`);
    if (Array.isArray(cached)) {
      this.conversationList = cached.map((item) => ({
        ...item,
        channel: new Channel(item.channel.channelID, item.channel.channelType),
      }));
    }
  }

  saveConversationsToCache() {
    if (!this.currentUserId) {
      return;
    }
    const cache = this.conversationList.map((item) => ({
      channel: {
        channelID: item.channel?.channelID,
        channelType: item.channel?.channelType,
      },
      unread: item.unread || 0,
      lastMsgTimestamp: item.lastMsgTimestamp || item.timestamp || item.lastMessage?.timestamp || 0,
      lastMsgSeq: item.lastMsgSeq || item.lastMessage?.messageSeq || 0,
      lastClientMsgNo: item.lastClientMsgNo || item.lastMessage?.clientMsgNo || "",
      version: item.version || 0,
      title: item.title || item.channelInfo?.title || item.extra?.title || "",
      recents: [],
    }));
    uni.setStorageSync(`${CONVERSATION_CACHE_KEY}_${this.currentUserId}`, cache);
  }

  clearConversationsCache() {
    if (this.currentUserId) {
      uni.removeStorageSync(`${CONVERSATION_CACHE_KEY}_${this.currentUserId}`);
    }
    this.conversationList = [];
    this.currentUserId = "";
  }

  addListener(event, callback) {
    this.listeners.push({ event, callback });
  }

  removeListener(event, callback) {
    this.listeners = this.listeners.filter(
      (item) => item.event !== event || item.callback !== callback,
    );
  }

  notifyListeners(event, data) {
    this.listeners.forEach((item) => {
      if (item.event === event) {
        item.callback(data);
      }
    });
  }

  request(path, method, data) {
    if (!IM_ENABLED) {
      return Promise.resolve(null);
    }
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${resolveImApiUrl()}${path}`,
        method,
        data,
        timeout: 15000,
        header: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${getAccessToken()}`,
          "tenant-id": uni.getStorageSync("tenant-id") || 1,
        },
        success: (res) => {
          const body = res.data || {};
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(Object.prototype.hasOwnProperty.call(body, "data") ? body.data : body);
          } else {
            reject(body || res);
          }
        },
        fail: reject,
      });
    });
  }
}

export const WUKONG_CHANNEL_TYPE_PERSON = ChannelTypePerson;
export const WUKONG_CHANNEL_TYPE_GROUP = ChannelTypeGroup;
export const wukongImClient = new WukongIMService();

export default wukongImClient;
