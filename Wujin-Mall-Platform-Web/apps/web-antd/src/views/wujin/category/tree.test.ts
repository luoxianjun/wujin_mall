import { describe, expect, it } from 'vitest';

import {
  buildCategoryIndex,
  buildChildCode,
  countDescendants,
  getAncestorPath,
  getChildren,
  hasChildren,
  highlightSegments,
  matchCategories,
  moveSiblingId,
  splitChildCode,
} from './tree';

const categories = [
  {
    code: 'P',
    id: 1,
    lane: 'PRODUCT' as const,
    level: 1,
    name: '五金工具',
    parentId: 0,
    sort: 2,
  },
  {
    code: 'M',
    id: 2,
    lane: 'MATERIAL' as const,
    level: 1,
    name: '五金原材料',
    parentId: 0,
    sort: 1,
  },
  { code: 'P.TIRE', id: 3, level: 2, name: '轮胎', parentId: 1, sort: 1 },
  { code: 'P.BOLT', id: 4, level: 2, name: '紧固件', parentId: 1, sort: 2 },
  {
    code: 'P.TIRE.CAR',
    id: 5,
    level: 3,
    name: '乘用车轮胎',
    parentId: 3,
    sort: 1,
  },
  {
    code: 'P.TIRE.CAR.SUV',
    id: 6,
    level: 4,
    name: 'SUV轮胎',
    parentId: 5,
    sort: 1,
  },
  { code: 'X', id: 7, level: 2, name: '孤儿类目', parentId: 99, sort: 9 },
];

describe('wujin category tree helpers', () => {
  const index = buildCategoryIndex(categories);

  it('groups children under their parent ordered by sort', () => {
    expect(getChildren(index, 0).map((item) => item.id)).toEqual([2, 1, 7]);
    expect(getChildren(index, 1).map((item) => item.id)).toEqual([3, 4]);
    expect(hasChildren(index, 3)).toBe(true);
    expect(hasChildren(index, 4)).toBe(false);
  });

  it('counts all descendants for the cascade delete confirmation', () => {
    expect(countDescendants(index, 1)).toBe(4);
    expect(countDescendants(index, 5)).toBe(1);
    expect(countDescendants(index, 6)).toBe(0);
  });

  it('expands the full path of a search hit', () => {
    expect(getAncestorPath(index, 6).map((item) => item.name)).toEqual([
      '五金工具',
      '轮胎',
      '乘用车轮胎',
      'SUV轮胎',
    ]);
    const hits = matchCategories(categories, 'tire');
    expect(hits.map((item) => item.id)).toEqual([3, 5, 6]);
    expect(matchCategories(categories, '  ')).toEqual([]);
  });

  it('inherits the parent code for new children', () => {
    expect(buildChildCode('P', 'TIRE')).toBe('P.TIRE');
    expect(buildChildCode('P.TIRE', '.CAR ')).toBe('P.TIRE.CAR');
    expect(buildChildCode(undefined, 'P')).toBe('P');
    expect(buildChildCode('P', '')).toBe('');
    expect(splitChildCode('P.TIRE', 'P.TIRE.CAR')).toEqual({
      inherited: true,
      suffix: 'CAR',
    });
    expect(splitChildCode('P.TIRE', 'LEGACY-CODE')).toEqual({
      inherited: false,
      suffix: 'LEGACY-CODE',
    });
  });

  it('reorders siblings for drag sorting', () => {
    expect(moveSiblingId([1, 2, 3, 4], 4, 2)).toEqual([1, 4, 2, 3]);
    expect(moveSiblingId([1, 2, 3, 4], 1, 3, true)).toEqual([2, 3, 1, 4]);
    expect(moveSiblingId([1, 2, 3], 2, 2)).toEqual([1, 2, 3]);
    expect(moveSiblingId([1, 2, 3], 9, 2)).toEqual([1, 2, 3]);
  });

  it('splits names into highlighted segments', () => {
    expect(highlightSegments('乘用车轮胎', '轮胎')).toEqual([
      { hit: false, text: '乘用车' },
      { hit: true, text: '轮胎' },
    ]);
    expect(highlightSegments('SUV轮胎', 'suv')[0]).toEqual({
      hit: true,
      text: 'SUV',
    });
    expect(highlightSegments('轴承', '')).toEqual([
      { hit: false, text: '轴承' },
    ]);
  });
});
