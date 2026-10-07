package cn.iocoder.yudao.module.wujin.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinMonitorSnapshotListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinMonitorSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WujinMonitorSnapshotMapper extends BaseMapperX<WujinMonitorSnapshotDO> {

    default List<WujinMonitorSnapshotDO> selectList(WujinMonitorSnapshotListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinMonitorSnapshotDO>()
                .eqIfPresent(WujinMonitorSnapshotDO::getMetric, reqVO.getMetric())
                .eqIfPresent(WujinMonitorSnapshotDO::getAuditAction, reqVO.getAuditAction())
                .eqIfPresent(WujinMonitorSnapshotDO::getAlertFlag, reqVO.getAlertFlag())
                .orderByDesc(WujinMonitorSnapshotDO::getId));
    }
}
