package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySortReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;

import java.util.List;

public interface WujinCategoryAdminService {

    Long createCategory(WujinCategorySaveReqVO createReqVO);

    void updateCategory(WujinCategorySaveReqVO updateReqVO);

    int batchMigrateCategory(WujinCategoryBatchMigrateReqVO migrateReqVO);

    void deleteCategory(Long id);

    /**
     * 删除分类；cascade 为 true 时同时删除全部子级
     *
     * @return 删除的分类数量（含自身）
     */
    int deleteCategory(Long id, boolean cascade);

    /**
     * 按传入顺序更新同级分类排序
     */
    void updateCategorySort(WujinCategorySortReqVO sortReqVO);

    WujinCategoryDO getCategory(Long id);

    List<WujinCategoryDO> getCategoryList(WujinCategoryListReqVO listReqVO);
}
