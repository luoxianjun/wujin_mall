package cn.iocoder.yudao.module.wujin.service.category;

import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.category.vo.WujinCategoryMappingSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.category.WujinCategoryMappingDO;

import java.util.List;

public interface WujinCategoryMappingAdminService {

    Long createMapping(WujinCategoryMappingSaveReqVO createReqVO);

    void updateMapping(WujinCategoryMappingSaveReqVO updateReqVO);

    void deleteMapping(Long id);

    WujinCategoryMappingDO getMapping(Long id);

    List<WujinCategoryMappingDO> getMappingList(WujinCategoryMappingListReqVO listReqVO);
}
