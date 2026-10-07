import { requestClient } from '#/api/request';

export namespace WujinPlatformApi {
  export interface Category {
    id?: number;
    parentId?: number;
    lane?: WujinLane;
    code?: string;
    name?: string;
    level?: number;
    sort?: number;
    status?: number;
    displayDepth?: number;
    healthStatus?: WujinHealthStatus;
    description?: string;
    createTime?: string;
  }

  export interface CategoryBatchMigrateRequest {
    ids?: number[];
    targetParentId?: number;
    targetLane?: WujinLane;
    targetLevel?: number;
    displayDepth?: number;
    healthStatus?: WujinHealthStatus;
  }

  export interface CategoryMapping {
    id?: number;
    sourceCategoryId?: number;
    sourceLane?: WujinLane;
    targetCategoryId?: number;
    targetLane?: WujinLane;
    mappingType?: string;
    confidence?: number;
    status?: number;
    riskNote?: string;
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

  export interface IndustryTemplateItem {
    id?: number;
    templateId?: number;
    entityId?: number;
    relationType?: string;
    requiredFlag?: boolean;
    sort?: number;
    weight?: number;
    remark?: string;
    createTime?: string;
  }

  export interface IndustryTemplateItemBatchMigrateRequest {
    ids?: number[];
    targetTemplateId?: number;
    relationType?: string;
    requiredFlag?: boolean;
    weight?: number;
  }

  export interface RelationAuditRecord {
    id?: number;
    submissionId?: number;
    auditorId?: number;
    action?: string;
    reason?: string;
    comment?: string;
    effectiveFlag?: boolean;
    createTime?: string;
  }

  export interface RelationAuditReviewRequest {
    submissionId?: number;
    auditorId?: number;
    action?: string;
    comment?: string;
  }

  export interface RelationAuditReviewResult {
    submissionId?: number;
    auditRecordId?: number;
    action?: string;
    reason?: string;
    effectiveFlag?: boolean;
    productEntityId?: number;
    effectiveRelationCount?: number;
  }

  export interface SearchRuleConfig {
    id?: number;
    ruleType?: string;
    lane?: WujinLane;
    industryCode?: string;
    ruleValue?: string;
    weight?: number;
    status?: number;
    remark?: string;
    createTime?: string;
  }

  export interface SearchBehaviorLog {
    id?: number;
    userId?: number;
    keyword?: string;
    intent?: string;
    resultLane?: WujinLane;
    industryCode?: string;
    chainViewed?: boolean;
    classificationCorrect?: boolean;
    highRiskWarningTriggered?: boolean;
    satisfactionScore?: number;
    responseTimeMillis?: number;
    createTime?: string;
  }

  export interface MonitorSnapshot {
    id?: number;
    metric?: string;
    metricValue?: number;
    thresholdValue?: number;
    auditAction?: string;
    alertFlag?: boolean;
    remark?: string;
    createTime?: string;
  }

  export interface MonitorDashboardSummary {
    searchSatisfaction?: number;
    chainViewRate?: number;
    classificationAccuracy?: number;
    averageResponseTimeMillis?: number;
    relationAuditPassRate?: number;
    highRiskWarningCount?: number;
    searchSampleCount?: number;
    auditSampleCount?: number;
    alerts?: MonitorDashboardAlert[];
  }

  export interface MonitorDashboardAlert {
    metric?: string;
    message?: string;
  }

  export interface SourcingLead {
    id?: number;
    userId?: number;
    keyword?: string;
    lane?: WujinLane;
    sourceKeyword?: string;
    industry?: string;
    supplierId?: number;
    supplierName?: string;
    merchantId?: number;
    contactName?: string;
    contactPhone?: string;
    requirement?: string;
    leadStatus?: string;
    dispatchStatus?: string;
    dispatchRemark?: string;
    dispatchTime?: string;
    handleRemark?: string;
    followStage?: string;
    nextFollowTime?: string;
    quotedAmount?: number;
    winProbability?: number;
    createTime?: string;
  }

  export interface SourcingLeadDispatchRequest {
    dispatchRemark?: string;
    leadId?: number;
    merchantId?: number;
  }

  export interface AttributeDictionary {
    id?: number;
    code?: string;
    name?: string;
    groupName?: string;
    lane?: WujinLane;
    categoryId?: number;
    valueType?: AttributeValueType;
    valueOptions?: string[];
    unit?: string;
    requiredFlag?: boolean;
    searchableFlag?: boolean;
    sort?: number;
    status?: number;
    remark?: string;
    createTime?: string;
  }

  export interface ProductCustomTag {
    id?: number;
    submissionId?: number;
    merchantId?: number;
    productId?: number;
    productName?: string;
    tagName?: string;
    reviewNote?: string;
    auditStatus?: number;
    auditComment?: string;
    auditorId?: number;
    auditTime?: string;
    createTime?: string;
  }

  export interface ProductCustomTagReviewRequest {
    id?: number;
    action?: 'APPROVE' | 'REJECT';
    comment?: string;
  }

  export interface ProductAttributeValue {
    id?: number;
    submissionId?: number;
    productId?: number;
    attributeId?: number;
    attributeCode?: string;
    attributeName?: string;
    attributeValue?: string;
    standardFlag?: boolean;
  }

  export interface MonitorTrend {
    startDate?: string;
    endDate?: string;
    totalSearchCount?: number;
    points?: MonitorTrendPoint[];
    topKeywords?: MonitorTrendKeyword[];
    laneStats?: Array<{ lane?: string; searchCount?: number }>;
  }

  export interface MonitorTrendPoint {
    date?: string;
    searchCount?: number;
    searchSatisfaction?: number;
    chainViewRate?: number;
    classificationAccuracy?: number;
    averageResponseTimeMillis?: number;
    highRiskWarningCount?: number;
  }

  export interface MonitorTrendKeyword {
    keyword?: string;
    searchCount?: number;
    chainViewRate?: number;
  }

  export type AttributeValueType =
    | 'BOOLEAN'
    | 'ENUM'
    | 'MULTI_ENUM'
    | 'NUMBER'
    | 'TEXT';
  export type WujinHealthStatus = 'HEALTHY' | 'NEEDS_SPLIT' | 'UNBOUND';
  export type WujinLane = 'MATERIAL' | 'PROCESS' | 'PRODUCT';
}

export function createCategory(data: WujinPlatformApi.Category) {
  return requestClient.post<number>('/wujin/category/create', data);
}

export function updateCategory(data: WujinPlatformApi.Category) {
  return requestClient.put<boolean>('/wujin/category/update', data);
}

export function batchMigrateCategory(
  data: WujinPlatformApi.CategoryBatchMigrateRequest,
) {
  return requestClient.put<number>('/wujin/category/batch-migrate', data);
}

export function deleteCategory(id: number) {
  return requestClient.delete<boolean>('/wujin/category/delete', {
    params: { id },
  });
}

export function getCategoryList(params?: Partial<WujinPlatformApi.Category>) {
  return requestClient.get<WujinPlatformApi.Category[]>(
    '/wujin/category/list',
    {
      params,
    },
  );
}

export function createCategoryMapping(data: WujinPlatformApi.CategoryMapping) {
  return requestClient.post<number>('/wujin/category-mapping/create', data);
}

export function updateCategoryMapping(data: WujinPlatformApi.CategoryMapping) {
  return requestClient.put<boolean>('/wujin/category-mapping/update', data);
}

export function deleteCategoryMapping(id: number) {
  return requestClient.delete<boolean>('/wujin/category-mapping/delete', {
    params: { id },
  });
}

export function getCategoryMappingList(
  params?: Partial<WujinPlatformApi.CategoryMapping>,
) {
  return requestClient.get<WujinPlatformApi.CategoryMapping[]>(
    '/wujin/category-mapping/list',
    { params },
  );
}

export function getChainEntityList(
  params?: WujinPlatformApi.ChainEntityListParams,
) {
  return requestClient.get<WujinPlatformApi.ChainEntity[]>(
    '/wujin/chain-entity/list',
    { params },
  );
}

export function getIndustryTemplateList(
  params?: Partial<WujinPlatformApi.IndustryTemplate>,
) {
  return requestClient.get<WujinPlatformApi.IndustryTemplate[]>(
    '/wujin/industry-template/list',
    { params },
  );
}

export function createIndustryTemplate(
  data: WujinPlatformApi.IndustryTemplate,
) {
  return requestClient.post<number>('/wujin/industry-template/create', data);
}

export function updateIndustryTemplate(
  data: WujinPlatformApi.IndustryTemplate,
) {
  return requestClient.put<boolean>('/wujin/industry-template/update', data);
}

export function createIndustryTemplateItem(
  data: WujinPlatformApi.IndustryTemplateItem,
) {
  return requestClient.post<number>(
    '/wujin/industry-template-item/create',
    data,
  );
}

export function updateIndustryTemplateItem(
  data: WujinPlatformApi.IndustryTemplateItem,
) {
  return requestClient.put<boolean>(
    '/wujin/industry-template-item/update',
    data,
  );
}

export function batchMigrateIndustryTemplateItem(
  data: WujinPlatformApi.IndustryTemplateItemBatchMigrateRequest,
) {
  return requestClient.put<number>(
    '/wujin/industry-template-item/batch-migrate',
    data,
  );
}

export function deleteIndustryTemplateItem(id: number) {
  return requestClient.delete<boolean>('/wujin/industry-template-item/delete', {
    params: { id },
  });
}

export function getIndustryTemplateItem(id: number) {
  return requestClient.get<WujinPlatformApi.IndustryTemplateItem>(
    '/wujin/industry-template-item/get',
    { params: { id } },
  );
}

export function getIndustryTemplateItemList(
  params?: Partial<WujinPlatformApi.IndustryTemplateItem>,
) {
  return requestClient.get<WujinPlatformApi.IndustryTemplateItem[]>(
    '/wujin/industry-template-item/list',
    { params },
  );
}

export function getRelationAuditRecordList(
  params?: Partial<WujinPlatformApi.RelationAuditRecord>,
) {
  return requestClient.get<WujinPlatformApi.RelationAuditRecord[]>(
    '/wujin/relation-audit-record/list',
    { params },
  );
}

export function reviewRelationSubmission(
  data: WujinPlatformApi.RelationAuditReviewRequest,
) {
  return requestClient.post<WujinPlatformApi.RelationAuditReviewResult>(
    '/wujin/relation-audit-review/review',
    data,
  );
}

export function getSearchRuleConfigList(
  params?: Partial<WujinPlatformApi.SearchRuleConfig>,
) {
  return requestClient.get<WujinPlatformApi.SearchRuleConfig[]>(
    '/wujin/search-rule-config/list',
    { params },
  );
}

export function createSearchRuleConfig(
  data: WujinPlatformApi.SearchRuleConfig,
) {
  return requestClient.post<number>('/wujin/search-rule-config/create', data);
}

export function updateSearchRuleConfig(
  data: WujinPlatformApi.SearchRuleConfig,
) {
  return requestClient.put<boolean>('/wujin/search-rule-config/update', data);
}

export function deleteSearchRuleConfig(id: number) {
  return requestClient.delete<boolean>('/wujin/search-rule-config/delete', {
    params: { id },
  });
}

export function getSearchRuleConfig(id: number) {
  return requestClient.get<WujinPlatformApi.SearchRuleConfig>(
    '/wujin/search-rule-config/get',
    { params: { id } },
  );
}

export function getSearchBehaviorLogList(
  params?: Partial<WujinPlatformApi.SearchBehaviorLog>,
) {
  return requestClient.get<WujinPlatformApi.SearchBehaviorLog[]>(
    '/wujin/search-behavior-log/list',
    { params },
  );
}

export function getMonitorSnapshotList(
  params?: Partial<WujinPlatformApi.MonitorSnapshot>,
) {
  return requestClient.get<WujinPlatformApi.MonitorSnapshot[]>(
    '/wujin/monitor-snapshot/list',
    { params },
  );
}

export function getMonitorDashboardSummary() {
  return requestClient.get<WujinPlatformApi.MonitorDashboardSummary>(
    '/wujin/monitor-dashboard/summary',
  );
}

export function getSourcingLeadList(
  params?: Partial<WujinPlatformApi.SourcingLead>,
) {
  return requestClient.get<WujinPlatformApi.SourcingLead[]>(
    '/wujin/sourcing-lead/list',
    { params },
  );
}

export function dispatchSourcingLead(
  data: WujinPlatformApi.SourcingLeadDispatchRequest,
) {
  return requestClient.post<boolean>('/wujin/sourcing-lead/dispatch', data);
}

export function autoDispatchSourcingLead(leadId: number) {
  return requestClient.post<boolean>(
    '/wujin/sourcing-lead/auto-dispatch',
    null,
    {
      params: { leadId },
    },
  );
}

export function getMonitorTrend(days: number) {
  return requestClient.get<WujinPlatformApi.MonitorTrend>(
    '/wujin/monitor-dashboard/trend',
    { params: { days } },
  );
}

export function getAttributeDictionaryList(
  params?: Partial<WujinPlatformApi.AttributeDictionary>,
) {
  return requestClient.get<WujinPlatformApi.AttributeDictionary[]>(
    '/wujin/attribute-dictionary/list',
    { params },
  );
}

export function createAttributeDictionary(
  data: WujinPlatformApi.AttributeDictionary,
) {
  return requestClient.post<number>('/wujin/attribute-dictionary/create', data);
}

export function updateAttributeDictionary(
  data: WujinPlatformApi.AttributeDictionary,
) {
  return requestClient.put<boolean>('/wujin/attribute-dictionary/update', data);
}

export function deleteAttributeDictionary(id: number) {
  return requestClient.delete<boolean>('/wujin/attribute-dictionary/delete', {
    params: { id },
  });
}

export function getProductCustomTagList(
  params?: Partial<WujinPlatformApi.ProductCustomTag>,
) {
  return requestClient.get<WujinPlatformApi.ProductCustomTag[]>(
    '/wujin/product-attribute/custom-tag/list',
    { params },
  );
}

export function reviewProductCustomTag(
  data: WujinPlatformApi.ProductCustomTagReviewRequest,
) {
  return requestClient.post<boolean>(
    '/wujin/product-attribute/custom-tag/review',
    data,
  );
}

export function getProductAttributeValueList(params: {
  productId?: number;
  submissionId?: number;
}) {
  return requestClient.get<WujinPlatformApi.ProductAttributeValue[]>(
    '/wujin/product-attribute/value-list',
    { params },
  );
}
