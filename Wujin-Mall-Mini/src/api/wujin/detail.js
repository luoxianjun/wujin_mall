import request from "@/utils/request";

export function getWujinEntityDetail(params = {}) {
  const {
    id,
    entityType,
    lane,
    keyword,
    sourceKeyword,
    industry,
    ...extraParams
  } = params;

  return request({
    url: "/wujin/detail/entity",
    method: "GET",
    params: {
      id,
      entityType,
      lane,
      keyword,
      sourceKeyword,
      industry,
      ...extraParams,
    },
    showLoading: true,
    loadingText: "加载详情",
  });
}

/**
 * 商城商品真实详情；商品不存在或已下架时返回 exists=false
 */
export function getWujinProductDetail(id) {
  return request({
    url: "/wujin/detail/product",
    method: "GET",
    params: { id },
    showLoading: true,
    loadingText: "加载详情",
  });
}

export function getWujinTraceGraph(params = {}) {
  const { id, entityType, lane, keyword, sourceKeyword, industry, ...extraParams } =
    params;

  return request({
    url: "/wujin/trace/graph",
    method: "GET",
    params: {
      id,
      entityType,
      lane,
      keyword,
      sourceKeyword,
      industry,
      ...extraParams,
    },
    showLoading: true,
    loadingText: "加载图谱",
  });
}
