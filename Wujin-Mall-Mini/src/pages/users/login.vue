<template>
  <view class="login-page">
    <view class="login-hero">
      <view class="login-top">
        <view class="back-btn" @tap="goBack">
          <AppIcon name="arrow_back" variant="dark" size="38rpx" />
        </view>
        <text class="login-brand">五金商城</text>
        <view class="top-spacer"></view>
      </view>

      <text class="hero-title">工业采购入口</text>
      <text class="hero-desc">
        登录后同步寻源线索、供应沟通、询价进度和收藏记录。
      </text>

      <view class="hero-metrics">
        <view class="metric-item">
          <text class="metric-value">3</text>
          <text class="metric-label">泳道搜索</text>
        </view>
        <view class="metric-item">
          <text class="metric-value">24h</text>
          <text class="metric-label">线索响应</text>
        </view>
        <view class="metric-item">
          <text class="metric-value">IM</text>
          <text class="metric-label">供应沟通</text>
        </view>
      </view>
    </view>

    <view class="login-body">
      <view class="login-card">
        <view class="section-head">
          <text class="section-title">手机号登录</text>
          <text class="section-note">微信授权优先，验证码兜底</text>
        </view>

        <button
          class="primary-login-btn"
          type="default"
          :class="{ disabled: !agreed || logging }"
          :disabled="!agreed || logging"
          open-type="getPhoneNumber"
          @getphonenumber="handlePhoneNumberAuthorize"
          @tap="handleOneTapTip"
        >
          <AppIcon name="touch_app" variant="light" size="34rpx" />
          <text>{{ logging ? "登录中..." : "手机号一键授权登录" }}</text>
        </button>

        <button
          class="secondary-login-btn"
          type="default"
          :disabled="logging"
          @tap="toggleSmsPanel"
        >
          {{ showSmsPanel ? "收起验证码登录" : "使用验证码登录" }}
        </button>

        <view v-if="showSmsPanel" class="sms-panel">
          <view class="phone-row">
            <picker
              class="area-picker"
              mode="selector"
              :range="areaCodeOptions"
              range-key="label"
              :value="areaCodeIndex"
              @change="onAreaCodeChange"
            >
              <view class="area-inner">
                <text>{{ areaCodeOptions[areaCodeIndex].label }}</text>
                <AppIcon name="expand_more" variant="muted" size="24rpx" />
              </view>
            </picker>
            <input
              v-model="smsForm.phone"
              class="login-input"
              maxlength="11"
              placeholder="请输入手机号"
              type="number"
            />
          </view>

          <view class="code-row">
            <input
              v-model="smsForm.code"
              class="login-input code-input"
              maxlength="6"
              placeholder="短信验证码"
              type="number"
            />
            <button
              class="code-btn"
              :disabled="countdown > 0 || isSendingCode"
              :class="{ disabled: countdown > 0 || isSendingCode }"
              @tap="sendSmsCode"
            >
              {{ countdown > 0 ? `${countdown}s` : "获取验证码" }}
            </button>
          </view>

          <button
            class="submit-btn"
            :disabled="!canSubmit || logging"
            :class="{ disabled: !canSubmit || logging }"
            @tap="handleSmsLogin"
          >
            登录 / 注册
          </button>
        </view>
      </view>

      <view class="agreement">
        <view class="agree-box" :class="{ checked: agreed }" @tap="toggleAgreement"></view>
        <text class="agreement-text">
          登录即表示同意
          <text class="link" @tap.stop="goToTerms">《用户协议》</text>
          和
          <text class="link" @tap.stop="goToPrivacy">《隐私政策》</text>
        </text>
      </view>
    </view>

    <view v-if="showProfileModal" class="profile-modal-mask">
      <view class="profile-modal">
        <text class="profile-modal-title">完善采购名片</text>
        <text class="profile-modal-desc">头像和昵称会用于供应沟通</text>
        <button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
          <image
            v-if="profileForm.avatar"
            :src="profileForm.avatar"
            class="avatar-img"
            mode="aspectFill"
          />
          <view v-else class="avatar-placeholder">
            <AppIcon name="photo_camera" variant="muted" size="42rpx" />
            <text>选择头像</text>
          </view>
        </button>
        <input
          v-model="profileForm.nickname"
          class="nickname-input"
          placeholder="填写微信昵称或采购联系人"
          type="nickname"
          @blur="onNicknameBlur"
        />
        <button
          class="modal-confirm"
          :disabled="!profileForm.nickname"
          :class="{ disabled: !profileForm.nickname }"
          @tap="confirmProfile"
        >
          进入五金商城
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { loginByWeixinPhone, sendLoginSmsCode, smsLogin } from "@/api/auth";
import { getMemberUserInfo, getUserCenter, updateUserProfile } from "@/api/user";
import { saveAuthTokens, saveUserProfile } from "@/utils/session";
import { showToast, showSuccess } from "@/utils/toast";
import { wukongImClient } from "@/utils/wukong-im";

export default {
  data() {
    return {
      redirectUrl: "",
      smsForm: {
        phone: "",
        code: "",
      },
      areaCodeIndex: 0,
      areaCodeOptions: [
        { label: "+86", value: "86" },
        { label: "+853", value: "853" },
      ],
      agreed: false,
      logging: false,
      isSendingCode: false,
      showSmsPanel: false,
      countdown: 0,
      timer: null,
      showProfileModal: false,
      profileForm: {
        avatar: "",
        nickname: "",
      },
    };
  },
  computed: {
    currentAreaCode() {
      return this.areaCodeOptions[this.areaCodeIndex].value;
    },
    isPhoneValid() {
      if (this.currentAreaCode === "853") {
        return /^6\d{7}$/.test(this.smsForm.phone);
      }
      return /^1\d{10}$/.test(this.smsForm.phone);
    },
    canSubmit() {
      return this.agreed && this.isPhoneValid && this.smsForm.code.length >= 4;
    },
  },
  onLoad(query = {}) {
    if (query.redirect) {
      this.redirectUrl = decodeURIComponent(query.redirect);
    }
  },
  beforeUnmount() {
    this.clearTimer();
  },
  beforeDestroy() {
    this.clearTimer();
  },
  methods: {
    goBack() {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        uni.navigateBack();
        return;
      }
      uni.reLaunch({ url: "/pages/wujin/search" });
    },
    toggleAgreement() {
      this.agreed = !this.agreed;
    },
    toggleSmsPanel() {
      this.showSmsPanel = !this.showSmsPanel;
    },
    onAreaCodeChange(event) {
      this.areaCodeIndex = Number(event.detail.value) || 0;
    },
    handleOneTapTip() {
      if (!this.agreed) {
        showToast("请先勾选协议");
      }
    },
    async handlePhoneNumberAuthorize(event) {
      if (!this.agreed) {
        showToast("请先勾选协议");
        return;
      }
      const phoneCode = event?.detail?.code;
      if (!phoneCode) {
        showToast(event?.detail?.errMsg || "未获取到手机号授权");
        return;
      }
      if (this.logging) {
        return;
      }
      this.logging = true;
      try {
        const loginCode = await this.getWechatLoginCode();
        const tokenResp = await loginByWeixinPhone({
          phoneCode,
          loginCode,
          state: this.generateState(),
        });
        await this.finishLogin(tokenResp);
      } catch (error) {
        console.error("Wujin one-tap login failed", error);
        showToast(error?.msg || error?.message || "登录失败，请稍后再试");
      } finally {
        this.logging = false;
      }
    },
    async sendSmsCode() {
      if (this.countdown > 0 || this.isSendingCode) {
        return;
      }
      if (!this.validatePhone()) {
        return;
      }
      this.isSendingCode = true;
      try {
        await sendLoginSmsCode(this.fullMobile());
        this.countdown = 60;
        this.startCountdown();
        showToast("验证码已发送");
      } catch (error) {
        console.error("Wujin SMS send failed", error);
        showToast(error?.msg || error?.message || "发送失败，请稍后再试");
      } finally {
        this.isSendingCode = false;
      }
    },
    async handleSmsLogin() {
      if (!this.canSubmit || this.logging) {
        return;
      }
      this.logging = true;
      try {
        const tokenResp = await smsLogin({
          mobile: this.fullMobile(),
          code: this.smsForm.code,
        });
        await this.finishLogin(tokenResp);
      } catch (error) {
        console.error("Wujin SMS login failed", error);
        showToast(error?.msg || error?.message || "登录失败，请稍后再试");
      } finally {
        this.logging = false;
      }
    },
    async finishLogin(tokenResp) {
      if (!tokenResp?.accessToken) {
        showToast("未获取到登录凭证");
        return;
      }
      saveAuthTokens(tokenResp);
      const profile = await this.loadProfile();
      if (profile) {
        saveUserProfile(profile);
        this.profileForm.avatar = profile.avatar || "";
        this.profileForm.nickname = profile.nickname || profile.name || "";
      }
      try {
        await wukongImClient.init();
      } catch (error) {
        console.warn("WukongIM init after login failed", error);
      }
      showSuccess("登录成功");
      if (!this.profileForm.nickname) {
        this.showProfileModal = true;
      } else {
        this.navigateAfterLogin();
      }
    },
    async loadProfile() {
      const results = await Promise.allSettled([
        getMemberUserInfo(),
        getUserCenter({ showLoading: false }),
      ]);
      const member = results[0].status === "fulfilled" ? results[0].value : null;
      const center = results[1].status === "fulfilled" ? results[1].value : null;
      const merged = {
        ...(member || {}),
        ...(center || {}),
      };
      if (!Object.keys(merged).length) {
        return null;
      }
      if (!merged.userId && (merged.id || merged.uid)) {
        merged.userId = merged.id || merged.uid;
      }
      return merged;
    },
    navigateAfterLogin() {
      const url = this.redirectUrl || "/pages/wujin/search";
      setTimeout(() => {
        uni.reLaunch({ url });
      }, 260);
    },
    onChooseAvatar(event) {
      const avatarUrl = event.detail?.avatarUrl;
      if (avatarUrl) {
        this.profileForm.avatar = avatarUrl;
      }
    },
    onNicknameBlur(event) {
      const value = event.detail?.value;
      if (value) {
        this.profileForm.nickname = value;
      }
    },
    async confirmProfile() {
      if (!this.profileForm.nickname) {
        showToast("请填写联系人昵称");
        return;
      }
      try {
        await updateUserProfile({
          avatar: this.profileForm.avatar,
          nickname: this.profileForm.nickname,
        });
      } catch (error) {
        console.warn("Wujin profile update failed", error);
      }
      const profile = await this.loadProfile();
      saveUserProfile({
        ...(profile || {}),
        avatar: this.profileForm.avatar || profile?.avatar,
        nickname: this.profileForm.nickname,
      });
      this.showProfileModal = false;
      this.navigateAfterLogin();
    },
    getWechatLoginCode() {
      return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        uni.login({
          provider: "weixin",
          onlyAuthorize: true,
          success: (res) => {
            if (res.code) {
              resolve(res.code);
            } else {
              reject(new Error("未获取到微信登录凭证"));
            }
          },
          fail: reject,
        });
        // #endif
        // #ifndef MP-WEIXIN
        reject(new Error("请在微信小程序内使用一键登录"));
        // #endif
      });
    },
    generateState() {
      return `wujin_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`;
    },
    fullMobile() {
      return this.currentAreaCode === "86"
        ? this.smsForm.phone
        : `${this.currentAreaCode}${this.smsForm.phone}`;
    },
    validatePhone() {
      if (this.isPhoneValid) {
        return true;
      }
      showToast(this.currentAreaCode === "853" ? "请输入正确的澳门手机号" : "请输入正确手机号");
      return false;
    },
    startCountdown() {
      this.clearTimer();
      this.timer = setInterval(() => {
        if (this.countdown <= 1) {
          this.clearTimer();
          this.countdown = 0;
        } else {
          this.countdown -= 1;
        }
      }, 1000);
    },
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    goToTerms() {
      uni.navigateTo({ url: "/pages/profile-sub/terms" });
    },
    goToPrivacy() {
      uni.navigateTo({ url: "/pages/profile-sub/privacy" });
    },
  },
};
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #f7f8fa;
  color: #111111;
}

.login-hero {
  position: relative;
  overflow: hidden;
  padding: 70rpx 34rpx 42rpx;
  background: #ffd21e;
}

.login-hero::after {
  content: "";
  position: absolute;
  right: -90rpx;
  top: 90rpx;
  width: 300rpx;
  height: 300rpx;
  border: 30rpx solid rgba(17, 17, 17, 0.08);
  transform: rotate(20deg);
}

.login-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn,
.top-spacer {
  width: 72rpx;
  height: 72rpx;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.62);
}

.login-brand {
  font-size: 28rpx;
  font-weight: 800;
}

.hero-title {
  position: relative;
  z-index: 1;
  display: block;
  margin-top: 54rpx;
  font-size: 54rpx;
  line-height: 1.16;
  font-weight: 900;
}

.hero-desc {
  position: relative;
  z-index: 1;
  display: block;
  width: 560rpx;
  max-width: 100%;
  margin-top: 18rpx;
  color: rgba(17, 17, 17, 0.76);
  font-size: 28rpx;
  line-height: 1.55;
}

.hero-metrics {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
  margin-top: 34rpx;
}

.metric-item {
  display: grid;
  gap: 4rpx;
  padding: 18rpx 16rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.62);
}

.metric-value {
  font-size: 30rpx;
  font-weight: 900;
}

.metric-label {
  font-size: 21rpx;
  color: rgba(17, 17, 17, 0.68);
}

.login-body {
  display: grid;
  gap: 20rpx;
  padding: 26rpx 28rpx 48rpx;
}

.login-card {
  display: grid;
  gap: 20rpx;
  padding: 26rpx;
  border: 1rpx solid #eceff3;
  border-radius: 20rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 30rpx rgba(17, 17, 17, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 900;
}

.section-note {
  color: #5d6673;
  font-size: 23rpx;
}

.primary-login-btn,
.secondary-login-btn,
.submit-btn,
.code-btn,
.modal-confirm {
  margin: 0;
  border: none;
  border-radius: 16rpx;
  font-weight: 800;
}

.primary-login-btn::after,
.secondary-login-btn::after,
.submit-btn::after,
.code-btn::after,
.modal-confirm::after {
  border: none;
}

.primary-login-btn {
  display: flex;
  height: 86rpx;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 29rpx;
  line-height: 86rpx;
}

.secondary-login-btn {
  height: 74rpx;
  background: #f7f8fa;
  color: #111111;
  font-size: 26rpx;
  line-height: 74rpx;
}

.disabled {
  opacity: 0.48;
}

.sms-panel {
  display: grid;
  gap: 16rpx;
}

.phone-row,
.code-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.area-picker {
  flex-shrink: 0;
}

.area-inner {
  display: flex;
  height: 76rpx;
  align-items: center;
  gap: 4rpx;
  padding: 0 18rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 16rpx;
  background: #ffffff;
  font-size: 26rpx;
}

.login-input {
  flex: 1;
  height: 76rpx;
  min-width: 0;
  padding: 0 20rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 16rpx;
  background: #ffffff;
  color: #111111;
  font-size: 28rpx;
  box-sizing: border-box;
}

.code-input {
  width: 0;
}

.code-btn {
  width: 194rpx;
  height: 76rpx;
  background: #ffd21e;
  color: #111111;
  font-size: 25rpx;
  line-height: 76rpx;
}

.submit-btn {
  height: 78rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 27rpx;
  line-height: 78rpx;
}

.agreement {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 12rpx;
  padding: 0 10rpx;
}

.agree-box {
  position: relative;
  width: 34rpx;
  height: 34rpx;
  flex-shrink: 0;
  margin-top: 2rpx;
  border: 2rpx solid #aeb6c2;
  border-radius: 10rpx;
}

.agree-box.checked {
  border-color: #111111;
  background: #ffd21e;
}

.agree-box.checked::after {
  content: "";
  position: absolute;
  left: 10rpx;
  top: 5rpx;
  width: 9rpx;
  height: 17rpx;
  border: 4rpx solid #111111;
  border-top: 0;
  border-left: 0;
  transform: rotate(45deg);
}

.agreement-text {
  color: #5d6673;
  font-size: 23rpx;
  line-height: 1.45;
}

.link {
  color: #111111;
  font-weight: 800;
}

.profile-modal-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(17, 17, 17, 0.52);
}

.profile-modal {
  display: grid;
  width: 620rpx;
  gap: 22rpx;
  padding: 40rpx;
  border-radius: 22rpx;
  background: #ffffff;
  box-sizing: border-box;
}

.profile-modal-title {
  font-size: 36rpx;
  font-weight: 900;
  text-align: center;
}

.profile-modal-desc {
  color: #5d6673;
  font-size: 25rpx;
  text-align: center;
}

.avatar-btn {
  justify-self: center;
  width: 156rpx;
  height: 156rpx;
  margin: 0;
  padding: 0;
  border-radius: 999rpx;
  background: #f7f8fa;
  overflow: hidden;
}

.avatar-btn::after {
  border: none;
}

.avatar-img {
  width: 156rpx;
  height: 156rpx;
}

.avatar-placeholder {
  display: grid;
  height: 156rpx;
  place-items: center;
  align-content: center;
  gap: 8rpx;
  color: #5d6673;
  font-size: 22rpx;
}

.nickname-input {
  height: 82rpx;
  padding: 0 20rpx;
  border: 1rpx solid #d8dde5;
  border-radius: 16rpx;
  background: #ffffff;
  color: #111111;
  font-size: 28rpx;
  box-sizing: border-box;
}

.modal-confirm {
  height: 82rpx;
  background: #111111;
  color: #ffd21e;
  font-size: 28rpx;
  line-height: 82rpx;
}
</style>
