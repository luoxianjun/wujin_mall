package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemListReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantRelationItemSaveReqVO;
import cn.iocoder.yudao.module.wujin.dal.dataobject.merchant.WujinMerchantRelationItemDO;

import java.util.List;

public interface WujinMerchantRelationItemAdminService {

    Long createItem(WujinMerchantRelationItemSaveReqVO createReqVO);

    void updateItem(WujinMerchantRelationItemSaveReqVO updateReqVO);

    void deleteItem(Long id);

    WujinMerchantRelationItemDO getItem(Long id);

    List<WujinMerchantRelationItemDO> getItemList(WujinMerchantRelationItemListReqVO listReqVO);
}
