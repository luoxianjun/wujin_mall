package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategorySaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryDO;

import java.util.List;

public interface WujinCategoryAdminService {

    Long createCategory(WujinCategorySaveReqVO createReqVO);

    void updateCategory(WujinCategorySaveReqVO updateReqVO);

    int batchMigrateCategory(WujinCategoryBatchMigrateReqVO migrateReqVO);

    void deleteCategory(Long id);

    WujinCategoryDO getCategory(Long id);

    List<WujinCategoryDO> getCategoryList(WujinCategoryListReqVO listReqVO);
}
