<script lang="ts" setup>
import type { WujinPlatformApi } from '#/api/wujin/platform';

import { computed } from 'vue';

import { Empty, Tag } from 'ant-design-vue';

import { laneOptions, optionLabel, relationTypeOptions } from '../data';

defineOptions({ name: 'WujinPlatformTemplateItemDesignerPanel' });

const props = withDefaults(
  defineProps<{
    entities?: WujinPlatformApi.ChainEntity[];
    items?: WujinPlatformApi.IndustryTemplateItem[];
    template?: WujinPlatformApi.IndustryTemplate;
  }>(),
  {
    entities: () => [],
    items: () => [],
  },
);

function relationRank(relationType?: string) {
  const index = relationTypeOptions.findIndex(
    (item) => item.value === relationType,
  );
  return index === -1 ? relationTypeOptions.length : index;
}

const sortedTemplateItems = computed(() =>
  [...props.items].sort((left, right) => {
    const relationSort =
      relationRank(left.relationType) - relationRank(right.relationType);
    if (relationSort !== 0) {
      return relationSort;
    }
    const itemSort = (left.sort ?? 0) - (right.sort ?? 0);
    if (itemSort !== 0) {
      return itemSort;
    }
    return (left.id ?? 0) - (right.id ?? 0);
  }),
);

const requiredTemplateItems = computed(() =>
  sortedTemplateItems.value.filter((item) => item.requiredFlag),
);

const optionalTemplateItems = computed(() =>
  sortedTemplateItems.value.filter((item) => !item.requiredFlag),
);

const weightTotal = computed(() =>
  sortedTemplateItems.value.reduce(
    (total, item) => total + Number(item.weight ?? 0),
    0,
  ),
);

const chainEntityMap = computed(
  () =>
    new Map(
      props.entities
        .filter((entity) => entity.id)
        .map((entity) => [entity.id as number, entity]),
    ),
);

function chainEntity(entityId?: number) {
  return chainEntityMap.value.get(entityId ?? -1);
}

function chainEntityName(entityId?: number) {
  return chainEntity(entityId)?.name ?? '未命名实体';
}

function chainEntityMeta(entityId?: number) {
  const entity = chainEntity(entityId);
  return [
    entity?.lane ? optionLabel(laneOptions, entity.lane) : undefined,
    entity?.entityCode,
    entityId ? `#${entityId}` : undefined,
  ]
    .filter(Boolean)
    .join(' · ');
}

const templateDesignerGroups = computed(() => {
  const groups = new Map<string, WujinPlatformApi.IndustryTemplateItem[]>();
  sortedTemplateItems.value.forEach((item) => {
    const relationType = item.relationType ?? 'UNSPECIFIED';
    groups.set(relationType, [...(groups.get(relationType) ?? []), item]);
  });

  return [...groups.entries()]
    .sort(
      ([leftRelationType], [rightRelationType]) =>
        relationRank(leftRelationType) - relationRank(rightRelationType),
    )
    .map(([relationType, items]) => ({
      items,
      label:
        relationType === 'UNSPECIFIED'
          ? '未配置关系'
          : optionLabel(relationTypeOptions, relationType),
      optionalCount: items.filter((item) => !item.requiredFlag).length,
      relationType,
      requiredCount: items.filter((item) => item.requiredFlag).length,
      weight: items.reduce(
        (total, item) => total + Number(item.weight ?? 0),
        0,
      ),
    }));
});

const templateDesignerStats = computed(() => [
  { label: '必需项', value: requiredTemplateItems.value.length },
  { label: '可选项', value: optionalTemplateItems.value.length },
  { label: '匹配权重合计', value: weightTotal.value },
]);
</script>

<template>
  <section class="template-item-designer">
    <div class="template-item-designer__header">
      <div>
        <div class="template-item-designer__title">模板关系概览</div>
        <div class="template-item-designer__subtitle">
          {{ template?.name ?? '未命名模板' }}
          <span v-if="template?.industryCode"
            >/ {{ template.industryCode }}</span
          >
        </div>
      </div>
      <div class="template-item-designer__stats">
        <div
          v-for="stat in templateDesignerStats"
          :key="stat.label"
          class="template-item-designer__stat"
        >
          <span>{{ stat.label }}</span>
          <strong>{{ stat.value }}</strong>
        </div>
      </div>
    </div>

    <div
      v-if="sortedTemplateItems.length === 0"
      class="template-item-designer__empty"
    >
      <Empty description="暂无关系项，请点击“新增关系项”" />
    </div>
    <template v-else>
      <div class="template-item-designer__groups">
        <div
          v-for="group in templateDesignerGroups"
          :key="group.relationType"
          class="template-item-designer__group"
        >
          <div class="template-item-designer__group-header">
            <Tag color="blue">{{ group.label }}</Tag>
            <span>
              共 {{ group.items.length }} 项 · 必需 {{ group.requiredCount }} ·
              可选
              {{ group.optionalCount }}
            </span>
          </div>

          <div class="template-item-designer__items">
            <div
              v-for="item in group.items"
              :key="item.id ?? `${item.entityId}-${item.sort}`"
              class="template-item-designer__item"
            >
              <div class="template-item-designer__item-main">
                <strong>{{ chainEntityName(item.entityId) }}</strong>
                <span>{{ chainEntityMeta(item.entityId) }}</span>
              </div>
              <Tag :color="item.requiredFlag ? 'red' : 'default'">
                {{ item.requiredFlag ? '必需' : '可选' }}
              </Tag>
              <div class="template-item-designer__item-meta">
                展示顺序 {{ item.sort ?? 0 }} · 匹配权重 {{ item.weight ?? 0 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<style scoped>
.template-item-designer {
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--background));
}

.template-item-designer__header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.template-item-designer__title {
  font-size: 15px;
  font-weight: 600;
  line-height: 22px;
}

.template-item-designer__subtitle {
  margin-top: 2px;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.template-item-designer__stats {
  display: grid;
  min-width: 260px;
  grid-template-columns: repeat(3, minmax(72px, 1fr));
  gap: 8px;
}

.template-item-designer__stat {
  padding: 8px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
  background: hsl(var(--muted) / 30%);
}

.template-item-designer__stat span {
  display: block;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.template-item-designer__stat strong {
  display: block;
  margin-top: 2px;
  font-size: 16px;
  line-height: 22px;
}

.template-item-designer__empty {
  padding: 16px 0 8px;
}

.template-item-designer__groups {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.template-item-designer__group {
  min-width: 0;
  padding: 10px;
  border: 1px solid hsl(var(--border));
  border-radius: 6px;
}

.template-item-designer__group-header {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.template-item-designer__items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.template-item-designer__item {
  display: grid;
  grid-template-columns: minmax(82px, 1fr) auto;
  gap: 4px 8px;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  background: hsl(var(--muted) / 35%);
}

.template-item-designer__item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.template-item-designer__item-main span,
.template-item-designer__item-meta {
  color: hsl(var(--muted-foreground));
  font-size: 12px;
}

.template-item-designer__item-main span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-item-designer__item-main strong {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-item-designer__item-meta {
  grid-column: 1 / -1;
}

@media (max-width: 768px) {
  .template-item-designer__header {
    flex-direction: column;
  }

  .template-item-designer__stats {
    width: 100%;
    min-width: 0;
  }
}
</style>
