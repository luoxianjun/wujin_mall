import request from "@/utils/request";

export function getUserCenter(opt = {}) {
  return request({
    url: "/forum/user/profile/center",
    method: "GET",
    showLoading: true,
    loadingText: "加载个人中心",
    ...opt,
  });
}

export function getUserStatistics() {
  return request({
    url: "/forum/user/profile/statistics",
    method: "GET",
  });
}

export function getPointBalance() {
  return request({
    url: "/forum/point/balance",
    method: "GET",
  });
}

export function getUserProfileDetail() {
  return request({
    url: "/forum/user/profile/get",
    method: "GET"
  });
}

export function getOtherUserProfileDetail(userId) {
  return request({
    url: "/forum/user/profile/get-by-user-id",
    method: "GET",
    params: { userId },
  });
}

export function sendSchoolEmailCode(email) {
  return request({
    url: "/forum/user/profile/school/send-code",
    method: "POST",
    data: { email },
    showLoading: true,
    loadingText: "发送验证码",
  });
}

export function verifySchoolEmailCode(payload) {
  return request({
    url: "/forum/user/profile/school/verify-code",
    method: "POST",
    data: payload,
  });
}

export function submitSchoolVerification(payload) {
  return request({
    url: "/forum/user/profile/school/submit",
    method: "POST",
    data: payload,
    showLoading: true,
    loadingText: "提交认证",
  });
}

export function updateSchoolInfoPublic(flag) {
  return request({
    url: `/forum/user/profile/school/update-public?schoolInfoPublic=${flag}`,
    method: "PUT",
  });
}

export function followUser(userId) {
  return request({
    url: "/member/user/follow",
    method: "POST",
    params: { userId },
  });
}

export function unfollowUser(userId) {
  return request({
    url: "/member/user/unfollow",
    method: "POST",
    params: { userId },
  });
}

export function updateUserProfile(data) {
  return request({
    url: "/forum/user/profile/update",
    method: "PUT",
    data,
    showLoading: true,
    loadingText: "保存中...",
    showErrorToast: false, // 禁用自动显示错误 toast，由调用方自己处理
  });
}


export function getSignStatus() {
  return request({
    url: "/forum/sign/status",
    method: "GET",
  });
}

export function doSign() {
  return request({
    url: "/forum/sign/do",
    method: "POST",
    showLoading: true,
    loadingText: "签到中",
  });
}

export function getMemberUserInfo() {
  return request({
    url: "/member/user/get",
    method: "GET",
  });
}

export function getMemberPointRecordPage(params) {
  return request({
    url: "/member/point/record/page",
    method: "GET",
    params,
  });
}

export function getMemberSignInConfigList() {
  return request({
    url: "/member/sign-in/config/list",
    method: "GET",
  });
}

export function getMemberSignInSummary() {
  return request({
    url: "/member/sign-in/record/get-summary",
    method: "GET",
  });
}

export function createMemberSignInRecord() {
  return request({
    url: "/member/sign-in/record/create",
    method: "POST",
    showLoading: true,
    loadingText: "\u7B7E\u5230\u4E2D...",
  });
}

export function getPointRecordPage(params) {
  return request({
    url: "/forum/point/record/page",
    method: "GET",
    params,
  });
}



export function getMemberPage(params) {
  return request({
    url: "/forum/user/profile/page",
    method: "GET",
    params,
  });
}

/**
 * 拉黑用户
 * @param {number} targetUserId - 目标用户ID
 */
export function blockUser(targetUserId) {
  return request({
    url: "/forum/user/profile/block",
    method: "POST",
    params: { targetUserId },
    showLoading: true,
    loadingText: "处理中...",
  });
}

/**
 * 取消拉黑用户
 * @param {number} targetUserId - 目标用户ID
 */
export function unblockUser(targetUserId) {
  return request({
    url: "/forum/user/profile/unblock",
    method: "POST",
    params: { targetUserId },
    showLoading: true,
    loadingText: "处理中...",
  });
}

/**
 * 检查是否已拉黑用户
 * @param {number} targetUserId - 目标用户ID
 */
export function isBlocked(targetUserId) {
  return request({
    url: "/forum/user/profile/is-blocked",
    method: "GET",
    params: { targetUserId },
  });
}

/**
 * 更新隐私设置
 * @param {Object} payload - { allowPrivateChat, allowSystemMessage }
 */
export function updatePrivacySettings(payload) {
  return request({
    url: "/forum/user/profile/privacy-settings",
    method: "PUT",
    data: payload,
    showLoading: true,
    loadingText: "保存设置...",
  });
}

