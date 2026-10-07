package cn.iocoder.yudao.module.wujin.service.template;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.template.WujinIndustryTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinIndustryTemplateAdminServiceImpl implements WujinIndustryTemplateAdminService {

    @Resource
    private WujinIndustryTemplateMapper templateMapper;

    @Override
    public Long createTemplate(WujinIndustryTemplateSaveReqVO createReqVO) {
        WujinIndustryTemplateDO template = BeanUtils.toBean(createReqVO, WujinIndustryTemplateDO.class);
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(WujinIndustryTemplateSaveReqVO updateReqVO) {
        validateTemplateExists(updateReqVO.getId());
        WujinIndustryTemplateDO template = BeanUtils.toBean(updateReqVO, WujinIndustryTemplateDO.class);
        templateMapper.updateById(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        validateTemplateExists(id);
        templateMapper.deleteById(id);
    }

    @Override
    public WujinIndustryTemplateDO getTemplate(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public List<WujinIndustryTemplateDO> getTemplateList(WujinIndustryTemplateListReqVO listReqVO) {
        return templateMapper.selectList(listReqVO);
    }

    private void validateTemplateExists(Long id) {
        if (id == null || templateMapper.selectById(id) == null) {
            throw new IllegalArgumentException("行业模板不存在");
        }
    }
}
