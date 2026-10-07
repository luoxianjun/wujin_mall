const DEFAULT_SHARE_TITLE = "五金商城 - 成品·加工·原材料三泳道寻源溯源";
const DEFAULT_SHARE_PATH = "/pages/wujin/search";

function encodeQuery(options = {}) {
  return Object.keys(options)
    .filter((key) => options[key] !== undefined && options[key] !== null)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(options[key])}`)
    .join("&");
}

function getCurrentPageSharePath() {
  try {
    if (typeof getCurrentPages !== "function") {
      return DEFAULT_SHARE_PATH;
    }
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    if (!currentPage || !currentPage.route) {
      return DEFAULT_SHARE_PATH;
    }
    const route = currentPage.route.startsWith("/")
      ? currentPage.route
      : `/${currentPage.route}`;
    const query = encodeQuery(currentPage.options || {});
    return query ? `${route}?${query}` : route;
  } catch (e) {
    console.warn("Failed to build default share path:", e);
    return DEFAULT_SHARE_PATH;
  }
}

function getDefaultShareInfo() {
  const path = getCurrentPageSharePath();
  const [, query = ""] = path.split("?");
  return {
    title: DEFAULT_SHARE_TITLE,
    path,
    query,
  };
}

export default {
  onLoad() {
    // #ifdef MP-WEIXIN
    if (typeof uni.showShareMenu === "function") {
      uni.showShareMenu({
        menus: ["shareAppMessage", "shareTimeline"],
      });
    }
    // #endif
  },
  onShareAppMessage() {
    const shareInfo = getDefaultShareInfo();
    return {
      title: shareInfo.title,
      path: shareInfo.path,
    };
  },
  onShareTimeline() {
    const shareInfo = getDefaultShareInfo();
    return {
      title: shareInfo.title,
      query: shareInfo.query,
    };
  },
};
