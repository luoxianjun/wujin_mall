/**
 * 状态栏高度 mixin
 * 解决安卓端 CSS env(safe-area-inset-top) 不生效的问题
 *
 * 使用方法：
 * 1. 在页面中导入: import statusBarMixin from '@/mixins/statusBar'
 * 2. 在 mixins 中注册: mixins: [statusBarMixin]
 * 3. 在模板中使用计算属性:
 *    - statusBarHeight: 状态栏高度 (px)
 *    - statusBarStyle: { paddingTop: 'Xpx' }
 *    - navBarStyle: { height: 'calc(88rpx + Xpx)', paddingTop: 'Xpx' }
 */
export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarContentHeight: 44,
    }
  },
  computed: {
    // 状态栏内边距样式
    statusBarStyle() {
      return {
        paddingTop: `${this.statusBarHeight}px`
      }
    },
    // 导航栏样式 (高度88rpx + 状态栏高度)
    navBarStyle() {
      const totalHeight = this.statusBarHeight + this.navBarContentHeight
      return {
        height: `${totalHeight}px`,
        paddingTop: `${this.statusBarHeight}px`,
        boxSizing: 'border-box'
      }
    },
    // 页面顶部内容区域样式 (顶部padding = 状态栏 + 导航栏高度)
    topContentStyle() {
      const topOffset = this.statusBarHeight + this.navBarContentHeight
      return {
        paddingTop: `${topOffset}px`
      }
    },
    // 吸顶元素的 top 值
    stickyTopStyle() {
      const topOffset = this.statusBarHeight + this.navBarContentHeight
      return {
        top: `${topOffset}px`
      }
    }
  },
  created() {
    this.initStatusBarHeight()
  },
  methods: {
    initStatusBarHeight() {
      try {
        const systemInfo = uni.getSystemInfoSync()
        this.statusBarHeight = systemInfo.statusBarHeight || 0
        const menuButtonInfo = typeof uni.getMenuButtonBoundingClientRect === 'function'
          ? uni.getMenuButtonBoundingClientRect()
          : (typeof wx !== 'undefined' && typeof wx.getMenuButtonBoundingClientRect === 'function'
              ? wx.getMenuButtonBoundingClientRect()
              : null)

        if (menuButtonInfo && menuButtonInfo.top && menuButtonInfo.height) {
          const gap = Math.max(menuButtonInfo.top - this.statusBarHeight, 0)
          this.navBarContentHeight = Math.round(gap * 2 + menuButtonInfo.height)
          return
        }

        const windowWidth = systemInfo.windowWidth || 375
        this.navBarContentHeight = Math.round((windowWidth / 750) * 88)
      } catch (e) {
        console.warn('Failed to get statusBarHeight:', e)
        this.statusBarHeight = 0
        this.navBarContentHeight = 44
      }
    }
  }
}
