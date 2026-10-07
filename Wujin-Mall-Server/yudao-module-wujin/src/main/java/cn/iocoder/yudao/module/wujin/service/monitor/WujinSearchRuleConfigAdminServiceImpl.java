package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.monitor.WujinSearchRuleConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinSearchRuleConfigAdminServiceImpl implements WujinSearchRuleConfigAdminService {

    @Resource
    private WujinSearchRuleConfigMapper ruleConfigMapper;

    @Override
    public Long createRuleConfig(WujinSearchRuleConfigSaveReqVO createReqVO) {
        WujinSearchRuleConfigDO ruleConfig = BeanUtils.toBean(createReqVO, WujinSearchRuleConfigDO.class);
        ruleConfigMapper.insert(ruleConfig);
        return ruleConfig.getId();
    }

    @Override
    public void updateRuleConfig(WujinSearchRuleConfigSaveReqVO updateReqVO) {
        validateRuleConfigExists(updateReqVO.getId());
        WujinSearchRuleConfigDO ruleConfig = BeanUtils.toBean(updateReqVO, WujinSearchRuleConfigDO.class);
        ruleConfigMapper.updateById(ruleConfig);
    }

    @Override
    public void deleteRuleConfig(Long id) {
        validateRuleConfigExists(id);
        ruleConfigMapper.deleteById(id);
    }

    @Override
    public WujinSearchRuleConfigDO getRuleConfig(Long id) {
        return ruleConfigMapper.selectById(id);
    }

    @Override
    public List<WujinSearchRuleConfigDO> getRuleConfigList(WujinSearchRuleConfigListReqVO listReqVO) {
        return ruleConfigMapper.selectList(listReqVO);
    }

    private void validateRuleConfigExists(Long id) {
        if (id == null || ruleConfigMapper.selectById(id) == null) {
            throw new IllegalArgumentException("搜索规则配置不存在");
        }
    }
}
