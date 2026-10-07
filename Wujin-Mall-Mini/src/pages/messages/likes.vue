<template>
  <view class="message-subpage">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-left" @click="goBack">
        <AppIcon class="material-icon" name="arrow_back_ios_new" />
      </view>
      <text class="header-title">赞</text>
      <view class="header-right"></view>
    </view>
    <scroll-view scroll-y class="list-container" @scrolltolower="loadMore">
      <view 
        v-for="item in list" 
        :key="item.id" 
        class="message-item" 
        :class="{ 'unread': !item.readStatus }"
        @click="handleItemClick(item)"
      >
        <view class="avatar-box">
          <!-- Use a default icon since avatar is not provided -->
          <view class="avatar-placeholder">
            <AppIcon class="material-icon" name="favorite" />
          </view>
        </view>
        <view class="content-box">
          <view class="item-header">
            <text class="username">{{ item.title }}</text>
            <text class="time">{{ formatDate(item.createTime) }}</text>
          </view>
          <text class="action-text">{{ item.content }}</text>
        </view>
      </view>
      <view v-if="loading" class="loading-text">加载中...</view>
      <view v-if="!hasMore && list.length > 0" class="no-more-text">
        没有更多了
      </view>
      <view v-if="!loading && list.length === 0" class="empty-state">
        <text>暂无点赞消息</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getSystemNoticePage, markNoticeAsRead } from "@/api/message";
import statusBarMixin from "@/mixins/statusBar";

export default {
  mixins: [statusBarMixin],
  data() {
    return {
      list: [],
      pageNo: 1,
      pageSize: 20,
      hasMore: true,
      loading: false,
    };
  },
  onLoad() {
    this.loadData();
  },
  methods: {
    async loadData() {
      if (this.loading || !this.hasMore) return;
      this.loading = true;
      try {
        const res = await getSystemNoticePage({
          noticeType: 1, // 1-Like
          pageNo: this.pageNo,
          pageSize: this.pageSize,
        });
        
        const newList = res.list || [];
        if (this.pageNo === 1) {
          this.list = newList;
        } else {
          this.list = [...this.list, ...newList];
        }
        
        if (newList.length < this.pageSize) {
            this.hasMore = false;
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.loading = false;
      }
    },
    loadMore() {
      this.pageNo++;
      this.loadData();
    },
    async handleItemClick(item) {
      if (!item.readStatus) {
        try {
          await markNoticeAsRead(item.id);
          item.readStatus = true;
        } catch (e) {
          console.error("Failed to mark as read", e);
        }
      }
      
      if (item.relatedType === 1 && item.relatedId) {
        uni.navigateTo({ url: `/pages/posts/detail?id=${item.relatedId}` });
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    },
    goBack() {
      uni.navigateBack();
    }
  },
};
</script>

<style lang="scss">
.material-icon {
  font-weight: normal;
  font-style: normal;
  display: inline-block;
  line-height: 1;
  text-transform: none;
  letter-spacing: normal;
  word-wrap: normal;
  white-space: nowrap;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
  text-rendering: optimizeLegibility;
  -moz-osx-font-smoothing: grayscale;
  font-feature-settings: "liga";
}

.message-subpage {
  height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0 32rpx 0;
  height: 88rpx;
  box-sizing: content-box;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #111b2c;
}

.header-left,
.header-right {
  width: 60rpx;
  display: flex;
  align-items: center;
}

.list-container {
  flex: 1;
  height: 0;
}

.message-item {
  display: flex;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f1f5f9;
  background: #fff;
  transition: background 0.2s;
  
  &.unread {
    background: #f0f9ff;
  }
  
  &:active {
    background: #f8fafc;
  }
}

.avatar-box {
  margin-right: 24rpx;
}

.avatar-placeholder {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .material-icon {
    color: #fff;
    font-size: 40rpx;
  }
}

.content-box {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.username {
  font-size: 30rpx;
  font-weight: 600;
  color: #0f172a;
}

.time {
  font-size: 24rpx;
  color: #94a3b8;
}

.action-text {
  font-size: 28rpx;
  color: #475467;
  line-height: 1.5;
}

.loading-text,
.no-more-text,
.empty-state {
  text-align: center;
  padding: 32rpx;
  color: #94a3b8;
  font-size: 26rpx;
}
</style>
