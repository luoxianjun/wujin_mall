import {
  getAccessToken,
  getRefreshToken,
  saveAuthTokens,
  clearAuthSession,
} from "@/utils/session";
import { showToast } from "@/utils/toast";

const ENV_CONFIG = {
  dev: "http://localhost:16610/app-api",
  sit: "https://wj.halcyonz.com/app-api",
  prod: "https://wj.halcyonz.com/app-api",
};

/** 当前环境：dev | sit | prod，修改此处即可切换接口 baseUrl */
const CURRENT_ENV = "prod";
const DEFAULT_API_BASE_URL = ENV_CONFIG[CURRENT_ENV];
const API_BASE_KEY = "API_BASE_URL";
const VALID_API_ORIGINS = ["https://wj.halcyonz.com", "http://localhost:16610"];
let tokenExpiredNotice = false;
let refreshPromise = null;

export function resolveBaseUrl() {
  const storageBaseUrl = normalizeBaseUrl(uni.getStorageSync(API_BASE_KEY));
  const envBaseUrl = normalizeBaseUrl(import.meta.env?.VITE_APP_API_BASE);
  return (
    storageBaseUrl ||
    envBaseUrl ||
    DEFAULT_API_BASE_URL
  );
}

function normalizeBaseUrl(url) {
  const value = String(url || "").replace(/\/$/, "");
  if (!value) {
    return "";
  }
  if (!VALID_API_ORIGINS.some((origin) => value.startsWith(origin))) {
    uni.removeStorageSync?.(API_BASE_KEY);
    return "";
  }
  return value;
}

function buildRequestUrl(url) {
  return url.startsWith("http://") || url.startsWith("https://")
    ? url
    : `${resolveBaseUrl()}${url.startsWith("/") ? url : `/${url}`}`;
}

function shouldSkipRefresh(url = "") {
  const lower = url.toLowerCase();
  return (
    lower.includes("/member/auth/weixin-mini-app-login") ||
    lower.includes("/member/auth/sms-login") ||
    lower.includes("/member/auth/login") ||
    lower.includes("/member/auth/refresh-token")
  );
}

export function setApiBaseUrl(url) {
  if (url) {
    uni.setStorageSync(API_BASE_KEY, url);
  }
}

export function request(options) {
  const config = { ...options };
  const {
    url,
    data,
    params,
    method = "GET",
    header = {},
    showLoading = false,
    loadingText = "加载中",
    showErrorToast = true, // 是否自动显示错误 toast，默认为 true
  } = config;
  const token = getAccessToken();
  const tenantId = 1;
  const finalHeader = {
    "Content-Type": "application/json",
    ...header,
    "tenant-id": tenantId,
  };
  if (token) {
    finalHeader.Authorization = `Bearer ${token}`;
  }
  const displayLoading = showLoading && !config._retry;
  if (displayLoading) {
    uni.showLoading({ title: loadingText, mask: true });
  }
  let requestUrl = buildRequestUrl(url);
  if (params && typeof params === "object") {
    const search = Object.entries(params)
      .filter(
        ([, value]) => value !== undefined && value !== null && value !== ""
      )
      .map(
        ([key, value]) =>
          `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`
      )
      .join("&");
    if (search) {
      requestUrl += (requestUrl.includes("?") ? "&" : "?") + search;
    }
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: requestUrl,
      method,
      data,
      header: finalHeader,
      timeout: 15000,
      success: (res) => {
        const { statusCode, data: body } = res;
        if (
          body &&
          Object.prototype.hasOwnProperty.call(body, "code") &&
          Object.prototype.hasOwnProperty.call(body, "data")
        ) {
          if (body.code === 0) {
            resolve(body.data);
            return;
          }
          if (isUnauthorized(body.code)) {
            attemptRefreshAndRetry(config, requestUrl, resolve, reject);
          } else {
            if (showErrorToast) {
              handleError(body.code, body.msg || "请求失败");
            }
            reject(body);
          }
          return;
        }
        if (statusCode >= 200 && statusCode < 300) {
          resolve(body);
        } else if (isUnauthorized(statusCode)) {
          attemptRefreshAndRetry(config, requestUrl, resolve, reject);
        } else {
          if (showErrorToast) {
            handleError(statusCode, body?.msg || "请求失败");
          }
          reject(body);
        }
      },
      fail: (err) => {
        showToast("网络异常，请稍后再试");
        reject(err);
      },
      complete: () => {
        if (displayLoading) {
          uni.hideLoading();
        }
      },
    });
  });
}

function attemptRefreshAndRetry(options, requestUrl, resolve, reject) {
  if (options._retry || shouldSkipRefresh(requestUrl)) {
    handleUnauthorized();
    reject({ code: 401, msg: "未授权" });
    return;
  }
  refreshAccessToken()
    .then(() => {
      request({ ...options, _retry: true, showLoading: false })
        .then(resolve)
        .catch(reject);
    })
    .catch((err) => {
      handleUnauthorized();
      reject({ code: 401, msg: "登录失效" });
    });
}

function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    return Promise.reject(new Error("NO_REFRESH_TOKEN"));
  }
  if (!refreshPromise) {
    refreshPromise = new Promise((resolve, reject) => {
      const tenantId =
        uni.getStorageSync("tenant-id") !== ""
          ? uni.getStorageSync("tenant-id")
          : 1;
      uni.request({
        url: `${resolveBaseUrl()}/member/auth/refresh-token?refreshToken=${encodeURIComponent(
          refreshToken
        )}`,
        method: "POST",
        header: {
          "Content-Type": "application/json",
          "tenant-id": tenantId,
        },
        success: (res) => {
          const body = res.data || {};
          if (body.code === 0 && body.data) {
            saveAuthTokens(body.data);
            resolve(body.data);
          } else {
            reject(body);
          }
        },
        fail: (error) => reject(error),
        complete: () => {
          refreshPromise = null;
        },
      });
    });
  }
  return refreshPromise;
}

function isUnauthorized(code) {
  return code === 401 || code === 40101;
}

function handleUnauthorized() {
  clearAuthSession();
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const route = currentPage?.route || "";
  const options = currentPage?.options || {};
  const query = Object.entries(options)
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
    .join("&");
  const redirect = route ? `/${route}${query ? `?${query}` : ""}` : "/pages/wujin/search";

  if (!tokenExpiredNotice) {
    tokenExpiredNotice = true;
    showToast("登录失效，请重新登录");
    setTimeout(() => {
      tokenExpiredNotice = false;
      if (currentPage && currentPage.route.includes("pages/users/login")) {
        return;
      }
      uni.reLaunch({
        url: `/pages/users/login?redirect=${encodeURIComponent(redirect)}`,
        fail(error) {
          console.error("Navigate to login failed", error);
        },
      });
    }, 800);
  }
}

function handleError(code, message) {
  if (isUnauthorized(code)) {
    handleUnauthorized();
    return;
  }
  showToast(message || "请求失败");
}

export default request;
