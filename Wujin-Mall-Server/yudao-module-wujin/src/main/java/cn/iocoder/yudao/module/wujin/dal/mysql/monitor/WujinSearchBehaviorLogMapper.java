package cn.iocoder.yudao.module.wujin.dal.mysql.monitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.monitor.vo.WujinSearchBehaviorLogListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.monitor.WujinSearchBehaviorLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WujinSearchBehaviorLogMapper extends BaseMapperX<WujinSearchBehaviorLogDO> {

    default List<WujinSearchBehaviorLogDO> selectList(WujinSearchBehaviorLogListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinSearchBehaviorLogDO>()
                .eqIfPresent(WujinSearchBehaviorLogDO::getUserId, reqVO.getUserId())
                .likeIfPresent(WujinSearchBehaviorLogDO::getKeyword, reqVO.getKeyword())
                .eqIfPresent(WujinSearchBehaviorLogDO::getResultLane, reqVO.getResultLane())
                .eqIfPresent(WujinSearchBehaviorLogDO::getIndustryCode, reqVO.getIndustryCode())
                .eqIfPresent(WujinSearchBehaviorLogDO::getChainViewed, reqVO.getChainViewed())
                .eqIfPresent(WujinSearchBehaviorLogDO::getClassificationCorrect, reqVO.getClassificationCorrect())
                .eqIfPresent(WujinSearchBehaviorLogDO::getHighRiskWarningTriggered, reqVO.getHighRiskWarningTriggered())
                .orderByDesc(WujinSearchBehaviorLogDO::getId));
    }

    default List<WujinSearchBehaviorLogDO> selectListByCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<WujinSearchBehaviorLogDO>()
                .geIfPresent(WujinSearchBehaviorLogDO::getCreateTime, startTime)
                .ltIfPresent(WujinSearchBehaviorLogDO::getCreateTime, endTime)
                .orderByAsc(WujinSearchBehaviorLogDO::getId));
    }
}
