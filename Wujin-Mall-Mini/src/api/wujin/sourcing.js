import request from "@/utils/request";

export function getWujinSupplierCandidates(params = {}) {
  const { keyword, lane, sourceKeyword, industry, ...extraParams } = params;

  return request({
    url: "/wujin/sourcing/supplier-candidates",
    method: "GET",
    params: {
      keyword,
      lane,
      sourceKeyword,
      industry,
      ...extraParams,
    },
    showLoading: true,
    loadingText: "加载供应商",
  });
}

export function submitWujinSourcingLead(data = {}) {
  return request({
    url: "/wujin/sourcing/lead/submit",
    method: "POST",
    data,
    showLoading: true,
    loadingText: "提交中",
  });
}

export function getWujinSupplierCapability(params = {}) {
  const {
    supplierId,
    supplierType,
    keyword,
    lane,
    sourceKeyword,
    industry,
    ...extraParams
  } = params;

  return request({
    url: "/wujin/sourcing/supplier-capability",
    method: "GET",
    params: {
      supplierId,
      supplierType,
      keyword,
      lane,
      sourceKeyword,
      industry,
      ...extraParams,
    },
    showLoading: true,
    loadingText: "加载供应能力",
  });
}

export function getWujinSourcingLeadProgress(params = {}) {
  const { leadId } = params;

  return request({
    url: "/wujin/sourcing/lead/progress",
    method: "GET",
    params: { leadId },
    showLoading: true,
    loadingText: "加载线索进度",
  });
}

export function getWujinMyLeads() {
  return request({
    url: "/wujin/sourcing/lead/my-list",
    method: "GET",
    showLoading: true,
    loadingText: "加载我的线索",
  });
}
