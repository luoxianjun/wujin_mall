<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ActionItem } from '#/components/table-action/typing';

import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useSortable } from '@vueuse/integrations/useSortable';
import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  createIndustryTemplateItem,
  deleteIndustryTemplateItem,
  getChainEntityList,
  getIndustryTemplateItemList,
  updateIndustryTemplateItem,
} from '#/api/wujin/platform';

import { useTemplateItemColumns, useTemplateItemFormSchema } from '../data';
import TemplateItemBatchMigrateForm from './template-item-batch-migrate-form.vue';
import TemplateItemDesignerPanel from './template-item-designer-panel.vue';
import TemplateItemForm from './template-item-form.vue';

defineOptions({ name: 'WujinPlatformTemplateItemManage' });

const currentTemplate = ref<WujinPlatformApi.IndustryTemplate>();
const chainEntities = ref<WujinPlatformApi.ChainEntity[]>([]);
const templateItems = ref<WujinPlatformApi.IndustryTemplateItem[]>([]);
const originalTemplateItems = ref<WujinPlatformApi.IndustryTemplateItem[]>([]);
const isTemplateItemSorting = ref(false);
const sortableInstance = ref<any>(null);
const selectedTemplateItemIds = ref<number[]>([]);

const templateItemSortableSelector =
  '.wujin-template-item-sort-area .vxe-table .vxe-table--body-wrapper:not(.fixed-right--wrapper) .vxe-table--body tbody';

const modalTitle = computed(() =>
  currentTemplate.value?.name
    ? `模板关系配置 - ${currentTemplate.value.name}`
    : '模板关系配置',
);

const chainEntityMap = computed(
  () =>
    new Map(
      chainEntities.value
        .filter((entity) => entity.id)
        .map((entity) => [entity.id as number, entity]),
    ),
);

function chainEntityName(entityId?: number) {
  return chainEntityMap.value.get(entityId ?? -1)?.name ?? '未命名实体';
}

function chainEntityMeta(entityId?: number) {
  const entity = chainEntityMap.value.get(entityId ?? -1);
  return [entity?.entityCode, entityId ? `#${entityId}` : undefined]
    .filter(Boolean)
    .join(' · ');
}

const templateItemToolbarActions = computed<ActionItem[]>(() => {
  const actions: ActionItem[] = [
    {
      label: '新增关系项',
      type: 'primary',
      icon: ACTION_ICON.ADD,
      disabled: isTemplateItemSorting.value,
      auth: ['wujin:industry-template-item:create'],
      onClick: handleCreateTemplateItem,
    },
    {
      label: '批量迁移',
      type: 'primary',
      disabled: isTemplateItemSorting.value,
      auth: ['wujin:industry-template-item:update'],
      onClick: handleBatchMigrateTemplateItem,
    },
  ];

  if (isTemplateItemSorting.value) {
    actions.push(
      {
        label: '保存排序',
        type: 'primary',
        auth: ['wujin:industry-template-item:update'],
        onClick: handleTemplateItemSortSubmit,
      },
      {
        label: '取消排序',
        onClick: handleTemplateItemSortCancel,
      },
    );
  } else {
    actions.push({
      label: '拖拽排序',
      auth: ['wujin:industry-template-item:update'],
      onClick: handleTemplateItemSortStart,
    });
  }

  return actions;
});

function cloneTemplateItems(items: WujinPlatformApi.IndustryTemplateItem[]) {
  return items.map((item) => ({ ...item }));
}

function sortTemplateItems(items: WujinPlatformApi.IndustryTemplateItem[]) {
  return [...items].sort((left, right) => {
    const sortResult = (left.sort ?? 0) - (right.sort ?? 0);
    if (sortResult !== 0) {
      return sortResult;
    }
    return (left.id ?? 0) - (right.id ?? 0);
  });
}

function disableTemplateItemSortable() {
  if (sortableInstance.value) {
    sortableInstance.value.option('disabled', true);
  }
}

function resetTemplateItemSortState() {
  isTemplateItemSorting.value = false;
  originalTemplateItems.value = [];
  disableTemplateItemSortable();
  sortableInstance.value = null;
}

function resetTemplateItemSelection() {
  selectedTemplateItemIds.value = [];
}

const [TemplateItemFormModal, templateItemFormModalApi] = useVbenModal({
  connectedComponent: TemplateItemForm,
  destroyOnClose: true,
});

const [TemplateItemBatchMigrateModal, templateItemBatchMigrateModalApi] =
  useVbenModal({
    connectedComponent: TemplateItemBatchMigrateForm,
    destroyOnClose: true,
  });

const [TemplateItemGrid, templateItemGridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useTemplateItemFormSchema(),
  },
  gridOptions: {
    columns: useTemplateItemColumns(),
    height: 420,
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (_params, formValues) => {
          if (!currentTemplate.value?.id) {
            templateItems.value = [];
            return [];
          }
          const list = sortTemplateItems(
            await getIndustryTemplateItemList({
              ...formValues,
              templateId: currentTemplate.value?.id,
            }),
          );
          templateItems.value = list;
          resetTemplateItemSelection();
          resetTemplateItemSortState();
          return list;
        },
      },
    },
    checkboxConfig: {
      reserve: true,
    },
    events: {
      checkboxAll: handleTemplateItemSelectionChange,
      checkboxChange: handleTemplateItemSelectionChange,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions,
});

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen) {
    if (!isOpen) {
      currentTemplate.value = undefined;
      chainEntities.value = [];
      templateItems.value = [];
      resetTemplateItemSelection();
      resetTemplateItemSortState();
      return;
    }
    currentTemplate.value =
      modalApi.getData<WujinPlatformApi.IndustryTemplate>();
    chainEntities.value = await getChainEntityList();
    templateItemGridApi.query();
  },
});

function refreshTemplateItemGrid() {
  templateItemGridApi.query();
}

async function handleTemplateItemSortStart() {
  if (templateItems.value.length < 2) {
    message.warning('至少需要两个关系项才能排序');
    return;
  }
  originalTemplateItems.value = cloneTemplateItems(templateItems.value);
  isTemplateItemSorting.value = true;
  templateItemGridApi.setGridOptions({
    data: templateItems.value,
  });
  await nextTick();

  if (sortableInstance.value) {
    sortableInstance.value.option('disabled', false);
    return;
  }

  sortableInstance.value = useSortable(
    templateItemSortableSelector,
    templateItems.value,
    {
      animation: 150,
      disabled: false,
      draggable: '.vxe-body--row',
      handle: '.wujin-template-item-drag-handle',
      onEnd: ({ newDraggableIndex, oldDraggableIndex }) => {
        if (newDraggableIndex === oldDraggableIndex) {
          return;
        }
        const moved = templateItems.value.splice(oldDraggableIndex ?? 0, 1)[0];
        if (!moved) {
          return;
        }
        templateItems.value.splice(newDraggableIndex ?? 0, 0, moved);
        templateItemGridApi.setGridOptions({
          data: templateItems.value,
        });
      },
    },
  );
}

async function handleTemplateItemSortSubmit() {
  if (!isTemplateItemSorting.value) {
    return;
  }
  const hideLoading = message.loading({
    content: '正在保存关系项排序',
    key: 'wujin_template_item_sort',
  });
  try {
    await Promise.all(
      templateItems.value
        .map((item, index) => ({
          ...item,
          sort: index + 1,
          templateId: currentTemplate.value?.id ?? item.templateId,
        }))
        .filter((item) => item.id)
        .map((item) => updateIndustryTemplateItem(item)),
    );
    message.success({
      content: '关系项排序已保存',
      key: 'wujin_template_item_sort',
    });
    resetTemplateItemSortState();
    refreshTemplateItemGrid();
  } finally {
    hideLoading();
  }
}

function handleTemplateItemSortCancel() {
  templateItems.value = cloneTemplateItems(originalTemplateItems.value);
  templateItemGridApi.setGridOptions({
    data: templateItems.value,
  });
  resetTemplateItemSortState();
}

function handleTemplateItemSelectionChange() {
  const selectedRows =
    templateItemGridApi.grid.getCheckboxRecords?.() ??
    templateItemGridApi.grid.getCheckboxReserveRecords?.() ??
    [];
  selectedTemplateItemIds.value = selectedRows
    .map((row: WujinPlatformApi.IndustryTemplateItem) => row.id)
    .filter((id: number | undefined): id is number => Boolean(id));
}

function handleBatchMigrateTemplateItem() {
  handleTemplateItemSelectionChange();
  if (selectedTemplateItemIds.value.length === 0) {
    message.warning('至少选择一个关系项');
    return;
  }
  templateItemBatchMigrateModalApi
    .setData({
      selectedTemplateItemIds: selectedTemplateItemIds.value,
    })
    .open();
}

function handleCreateTemplateItem() {
  templateItemFormModalApi
    .setData({
      currentTemplateId: currentTemplate.value?.id,
      relationType: 'REQUIRES_MATERIAL',
      requiredFlag: true,
      sort: 0,
      weight: 100,
    })
    .open();
}

function handleEditTemplateItem(row: WujinPlatformApi.IndustryTemplateItem) {
  templateItemFormModalApi
    .setData({
      ...row,
      currentTemplateId: currentTemplate.value?.id,
    })
    .open();
}

async function handleDeleteTemplateItem(
  row: WujinPlatformApi.IndustryTemplateItem,
) {
  if (!row.id) {
    return;
  }
  const hideLoading = message.loading({
    content: `正在删除关系项 ${row.id}`,
    key: 'wujin_template_item_delete',
  });
  try {
    await deleteIndustryTemplateItem(row.id);
    message.success({
      content: `关系项 ${row.id} 已删除`,
      key: 'wujin_template_item_delete',
    });
    refreshTemplateItemGrid();
  } finally {
    hideLoading();
  }
}

async function handleCopyTemplateItem(
  row: WujinPlatformApi.IndustryTemplateItem,
) {
  if (!currentTemplate.value?.id) {
    message.warning('请选择行业模板');
    return;
  }

  const hideLoading = message.loading({
    content: `正在复制关系项 ${row.id ?? ''}`,
    key: 'wujin_template_item_copy',
  });
  try {
    await createIndustryTemplateItem({
      ...row,
      id: undefined,
      sort: templateItems.value.length + 1,
      templateId: currentTemplate.value?.id,
    });
    message.success({
      content: '关系项已复制',
      key: 'wujin_template_item_copy',
    });
    refreshTemplateItemGrid();
  } finally {
    hideLoading();
  }
}
</script>

<template>
  <Modal
    class="w-3/5"
    content-class="flex max-h-[80vh] flex-col overflow-hidden"
    :title="modalTitle"
  >
    <TemplateItemFormModal @success="refreshTemplateItemGrid" />
    <TemplateItemBatchMigrateModal @success="refreshTemplateItemGrid" />
    <TemplateItemDesignerPanel
      :entities="chainEntities"
      :items="templateItems"
      :template="currentTemplate"
    />
    <div class="mb-2 mt-1">
      <div class="text-sm font-semibold">关系项明细</div>
      <div class="text-muted-foreground text-xs">
        搜索范围为当前模板，可按产业链实体、关系类型或必需状态筛选
      </div>
    </div>
    <div
      class="wujin-template-item-sort-area min-h-[520px] flex-1 overflow-hidden"
    >
      <TemplateItemGrid table-title="关系项明细">
        <template #toolbar-tools>
          <TableAction :actions="templateItemToolbarActions" />
        </template>
        <template #templateItemDragHandle>
          <span
            :class="[
              'wujin-template-item-drag-handle icon-[ic--round-drag-indicator] text-lg',
              isTemplateItemSorting
                ? 'cursor-move text-gray-500'
                : 'cursor-not-allowed text-gray-300',
            ]"
            :title="isTemplateItemSorting ? '拖动排序' : '点击拖拽排序后可拖动'"
          ></span>
        </template>
        <template #templateItemEntity="{ row }">
          <div class="min-w-0 py-1">
            <div class="truncate font-medium">
              {{ chainEntityName(row.entityId) }}
            </div>
            <div class="text-muted-foreground truncate text-xs">
              {{ chainEntityMeta(row.entityId) }}
            </div>
          </div>
        </template>
        <template #templateItemActions="{ row }">
          <TableAction
            :actions="[
              {
                label: '编辑',
                type: 'link',
                icon: ACTION_ICON.EDIT,
                auth: ['wujin:industry-template-item:update'],
                onClick: handleEditTemplateItem.bind(null, row),
              },
              {
                label: '复制',
                type: 'link',
                icon: ACTION_ICON.COPY,
                auth: ['wujin:industry-template-item:create'],
                onClick: handleCopyTemplateItem.bind(null, row),
              },
              {
                label: '删除',
                type: 'link',
                danger: true,
                icon: ACTION_ICON.DELETE,
                auth: ['wujin:industry-template-item:delete'],
                popConfirm: {
                  title: `确认删除关系项 ${row.id}？`,
                  confirm: handleDeleteTemplateItem.bind(null, row),
                },
              },
            ]"
          />
        </template>
      </TemplateItemGrid>
    </div>
  </Modal>
</template>
