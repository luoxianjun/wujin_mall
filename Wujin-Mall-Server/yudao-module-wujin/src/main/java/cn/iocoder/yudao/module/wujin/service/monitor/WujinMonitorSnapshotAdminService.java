package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinMonitorSnapshotDO;

import java.util.List;

public interface WujinMonitorSnapshotAdminService {

    Long createSnapshot(WujinMonitorSnapshotSaveReqVO createReqVO);

    void updateSnapshot(WujinMonitorSnapshotSaveReqVO updateReqVO);

    void deleteSnapshot(Long id);

    WujinMonitorSnapshotDO getSnapshot(Long id);

    List<WujinMonitorSnapshotDO> getSnapshotList(WujinMonitorSnapshotListReqVO listReqVO);
}
