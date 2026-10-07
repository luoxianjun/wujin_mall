package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadProgressRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSourcingLeadSubmitRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCandidateRespVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityReqVO;
import cn.iocoder.yudao.module.wujin.controller.app.sourcing.vo.WujinSupplierCapabilityRespVO;

import java.util.List;

public interface WujinAppSourcingService {

    String SUPPLIER_TYPE_MERCHANT = "MERCHANT";
    String SUPPLIER_TYPE_PLATFORM = "PLATFORM";

    List<WujinSupplierCandidateRespVO> getSupplierCandidates(WujinSupplierCandidateReqVO reqVO);

    WujinSourcingLeadSubmitRespVO submitLead(WujinSourcingLeadSubmitReqVO reqVO);

    /**
     * 获得寻源线索进度，只允许线索提交人查看
     *
     * @param leadId 线索编号
     * @param userId 当前登录用户编号
     */
    WujinSourcingLeadProgressRespVO getLeadProgress(Long leadId, Long userId);

    /**
     * 获得当前用户提交的寻源线索，按提交时间倒序
     */
    List<WujinSourcingLeadProgressRespVO> getMyLeadList(Long userId);

    /**
     * 获得供应商供应能力：商家候选汇总其启用的供应能力索引，平台兜底候选展示对应产业链实体
     */
    WujinSupplierCapabilityRespVO getSupplierCapability(WujinSupplierCapabilityReqVO reqVO);
}
