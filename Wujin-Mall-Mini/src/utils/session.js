const TOKEN_KEY = "token";
const REFRESH_TOKEN_KEY = "refresh_token";
const TOKEN_EXPIRES_KEY = "token_expires_at";
const USER_PROFILE_KEY = "user_profile";

export function saveAuthTokens(payload = {}) {
  const { accessToken, refreshToken, expiresTime } = payload || {};
  if (accessToken) {
    uni.setStorageSync(TOKEN_KEY, accessToken);
  } else {
    uni.removeStorageSync(TOKEN_KEY);
  }
  if (refreshToken) {
    uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken);
  } else {
    uni.removeStorageSync(REFRESH_TOKEN_KEY);
  }
  if (expiresTime) {
    uni.setStorageSync(TOKEN_EXPIRES_KEY, expiresTime);
  } else {
    uni.removeStorageSync(TOKEN_EXPIRES_KEY);
  }
}

export function getAccessToken() {
  return uni.getStorageSync(TOKEN_KEY) || "";
}

export function getRefreshToken() {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || "";
}

export function clearAuthSession() {
  uni.removeStorageSync(TOKEN_KEY);
  uni.removeStorageSync(REFRESH_TOKEN_KEY);
  uni.removeStorageSync(TOKEN_EXPIRES_KEY);
  uni.removeStorageSync(USER_PROFILE_KEY);
}

export function saveUserProfile(profile) {
  if (profile) {
    uni.setStorageSync(USER_PROFILE_KEY, profile);
  } else {
    uni.removeStorageSync(USER_PROFILE_KEY);
  }
}

export function getUserProfile() {
  return uni.getStorageSync(USER_PROFILE_KEY) || null;
}

export default {
  saveAuthTokens,
  getAccessToken,
  getRefreshToken,
  clearAuthSession,
  saveUserProfile,
  getUserProfile,
};
