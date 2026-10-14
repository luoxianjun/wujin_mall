<script lang="ts" setup>
import type { CategoryEditorForm } from './tree';

import type { WujinCategoryApi } from '#/api/wujin/category';

import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';
import {
  ArrowDown,
  ArrowUp,
  ChevronRight,
  Ellipsis,
  GripVertical,
  IconifyIcon,
  RotateCw,
} from '@vben/icons';

import {
  Button,
  Dropdown,
  Empty,
  Input,
  Menu,
  message,
  Modal,
  Radio,
  Spin,
  Tag,
  Tooltip,
} from 'ant-design-vue';

import {
  createWujinCategory,
  deleteWujinCategory,
  getWujinCategoryList,
  updateWujinCategory,
  updateWujinCategorySort,
} from '#/api/wujin/category';
import { ACTION_ICON } from '#/components/table-action/icons';

import CategoryInlineEditor from './modules/inline-editor.vue';
import {
  buildCategoryIndex,
  buildChildCode,
  CATEGORY_ADD_LABELS,
  CATEGORY_COLUMN_TITLES,
  CATEGORY_HEALTH_OPTIONS,
  CATEGORY_LANE_OPTIONS,
  countDescendants,
  getAncestorPath,
  getChildren,
  hasChildren,
  highlightSegments,
  matchCategories,
  MAX_CATEGORY_LEVEL,
  moveSiblingId,
  ROOT_PARENT_ID,
  splitChildCode,
} from './tree';

defineOptions({ name: 'WujinCategoryColumns' });

type Category = WujinCategoryApi.Category;
type LaneFilter = 'ALL' | WujinCategoryApi.Lane;

interface CategoryColumn {
  /** 1 大行业 … 4 三级分类 */
  level: number;
  title: string;
  addLabel: string;
  parent?: Category;
  parentId?: number;
  items: Category[];
  emptyText: string;
}

interface EditorState {
  mode: 'create' | 'edit';
  level: number;
  parentId: number;
  parentCode?: string;
  codeInherited: boolean;
  target?: Category;
  form: CategoryEditorForm;
}

const route = useRoute();
const { hasAccessByCodes } = useAccess();

const canCreate = computed(() => hasAccessByCodes(['wujin:category:create']));
const canUpdate = computed(() => hasAccessByCodes(['wujin:category:update']));
const canDelete = computed(() => hasAccessByCodes(['wujin:category:delete']));
const pageTitle = computed(() => String(route.meta?.title || '类目管理'));

const loading = ref(false);
const saving = ref(false);
const categories = ref<Category[]>([]);
const categoryIndex = computed(() => buildCategoryIndex(categories.value));
const laneFilter = ref<LaneFilter>('ALL');
/** 每一列当前选中的节点，selectedIds[0] 为大行业 */
const selectedIds = ref<number[]>([]);
const editor = ref<EditorState | null>(null);

const keyword = ref('');
const appliedKeyword = ref('');
const searchHitIds = ref<number[]>([]);
const searchCursor = ref(0);
const activeHitId = computed(() => searchHitIds.value[searchCursor.value]);

const armedDragId = ref<number>();
const dragging = ref<{ id: number; parentId: number }>();
const dropTarget = ref<{ after: boolean; id: number }>();

const laneFilterOptions = [
  { label: '全部泳道', value: 'ALL' },
  ...CATEGORY_LANE_OPTIONS.map(({ label, value }) => ({ label, value })),
];

const rootCategories = computed(() =>
  getChildren(categoryIndex.value, ROOT_PARENT_ID).filter(
    (item) => laneFilter.value === 'ALL' || item.lane === laneFilter.value,
  ),
);

const columns = computed<CategoryColumn[]>(() => {
  const result: CategoryColumn[] = [];
  for (let level = 1; level <= MAX_CATEGORY_LEVEL; level++) {
    const title = CATEGORY_COLUMN_TITLES[level - 1] as string;
    const addLabel = CATEGORY_ADD_LABELS[level - 1] as string;
    if (level === 1) {
      result.push({
        addLabel,
        emptyText: '暂无大行业，点击下方新增',
        items: rootCategories.value,
        level,
        parentId: ROOT_PARENT_ID,
        title,
      });
      continue;
    }
    const parentId = selectedIds.value[level - 2];
    const parent =
      parentId === undefined
        ? undefined
        : categoryIndex.value.byId.get(parentId);
    result.push({
      addLabel,
      emptyText: parent
        ? `「${parent.name}」下暂无${title}`
        : `请先选择左侧${CATEGORY_COLUMN_TITLES[level - 2]}`,
      items: parent
        ? getChildren(categoryIndex.value, parent.id as number)
        : [],
      level,
      parent,
      parentId: parent?.id,
      title,
    });
  }
  return result;
});

const searchSummary = computed(() => {
  if (!appliedKeyword.value) {
    return '';
  }
  return searchHitIds.value.length > 0
    ? `命中 ${searchCursor.value + 1}/${searchHitIds.value.length}`
    : '未找到匹配的类目';
});

function laneOption(lane?: string) {
  return CATEGORY_LANE_OPTIONS.find((item) => item.value === lane);
}

function healthLabel(value?: string) {
  return (
    CATEGORY_HEALTH_OPTIONS.find((item) => item.value === value)?.label ?? value
  );
}

function nameSegments(item: Category) {
  return highlightSegments(item.name, appliedKeyword.value);
}

function isSelected(column: CategoryColumn, item: Category) {
  return selectedIds.value[column.level - 1] === item.id;
}

function isEditing(item: Category) {
  return editor.value?.mode === 'edit' && editor.value.target?.id === item.id;
}

function isCreatingIn(column: CategoryColumn) {
  return editor.value?.mode === 'create' && editor.value.level === column.level;
}

async function loadCategories() {
  loading.value = true;
  try {
    categories.value = await getWujinCategoryList();
    pruneSelection();
    if (appliedKeyword.value) {
      refreshSearchHits(false);
    }
  } finally {
    loading.value = false;
  }
}

/** 删除或移动后，去掉已不存在或不再连续的选中节点 */
function pruneSelection() {
  const next: number[] = [];
  let parentId = ROOT_PARENT_ID;
  for (const id of selectedIds.value) {
    const item = categoryIndex.value.byId.get(id);
    if (!item || Number(item.parentId ?? ROOT_PARENT_ID) !== parentId) {
      break;
    }
    if (
      next.length === 0 &&
      laneFilter.value !== 'ALL' &&
      item.lane !== laneFilter.value
    ) {
      break;
    }
    next.push(id);
    parentId = id;
  }
  selectedIds.value = next;
}

function selectNode(column: CategoryColumn, item: Category) {
  if (editor.value) {
    return;
  }
  // 选中该行，右侧列刷新为它的下一级，更右侧的列清空
  selectedIds.value = [
    ...selectedIds.value.slice(0, column.level - 1),
    item.id as number,
  ];
}

function handleLaneChange() {
  editor.value = null;
  pruneSelection();
  if (appliedKeyword.value) {
    refreshSearchHits(true);
  }
}

function visibleForLane(item: Category) {
  return laneFilter.value === 'ALL' || item.lane === laneFilter.value;
}

function refreshSearchHits(focus: boolean) {
  const hits = matchCategories(
    categories.value.filter((item) => visibleForLane(item)),
    appliedKeyword.value,
  );
  searchHitIds.value = hits.map((item) => item.id as number);
  searchCursor.value = Math.min(
    searchCursor.value,
    Math.max(hits.length - 1, 0),
  );
  if (focus) {
    searchCursor.value = 0;
    if (hits.length > 0) {
      focusHit(0);
    }
  }
}

function handleSearch() {
  appliedKeyword.value = keyword.value.trim();
  if (!appliedKeyword.value) {
    searchHitIds.value = [];
    return;
  }
  refreshSearchHits(true);
  if (searchHitIds.value.length === 0) {
    message.info(`未找到包含「${appliedKeyword.value}」的类目`);
  }
}

function handleSearchInput() {
  if (!keyword.value) {
    appliedKeyword.value = '';
    searchHitIds.value = [];
  }
}

function stepHit(offset: number) {
  const total = searchHitIds.value.length;
  if (!total) {
    return;
  }
  focusHit((searchCursor.value + offset + total) % total);
}

/** 命中节点：自动展开完整路径并滚动定位到所在列 */
async function focusHit(cursor: number) {
  searchCursor.value = cursor;
  const id = searchHitIds.value[cursor];
  if (id === undefined) {
    return;
  }
  editor.value = null;
  selectedIds.value = getAncestorPath(categoryIndex.value, id).map(
    (item) => item.id as number,
  );
  await nextTick();
  document.querySelector(`[data-category-id="${id}"]`)?.scrollIntoView({
    behavior: 'smooth',
    block: 'nearest',
    inline: 'nearest',
  });
}

function defaultForm(
  lane: WujinCategoryApi.Lane,
  parent?: Category,
): CategoryEditorForm {
  return {
    codeSuffix: '',
    description: '',
    displayDepth: parent?.displayDepth ?? 3,
    enabled: true,
    healthStatus: 'HEALTHY',
    lane,
    name: '',
  };
}

function openCreate(column: CategoryColumn) {
  if (!canCreate.value) {
    return;
  }
  if (column.level > 1 && !column.parent) {
    message.warning(`请先选择左侧${CATEGORY_COLUMN_TITLES[column.level - 2]}`);
    return;
  }
  const parent = column.parent;
  const lane =
    parent?.lane ?? (laneFilter.value === 'ALL' ? 'PRODUCT' : laneFilter.value);
  editor.value = {
    codeInherited: Boolean(parent?.code),
    form: defaultForm(lane, parent),
    level: column.level,
    mode: 'create',
    parentCode: parent?.code,
    parentId: parent?.id ?? ROOT_PARENT_ID,
  };
  scrollEditorIntoView();
}

function openCreateChild(column: CategoryColumn, item: Category) {
  if (column.level >= MAX_CATEGORY_LEVEL) {
    return;
  }
  selectedIds.value = [
    ...selectedIds.value.slice(0, column.level - 1),
    item.id as number,
  ];
  editor.value = null;
  const childColumn = columns.value[column.level];
  if (childColumn) {
    openCreate(childColumn);
  }
}

function openEdit(column: CategoryColumn, item: Category) {
  if (!canUpdate.value) {
    return;
  }
  const parent =
    column.level === 1
      ? undefined
      : categoryIndex.value.byId.get(Number(item.parentId));
  const { inherited, suffix } = splitChildCode(parent?.code, item.code);
  editor.value = {
    codeInherited: inherited,
    form: {
      codeSuffix: suffix,
      description: item.description ?? '',
      displayDepth: item.displayDepth,
      enabled: Number(item.status ?? 0) === 0,
      healthStatus: item.healthStatus,
      lane: (item.lane ?? 'PRODUCT') as WujinCategoryApi.Lane,
      name: item.name ?? '',
    },
    level: column.level,
    mode: 'edit',
    parentCode: parent?.code,
    parentId: Number(item.parentId ?? ROOT_PARENT_ID),
    target: item,
  };
  scrollEditorIntoView();
}

async function scrollEditorIntoView() {
  await nextTick();
  document
    .querySelector('.category-inline-editor')
    ?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function cancelEditor() {
  editor.value = null;
}

async function saveEditor() {
  const state = editor.value;
  if (!state || saving.value) {
    return;
  }
  const name = state.form.name.trim();
  const code = state.codeInherited
    ? buildChildCode(state.parentCode, state.form.codeSuffix)
    : state.form.codeSuffix.trim();
  if (!name) {
    message.warning('请输入类目名称');
    return;
  }
  if (!code) {
    message.warning('请输入类目编码');
    return;
  }
  const parent = categoryIndex.value.byId.get(state.parentId);
  const payload: Category = {
    ...state.target,
    code,
    description: state.form.description.trim(),
    displayDepth: state.form.displayDepth,
    healthStatus: state.form.healthStatus,
    lane: parent?.lane ?? state.form.lane,
    level: state.level,
    name,
    parentId: state.parentId,
    status: state.form.enabled ? 0 : 1,
  };
  saving.value = true;
  try {
    if (state.mode === 'create') {
      const siblings = getChildren(categoryIndex.value, state.parentId);
      payload.sort =
        Math.max(0, ...siblings.map((item) => Number(item.sort ?? 0))) + 1;
      const id = await createWujinCategory(payload);
      message.success(`已新增「${name}」`);
      editor.value = null;
      if (
        state.level === 1 &&
        laneFilter.value !== 'ALL' &&
        laneFilter.value !== payload.lane
      ) {
        // 新大类不在当前泳道筛选内时切到它的泳道，保证能看到刚新增的节点
        laneFilter.value = payload.lane as WujinCategoryApi.Lane;
      }
      await loadCategories();
      if (categoryIndex.value.byId.has(id)) {
        selectedIds.value = [
          ...selectedIds.value.slice(0, state.level - 1),
          id,
        ];
      }
    } else {
      await updateWujinCategory(payload);
      message.success(`已保存「${name}」`);
      editor.value = null;
      await loadCategories();
    }
  } finally {
    saving.value = false;
  }
}

function confirmDelete(column: CategoryColumn, item: Category) {
  if (!canDelete.value) {
    return;
  }
  const childCount = countDescendants(categoryIndex.value, item.id as number);
  Modal.confirm({
    content:
      childCount > 0
        ? `将同时删除 ${childCount} 个子级，删除后不可恢复，是否继续？`
        : `确定删除「${item.name}」吗？`,
    okText: '删除',
    okType: 'danger',
    title: childCount > 0 ? `删除「${item.name}」及其子级` : '删除类目',
    async onOk() {
      const deleted = await deleteWujinCategory(
        item.id as number,
        childCount > 0,
      );
      message.success(`已删除 ${deleted ?? childCount + 1} 个类目`);
      if (selectedIds.value[column.level - 1] === item.id) {
        selectedIds.value = selectedIds.value.slice(0, column.level - 1);
      }
      editor.value = null;
      await loadCategories();
    },
  });
}

async function saveSiblingOrder(column: CategoryColumn, ids: number[]) {
  const current = column.items.map((item) => item.id as number);
  if (ids.join(',') === current.join(',')) {
    return;
  }
  const parentId = column.parentId ?? ROOT_PARENT_ID;
  // 先本地更新顺序，失败时以服务端数据为准
  const order = new Map(ids.map((id, position) => [id, position + 1]));
  categories.value = categories.value.map((item) =>
    order.has(item.id as number)
      ? { ...item, sort: order.get(item.id as number) }
      : item,
  );
  try {
    // 后端按同一父级下的全部兄弟排序；按泳道过滤时补上未展示的兄弟节点
    const hidden = getChildren(categoryIndex.value, parentId)
      .map((item) => item.id as number)
      .filter((id) => !ids.includes(id));
    await updateWujinCategorySort({ ids: [...ids, ...hidden], parentId });
    message.success('排序已更新');
  } catch (error) {
    await loadCategories();
    throw error;
  }
}

function moveByStep(column: CategoryColumn, item: Category, offset: number) {
  const ids = column.items.map((row) => row.id as number);
  const position = ids.indexOf(item.id as number);
  const target = ids[position + offset];
  if (target === undefined) {
    return;
  }
  saveSiblingOrder(
    column,
    moveSiblingId(ids, item.id as number, target, offset > 0),
  );
}

function handleMenuClick(column: CategoryColumn, item: Category, key: string) {
  switch (key) {
    case 'addChild': {
      openCreateChild(column, item);
      break;
    }
    case 'delete': {
      confirmDelete(column, item);
      break;
    }
    case 'down': {
      moveByStep(column, item, 1);
      break;
    }
    case 'edit': {
      openEdit(column, item);
      break;
    }
    case 'up': {
      moveByStep(column, item, -1);
      break;
    }
  }
}

function armDrag(item: Category) {
  if (canUpdate.value && !editor.value) {
    armedDragId.value = item.id;
  }
}

function disarmDrag() {
  armedDragId.value = undefined;
}

function handleDragStart(
  event: DragEvent,
  column: CategoryColumn,
  item: Category,
) {
  if (armedDragId.value !== item.id) {
    event.preventDefault();
    return;
  }
  dragging.value = {
    id: item.id as number,
    parentId: column.parentId ?? ROOT_PARENT_ID,
  };
  event.dataTransfer?.setData('text/plain', String(item.id));
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
  }
}

function handleDragOver(
  event: DragEvent,
  column: CategoryColumn,
  item: Category,
) {
  // 只允许同级拖拽
  if (
    !dragging.value ||
    dragging.value.parentId !== (column.parentId ?? ROOT_PARENT_ID)
  ) {
    return;
  }
  event.preventDefault();
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
  dropTarget.value = {
    after: event.clientY > rect.top + rect.height / 2,
    id: item.id as number,
  };
}

function handleDrop(event: DragEvent, column: CategoryColumn) {
  event.preventDefault();
  const drag = dragging.value;
  const target = dropTarget.value;
  resetDrag();
  if (!drag || !target) {
    return;
  }
  const ids = column.items.map((item) => item.id as number);
  saveSiblingOrder(
    column,
    moveSiblingId(ids, drag.id, target.id, target.after),
  );
}

function resetDrag() {
  dragging.value = undefined;
  dropTarget.value = undefined;
  armedDragId.value = undefined;
}

onMounted(() => {
  loadCategories();
  window.addEventListener('mouseup', disarmDrag);
});

onBeforeUnmount(() => {
  window.removeEventListener('mouseup', disarmDrag);
});
</script>

<template>
  <Page auto-content-height>
    <div class="wujin-category-page">
      <div class="category-toolbar">
        <div class="category-toolbar-main">
          <span class="category-page-title">{{ pageTitle }}</span>
          <Input.Search
            v-model:value="keyword"
            class="category-search"
            allow-clear
            placeholder="搜索类目名称或编码"
            @change="handleSearchInput"
            @search="handleSearch"
          />
          <template v-if="searchSummary">
            <span class="category-search-summary">{{ searchSummary }}</span>
            <template v-if="searchHitIds.length > 1">
              <Button size="small" title="上一个" @click="stepHit(-1)">
                <ArrowUp class="size-4" />
              </Button>
              <Button size="small" title="下一个" @click="stepHit(1)">
                <ArrowDown class="size-4" />
              </Button>
            </template>
          </template>
        </div>
        <div class="category-toolbar-actions">
          <Radio.Group
            v-model:value="laneFilter"
            button-style="solid"
            size="small"
            @change="handleLaneChange"
          >
            <Radio.Button
              v-for="option in laneFilterOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </Radio.Button>
          </Radio.Group>
          <Button :loading="loading" title="刷新" @click="loadCategories">
            <RotateCw class="size-4" />
          </Button>
          <Button
            v-if="canCreate"
            type="primary"
            @click="openCreate(columns[0] as CategoryColumn)"
          >
            + 新增大类
          </Button>
        </div>
      </div>

      <Spin :spinning="loading" wrapper-class-name="category-spin">
        <div class="category-columns">
          <section
            v-for="column in columns"
            :key="column.level"
            class="category-column"
          >
            <header class="category-column-head">
              <span>{{ column.title }}（{{ column.items.length }}）</span>
              <Tooltip title="每列可独立上下滚动">
                <span class="category-column-scroll-hint">↕</span>
              </Tooltip>
            </header>

            <div class="category-column-body" @dragover.prevent>
              <template v-for="item in column.items" :key="item.id">
                <CategoryInlineEditor
                  v-if="isEditing(item) && editor"
                  v-model:form="editor.form"
                  mode="edit"
                  :code-inherited="editor.codeInherited"
                  :parent-code="editor.parentCode"
                  :saving="saving"
                  :show-lane="false"
                  @cancel="cancelEditor"
                  @save="saveEditor"
                />
                <div
                  v-else
                  class="category-row"
                  :class="{
                    'is-selected': isSelected(column, item),
                    'is-hit': searchHitIds.includes(item.id as number),
                    'is-hit-active': activeHitId === item.id,
                    'is-disabled': Number(item.status) === 1,
                    'drop-before':
                      dropTarget?.id === item.id && !dropTarget?.after,
                    'drop-after':
                      dropTarget?.id === item.id && dropTarget?.after,
                  }"
                  :data-category-id="item.id"
                  :draggable="armedDragId === item.id"
                  @click="selectNode(column, item)"
                  @dragend="resetDrag"
                  @dragover="handleDragOver($event, column, item)"
                  @dragstart="handleDragStart($event, column, item)"
                  @drop="handleDrop($event, column)"
                >
                  <span
                    v-if="canUpdate"
                    class="category-drag-handle"
                    title="⇅ 拖拽调整同级顺序"
                    @mousedown="armDrag(item)"
                  >
                    <GripVertical class="size-4" />
                  </span>
                  <span class="category-row-arrow">
                    <ChevronRight
                      v-if="hasChildren(categoryIndex, item.id)"
                      class="size-4"
                    />
                  </span>
                  <div class="category-row-main">
                    <div class="category-row-name" :title="item.name">
                      <template
                        v-for="(segment, segmentIndex) in nameSegments(item)"
                        :key="segmentIndex"
                      >
                        <mark v-if="segment.hit">{{ segment.text }}</mark>
                        <template v-else>{{ segment.text }}</template>
                      </template>
                    </div>
                    <!-- 标签放在编码行，名称独占第一行，窄列也能看清 -->
                    <div class="category-row-meta">
                      <span class="category-row-code" :title="item.code">{{
                        item.code
                      }}</span>
                      <Tag
                        v-if="column.level === 1 && laneOption(item.lane)"
                        :color="laneOption(item.lane)?.color"
                      >
                        {{ laneOption(item.lane)?.label }}
                      </Tag>
                      <Tag v-if="Number(item.status) === 1">停用</Tag>
                      <Tag
                        v-if="
                          item.healthStatus && item.healthStatus !== 'HEALTHY'
                        "
                        color="orange"
                      >
                        {{ healthLabel(item.healthStatus) }}
                      </Tag>
                    </div>
                  </div>
                  <div class="category-row-actions" @click.stop>
                    <span class="category-row-quick">
                      <Tooltip v-if="canUpdate" title="编辑">
                        <Button
                          size="small"
                          type="text"
                          @click="openEdit(column, item)"
                        >
                          <IconifyIcon :icon="ACTION_ICON.EDIT" />
                        </Button>
                      </Tooltip>
                      <Tooltip
                        v-if="canCreate && column.level < MAX_CATEGORY_LEVEL"
                        title="添加子级"
                      >
                        <Button
                          size="small"
                          type="text"
                          @click="openCreateChild(column, item)"
                        >
                          <IconifyIcon :icon="ACTION_ICON.ADD" />
                        </Button>
                      </Tooltip>
                      <Tooltip v-if="canDelete" title="删除">
                        <Button
                          danger
                          size="small"
                          type="text"
                          @click="confirmDelete(column, item)"
                        >
                          <IconifyIcon :icon="ACTION_ICON.DELETE" />
                        </Button>
                      </Tooltip>
                    </span>
                    <Dropdown :trigger="['click']">
                      <Button size="small" type="text">
                        <Ellipsis class="size-4" />
                      </Button>
                      <template #overlay>
                        <Menu
                          @click="
                            ({ key }) =>
                              handleMenuClick(column, item, String(key))
                          "
                        >
                          <Menu.Item v-if="canUpdate" key="edit">
                            ✎ 编辑
                          </Menu.Item>
                          <Menu.Item
                            v-if="
                              canCreate && column.level < MAX_CATEGORY_LEVEL
                            "
                            key="addChild"
                          >
                            ＋ 添加子级
                          </Menu.Item>
                          <Menu.Item v-if="canUpdate" key="up">
                            ⇅ 上移
                          </Menu.Item>
                          <Menu.Item v-if="canUpdate" key="down">
                            ⇅ 下移
                          </Menu.Item>
                          <Menu.Item v-if="canDelete" key="delete" danger>
                            🗑 删除
                          </Menu.Item>
                        </Menu>
                      </template>
                    </Dropdown>
                  </div>
                </div>
              </template>

              <Empty
                v-if="column.items.length === 0 && !isCreatingIn(column)"
                class="category-column-empty"
                :description="column.emptyText"
                :image="Empty.PRESENTED_IMAGE_SIMPLE"
              />

              <CategoryInlineEditor
                v-if="isCreatingIn(column) && editor"
                v-model:form="editor.form"
                mode="create"
                :code-inherited="editor.codeInherited"
                :parent-code="editor.parentCode"
                :saving="saving"
                :show-lane="column.level === 1"
                @cancel="cancelEditor"
                @save="saveEditor"
              />
            </div>

            <footer class="category-column-foot">
              <Button
                v-if="canCreate"
                block
                type="dashed"
                :disabled="column.level > 1 && !column.parent"
                @click="openCreate(column)"
              >
                + {{ column.addLabel }}
              </Button>
            </footer>
          </section>
        </div>
      </Spin>
    </div>
  </Page>
</template>

<style scoped>
.wujin-category-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.category-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: hsl(var(--card));
  border-radius: 8px;
}

.category-toolbar-main,
.category-toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.category-page-title {
  margin-right: 8px;
  font-size: 16px;
  font-weight: 600;
}

.category-search {
  width: 280px;
}

.category-search-summary {
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}

.category-spin {
  flex: 1;
  min-height: 0;
}

.category-spin :deep(.ant-spin-container) {
  height: 100%;
}

.category-columns {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: 12px;
  height: 100%;
  min-height: 420px;
  overflow-x: auto;
}

.category-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
}

.category-column-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  font-weight: 600;
  border-bottom: 1px solid hsl(var(--border));
}

.category-column-scroll-hint {
  color: hsl(var(--muted-foreground));
  cursor: default;
}

/* 每列独立上下滚动，互不影响 */
.category-column-body {
  flex: 1;
  min-height: 0;
  padding: 6px;
  overflow-y: auto;
}

.category-column-foot {
  padding: 8px;
  border-top: 1px solid hsl(var(--border));
}

.category-column-empty {
  margin: 24px 0;
}

.category-row {
  position: relative;
  display: flex;
  gap: 4px;
  align-items: center;
  min-height: 44px;
  padding: 4px 6px;
  cursor: pointer;
  border: 1px solid transparent;
  border-radius: 6px;
}

.category-row:hover {
  background: hsl(var(--accent));
}

.category-row.is-selected {
  background: hsl(var(--primary) / 12%);
  border-color: hsl(var(--primary) / 40%);
}

.category-row.is-hit {
  background: hsl(48deg 100% 92%);
}

.category-row.is-hit-active {
  border-color: hsl(38deg 92% 50%);
}

.category-row.is-disabled .category-row-name {
  color: hsl(var(--muted-foreground));
}

.category-row.drop-before {
  box-shadow: inset 0 2px 0 hsl(var(--primary));
}

.category-row.drop-after {
  box-shadow: inset 0 -2px 0 hsl(var(--primary));
}

.category-drag-handle {
  display: flex;
  color: hsl(var(--muted-foreground));
  cursor: grab;
  visibility: hidden;
}

.category-row:hover .category-drag-handle {
  visibility: visible;
}

.category-row-arrow {
  display: flex;
  flex-shrink: 0;
  width: 16px;
  color: hsl(var(--primary));
}

.category-row-main {
  flex: 1;
  min-width: 0;
}

.category-row-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-row-name mark {
  padding: 0 1px;
  background: hsl(48deg 100% 70%);
  border-radius: 2px;
}

.category-row-meta {
  display: flex;
  gap: 4px;
  align-items: center;
  min-width: 0;
}

.category-row-code {
  min-width: 0;
  overflow: hidden;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-row-meta :deep(.ant-tag) {
  flex-shrink: 0;
  margin-inline-end: 0;
  font-size: 11px;
  line-height: 16px;
  padding-inline: 4px;
}

.category-row-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
}

/* 悬停操作浮在行右侧（⋯ 左边），不挤压名称和标签，避免悬停时布局跳动 */
.category-row-quick {
  position: absolute;
  top: 50%;
  right: 34px;
  display: none;
  background: hsl(var(--card));
  border-radius: 6px;
  box-shadow: 0 1px 4px hsl(var(--foreground) / 12%);
  transform: translateY(-50%);
}

.category-row:hover .category-row-quick {
  display: inline-flex;
}
</style>
