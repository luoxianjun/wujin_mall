<template>
  <view class="message-subpage">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-left" @click="goBack">
        <AppIcon class="material-icon" name="arrow_back_ios_new" />
      </view>
      <text class="header-title">评论</text>
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
          <view class="avatar-placeholder">
            <AppIcon class="material-icon" name="mode_comment" />
          </view>
        </view>
        <view class="content-box">
          <view class="item-header">
            <text class="username">{{ getCommenterName(item) }}</text>
            <text class="time">{{ formatDate(item.createTime) }}</text>
          </view>
          <text class="comment-content">{{ item.content }}</text>
        </view>
      </view>
      <view v-if="loading" class="loading-text">加载中...</view>
      <view v-if="!hasMore && list.length > 0" class="no-more-text">
        没有更多了
      </view>
      <view v-if="!loading && list.length === 0" class="empty-state">
        <text>暂无评论消息</text>
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
          noticeType: 2, // 2-Comment
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
      
      // relatedId 现在直接是帖子 ID，可以直接跳转
      if (item.relatedId) {
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
    getCommenterName(item) {
      // 从 content 中提取评论者名称
      // content 格式通常是："用户名 评论了你的帖子/评论：评论内容"
      if (!item.content) return item.title || '用户';
      
      // 提取 content 开头的用户名（在"评论了"之前的部分）
      const match = item.content.match(/^(.+?)\s+评论了/);
      if (match && match[1]) {
        const name = match[1].trim();
        // 检查是否是匿名用户（匿名昵称格式：匿名用户 + 4位数字，如"匿名用户1234"）
        // 匹配以"匿名用户"开头的所有情况
        if (/^匿名用户/.test(name)) {
          return '匿名用户';
        }
        return name;
      }
      
      // 如果无法从 content 提取，使用 title 作为后备
      return item.title || '用户';
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
  background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
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

.comment-content {
  font-size: 28rpx;
  color: #1e293b;
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
