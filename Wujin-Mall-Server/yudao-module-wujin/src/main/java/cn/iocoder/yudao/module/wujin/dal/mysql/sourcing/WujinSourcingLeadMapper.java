package cn.iocoder.yudao.module.wujin.dal.mysql.sourcing;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WujinSourcingLeadMapper extends BaseMapperX<WujinSourcingLeadDO> {

    default List<WujinSourcingLeadDO> selectList(WujinSourcingLeadListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WujinSourcingLeadDO>()
                .likeIfPresent(WujinSourcingLeadDO::getKeyword, reqVO.getKeyword())
                .eqIfPresent(WujinSourcingLeadDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinSourcingLeadDO::getLeadStatus, reqVO.getLeadStatus())
                .eqIfPresent(WujinSourcingLeadDO::getDispatchStatus, reqVO.getDispatchStatus())
                .eqIfPresent(WujinSourcingLeadDO::getMerchantId, reqVO.getMerchantId())
                .orderByDesc(WujinSourcingLeadDO::getId));
    }

    default List<WujinSourcingLeadDO> selectConversionReportList(WujinSourcingLeadConversionReportReqVO reqVO) {
        LocalDateTime startTime = reqVO.getStartDate() == null ? null : reqVO.getStartDate().atStartOfDay();
        LocalDateTime endTime = reqVO.getEndDate() == null ? null : reqVO.getEndDate().plusDays(1).atStartOfDay();
        return selectList(new LambdaQueryWrapperX<WujinSourcingLeadDO>()
                .geIfPresent(WujinSourcingLeadDO::getCreateTime, startTime)
                .ltIfPresent(WujinSourcingLeadDO::getCreateTime, endTime)
                .eqIfPresent(WujinSourcingLeadDO::getMerchantId, reqVO.getMerchantId())
                .eqIfPresent(WujinSourcingLeadDO::getLane, reqVO.getLane())
                .eqIfPresent(WujinSourcingLeadDO::getIndustry, reqVO.getIndustry())
                .orderByDesc(WujinSourcingLeadDO::getId));
    }
}
