package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadDispatchReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadConversionReportRespVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;

import java.util.List;

public interface WujinSourcingLeadAdminService {

    List<WujinSourcingLeadDO> getLeadList(WujinSourcingLeadListReqVO reqVO);

    void dispatchLead(WujinSourcingLeadDispatchReqVO reqVO);

    void autoDispatchLead(Long leadId);

    WujinSourcingLeadConversionReportRespVO getConversionReport(WujinSourcingLeadConversionReportReqVO reqVO);
}
