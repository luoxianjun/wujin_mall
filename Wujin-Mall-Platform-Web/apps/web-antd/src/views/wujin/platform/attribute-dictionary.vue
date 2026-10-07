<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed, ref } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteAttributeDictionary,
  getAttributeDictionaryList,
} from '#/api/wujin/platform';
import { $t } from '#/locales';

import {
  useAttributeDictionaryColumns,
  useAttributeDictionaryFormSchema,
} from './data';
import AttributeDictionaryForm from './modules/attribute-dictionary-form.vue';

defineOptions({ name: 'WujinPlatformAttributeDictionary' });

const dictionaryItems = ref<WujinPlatformApi.AttributeDictionary[]>([]);

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: AttributeDictionaryForm,
  destroyOnClose: true,
});

const summaryCards = computed(() => {
  const items = dictionaryItems.value;
  return [
    { label: '属性总数', value: items.length },
    {
      label: '发布必填',
      value: items.filter((item) => item.requiredFlag).length,
    },
    {
      label: '参与搜索筛选',
      value: items.filter((item) => item.searchableFlag).length,
    },
    {
      label: '已停用',
      value: items.filter((item) => Number(item.status) !== 0).length,
    },
  ];
});

async function queryDictionaryList(formValues: Record<string, any>) {
  const list = await getAttributeDictionaryList(formValues);
  dictionaryItems.value = list;
  return list;
}

function onRefresh() {
  gridApi.query();
}

function handleCreate() {
  formModalApi.setData(null).open();
}

function handleEdit(row: WujinPlatformApi.AttributeDictionary) {
  formModalApi.setData(row).open();
}

async function handleDelete(row: WujinPlatformApi.AttributeDictionary) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.name]),
    key: 'action_key_msg',
  });
  try {
    await deleteAttributeDictionary(row.id as number);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.name]),
      key: 'action_key_msg',
    });
    onRefresh();
  } finally {
    hideLoading();
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useAttributeDictionaryFormSchema(),
  },
  gridOptions: {
    columns: useAttributeDictionaryColumns(),
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params, formValues) =>
          await queryDictionaryList(formValues),
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<WujinPlatformApi.AttributeDictionary>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="onRefresh" />
    <div class="wujin-attribute-summary-row">
      <div
        v-for="card in summaryCards"
        :key="card.label"
        class="wujin-attribute-summary"
      >
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </div>
    </div>
    <Grid table-title="平台属性字典">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['属性']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wujin:attribute-dictionary:create'],
              onClick: handleCreate,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['wujin:attribute-dictionary:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wujin:attribute-dictionary:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.name]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>

<style scoped>
.wujin-attribute-summary-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.wujin-attribute-summary {
  min-height: 72px;
  padding: 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--card));
}

.wujin-attribute-summary span {
  display: block;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.wujin-attribute-summary strong {
  display: block;
  margin-top: 6px;
  font-size: 20px;
  line-height: 26px;
}

@media (max-width: 768px) {
  .wujin-attribute-summary-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
