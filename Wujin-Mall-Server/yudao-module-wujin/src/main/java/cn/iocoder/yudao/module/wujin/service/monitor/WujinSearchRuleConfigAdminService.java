package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchRuleConfigSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchRuleConfigDO;

import java.util.List;

public interface WujinSearchRuleConfigAdminService {

    Long createRuleConfig(WujinSearchRuleConfigSaveReqVO createReqVO);

    void updateRuleConfig(WujinSearchRuleConfigSaveReqVO updateReqVO);

    void deleteRuleConfig(Long id);

    WujinSearchRuleConfigDO getRuleConfig(Long id);

    List<WujinSearchRuleConfigDO> getRuleConfigList(WujinSearchRuleConfigListReqVO listReqVO);
}
