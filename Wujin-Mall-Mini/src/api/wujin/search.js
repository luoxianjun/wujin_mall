import request from "@/utils/request";

/**
 * @param {object} options.silent 为 true 时不显示加载遮罩（例如仅记录筛选行为）
 */
export function searchWujin(params = {}, options = {}) {
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
    showLoading: !options.silent,
    loadingText: "搜索中",
  });
}

/**
 * 按泳道分页查询商品：同一商品只归属一个泳道；加工/原材料泳道返回关键词商品的上游商品
 */
export function getWujinLaneProducts(params = {}) {
  const {
    keyword,
    lane,
    categoryId,
    brandId,
    sourceProductId,
    sort,
    pageNo = 1,
    pageSize = 10,
    ...extraParams
  } = params;

  return request({
    url: "/wujin/search/products",
    method: "GET",
    params: {
      keyword,
      lane,
      categoryId,
      brandId,
      sourceProductId,
      sort,
      pageNo,
      pageSize,
      ...extraParams,
    },
  });
}
