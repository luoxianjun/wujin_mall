package cn.iocoder.yudao.module.wujin.service.template;

import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;

import java.util.List;

public interface WujinIndustryTemplateItemAdminService {

    Long createTemplateItem(WujinIndustryTemplateItemSaveReqVO createReqVO);

    void updateTemplateItem(WujinIndustryTemplateItemSaveReqVO updateReqVO);

    int batchMigrateTemplateItem(WujinIndustryTemplateItemBatchMigrateReqVO migrateReqVO);

    void deleteTemplateItem(Long id);

    WujinIndustryTemplateItemDO getTemplateItem(Long id);

    List<WujinIndustryTemplateItemDO> getTemplateItemList(WujinIndustryTemplateItemListReqVO listReqVO);
}
