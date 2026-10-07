package cn.iocoder.yudao.module.wujin.service.template;

import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;

import java.util.List;

public interface WujinIndustryTemplateAdminService {

    Long createTemplate(WujinIndustryTemplateSaveReqVO createReqVO);

    void updateTemplate(WujinIndustryTemplateSaveReqVO updateReqVO);

    void deleteTemplate(Long id);

    WujinIndustryTemplateDO getTemplate(Long id);

    List<WujinIndustryTemplateDO> getTemplateList(WujinIndustryTemplateListReqVO listReqVO);
}
