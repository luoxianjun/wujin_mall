<template>
  <view class="user-select-page">
    <view class="header">
      <view class="header-left">
        <view class="header-right" @click="goBack">
        <image src="/static/icons/back.svg" class="back-icon" />
      </view>

      </view>
      <button
          class="confirm-btn"
          :class="{ disabled: selectedUsers.length === 0 }"
          @click="handleConfirm"
        >
          确定({{ selectedUsers.length }})
        </button>
      <text class="header-title">选择联系人</text>

    </view>

    <view class="search-bar">
      <input
        class="search-input"
        type="text"
        placeholder="搜索用户昵称"
        :value="keyword"
        @input="onSearchInput"
        confirm-type="search"
        @confirm="onSearchConfirm"
      />
      <text v-if="keyword" class="search-clear" @click="clearSearch">✕</text>
    </view>

    <scroll-view
      scroll-y
      class="user-list"
      :style="{ height: scrollHeight + 'px' }"
      @scrolltolower="loadMore"
      :lower-threshold="100"
    >
      <view
        v-for="user in userList"
        :key="user.userId"
        class="user-item"
        @click="selectUser(user)"
      >
        <view class="user-avatar">
          <image
            :src="
              user.avatar ||
              'https://web.sdk.qcloud.com/component/TUIKit/assets/avatar_21.png'
            "
            mode="aspectFill"
            class="avatar-img"
          />
        </view>
        <view class="user-info">
          <text class="user-name">{{ user.nickname || user.userId }}</text>
        </view>
        <view class="user-select">
          <view
            class="radio-circle"
            :class="{ selected: isSelected(user) }"
          >
            <view class="radio-inner" v-if="isSelected(user)"></view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { showToast, showSuccess } from "@/utils/toast";
import { getMemberPage } from "@/api/user";
import { ensureIMReady, getIMModules } from "@/utils/im-helper";

export default {
  data() {
    return {
      userList: [],
      selectedUsers: [],
      pageNo: 1,
      pageSize: 20,
      total: 0,
      isLoading: false,
      keyword: "",
      searchTimer: null,
      scrollHeight: 0,
    };
  },
  onLoad() {
    this.calcScrollHeight();
    this.getMemberList();
  },
  methods: {
    calcScrollHeight() {
      const sysInfo = uni.getSystemInfoSync();
      // header ~56px + search-bar ~52px + status-bar
      const headerHeight = (sysInfo.statusBarHeight || 44) + 56 + 52;
      this.scrollHeight = sysInfo.windowHeight - headerHeight;
    },
    goBack() {
      uni.navigateBack();
    },
    onSearchInput(e) {
      const val = e.detail.value;
      this.keyword = val;
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        this.resetAndSearch();
      }, 400);
    },
    onSearchConfirm() {
      if (this.searchTimer) clearTimeout(this.searchTimer);
      this.resetAndSearch();
    },
    clearSearch() {
      this.keyword = "";
      this.resetAndSearch();
    },
    resetAndSearch() {
      this.pageNo = 1;
      this.userList = [];
      this.total = 0;
      this.isLoading = false;
      this.getMemberList();
    },
    getMemberList() {
      if (this.isLoading) return;
      this.isLoading = true;

      const params = {
        pageNo: this.pageNo,
        pageSize: this.pageSize,
      };
      if (this.keyword.trim()) {
        params.nickname = this.keyword.trim();
      }

      getMemberPage(params)
        .then((res) => {
          if (this.pageNo === 1) {
            this.userList = res.list;
          } else {
            this.userList = [...this.userList, ...res.list];
          }
          this.total = res.total;
        })
        .catch((err) => {
          console.warn("getMemberPage error:", err);
          showToast("获取用户列表失败");
        })
        .finally(() => {
          this.isLoading = false;
        });
    },
    loadMore() {
      if (this.isLoading) return;
      if (this.userList.length < this.total) {
        this.pageNo++;
        this.getMemberList();
      }
    },
    isSelected(user) {
      return this.selectedUsers.some(u => u.userId === user.userId);
    },
    selectUser(user) {
      const index = this.selectedUsers.findIndex(u => u.userId === user.userId);
      if (index > -1) {
        this.selectedUsers.splice(index, 1);
      } else {
        this.selectedUsers.push(user);
      }
    },
    async handleConfirm() {
      if (this.selectedUsers.length === 0) return;

      // 检查 TUIChatEngine 是否已就绪，如果未就绪则尝试刷新 userSig
      const { TUILogin, TUIChatEngine } = getIMModules();
      const { TUIGroupService } = require("@tencentcloud/chat-uikit-engine-lite");
      const { chat } = TUILogin.getContext();
      if (!chat || !chat.isReady()) {
        uni.showLoading({ title: "连接中...", mask: true });
        try {
          const isReady = await ensureIMReady();
          uni.hideLoading();
          if (!isReady) {
            showToast("消息服务连接失败，请重新登录");
            return;
          }
        } catch (error) {
          uni.hideLoading();
          showToast("消息服务未就绪，请稍后再试");
          return;
        }
      }

      // Create group chat with selected users
      // Filter out invalid users if necessary, and map to userID string
      const memberList = this.selectedUsers.map(user => ({
        userID: String(user.userId),
        // nick: user.nickname, // Optional: pass nick if needed
        // avatar: user.avatar // Optional
      }));

      const groupName = this.selectedUsers.map(u => u.nickname).join(", ").slice(0, 20) + (this.selectedUsers.length > 3 ? "..." : "");

      const options = {
        name: groupName || "群聊",
        type: TUIChatEngine.TYPES.GRP_WORK, // Using Work type for normal group chat
        memberList: memberList,
      };

      TUIGroupService.createGroup(options)
        .then((res) => {
          const conversationID = `GROUP${res.data.group.groupID}`;
          uni.redirectTo({
            url: `/pages/messages/chat?conversationID=${conversationID}`,
          });
        })
        .catch((err) => {
          console.warn("createGroup error:", err);
          showToast("创建群聊失败");
        });
    },
  },
};
</script>

<style lang="scss">
.user-select-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 44px 16px 12px; // Adjust for status bar
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 20px; // 避开小程序右上角胶囊按钮
}

.back-icon {
  width: 24px;
  height: 24px;
}

.cancel-text {
  font-size: 14px;
  color: #666;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  flex: 1;
  text-align: left;
  margin-left: 20rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.search-input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  background-color: #f5f5f5;
  border-radius: 18px;
  border: none;
}

.search-clear {
  margin-left: 8px;
  font-size: 14px;
  color: #999;
  padding: 4px;
}

.confirm-btn {
  font-size: 14px;
  color: #fff;
  background-color: #007aff;
  padding: 4px 12px;
  border-radius: 4px;
  line-height: 1.5;
  margin: 0;

  &.disabled {
    background-color: #ccc;
    color: #fff;
  }
}

.user-list {
  flex: 1;
  padding: 12px 0;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #f5f5f5;

  &:active {
    background-color: #fafafa;
  }
}

.user-avatar {
  margin-right: 12px;
}

.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #eee;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  color: #333;
}

.user-select {
  margin-left: 12px;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;

  &.selected {
    border-color: #007aff;
    background-color: #007aff;
  }
}

.radio-inner {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #fff;
}
</style>
