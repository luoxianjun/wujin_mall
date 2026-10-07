import { TUILogin } from "@tencentcloud/tui-core-lite";
import { TUIChatEngine, TUIStore, StoreName } from "@tencentcloud/chat-uikit-engine-lite";
import { getUserCenter } from "@/api/user";
import { getAccessToken, getUserProfile, saveUserProfile } from "@/utils/session";

// 暴露模块供外部使用
export function getIMModules() {
  return { TUILogin, TUIChatEngine, TUIStore, StoreName };
}

export function setupGlobalIMUnreadWatcher() {
  if (!uni.$imUnreadWatcher) {
    uni.$imUnreadWatcher = (count) => {
      if (!getAccessToken()) return;
      const app = getApp();
      if (!app?.globalData) return;
      app.globalData.imUnreadCount = count || 0;
      app.updateTotalUnread?.();
    };
  }

  try {
    TUIStore.unwatch(StoreName.CONV, {
      totalUnreadCount: uni.$imUnreadWatcher,
    });
  } catch (error) {
    // 忽略首次 unwatch 失败
  }

  TUIStore.watch(StoreName.CONV, {
    totalUnreadCount: uni.$imUnreadWatcher,
  });

  const unreadCount = TUIStore.getData?.(StoreName.CONV, "totalUnreadCount");
  if (typeof unreadCount === "number") {
    uni.$imUnreadWatcher(unreadCount);
  }
}

// 标记是否正在刷新，避免重复刷新
let isRefreshing = false;
let refreshPromise = null;

/**
 * 刷新 IM 登录（重新获取 userSig 并登录）
 * @returns {Promise<boolean>} 刷新是否成功
 */
export async function refreshIMLogin() {
  // 如果正在刷新，返回现有的 Promise
  if (isRefreshing && refreshPromise) {
    return refreshPromise;
  }

  isRefreshing = true;
  refreshPromise = doRefreshIMLogin();

  try {
    const result = await refreshPromise;
    return result;
  } finally {
    isRefreshing = false;
    refreshPromise = null;
  }
}

async function doRefreshIMLogin() {
  try {
    console.log("[IM Helper] 开始刷新 userSig...");

    // 1. 从后台获取新的 userSig
    const center = await getUserCenter();
    if (!center || !center.userSig) {
      console.warn("[IM Helper] 获取 userSig 失败：返回数据无效");
      return false;
    }

    // 2. 更新本地存储的用户信息
    const oldProfile = getUserProfile() || {};
    const newProfile = { ...oldProfile, ...center };
    saveUserProfile(newProfile);

    // 3. 先登出再重新登录
    try {
      await TUILogin.logout();
    } catch (e) {
      // 忽略登出错误
      console.warn("[IM Helper] 登出时出错（可忽略）:", e);
    }

    // 4. 使用新的 userSig 重新登录
    const userID = String(center.userId || center.id);
    uni.$userID = userID;

    await TUILogin.login({
      SDKAppID: uni.$SDKAppID,
      userID: userID,
      userSig: center.userSig,
      framework: "vue3",
    });

    // 5. 等待 SDK 就绪
    await waitForSDKReady();

    console.log("[IM Helper] userSig 刷新成功");
    return true;
  } catch (error) {
    console.error("[IM Helper] 刷新 userSig 失败:", error);
    return false;
  }
}

/**
 * 等待 SDK 就绪
 * @param {number} timeout 超时时间（毫秒）
 * @returns {Promise<void>}
 */
export function waitForSDKReady(timeout = 15000) {
  return new Promise((resolve, reject) => {
    const { chat } = TUILogin.getContext();

    if (chat && chat.isReady()) {
      resolve();
      return;
    }

    const timer = setTimeout(() => {
      reject(new Error("等待 SDK 就绪超时"));
    }, timeout);

    const onReady = () => {
      clearTimeout(timer);
      if (chat) {
        chat.off(TUIChatEngine.EVENT.SDK_READY, onReady);
      }
      resolve();
    };

    if (chat) {
      chat.on(TUIChatEngine.EVENT.SDK_READY, onReady);
    } else {
      clearTimeout(timer);
      reject(new Error("Chat 实例不存在"));
    }
  });
}

/**
 * 检查 IM 是否可用，如果不可用则尝试刷新
 * @returns {Promise<boolean>} IM 是否可用
 */
export async function ensureIMReady() {
  const { chat } = TUILogin.getContext();

  // 如果 SDK 已就绪，直接返回
  if (chat && chat.isReady()) {
    return true;
  }

  // 检查是否有 userSig（兼容 userId/id/uid）
  const userProfile = getUserProfile();
  const userId = userProfile && (userProfile.userId || userProfile.id || userProfile.uid);
  if (!userId) {
    console.warn("[IM Helper] 用户未登录");
    return false;
  }

  // 尝试刷新 IM 登录
  return await refreshIMLogin();
}

/**
 * 检查错误是否是 userSig 相关错误
 * @param {Error|Object} error 错误对象
 * @returns {boolean}
 */
export function isUserSigError(error) {
  if (!error) return false;

  const code = error.code || error.errorCode;
  // 70001: USERSIG_EXPIRED
  // 70003: USERSIG_INVALID
  // 70005: USERSIG_EMPTY
  // 70009: USERSIG_NOT_MATCH
  const userSigErrorCodes = [70001, 70003, 70005, 70009];

  if (userSigErrorCodes.includes(code)) {
    return true;
  }

  // 检查错误消息
  const message = (error.message || error.msg || "").toLowerCase();
  if (
    message.includes("usersig") ||
    message.includes("expired") ||
    message.includes("invalid signature")
  ) {
    return true;
  }

  return false;
}

/**
 * 设置 SDK 错误监听器（在 App.vue 中调用）
 * @param {Function} onUnreadCountChange 未读消息数变化回调
 */
export function setupSDKErrorListener(onUnreadCountChange) {
  const { chat } = TUILogin.getContext();
  if (!chat) return;

  // 监听被踢下线事件
  chat.on(TUIChatEngine.EVENT.KICKED_OUT, async (event) => {
    console.warn("[IM Helper] 被踢下线:", event.data?.type);

    // 如果是因为签名过期被踢，尝试刷新
    if (event.data?.type === TUIChatEngine.TYPES.KICKED_OUT_USERSIG_EXPIRED) {
      const success = await refreshIMLogin();
      if (success && onUnreadCountChange) {
        // 重新监听未读消息
        TUIStore.watch(StoreName.CONV, {
          totalUnreadCount: onUnreadCountChange,
        });
      }
    }
  });

  // 监听 SDK 未就绪错误
  chat.on(TUIChatEngine.EVENT.ERROR, async (event) => {
    console.error("[IM Helper] SDK 错误:", event);

    if (isUserSigError(event)) {
      await refreshIMLogin();
    }
  });
}

/**
 * 同步会话列表（带重试机制）
 */
export async function syncConversationList(maxRetries = 3, retryDelay = 1500) {
  const { chat } = TUILogin.getContext();
  if (!chat || !chat.isReady()) {
    console.warn("[IM Helper] SDK 未就绪，无法同步会话列表");
    return false;
  }

  for (let i = 0; i < maxRetries; i++) {
    try {
      const result = await chat.getConversationList();
      const list = result?.data?.conversationList || [];
      console.log(
        `[IM Helper] 同步会话列表完成，共 ${list.length} 个会话（第${i + 1}次尝试）`
      );
      if (list.length > 0) {
        return true;
      }
      if (i < maxRetries - 1) {
        console.log(
          `[IM Helper] 会话列表为空，${retryDelay}ms 后重试...`
        );
        await new Promise((resolve) => setTimeout(resolve, retryDelay));
        retryDelay = Math.round(retryDelay * 1.5);
      }
    } catch (error) {
      console.error(
        `[IM Helper] 同步会话列表失败（第${i + 1}次尝试）:`,
        error
      );
      if (i < maxRetries - 1) {
        await new Promise((resolve) => setTimeout(resolve, retryDelay));
        retryDelay = Math.round(retryDelay * 1.5);
      }
    }
  }
  return true;
}

export default {
  refreshIMLogin,
  waitForSDKReady,
  ensureIMReady,
  isUserSigError,
  setupSDKErrorListener,
  syncConversationList,
  getIMModules,
  setupGlobalIMUnreadWatcher,
};
