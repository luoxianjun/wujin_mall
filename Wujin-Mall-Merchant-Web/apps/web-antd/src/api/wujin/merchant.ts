import { requestClient } from '#/api/request';

type ListRequestOptions = {
  silentErrorMessage?: boolean;
};

function createListRequestConfig(
  params?: Record<string, any>,
  options?: ListRequestOptions,
) {
  return {
    params,
    silentErrorMessage: options?.silentErrorMessage,
  };
}

export namespace WujinMerchantApi {
  export interface RelationSubmission {
    id?: number;
    merchantId?: number;
    productId?: number;
    productName?: string;
    productLane?: WujinLane;
    productCategoryId?: number;
    templateId?: number;
    auditStatus?: number;
    auditRoute?: string;
    auditReason?: string;
    completenessScore?: number;
    completenessSuggestion?: string;
    missingItems?: string[] | string;
    remark?: string;
    createTime?: string;
  }

  export interface RelationSubmissionUpdateRequest extends RelationSubmission {
    certificationCount?: number;
    hasApplicationDescription?: boolean;
  }

  export interface RelationItem {
    id?: number;
    submissionId?: number;
    entityId?: number;
    relationType?: string;
    fromTemplate?: boolean;
    requiredFlag?: boolean;
    remark?: string;
    createTime?: string;
  }

  export interface ChainEntity {
    id?: number;
    entityCode?: string;
    name?: string;
    lane?: WujinLane;
    industries?: string;
    junctionFlag?: boolean;
    riskNote?: string;
    status?: number;
    createTime?: string;
  }

  export interface ChainEntityListParams extends Partial<ChainEntity> {
    industry?: string;
  }

  export interface RelationSubmitRequest {
    merchantId?: number;
    productId?: number;
    productName?: string;
    productCategoryId?: number;
    productBrandId?: number;
    productPicUrl?: string;
    productPrice?: number;
    productMarketPrice?: number;
    productCostPrice?: number;
    productStock?: number;
    supplyEntityId?: number;
    supplyMinOrderQuantity?: number;
    supplyDeliveryDays?: number;
    supplyServiceArea?: string;
    supplyRemark?: string;
    standardAttributes?: Array<{
      name?: string;
      value?: string;
    }>;
    customTags?: string[];
    customTagReviewNote?: string;
    customTagReviewRequired?: boolean;
    templateId?: number;
    certificationCount?: number;
    hasApplicationDescription?: boolean;
    customRelations?: Array<{
      entityId?: number;
      entityName?: string;
      relationType?: string;
      requiredFlag?: boolean;
      remark?: string;
    }>;
  }

  export interface RelationSubmitResult {
    submissionId?: number;
    productId?: number;
    auditStatus?: number;
    auditRoute?: string;
    auditReason?: string;
    completenessScore?: number;
    completenessSuggestion?: string;
    copiedTemplateItemCount?: number;
  }

  export interface IndustryTemplate {
    id?: number;
    templateCode?: string;
    name?: string;
    industryCode?: string;
    productLane?: WujinLane;
    status?: number;
    remark?: string;
    createTime?: string;
  }

  export interface SourcingLead {
    id?: number;
    userId?: number;
    keyword?: string;
    lane?: WujinLane;
    sourceKeyword?: string;
    industry?: string;
    supplierName?: string;
    merchantId?: number;
    contactName?: string;
    contactPhone?: string;
    requirement?: string;
    leadStatus?: string;
    followStage?: string;
    nextFollowTime?: string;
    quotedAmount?: number;
    winProbability?: number;
    dispatchStatus?: string;
    dispatchRemark?: string;
    handleRemark?: string;
    createTime?: string;
  }

  export interface SourcingLeadHandleRequest {
    handleAction?: string;
    handleRemark?: string;
    followStage?: string;
    leadId?: number;
    merchantId?: number;
    nextFollowTime?: string;
    quotedAmount?: number;
    winProbability?: number;
  }

  export interface SupplyCapability {
    id?: number;
    merchantId?: number;
    productId?: number;
    productName?: string;
    entityId?: number;
    lane?: WujinLane;
    industry?: string;
    supplyStatus?: number;
    stockCount?: number;
    minOrderQuantity?: number;
    deliveryDays?: number;
    serviceArea?: string;
    remark?: string;
    createTime?: string;
  }

  export type WujinLane = 'MATERIAL' | 'PROCESS' | 'PRODUCT';
}

export function createRelationSubmission(
  data: WujinMerchantApi.RelationSubmission,
) {
  return requestClient.post<number>(
    '/wujin/merchant-relation-submission/create',
    data,
  );
}

export function updateRelationSubmission(
  data: WujinMerchantApi.RelationSubmissionUpdateRequest,
) {
  return requestClient.put<boolean>(
    '/wujin/merchant-relation-submission/update',
    data,
  );
}

export function deleteRelationSubmission(id: number) {
  return requestClient.delete<boolean>(
    '/wujin/merchant-relation-submission/delete',
    { params: { id } },
  );
}

export function getRelationSubmissionList(
  params?: Partial<WujinMerchantApi.RelationSubmission>,
  options?: ListRequestOptions,
) {
  return requestClient.get<WujinMerchantApi.RelationSubmission[]>(
    '/wujin/merchant-relation-submission/list',
    createListRequestConfig(params, options),
  );
}

export function submitRelation(data: WujinMerchantApi.RelationSubmitRequest) {
  return requestClient.post<WujinMerchantApi.RelationSubmitResult>(
    '/wujin/merchant-relation-submit/submit',
    data,
  );
}

export function createRelationItem(data: WujinMerchantApi.RelationItem) {
  return requestClient.post<number>(
    '/wujin/merchant-relation-item/create',
    data,
  );
}

export function updateRelationItem(data: WujinMerchantApi.RelationItem) {
  return requestClient.put<boolean>(
    '/wujin/merchant-relation-item/update',
    data,
  );
}

export function deleteRelationItem(id: number) {
  return requestClient.delete<boolean>('/wujin/merchant-relation-item/delete', {
    params: { id },
  });
}

export function getRelationItemList(
  params?: Partial<WujinMerchantApi.RelationItem>,
) {
  return requestClient.get<WujinMerchantApi.RelationItem[]>(
    '/wujin/merchant-relation-item/list',
    { params },
  );
}

export function getChainEntityList(
  params?: WujinMerchantApi.ChainEntityListParams,
) {
  return requestClient.get<WujinMerchantApi.ChainEntity[]>(
    '/wujin/chain-entity/list',
    { params },
  );
}

export function getIndustryTemplateList(
  params?: Partial<WujinMerchantApi.IndustryTemplate>,
) {
  return requestClient.get<WujinMerchantApi.IndustryTemplate[]>(
    '/wujin/industry-template/list',
    { params },
  );
}

export function getSourcingLeadList(
  params?: Partial<WujinMerchantApi.SourcingLead>,
  options?: ListRequestOptions,
) {
  return requestClient.get<WujinMerchantApi.SourcingLead[]>(
    '/wujin/merchant-sourcing-lead/list',
    createListRequestConfig(params, options),
  );
}

export function handleSourcingLead(
  data: WujinMerchantApi.SourcingLeadHandleRequest,
) {
  return requestClient.post<boolean>(
    '/wujin/merchant-sourcing-lead/handle',
    data,
  );
}

export function getSupplyCapabilityList(
  params?: Partial<WujinMerchantApi.SupplyCapability>,
  options?: ListRequestOptions,
) {
  return requestClient.get<WujinMerchantApi.SupplyCapability[]>(
    '/wujin/merchant-supply-capability/list',
    createListRequestConfig(params, options),
  );
}

export function createSupplyCapability(
  data: WujinMerchantApi.SupplyCapability,
) {
  return requestClient.post<number>(
    '/wujin/merchant-supply-capability/create',
    data,
  );
}

export function updateSupplyCapability(
  data: WujinMerchantApi.SupplyCapability,
) {
  return requestClient.put<boolean>(
    '/wujin/merchant-supply-capability/update',
    data,
  );
}
