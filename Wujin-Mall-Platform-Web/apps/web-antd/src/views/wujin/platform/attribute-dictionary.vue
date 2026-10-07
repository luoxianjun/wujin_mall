<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Alert, Card, Col, Row, Table, Tag } from 'ant-design-vue';

import { getPlatformAttributeDictionaryPlaceholder } from '#/api/wujin/platform';

defineOptions({ name: 'WujinPlatformAttributeDictionary' });

const loading = ref(false);
const serverReady = ref(false);
const dictionaryItems = ref<WujinPlatformApi.PlatformAttributeDictionaryItem[]>(
  [],
);

const columns = [
  { dataIndex: 'groupName', title: '属性分组', width: 140 },
  { dataIndex: 'name', title: '属性名称', width: 160 },
  { dataIndex: 'code', title: '属性编码', width: 220 },
  { dataIndex: 'valueType', title: '值类型', width: 120 },
  { dataIndex: 'requiredFlag', title: '必填', width: 90 },
  { dataIndex: 'values', title: '可选值' },
];

async function loadPlaceholder() {
  loading.value = true;
  try {
    const data = await getPlatformAttributeDictionaryPlaceholder();
    dictionaryItems.value = data.items;
    serverReady.value = data.serverReady;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadPlaceholder();
});
</script>

<template>
  <Page auto-content-height>
    <Card :bordered="false" title="平台属性字典">
      <Alert
        class="mb-3"
        message="服务端接口未接入"
        show-icon
        type="info"
        description="当前为字典入口骨架，用于明确平台属性字典的页面位置、字段结构和后续接口契约。"
      />

      <Row :gutter="[12, 12]" class="mb-3">
        <Col :lg="8" :sm="12" :xs="24">
          <div class="wujin-attribute-summary">
            <span>字典入口骨架</span>
            <strong>{{ dictionaryItems.length }}</strong>
          </div>
        </Col>
        <Col :lg="8" :sm="12" :xs="24">
          <div class="wujin-attribute-summary">
            <span>接口状态</span>
            <strong>{{ serverReady ? '已接入' : '占位中' }}</strong>
          </div>
        </Col>
        <Col :lg="8" :sm="12" :xs="24">
          <div class="wujin-attribute-summary">
            <span>覆盖泳道</span>
            <strong>成品 / 加工 / 原材料</strong>
          </div>
        </Col>
      </Row>

      <Table
        :columns="columns"
        :data-source="dictionaryItems"
        :loading="loading"
        :pagination="false"
        row-key="code"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'requiredFlag'">
            <Tag :color="record.requiredFlag ? 'red' : 'default'">
              {{ record.requiredFlag ? '必填' : '可选' }}
            </Tag>
          </template>
          <template v-if="column.dataIndex === 'values'">
            <template v-if="record.values?.length">
              <Tag
                v-for="value in record.values"
                :key="`${record.code}-${value}`"
                color="blue"
              >
                {{ value }}
              </Tag>
            </template>
            <span v-else>-</span>
          </template>
        </template>
      </Table>
    </Card>
  </Page>
</template>

<style scoped>
.wujin-attribute-summary {
  min-height: 78px;
  padding: 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--muted) / 28%);
}

.wujin-attribute-summary span {
  display: block;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-attribute-summary strong {
  display: block;
  margin-top: 6px;
  font-size: 18px;
  line-height: 24px;
}
</style>
