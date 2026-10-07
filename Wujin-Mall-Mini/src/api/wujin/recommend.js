import request from "@/utils/request";

export function getWujinProductRecommendations(params = {}) {
  const { pageNo = 1, pageSize = 10, keyword, categoryId, sortField, sortAsc, ...extraParams } = params;

  return request({
    url: "/product/spu/page",
    method: "GET",
    params: {
      pageNo,
      pageSize,
      keyword,
      categoryId,
      sortField,
      sortAsc,
      ...extraParams,
    },
  });
}
