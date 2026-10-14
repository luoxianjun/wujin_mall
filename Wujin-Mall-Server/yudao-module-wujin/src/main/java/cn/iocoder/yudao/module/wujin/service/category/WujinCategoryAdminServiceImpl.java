package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySortReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class WujinCategoryAdminServiceImpl implements WujinCategoryAdminService {

    /**
     * 最大层级：大行业（1）+ 一级、二级、三级分类（2-4）
     */
    static final int MAX_CATEGORY_LEVEL = 4;

    @Resource
    private WujinCategoryMapper categoryMapper;

    @Override
    public Long createCategory(WujinCategorySaveReqVO createReqVO) {
        createReqVO.setLevel(resolveLevel(validateParentCategory(createReqVO.getParentId(), createReqVO.getLane())));
        validateLevelDepth(createReqVO.getLevel(), 0);
        WujinCategoryDO category = BeanUtils.toBean(createReqVO, WujinCategoryDO.class);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(WujinCategorySaveReqVO updateReqVO) {
        validateCategoryExists(updateReqVO.getId());
        List<WujinCategoryDO> all = categoryMapper.selectList();
        validateNotMovedUnderItself(updateReqVO.getId(), updateReqVO.getParentId(), all);
        WujinCategoryDO parent = validateParentCategory(updateReqVO.getParentId(), updateReqVO.getLane());
        updateReqVO.setLevel(resolveLevel(parent));
        validateLevelDepth(updateReqVO.getLevel(), subtreeHeight(updateReqVO.getId(), all));
        WujinCategoryDO category = BeanUtils.toBean(updateReqVO, WujinCategoryDO.class);
        categoryMapper.updateById(category);
        refreshDescendants(updateReqVO.getId(), updateReqVO.getLane(), updateReqVO.getLevel(), all);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMigrateCategory(WujinCategoryBatchMigrateReqVO migrateReqVO) {
        WujinCategoryDO parent = validateParentCategory(migrateReqVO.getTargetParentId(), migrateReqVO.getTargetLane());
        int targetLevel = resolveLevel(parent);
        List<WujinCategoryDO> all = categoryMapper.selectList();
        int count = 0;
        for (Long id : migrateReqVO.getIds()) {
            WujinCategoryDO category = categoryMapper.selectById(id);
            if (category == null) {
                throw new IllegalArgumentException("五金分类不存在");
            }
            validateNotMovedUnderItself(id, migrateReqVO.getTargetParentId(), all);
            validateLevelDepth(targetLevel, subtreeHeight(id, all));
            category.setParentId(migrateReqVO.getTargetParentId());
            category.setLane(migrateReqVO.getTargetLane());
            category.setLevel(targetLevel);
            category.setDisplayDepth(migrateReqVO.getDisplayDepth());
            category.setHealthStatus(migrateReqVO.getHealthStatus());
            categoryMapper.updateById(category);
            refreshDescendants(id, migrateReqVO.getTargetLane(), targetLevel, all);
            count++;
        }
        return count;
    }

    @Override
    public void deleteCategory(Long id) {
        deleteCategory(id, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCategory(Long id, boolean cascade) {
        validateCategoryExists(id);
        if (!cascade && categoryMapper.selectCountByParentId(id) > 0) {
            throw new IllegalArgumentException("存在子分类，无法删除");
        }
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        collectDescendantIds(id, categoryMapper.selectList(), ids);
        categoryMapper.deleteByIds(ids);
        return ids.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategorySort(WujinCategorySortReqVO sortReqVO) {
        List<Long> ids = sortReqVO.getIds();
        for (int i = 0; i < ids.size(); i++) {
            WujinCategoryDO category = categoryMapper.selectById(ids.get(i));
            if (category == null) {
                throw new IllegalArgumentException("五金分类不存在");
            }
            if (!Objects.equals(category.getParentId(), sortReqVO.getParentId())) {
                throw new IllegalArgumentException("只能对同一父级下的分类排序");
            }
            WujinCategoryDO update = new WujinCategoryDO();
            update.setId(category.getId());
            update.setSort(i + 1);
            categoryMapper.updateById(update);
        }
    }

    @Override
    public WujinCategoryDO getCategory(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<WujinCategoryDO> getCategoryList(WujinCategoryListReqVO listReqVO) {
        return categoryMapper.selectList(listReqVO);
    }

    private void validateCategoryExists(Long id) {
        if (id == null || categoryMapper.selectById(id) == null) {
            throw new IllegalArgumentException("五金分类不存在");
        }
    }

    /**
     * 校验父分类与泳道一致，返回父分类；根节点返回 null
     */
    private WujinCategoryDO validateParentCategory(Long parentId, String lane) {
        if (Objects.equals(parentId, WujinCategoryDO.PARENT_ID_ROOT)) {
            return null;
        }
        WujinCategoryDO parent = categoryMapper.selectById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父分类不存在");
        }
        if (!Objects.equals(parent.getLane(), lane)) {
            throw new IllegalArgumentException("类目父子节点必须属于同一泳道");
        }
        return parent;
    }

    private int resolveLevel(WujinCategoryDO parent) {
        return parent == null || parent.getLevel() == null ? 1 : parent.getLevel() + 1;
    }

    /**
     * 校验节点及其子树移动后不超过最大层级
     *
     * @param level         节点新层级
     * @param subtreeHeight 节点下方子树高度，叶子节点为 0
     */
    private void validateLevelDepth(int level, int subtreeHeight) {
        if (level + subtreeHeight > MAX_CATEGORY_LEVEL) {
            throw new IllegalArgumentException("类目最多支持到三级分类");
        }
    }

    private void validateNotMovedUnderItself(Long id, Long parentId, List<WujinCategoryDO> all) {
        if (Objects.equals(id, parentId)) {
            throw new IllegalArgumentException("不能将分类移动到自身下");
        }
        List<Long> descendants = new ArrayList<>();
        collectDescendantIds(id, all, descendants);
        if (descendants.contains(parentId)) {
            throw new IllegalArgumentException("不能将分类移动到自己的子级下");
        }
    }

    private int subtreeHeight(Long id, List<WujinCategoryDO> all) {
        int height = 0;
        for (WujinCategoryDO category : all) {
            if (Objects.equals(category.getParentId(), id)) {
                height = Math.max(height, 1 + subtreeHeight(category.getId(), all));
            }
        }
        return height;
    }

    private void collectDescendantIds(Long id, List<WujinCategoryDO> all, List<Long> ids) {
        for (WujinCategoryDO category : all) {
            if (Objects.equals(category.getParentId(), id) && !ids.contains(category.getId())) {
                ids.add(category.getId());
                collectDescendantIds(category.getId(), all, ids);
            }
        }
    }

    /**
     * 父节点层级或泳道变化后，同步子树的层级与泳道
     */
    private void refreshDescendants(Long id, String lane, int level, List<WujinCategoryDO> all) {
        for (WujinCategoryDO category : all) {
            if (!Objects.equals(category.getParentId(), id)) {
                continue;
            }
            if (!Objects.equals(category.getLevel(), level + 1) || !Objects.equals(category.getLane(), lane)) {
                WujinCategoryDO update = new WujinCategoryDO();
                update.setId(category.getId());
                update.setLevel(level + 1);
                update.setLane(lane);
                categoryMapper.updateById(update);
            }
            refreshDescendants(category.getId(), lane, level + 1, all);
        }
    }
}
