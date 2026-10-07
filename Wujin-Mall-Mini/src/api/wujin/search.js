import request from "@/utils/request";

export function searchWujin(params = {}) {
  const {
    keyword,
    requestedLane,
    entryPath,
    sourceLane,
    sourceKeyword,
    sourceProductId,
    sourceEntityId,
    industry,
    categoryId,
    categoryPath,
    ...extraParams
  } = params;

  return request({
    url: "/wujin/search/result",
    method: "GET",
    params: {
      keyword,
      requestedLane,
      entryPath,
      sourceLane,
      sourceKeyword,
      sourceProductId,
      sourceEntityId,
      industry,
      categoryId,
      categoryPath,
      ...extraParams,
    },
    showLoading: true,
    loadingText: "搜索中",
  });
}
