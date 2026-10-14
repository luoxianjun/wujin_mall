import type { WujinCategoryApi } from '#/api/wujin/category';

type Category = WujinCategoryApi.Category;

export const ROOT_PARENT_ID = 0;

/** 大行业 + 一级、二级、三级分类，与后端最大层级保持一致 */
export const MAX_CATEGORY_LEVEL = 4;

export const CATEGORY_COLUMN_TITLES = [
  '大行业',
  '一级分类',
  '二级分类',
  '三级分类',
];

export const CATEGORY_ADD_LABELS = [
  '新增大类',
  '新增一级',
  '新增二级',
  '新增三级',
];

export interface CategoryIndex {
  byId: Map<number, Category>;
  children: Map<number, Category[]>;
}

function compareCategory(left: Category, right: Category) {
  const sortDiff = Number(left.sort ?? 0) - Number(right.sort ?? 0);
  return sortDiff === 0 ? Number(left.id) - Number(right.id) : sortDiff;
}

export function buildCategoryIndex(list: Category[]): CategoryIndex {
  const byId = new Map<number, Category>();
  const children = new Map<number, Category[]>();
  for (const item of list) {
    if (item.id !== undefined) {
      byId.set(item.id, item);
    }
  }
  for (const item of byId.values()) {
    const parentId = Number(item.parentId ?? ROOT_PARENT_ID);
    // 父级已不存在的孤儿节点按根节点展示，避免数据不可见
    const key =
      parentId === ROOT_PARENT_ID || byId.has(parentId)
        ? parentId
        : ROOT_PARENT_ID;
    const siblings = children.get(key) ?? [];
    siblings.push(item);
    children.set(key, siblings);
  }
  for (const siblings of children.values()) {
    siblings.sort(compareCategory);
  }
  return { byId, children };
}

export function getChildren(index: CategoryIndex, parentId: number) {
  return index.children.get(parentId) ?? [];
}

export function hasChildren(index: CategoryIndex, id?: number) {
  return id !== undefined && getChildren(index, id).length > 0;
}

export function countDescendants(index: CategoryIndex, id: number): number {
  let total = 0;
  for (const child of getChildren(index, id)) {
    total += 1 + countDescendants(index, child.id as number);
  }
  return total;
}

/** 从根节点到目标节点的完整路径（含目标节点） */
export function getAncestorPath(index: CategoryIndex, id: number): Category[] {
  const path: Category[] = [];
  const visited = new Set<number>();
  let current = index.byId.get(id);
  while (current && current.id !== undefined && !visited.has(current.id)) {
    visited.add(current.id);
    path.unshift(current);
    current = index.byId.get(Number(current.parentId ?? ROOT_PARENT_ID));
  }
  return path;
}

/** 按名称或编码模糊匹配，浅层级优先 */
export function matchCategories(list: Category[], keyword: string): Category[] {
  const text = keyword.trim().toLowerCase();
  if (!text) {
    return [];
  }
  return list
    .filter(
      (item) =>
        String(item.name ?? '')
          .toLowerCase()
          .includes(text) ||
        String(item.code ?? '')
          .toLowerCase()
          .includes(text),
    )
    .sort(
      (left, right) =>
        Number(left.level ?? 0) - Number(right.level ?? 0) ||
        compareCategory(left, right),
    );
}

/** 子级编码继承父级：P → P.TIRE → P.TIRE.CAR */
export function buildChildCode(parentCode: string | undefined, suffix: string) {
  const normalizedSuffix = suffix.trim().replace(/^\.+/, '');
  const prefix = (parentCode ?? '').trim();
  if (!prefix) {
    return normalizedSuffix;
  }
  return normalizedSuffix ? `${prefix}.${normalizedSuffix}` : '';
}

/** 编辑时拆出父级前缀后的编码；不符合继承规则的历史编码原样返回 */
export function splitChildCode(
  parentCode: string | undefined,
  code: string | undefined,
) {
  const prefix = (parentCode ?? '').trim();
  const value = (code ?? '').trim();
  if (prefix && value.startsWith(`${prefix}.`)) {
    return { inherited: true, suffix: value.slice(prefix.length + 1) };
  }
  return { inherited: false, suffix: value };
}

/** 同级拖拽：把 dragId 移到 targetId 之前（after=true 时移到之后） */
export function moveSiblingId(
  ids: number[],
  dragId: number,
  targetId: number,
  after = false,
) {
  if (dragId === targetId || !ids.includes(dragId) || !ids.includes(targetId)) {
    return [...ids];
  }
  const rest = ids.filter((id) => id !== dragId);
  const targetIndex = rest.indexOf(targetId);
  rest.splice(after ? targetIndex + 1 : targetIndex, 0, dragId);
  return rest;
}

export const CATEGORY_LANE_OPTIONS: Array<{
  color: string;
  label: string;
  value: WujinCategoryApi.Lane;
}> = [
  { color: 'blue', label: '成品', value: 'PRODUCT' },
  { color: 'purple', label: '加工', value: 'PROCESS' },
  { color: 'green', label: '原材料', value: 'MATERIAL' },
];

export const CATEGORY_HEALTH_OPTIONS = [
  { label: '健康', value: 'HEALTHY' },
  { label: '需拆分', value: 'NEEDS_SPLIT' },
  { label: '无绑定', value: 'UNBOUND' },
];

/** 行内编辑表单，沿用类目编码、说明、搜索展示层级、健康状态、状态字段 */
export interface CategoryEditorForm {
  name: string;
  codeSuffix: string;
  lane: WujinCategoryApi.Lane;
  displayDepth?: number;
  healthStatus?: string;
  enabled: boolean;
  description: string;
}

export interface HighlightSegment {
  hit: boolean;
  text: string;
}

/** 把文本按关键字切成高亮片段（忽略大小写） */
export function highlightSegments(text: string | undefined, keyword: string) {
  const value = text ?? '';
  const needle = keyword.trim().toLowerCase();
  if (!needle) {
    return [{ hit: false, text: value }];
  }
  const segments: HighlightSegment[] = [];
  const lower = value.toLowerCase();
  let cursor = 0;
  let found = lower.indexOf(needle, cursor);
  while (found !== -1) {
    if (found > cursor) {
      segments.push({ hit: false, text: value.slice(cursor, found) });
    }
    segments.push({
      hit: true,
      text: value.slice(found, found + needle.length),
    });
    cursor = found + needle.length;
    found = lower.indexOf(needle, cursor);
  }
  if (cursor < value.length) {
    segments.push({ hit: false, text: value.slice(cursor) });
  }
  return segments;
}
