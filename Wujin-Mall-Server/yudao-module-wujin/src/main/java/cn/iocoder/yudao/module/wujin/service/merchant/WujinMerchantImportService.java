package cn.iocoder.yudao.module.wujin.service.merchant;

import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportPreviewRespVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportReqVO;
import cn.iocoder.yudao.module.wujin.controller.admin.merchant.vo.WujinMerchantImportResultRespVO;

public interface WujinMerchantImportService {

    WujinMerchantImportPreviewRespVO previewImport(WujinMerchantImportReqVO reqVO);

    WujinMerchantImportResultRespVO importRows(WujinMerchantImportReqVO reqVO);
}
