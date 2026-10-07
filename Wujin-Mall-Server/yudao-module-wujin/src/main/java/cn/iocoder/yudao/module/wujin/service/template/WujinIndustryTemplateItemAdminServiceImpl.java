package cn.iocoder.yudao.module.wujin.service.template;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemBatchMigrateReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.template.vo.WujinIndustryTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.template.WujinIndustryTemplateItemDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.chain.WujinChainEntityMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.template.WujinIndustryTemplateItemMapper;
import cn.iocoder.yudao.module.wujin.dal.mysql.template.WujinIndustryTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinIndustryTemplateItemAdminServiceImpl implements WujinIndustryTemplateItemAdminService {

    @Resource
    private WujinIndustryTemplateItemMapper templateItemMapper;
    @Resource
    private WujinIndustryTemplateMapper templateMapper;
    @Resource
    private WujinChainEntityMapper entityMapper;

    @Override
    public Long createTemplateItem(WujinIndustryTemplateItemSaveReqVO createReqVO) {
        validateTemplateItem(createReqVO);
        WujinIndustryTemplateItemDO item = BeanUtils.toBean(createReqVO, WujinIndustryTemplateItemDO.class);
        templateItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateTemplateItem(WujinIndustryTemplateItemSaveReqVO updateReqVO) {
        validateTemplateItemExists(updateReqVO.getId());
        validateTemplateItem(updateReqVO);
        WujinIndustryTemplateItemDO item = BeanUtils.toBean(updateReqVO, WujinIndustryTemplateItemDO.class);
        templateItemMapper.updateById(item);
    }

    @Override
    public int batchMigrateTemplateItem(WujinIndustryTemplateItemBatchMigrateReqVO migrateReqVO) {
        validateTemplateExists(migrateReqVO.getTargetTemplateId());
        int count = 0;
        for (Long id : migrateReqVO.getIds()) {
            WujinIndustryTemplateItemDO item = templateItemMapper.selectById(id);
            if (item == null) {
                throw new IllegalArgumentException("行业模板项不存在");
            }
            item.setTemplateId(migrateReqVO.getTargetTemplateId());
            if (migrateReqVO.getRelationType() != null) {
                item.setRelationType(migrateReqVO.getRelationType());
            }
            if (migrateReqVO.getRequiredFlag() != null) {
                item.setRequiredFlag(migrateReqVO.getRequiredFlag());
            }
            if (migrateReqVO.getWeight() != null) {
                item.setWeight(migrateReqVO.getWeight());
            }
            templateItemMapper.updateById(item);
            count++;
        }
        return count;
    }

    @Override
    public void deleteTemplateItem(Long id) {
        validateTemplateItemExists(id);
        templateItemMapper.deleteById(id);
    }

    @Override
    public WujinIndustryTemplateItemDO getTemplateItem(Long id) {
        return templateItemMapper.selectById(id);
    }

    @Override
    public List<WujinIndustryTemplateItemDO> getTemplateItemList(WujinIndustryTemplateItemListReqVO listReqVO) {
        return templateItemMapper.selectList(listReqVO);
    }

    private void validateTemplateItemExists(Long id) {
        if (id == null || templateItemMapper.selectById(id) == null) {
            throw new IllegalArgumentException("行业模板项不存在");
        }
    }

    private void validateTemplateItem(WujinIndustryTemplateItemSaveReqVO reqVO) {
        validateTemplateExists(reqVO.getTemplateId());
        if (entityMapper.selectById(reqVO.getEntityId()) == null) {
            throw new IllegalArgumentException("行业模板关联实体不存在");
        }
    }

    private void validateTemplateExists(Long templateId) {
        if (templateMapper.selectById(templateId) == null) {
            throw new IllegalArgumentException("行业模板不存在");
        }
    }
}
