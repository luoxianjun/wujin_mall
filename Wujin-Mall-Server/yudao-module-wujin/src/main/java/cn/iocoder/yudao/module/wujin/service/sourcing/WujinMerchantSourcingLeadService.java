package cn.iocoder.yudao.module.wujin.service.sourcing;

import cn.iocoder.yudao.module.wujin.controller.admin.sourcing.vo.WujinSourcingLeadListReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.sourcing.vo.WujinMerchantSourcingLeadHandleReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.sourcing.WujinSourcingLeadDO;

import java.util.List;

public interface WujinMerchantSourcingLeadService {

    List<WujinSourcingLeadDO> getLeadList(WujinSourcingLeadListReqVO reqVO);

    void handleLead(WujinMerchantSourcingLeadHandleReqVO reqVO);
}
