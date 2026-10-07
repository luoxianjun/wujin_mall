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
