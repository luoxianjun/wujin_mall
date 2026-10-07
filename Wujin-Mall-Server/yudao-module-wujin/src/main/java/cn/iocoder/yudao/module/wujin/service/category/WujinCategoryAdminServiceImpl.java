package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.category.WujinCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Validated
public class WujinCategoryAdminServiceImpl implements WujinCategoryAdminService {

    @Resource
    private WujinCategoryMapper categoryMapper;

    @Override
    public Long createCategory(WujinCategorySaveReqVO createReqVO) {
        validateParentCategory(createReqVO.getParentId(), createReqVO.getLane());
        WujinCategoryDO category = BeanUtils.toBean(createReqVO, WujinCategoryDO.class);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(WujinCategorySaveReqVO updateReqVO) {
        validateCategoryExists(updateReqVO.getId());
        validateParentCategory(updateReqVO.getParentId(), updateReqVO.getLane());
        WujinCategoryDO category = BeanUtils.toBean(updateReqVO, WujinCategoryDO.class);
        categoryMapper.updateById(category);
    }

    @Override
    public int batchMigrateCategory(WujinCategoryBatchMigrateReqVO migrateReqVO) {
        validateParentCategory(migrateReqVO.getTargetParentId(), migrateReqVO.getTargetLane());
        int count = 0;
        for (Long id : migrateReqVO.getIds()) {
            WujinCategoryDO category = categoryMapper.selectById(id);
            if (category == null) {
                throw new IllegalArgumentException("五金分类不存在");
            }
            category.setParentId(migrateReqVO.getTargetParentId());
            category.setLane(migrateReqVO.getTargetLane());
            category.setLevel(migrateReqVO.getTargetLevel());
            category.setDisplayDepth(migrateReqVO.getDisplayDepth());
            category.setHealthStatus(migrateReqVO.getHealthStatus());
            categoryMapper.updateById(category);
            count++;
        }
        return count;
    }

    @Override
    public void deleteCategory(Long id) {
        validateCategoryExists(id);
        if (categoryMapper.selectCountByParentId(id) > 0) {
            throw new IllegalArgumentException("存在子分类，无法删除");
        }
        categoryMapper.deleteById(id);
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

    private void validateParentCategory(Long parentId, String lane) {
        if (Objects.equals(parentId, WujinCategoryDO.PARENT_ID_ROOT)) {
            return;
        }
        WujinCategoryDO parent = categoryMapper.selectById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父分类不存在");
        }
        if (!Objects.equals(parent.getLane(), lane)) {
            throw new IllegalArgumentException("类目父子节点必须属于同一泳道");
        }
    }
}
