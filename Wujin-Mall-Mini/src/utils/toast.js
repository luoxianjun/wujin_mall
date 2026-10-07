/**
 * 全局 Toast 工具
 * 统一管理 showToast 的默认配置
 */

const DEFAULT_DURATION = 2500; // 默认显示 2.5 秒

/**
 * 显示 Toast 提示
 * @param {string|object} options - 提示文本或配置对象
 * @param {string} options.title - 提示文本
 * @param {string} options.icon - 图标类型: success/error/loading/none
 * @param {number} options.duration - 显示时长(ms)
 * @param {boolean} options.mask - 是否显示透明蒙层
 */
export function showToast(options) {
  const config = typeof options === 'string'
    ? { title: options }
    : { ...options };

  uni.showToast({
    icon: 'none',
    duration: DEFAULT_DURATION,
    ...config,
  });
}

/**
 * 显示成功提示
 * @param {string} title - 提示文本
 * @param {number} duration - 显示时长(ms)
 */
export function showSuccess(title, duration = DEFAULT_DURATION) {
  uni.showToast({
    title,
    icon: 'success',
    duration,
  });
}

/**
 * 显示错误提示
 * @param {string} title - 提示文本
 * @param {number} duration - 显示时长(ms)
 */
export function showError(title, duration = DEFAULT_DURATION) {
  uni.showToast({
    title,
    icon: 'error',
    duration,
  });
}

/**
 * 显示加载中
 * @param {string} title - 提示文本
 */
export function showLoading(title = '加载中...') {
  uni.showLoading({
    title,
    mask: true,
  });
}

/**
 * 隐藏加载
 */
export function hideLoading() {
  uni.hideLoading();
}

export default {
  show: showToast,
  success: showSuccess,
  error: showError,
  loading: showLoading,
  hideLoading,
};
