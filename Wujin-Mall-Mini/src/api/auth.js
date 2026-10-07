import request from "@/utils/request";

export function loginByWeixinPhone(data) {
  return request({
    url: "/member/auth/weixin-mini-app-login",
    method: "POST",
    data,
    showLoading: true,
    loadingText: "登录中...",
  });
}

export function sendLoginSmsCode(mobile) {
  return request({
    url: "/member/auth/send-sms-code",
    method: "POST",
    data: {
      mobile,
      scene: 1,
    },
    showLoading: true,
    loadingText: "发送中...",
  });
}

export function smsLogin(data) {
  return request({
    url: "/member/auth/sms-login",
    method: "POST",
    data,
    showLoading: true,
    loadingText: "登录中...",
  });
}
