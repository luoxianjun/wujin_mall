package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitReqVO;
import cn.iocoder.yudao.module.wujin.controller.merchant.relation.vo.WujinMerchantRelationSubmitRespVO;

public interface WujinMerchantRelationSubmitService {

    WujinMerchantRelationSubmitRespVO submitRelation(WujinMerchantRelationSubmitReqVO reqVO);
}
