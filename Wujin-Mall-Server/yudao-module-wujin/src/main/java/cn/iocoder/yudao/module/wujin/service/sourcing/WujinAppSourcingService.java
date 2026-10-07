package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;

import java.util.List;

public interface WujinAppSourcingService {

    List<WujinSupplierCandidateRespVO> getSupplierCandidates(WujinSupplierCandidateReqVO reqVO);

    WujinSourcingLeadSubmitRespVO submitLead(WujinSourcingLeadSubmitReqVO reqVO);
}
