package cn.iocoder.yudao.module.wujin.service.monitor;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinMonitorSnapshotDO;
import cn.iocoder.yudao.module.wujin.dal.mysql.monitor.WujinMonitorSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

@Service
@Validated
public class WujinMonitorSnapshotAdminServiceImpl implements WujinMonitorSnapshotAdminService {

    @Resource
    private WujinMonitorSnapshotMapper monitorSnapshotMapper;

    @Override
    public Long createSnapshot(WujinMonitorSnapshotSaveReqVO createReqVO) {
        WujinMonitorSnapshotDO snapshot = BeanUtils.toBean(createReqVO, WujinMonitorSnapshotDO.class);
        monitorSnapshotMapper.insert(snapshot);
        return snapshot.getId();
    }

    @Override
    public void updateSnapshot(WujinMonitorSnapshotSaveReqVO updateReqVO) {
        validateSnapshotExists(updateReqVO.getId());
        WujinMonitorSnapshotDO snapshot = BeanUtils.toBean(updateReqVO, WujinMonitorSnapshotDO.class);
        monitorSnapshotMapper.updateById(snapshot);
    }

    @Override
    public void deleteSnapshot(Long id) {
        validateSnapshotExists(id);
        monitorSnapshotMapper.deleteById(id);
    }

    @Override
    public WujinMonitorSnapshotDO getSnapshot(Long id) {
        return monitorSnapshotMapper.selectById(id);
    }

    @Override
    public List<WujinMonitorSnapshotDO> getSnapshotList(WujinMonitorSnapshotListReqVO listReqVO) {
        return monitorSnapshotMapper.selectList(listReqVO);
    }

    private void validateSnapshotExists(Long id) {
        if (id == null || monitorSnapshotMapper.selectById(id) == null) {
            throw new IllegalArgumentException("监控指标快照不存在");
        }
    }
}
