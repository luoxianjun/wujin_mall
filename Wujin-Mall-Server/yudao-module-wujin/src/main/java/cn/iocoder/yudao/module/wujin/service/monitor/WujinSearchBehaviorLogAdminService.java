package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;

import java.util.List;

public interface WujinSearchBehaviorLogAdminService {

    Long createBehaviorLog(WujinSearchBehaviorLogSaveReqVO createReqVO);

    void updateBehaviorLog(WujinSearchBehaviorLogSaveReqVO updateReqVO);

    void deleteBehaviorLog(Long id);

    WujinSearchBehaviorLogDO getBehaviorLog(Long id);

    List<WujinSearchBehaviorLogDO> getBehaviorLogList(WujinSearchBehaviorLogListReqVO listReqVO);
}
