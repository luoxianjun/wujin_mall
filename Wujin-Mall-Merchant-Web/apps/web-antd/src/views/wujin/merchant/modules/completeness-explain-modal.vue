<script lang="ts" setup>
import type { WujinMerchantApi } from '#/api/wujin/merchant';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Alert, Descriptions, Progress, Tag } from 'ant-design-vue';

defineOptions({ name: 'WujinMerchantCompletenessExplainModal' });

const detail = ref<WujinMerchantApi.RelationSubmission>();

const missingItems = computed(() => {
  const raw = detail.value?.missingItems;
  if (Array.isArray(raw)) {
    return raw;
  }
  if (typeof raw === 'string' && raw.trim()) {
    return raw
      .split(/[,，\n]/)
      .map((item) => item.trim())
      .filter(Boolean);
  }
  return [];
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    detail.value = isOpen
      ? modalApi.getData<WujinMerchantApi.RelationSubmission>()
      : undefined;
  },
});
</script>

<template>
  <Modal class="w-2/5" title="完善度解释">
    <div class="wujin-completeness-explain">
      <Alert
        :description="
          detail?.completenessSuggestion || '后端暂未返回完善度建议。'
        "
        message="完善度建议"
        show-icon
        type="info"
      />
      <div class="wujin-completeness-explain__score">
        <span>关系完善度</span>
        <Progress :percent="detail?.completenessScore ?? 0" size="small" />
      </div>
      <Descriptions bordered size="small" :column="1">
        <Descriptions.Item label="商品名称">
          {{ detail?.productName || '-' }}
        </Descriptions.Item>
        <Descriptions.Item label="审核说明">
          {{ detail?.auditReason || '后端暂未返回审核说明。' }}
        </Descriptions.Item>
        <Descriptions.Item label="缺失项">
          <template v-if="missingItems.length">
            <Tag v-for="item in missingItems" :key="item" color="orange">
              {{ item }}
            </Tag>
          </template>
          <template v-else>后端未返回缺失项</template>
        </Descriptions.Item>
      </Descriptions>
    </div>
  </Modal>
</template>

<style scoped>
.wujin-completeness-explain {
  display: grid;
  gap: 12px;
  padding: 4px 0 8px;
}

.wujin-completeness-explain__score {
  display: grid;
  gap: 6px;
}
</style>
